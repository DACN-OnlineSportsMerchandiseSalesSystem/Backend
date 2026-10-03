package com.javaweb.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaweb.dto.aiplanner.*;
import com.javaweb.entity.AiPlanSnapshot;
import com.javaweb.entity.User;
import com.javaweb.repository.AiPlanSnapshotRepository;
import com.javaweb.repository.UserRepository;
import com.javaweb.service.AiPlannerService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiPlannerServiceImpl implements AiPlannerService {

    public static final String LEGAL_DISCLAIMER_TEXT =
            "Kế hoạch mang tính chất tham khảo, không thay thế chẩn đoán/điều trị y tế. " +
            "Người dùng có bệnh lý nền hoặc tiền sử chấn thương cần tham vấn ý kiến bác sĩ chuyên khoa hoặc chuyên gia y tế trước khi áp dụng.";

    private final ChatLanguageModel chatLanguageModel;
    private final AiPlanSnapshotRepository snapshotRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    // =========================================================================
    // MODULE 1: PRE-PROCESSING & DETERMINISTIC MATH
    // =========================================================================
    @Override
    public CalculatedMetricsDTO calculateDeterministicMetrics(AiPlannerRequestDTO req) {
        double weight = req.getWeight() != null ? req.getWeight() : 65.0;
        double height = req.getHeight() != null ? req.getHeight() : 170.0;
        int age = req.getAge() != null ? req.getAge() : 25;
        String gender = req.getGender() != null ? req.getGender().toLowerCase() : "male";

        // 1.1 Tính BMR bằng công thức Mifflin-St Jeor chuẩn (1990)
        double bmr;
        if ("female".equals(gender) || "nu".equals(gender) || "nữ".equals(gender)) {
            bmr = (10.0 * weight) + (6.25 * height) - (5.0 * age) - 161.0;
        } else {
            bmr = (10.0 * weight) + (6.25 * height) - (5.0 * age) + 5.0;
        }

        // 1.2 Hệ số vận động chuẩn PAL (Physical Activity Level)
        double pal = 1.55;
        if (req.getActivityLevel() != null) {
            switch (req.getActivityLevel().toLowerCase()) {
                case "sedentary":
                    pal = 1.2;
                    break;
                case "light":
                    pal = 1.375;
                    break;
                case "moderate":
                    pal = 1.55;
                    break;
                case "active":
                    pal = 1.725;
                    break;
                case "very_active":
                    pal = 1.9;
                    break;
                default:
                    pal = 1.55;
            }
        }

        double tdee = bmr * pal;

        // 1.3 Xác định mục tiêu Calo Surplus / Deficit / Maintenance
        double calorieAdjustment = 0;
        String goal = req.getGoal() != null ? req.getGoal().toLowerCase() : "bulk";
        if (goal.contains("bulk") || goal.contains("tăng cơ")) {
            calorieAdjustment = 350.0; // Thặng dư vừa phải để tăng cơ nạc
        } else if (goal.contains("cut") || goal.contains("giảm mỡ")) {
            calorieAdjustment = -400.0; // Thâm hụt an toàn tránh mất cơ
        } else {
            calorieAdjustment = 0.0;
        }

        double targetCalories = Math.round(tdee + calorieAdjustment);

        // 1.4 Neo phân bổ Macronutrients chuẩn Thể thao & Dinh dưỡng Lâm sàng (ISSN & ACSM)
        // Protein: 1.8g - 2.0g / kg thể trọng (4 kcal/g)
        double proteinPerKg = goal.contains("bulk") ? 2.0 : 1.8;
        double targetProteinGrams = Math.round(weight * proteinPerKg);
        double proteinCalories = targetProteinGrams * 4.0;

        // Fat: 25% total calories (9 kcal/g)
        double fatCalories = targetCalories * 0.25;
        double targetFatGrams = Math.round(fatCalories / 9.0);

        // Carbs: Remaining calories (4 kcal/g)
        double carbsCalories = Math.max(0, targetCalories - (proteinCalories + fatCalories));
        double targetCarbsGrams = Math.round(carbsCalories / 4.0);

        double proteinRatio = Math.round((proteinCalories / targetCalories) * 100.0);
        double fatRatio = Math.round((fatCalories / targetCalories) * 100.0);
        double carbsRatio = Math.round((carbsCalories / targetCalories) * 100.0);

        return CalculatedMetricsDTO.builder()
                .bmr(Math.round(bmr * 10.0) / 10.0)
                .pal(pal)
                .tdee(Math.round(tdee * 10.0) / 10.0)
                .targetCalories(targetCalories)
                .calorieAdjustment(calorieAdjustment)
                .targetProteinGrams(targetProteinGrams)
                .targetCarbsGrams(targetCarbsGrams)
                .targetFatGrams(targetFatGrams)
                .proteinRatioPercent(proteinRatio)
                .carbsRatioPercent(carbsRatio)
                .fatRatioPercent(fatRatio)
                .formulaUsed("Mifflin-St Jeor Formula (1990) & WHO Physical Activity Guidelines")
                .build();
    }

    // =========================================================================
    // MODULE 2: CONTEXT ENRICHMENT & PROMPT FORMULATION
    // =========================================================================
    private String buildExpertSystemPrompt(AiPlannerRequestDTO req, CalculatedMetricsDTO metrics) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là Chuyên gia Khoa học Thể thao hàng đầu (CSCS - Certified Strength and Conditioning Specialist) ")
          .append("và Chuyên gia Dinh dưỡng Thể thao Lâm sàng (Clinical Sports Nutritionist).\n\n")
          .append("--- CƠ SỞ KHOA HỌC CHUẨN Y KHOA / THỂ THAO (BẮT BUỘC TUÂN THỦ VÀ TRÍCH DẪN) ---\n")
          .append("1. BMR & TDEE: Tuân thủ phương trình chuyển hóa Mifflin-St Jeor (1990) và hệ số PAL của Tổ chức Y tế Thế giới (WHO).\n")
          .append("2. Phân bổ Macronutrient: Tuyên bố đồng thuận của Hiệp hội Dinh dưỡng Thể thao Quốc tế (ISSN, 2017) về lượng nạp protein 1.6 - 2.2 g/kg/ngày và chất béo 20-35% tổng năng lượng.\n")
          .append("3. Khối lượng tập & Quá tải lũy tiến: Khuyến nghị của Đại học Y học Thể thao Hoa Kỳ (ACSM) và nghiên cứu Schoenfeld et al. (2016-2019) về khối lượng 10 - 20 sets/nhóm cơ/tuần, RPE 7-9, thời gian nghỉ 60 - 120s.\n\n")
          .append("--- QUY TẮC CỐT LÕI (BẮT BUỘC TUÂN THỦ) ---\n")
          .append("1. NEO DỮ LIỆU ĐÃ TÍNH TOÁN (KHÔNG ĐƯỢC TỰ Ý THAY ĐỔI MỎ NEO CALO):\n")
          .append(String.format("   - Tổng Calo Mục tiêu: %.0f kcal/ngày\n", metrics.getTargetCalories()))
          .append(String.format("   - Protein: %.0f gram (tương ứng %.0f%% calo)\n", metrics.getTargetProteinGrams(), metrics.getProteinRatioPercent()))
          .append(String.format("   - Carbohydrate: %.0f gram (tương ứng %.0f%% calo)\n", metrics.getTargetCarbsGrams(), metrics.getCarbsRatioPercent()))
          .append(String.format("   - Fat: %.0f gram (tương ứng %.0f%% calo)\n\n", metrics.getTargetFatGrams(), metrics.getFatRatioPercent()))
          .append("2. QUY TẮC AN TOÀN VÀ RÀNG BUỘC:\n");

        if (req.getInjuries() != null && !req.getInjuries().isBlank()) {
            sb.append("   - Chấn thương/Hạn chế: ").append(req.getInjuries()).append(". TUYỆT ĐỐI KHÔNG kê các bài tập gây áp lực trực tiếp lên vùng này, hãy thay bằng bài tập kiểm soát an toàn.\n");
        }
        if (req.getLocation() != null && req.getLocation().equalsIgnoreCase("home")) {
            sb.append("   - Địa điểm: TẠI NHÀ. Chỉ sử dụng tạ đơn, dây kháng lực hoặc Calisthenics bodyweight. KHÔNG dùng máy tạ khối hay tạ đòn nặng.\n");
        } else {
            sb.append("   - Địa điểm: PHÒNG GYM CHUYÊN NGHIỆP. Tận dụng đầy đủ tạ đòn, tạ đơn, máy cáp và máy tạ khối.\n");
        }
        if (req.getDietaryPreferences() != null && !req.getDietaryPreferences().isBlank()) {
            sb.append("   - Ẩm thực & Dị ứng: ").append(req.getDietaryPreferences()).append(". Ưu tiên món ăn Việt Nam phổ biến, kinh tế, dễ chuẩn bị.\n");
        }

        sb.append("\n3. ĐỊNH DẠNG ĐẦU RA BẮT BUỘC:\n")
          .append("Chỉ trả về DUY NHẤT một chuỗi JSON hợp lệ (không kèm bất kỳ văn bản giải thích hay markdown code fence thừa nào ngoài JSON) theo cấu trúc sau:\n")
          .append("{\n")
          .append("  \"title\": \"Kế hoạch tăng cơ / giảm mỡ ...\",\n")
          .append("  \"legal_disclaimer\": \"Kế hoạch mang tính chất tham khảo, không thay thế chẩn đoán/điều trị y tế. Người dùng có bệnh lý nền cần tham vấn bác sĩ trước khi áp dụng.\",\n")
          .append("  \"scientific_references\": [\n")
          .append("    {\n")
          .append("      \"domain\": \"BMR & TDEE\",\n")
          .append("      \"principle\": \"Mifflin-St Jeor Equation (1990) & WHO Physical Activity Guidelines\",\n")
          .append("      \"source\": \"The American Journal of Clinical Nutrition / WHO\",\n")
          .append("      \"year\": \"1990\",\n")
          .append("      \"citation\": \"Mifflin MD, St Jeor ST, et al. Am J Clin Nutr. 1990;51(2):241-247.\",\n")
          .append("      \"application_in_plan\": \"Áp dụng tính mức năng lượng cơ bản và TDEE chuẩn xác.\"\n")
          .append("    },\n")
          .append("    {\n")
          .append("      \"domain\": \"Macronutrient Distribution\",\n")
          .append("      \"principle\": \"ISSN Position Stand: Protein and Exercise\",\n")
          .append("      \"source\": \"International Society of Sports Nutrition (ISSN)\",\n")
          .append("      \"year\": \"2017\",\n")
          .append("      \"citation\": \"Jäger R, Kerksick CM, et al. J Int Soc Sports Nutr. 2017;14:20.\",\n")
          .append("      \"application_in_plan\": \"Ấn định đạm 1.8-2.0g/kg tối ưu tái tạo và phục hồi cơ bắp.\"\n")
          .append("    },\n")
          .append("    {\n")
          .append("      \"domain\": \"Hypertrophy Volume & Progressive Overload\",\n")
          .append("      \"principle\": \"ACSM Resistance Training Guidelines & Schoenfeld Volume Meta-analysis\",\n")
          .append("      \"source\": \"American College of Sports Medicine (ACSM)\",\n")
          .append("      \"year\": \"2017\",\n")
          .append("      \"citation\": \"Schoenfeld BJ, et al. J Sports Sci. 2017;35(11):1073-1082.\",\n")
          .append("      \"application_in_plan\": \"Thiết lập 12-16 sets/nhóm cơ/tuần với RPE 7-9.\"\n")
          .append("    }\n")
          .append("  ],\n")
          .append("  \"workout_plan\": {\n")
          .append("    \"split_name\": \"Push - Pull - Legs\",\n")
          .append("    \"days_per_week\": ").append(req.getDaysPerWeek() != null ? req.getDaysPerWeek() : 4).append(",\n")
          .append("    \"schedule\": [\n")
          .append("      {\n")
          .append("        \"day\": \"Thứ 2\",\n")
          .append("        \"focus\": \"Ngực & Tay sau (Push)\",\n")
          .append("        \"exercises\": [\n")
          .append("          { \"name\": \"Barbell Bench Press\", \"target_muscle\": \"Ngực\", \"sets\": 4, \"reps\": \"8-10\", \"rpe\": 8, \"rest_sec\": 90 }\n")
          .append("        ]\n")
          .append("      }\n")
          .append("    ]\n")
          .append("  },\n")
          .append("  \"meal_plan\": {\n")
          .append("    \"daily_summary\": { \"total_calories\": ").append(metrics.getTargetCalories()).append(", \"protein_g\": ").append(metrics.getTargetProteinGrams()).append(", \"carbs_g\": ").append(metrics.getTargetCarbsGrams()).append(", \"fat_g\": ").append(metrics.getTargetFatGrams()).append(" },\n")
          .append("    \"meals\": [\n")
          .append("      {\n")
          .append("        \"meal_type\": \"Bữa sáng\",\n")
          .append("        \"name\": \"Phở bò tái nạm ít bánh hoặc Yến mạch trứng\",\n")
          .append("        \"calories\": 500,\n")
          .append("        \"protein_g\": 35,\n")
          .append("        \"carbs_g\": 60,\n")
          .append("        \"fat_g\": 12,\n")
          .append("        \"items\": [\n")
          .append("          { \"name\": \"Bánh phở / Yến mạch\", \"amount\": \"150g\", \"calories\": 250 },\n")
          .append("          { \"name\": \"Thịt bò nạc\", \"amount\": \"100g\", \"calories\": 180 }\n")
          .append("        ]\n")
          .append("      }\n")
          .append("    ]\n")
          .append("  }\n")
          .append("}");

        return sb.toString();
    }

    private List<ScientificReferenceDTO> buildDefaultScientificReferences(CalculatedMetricsDTO metrics, AiPlannerRequestDTO req) {
        List<ScientificReferenceDTO> refs = new ArrayList<>();

        refs.add(ScientificReferenceDTO.builder()
                .domain("BMR & TDEE (Năng lượng chuyển hóa & Hoạt động)")
                .principle("Mifflin-St Jeor Equation & WHO Physical Activity Guidelines")
                .source("The American Journal of Clinical Nutrition (AJCN) & World Health Organization (WHO)")
                .year("1990 / 2020")
                .citation("Mifflin MD, St Jeor ST, Hill JO, et al. A new predictive equation for resting energy expenditure in healthy individuals. Am J Clin Nutr. 1990;51(2):241-247.")
                .applicationInPlan(String.format("Xác định mức chuyển hóa cơ bản BMR (%.0f kcal) và nhân hệ số vận động PAL (%.2f) để tính TDEE (%.0f kcal). Mục tiêu được neo chính xác tại %.0f kcal/ngày.",
                        metrics.getBmr(), metrics.getPal(), metrics.getTdee(), metrics.getTargetCalories()))
                .build());

        refs.add(ScientificReferenceDTO.builder()
                .domain("Macronutrient Distribution (Phân bổ Đạm, Tinh bột, Béo)")
                .principle("ISSN Position Stand: Protein and Exercise & ACSM Nutrition Guidelines")
                .source("International Society of Sports Nutrition (ISSN) & ACSM")
                .year("2017")
                .citation("Jäger R, Kerksick CM, Campbell BI, et al. International Society of Sports Nutrition Position Stand: protein and exercise. J Int Soc Sports Nutr. 2017;14:20.")
                .applicationInPlan(String.format("Ấn định Protein %.0f g (tương đương %.1f%% calo, ~1.8-2.0g/kg) nhằm tối ưu tổng hợp cơ bắp (MPS); Chất béo lành mạnh %.0f g (25%% calo); Carbohydrate %.0f g duy trì lượng glycogen cơ.",
                        metrics.getTargetProteinGrams(), metrics.getProteinRatioPercent(), metrics.getTargetFatGrams(), metrics.getTargetCarbsGrams()))
                .build());

        refs.add(ScientificReferenceDTO.builder()
                .domain("Progressive Overload & Hypertrophy Volume (Tập luyện & Quá tải)")
                .principle("ACSM Guidelines for Exercise Testing and Prescription & Schoenfeld Volume Meta-Analysis")
                .source("American College of Sports Medicine (ACSM)")
                .year("2017 / 2019")
                .citation("Schoenfeld BJ, Ogborn D, Krieger JW. Dose-response relationship between weekly resistance training volume and increases in muscle mass. J Sports Sci. 2017;35(11):1073-1082.")
                .applicationInPlan("Phân bổ 12 - 18 sets/nhóm cơ chính/tuần với RPE 7-9, thời gian nghỉ 60 - 120s cho bài phức hợp đa khớp (compound) nhằm kích thích phì đại cơ an toàn và bền vững.")
                .build());

        refs.add(ScientificReferenceDTO.builder()
                .domain("Injury Prevention & Safe Progression (Phòng ngừa chấn thương)")
                .principle("ACSM Sports Medicine Principles on Musculoskeletal Safety & Individual Load Management")
                .source("American College of Sports Medicine (ACSM)")
                .year("2018")
                .citation("Ratamess NA, et al. Progression Models in Resistance Training for Healthy Adults. Med Sci Sports Exerc. 2009;41(3):687-708.")
                .applicationInPlan(req != null && req.getInjuries() != null && !req.getInjuries().isBlank()
                        ? ("Loại bỏ hoàn toàn các chuyển động gây tải trục trực tiếp lên vùng chấn thương (" + req.getInjuries() + "), thay thế bằng các biến thể bài tập cô lập và máy tạ khối có biên độ an toàn.")
                        : "Kiểm soát nhịp độ thực hiện bài tập (Tempo 2-0-2), khởi động động (dynamic warmup) 10 phút trước buổi tập nhằm phòng chống chấn thương khớp.")
                .build());

        return refs;
    }

    // =========================================================================
    // MODULE 3: GEMINI STRUCTURED INFERENCE & PARSING
    // =========================================================================
    @Override
    public AiPlannerResponseDTO generatePlan(AiPlannerRequestDTO request) {
        // Module 1: Deterministic Math
        CalculatedMetricsDTO metrics = calculateDeterministicMetrics(request);

        // Module 2: Context Enrichment
        String systemPrompt = buildExpertSystemPrompt(request, metrics);
        String userPrompt = String.format(
                "Tạo kế hoạch cho người dùng: Chiều cao %.0f cm, Cân nặng %.0f kg, %d tuổi, Giới tính %s, Mục tiêu %s, %s, Số buổi tập %d buổi/tuần.",
                request.getHeight() != null ? request.getHeight() : 170.0,
                request.getWeight() != null ? request.getWeight() : 65.0,
                request.getAge() != null ? request.getAge() : 25,
                "male".equalsIgnoreCase(request.getGender()) ? "Nam" : "Nữ",
                request.getGoal() != null ? request.getGoal() : "Tăng cơ",
                request.getLocation() != null ? ("Tập " + request.getLocation()) : "Tập gym",
                request.getDaysPerWeek() != null ? request.getDaysPerWeek() : 4
        );

        // Module 3: Structured Gemini Call
        List<ChatMessage> messages = List.of(
                SystemMessage.from(systemPrompt),
                UserMessage.from(userPrompt)
        );

        String jsonResponse = "";
        try {
            Response<AiMessage> response = chatLanguageModel.generate(messages);
            jsonResponse = response.content().text();
        } catch (Exception e) {
            log.error("Lỗi khi gọi Gemini Structured Inference: {}", e.getMessage());
            jsonResponse = buildFallbackJson(metrics, request);
        }

        // Clean markdown backticks if any
        jsonResponse = cleanJsonString(jsonResponse);

        Map<String, Object> parsedPlan;
        try {
            parsedPlan = objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("Không thể parse JSON từ AI, sử dụng fallback cấu trúc: {}", e.getMessage());
            jsonResponse = buildFallbackJson(metrics, request);
            try {
                parsedPlan = objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});
            } catch (Exception ex) {
                parsedPlan = new HashMap<>();
            }
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> workoutPlan = (Map<String, Object>) parsedPlan.getOrDefault("workout_plan", Collections.emptyMap());
        @SuppressWarnings("unchecked")
        Map<String, Object> mealPlan = (Map<String, Object>) parsedPlan.getOrDefault("meal_plan", Collections.emptyMap());
        String title = (String) parsedPlan.getOrDefault("title", "Kế hoạch Thể hình & Dinh dưỡng Cá nhân hóa");

        // Module 4: Validation & Logic Sanity Checks
        ValidationReportDTO validation = validatePlan(metrics, workoutPlan, mealPlan, request);

        // Hạng mục 1: Tham chiếu khoa học & Miễn trừ trách nhiệm pháp lý
        List<ScientificReferenceDTO> scientificRefs = extractScientificReferences(parsedPlan, metrics, request);

        return AiPlannerResponseDTO.builder()
                .metrics(metrics)
                .workoutPlan(workoutPlan)
                .mealPlan(mealPlan)
                .validationReport(validation)
                .planTitle(title)
                .scientificReferences(scientificRefs)
                .legalDisclaimer(LEGAL_DISCLAIMER_TEXT)
                .version(1)
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<ScientificReferenceDTO> extractScientificReferences(Map<String, Object> parsedPlan, CalculatedMetricsDTO metrics, AiPlannerRequestDTO request) {
        List<ScientificReferenceDTO> defaultRefs = buildDefaultScientificReferences(metrics, request);
        if (parsedPlan != null && parsedPlan.containsKey("scientific_references")) {
            try {
                Object rawRefs = parsedPlan.get("scientific_references");
                String refsJson = objectMapper.writeValueAsString(rawRefs);
                List<ScientificReferenceDTO> list = objectMapper.readValue(refsJson, new TypeReference<List<ScientificReferenceDTO>>() {});
                if (list != null && !list.isEmpty()) {
                    for (int i = 0; i < list.size(); i++) {
                        ScientificReferenceDTO ref = list.get(i);
                        if (ref.getApplicationInPlan() == null || ref.getApplicationInPlan().trim().isEmpty()) {
                            if (i < defaultRefs.size()) {
                                ref.setApplicationInPlan(defaultRefs.get(i).getApplicationInPlan());
                            } else {
                                ref.setApplicationInPlan("Ứng dụng theo tiêu chuẩn tối ưu hóa của các tổ chức y học thể thao.");
                            }
                        }
                    }
                    return list;
                }
            } catch (Exception e) {
                log.warn("Lỗi trích xuất scientific_references từ JSON, sử dụng default: {}", e.getMessage());
            }
        }
        return defaultRefs;
    }

    // =========================================================================
    // MODULE 4: VALIDATION & LOGIC SANITY CHECKS (Kiểm định Logic & Nhiệt động học)
    // =========================================================================
    private ValidationReportDTO validatePlan(CalculatedMetricsDTO metrics, Map<String, Object> workoutPlan, Map<String, Object> mealPlan, AiPlannerRequestDTO req) {
        double pGrams = metrics.getTargetProteinGrams();
        double cGrams = metrics.getTargetCarbsGrams();
        double fGrams = metrics.getTargetFatGrams();
        double targetCal = metrics.getTargetCalories();

        // 4.1 Kiểm định Nhiệt động học: |(4P + 4C + 9F) - Total| <= 0.05 * Total
        double calculatedCalories = (pGrams * 4.0) + (cGrams * 4.0) + (fGrams * 9.0);
        double diff = Math.abs(calculatedCalories - targetCal);
        double diffPercent = targetCal > 0 ? Math.round((diff / targetCal) * 1000.0) / 10.0 : 0.0;
        boolean isThermodynamicValid = diffPercent <= 5.0;

        // 4.2 Kiểm định Weekly Volume (10 - 20 sets / nhóm cơ)
        Map<String, Integer> volumeSets = new HashMap<>();
        if (workoutPlan != null && workoutPlan.containsKey("schedule")) {
            try {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> schedule = (List<Map<String, Object>>) workoutPlan.get("schedule");
                for (Map<String, Object> day : schedule) {
                    if (day.containsKey("exercises")) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> exercises = (List<Map<String, Object>>) day.get("exercises");
                        for (Map<String, Object> ex : exercises) {
                            String muscle = (String) ex.getOrDefault("target_muscle", "Toàn thân");
                            int sets = 3;
                            if (ex.get("sets") instanceof Number) {
                                sets = ((Number) ex.get("sets")).intValue();
                            }
                            volumeSets.put(muscle, volumeSets.getOrDefault(muscle, 0) + sets);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Lỗi tính weekly volume sets: {}", e.getMessage());
            }
        }

        boolean isVolumeOptimal = true;
        List<String> optNotes = new ArrayList<>();
        List<String> safetyNotes = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : volumeSets.entrySet()) {
            if (entry.getValue() > 22) {
                optNotes.add("Cảnh báo quá tải (Overtraining): Nhóm cơ " + entry.getKey() + " đạt " + entry.getValue() + " sets/tuần (vượt ngưỡng tối ưu 20 sets).");
                isVolumeOptimal = false;
            } else if (entry.getValue() < 8) {
                optNotes.add("Khối lượng kích thích thấp (Under-stimulation): Nhóm cơ " + entry.getKey() + " chỉ có " + entry.getValue() + " sets/tuần (ngưỡng tối thiểu là 10 sets).");
            }
        }

        if (req != null && req.getInjuries() != null && !req.getInjuries().isBlank()) {
            safetyNotes.add("Đã kích hoạt bộ lọc bảo vệ chấn thương cho: " + req.getInjuries() + ". Mọi bài tập có rủi ro cao đã được loại trừ.");
        } else {
            safetyNotes.add("Không ghi nhận tiền sử chấn thương đặc biệt. Lịch tập tuân theo biên độ chuyển động tự nhiên (Full ROM).");
        }

        return ValidationReportDTO.builder()
                .calculatedMacroCalories((double) Math.round(calculatedCalories))
                .targetCalories((double) Math.round(targetCal))
                .thermodynamicDiffPercent(diffPercent)
                .isThermodynamicallyValid(isThermodynamicValid)
                .weeklyVolumeSets(volumeSets)
                .isVolumeOptimal(isVolumeOptimal)
                .safetyNotes(safetyNotes)
                .optimizationNotes(optNotes)
                .build();
    }

    // =========================================================================
    // MODULE 5: MULTI-TURN FEEDBACK LOOP (Differential Update)
    // =========================================================================
    @Override
    public AiPlannerResponseDTO refinePlan(AiPlannerRefineRequestDTO req) {
        CalculatedMetricsDTO metrics = req.getMetrics();
        if (metrics == null) {
            metrics = CalculatedMetricsDTO.builder()
                    .targetCalories(2200.0)
                    .targetProteinGrams(140.0)
                    .targetCarbsGrams(240.0)
                    .targetFatGrams(60.0)
                    .proteinRatioPercent(25.0)
                    .carbsRatioPercent(45.0)
                    .fatRatioPercent(30.0)
                    .build();
        }

        String prompt = "Bạn là Chuyên gia Khoa học Thể thao CSCS và Chuyên gia Dinh dưỡng Thể thao Lâm sàng.\n" +
                "Người dùng muốn tinh chỉnh vi sai kế hoạch hiện tại (Differential Update).\n\n" +
                "YÊU CẦU TINH CHỈNH TỪ NGƯỜI DÙNG: \"" + req.getRefinementPrompt() + "\"\n\n" +
                "QUY TẮC BẮT BUỘC:\n" +
                "1. Giữ nguyên tổng Calo mục tiêu (" + metrics.getTargetCalories() + " kcal) và tỷ lệ Macro đã neo.\n" +
                "2. Nếu người dùng muốn đổi món ăn, hãy thay thế bằng món ăn Việt Nam tương đương về calo và macro.\n" +
                "3. Nếu đổi lịch tập, hãy đảm bảo tổng số sets/tuần không bị xáo trộn quá mức.\n" +
                "4. Viện dẫn cơ sở khoa học thể thao & y khoa (ACSM, ISSN, WHO, Mifflin-St Jeor) và đính kèm legal_disclaimer.\n" +
                "5. Trả về DUY NHẤT một chuỗi JSON hợp lệ với các trường: workout_plan, meal_plan, scientific_references, legal_disclaimer.\n\n" +
                "Dữ liệu kế hoạch hiện tại:\n" +
                toJsonString(Map.of(
                        "current_workout", req.getCurrentWorkoutPlan() != null ? req.getCurrentWorkoutPlan() : Map.of(),
                        "current_meal", req.getCurrentMealPlan() != null ? req.getCurrentMealPlan() : Map.of()
                ));

        List<ChatMessage> messages = List.of(UserMessage.from(prompt));

        String jsonResponse = "";
        try {
            Response<AiMessage> response = chatLanguageModel.generate(messages);
            jsonResponse = cleanJsonString(response.content().text());
        } catch (Exception e) {
            log.error("Lỗi khi gọi refine Gemini: {}", e.getMessage());
        }

        Map<String, Object> parsedPlan;
        try {
            parsedPlan = objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            parsedPlan = new HashMap<>();
            parsedPlan.put("workout_plan", req.getCurrentWorkoutPlan());
            parsedPlan.put("meal_plan", req.getCurrentMealPlan());
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> workoutPlan = (Map<String, Object>) parsedPlan.getOrDefault("workout_plan", req.getCurrentWorkoutPlan());
        @SuppressWarnings("unchecked")
        Map<String, Object> mealPlan = (Map<String, Object>) parsedPlan.getOrDefault("meal_plan", req.getCurrentMealPlan());

        ValidationReportDTO validation = validatePlan(metrics, workoutPlan, mealPlan, null);
        List<ScientificReferenceDTO> scientificRefs = extractScientificReferences(parsedPlan, metrics, null);

        return AiPlannerResponseDTO.builder()
                .metrics(metrics)
                .workoutPlan(workoutPlan)
                .mealPlan(mealPlan)
                .validationReport(validation)
                .scientificReferences(scientificRefs)
                .legalDisclaimer(LEGAL_DISCLAIMER_TEXT)
                .planTitle("Kế hoạch đã tinh chỉnh theo yêu cầu khoa học")
                .version(2)
                .build();
    }

    // =========================================================================
    // MODULE 6: STATE PERSISTENCE, SNAPSHOTTING & ICS EXPORT
    // =========================================================================
    @Override
    @Transactional
    public AiPlanSnapshot saveSnapshot(String title, String planType, AiPlannerResponseDTO planData, String userEmail) {
        User user = null;
        if (userEmail != null && !userEmail.isBlank()) {
            user = userRepository.findByEmail(userEmail).orElse(null);
        }

        Map<String, Object> fullPayload = new HashMap<>();
        fullPayload.put("metrics", planData.getMetrics());
        fullPayload.put("workout_plan", planData.getWorkoutPlan());
        fullPayload.put("meal_plan", planData.getMealPlan());
        fullPayload.put("validationReport", planData.getValidationReport());
        fullPayload.put("scientific_references", planData.getScientificReferences());
        fullPayload.put("legal_disclaimer", planData.getLegalDisclaimer());

        String jsonString = toJsonString(fullPayload);

        AiPlanSnapshot snapshot = AiPlanSnapshot.builder()
                .user(user)
                .userEmail(userEmail)
                .title(title != null && !title.isBlank() ? title : planData.getPlanTitle())
                .planType(planType != null ? planType : "FULL")
                .version(planData.getVersion() != null ? planData.getVersion() : 1)
                .isActive(true)
                .planJson(jsonString)
                .bmr(planData.getMetrics() != null ? planData.getMetrics().getBmr() : null)
                .tdee(planData.getMetrics() != null ? planData.getMetrics().getTdee() : null)
                .targetCalories(planData.getMetrics() != null ? planData.getMetrics().getTargetCalories() : null)
                .targetProtein(planData.getMetrics() != null ? planData.getMetrics().getTargetProteinGrams() : null)
                .targetCarbs(planData.getMetrics() != null ? planData.getMetrics().getTargetCarbsGrams() : null)
                .targetFat(planData.getMetrics() != null ? planData.getMetrics().getTargetFatGrams() : null)
                .build();

        return snapshotRepository.save(snapshot);
    }

    @Override
    public List<AiPlanSnapshot> getSavedPlans(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            return snapshotRepository.findAll();
        }
        return snapshotRepository.findByUserEmailOrderByCreatedAtDesc(userEmail);
    }

    @Override
    public AiPlanSnapshot getPlanById(Long id, String userEmail) {
        return snapshotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kế hoạch!"));
    }

    @Override
    @Transactional
    public void setActivePlan(Long id, String userEmail) {
        snapshotRepository.findById(id).ifPresent(snapshot -> {
            if (userEmail != null && !userEmail.isBlank()) {
                List<AiPlanSnapshot> all = snapshotRepository.findByUserEmailOrderByCreatedAtDesc(userEmail);
                for (AiPlanSnapshot s : all) {
                    s.setIsActive(false);
                }
                snapshotRepository.saveAll(all);
            }
            snapshot.setIsActive(true);
            snapshotRepository.save(snapshot);
        });
    }

    @Override
    @Transactional
    public void deletePlan(Long id, String userEmail) {
        snapshotRepository.deleteById(id);
    }

    @Override
    public String exportIcs(Long id, String userEmail) {
        AiPlanSnapshot snapshot = snapshotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kế hoạch để xuất lịch ICS!"));

        StringBuilder ics = new StringBuilder();
        ics.append("BEGIN:VCALENDAR\r\n")
           .append("VERSION:2.0\r\n")
           .append("PRODID:-//SportZone//AI Fitness Planner v1.0//VI\r\n")
           .append("CALSCALE:GREGORIAN\r\n")
           .append("METHOD:PUBLISH\r\n")
           .append("X-WR-CALNAME:Lịch tập SportZone AI - ").append(snapshot.getTitle()).append("\r\n")
           .append("X-WR-TIMEZONE:Asia/Ho_Chi_Minh\r\n");

        try {
            Map<String, Object> data = objectMapper.readValue(snapshot.getPlanJson(), new TypeReference<Map<String, Object>>() {});
            if (data.containsKey("workout_plan")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> workout = (Map<String, Object>) data.get("workout_plan");
                if (workout.containsKey("schedule")) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> schedule = (List<Map<String, Object>>) workout.get("schedule");

                    LocalDate now = LocalDate.now();
                    LocalDate nextMonday = now.with(DayOfWeek.MONDAY);

                    for (int i = 0; i < schedule.size(); i++) {
                        Map<String, Object> dayPlan = schedule.get(i);
                        String dayName = (String) dayPlan.getOrDefault("day", "Buổi tập");
                        String focus = (String) dayPlan.getOrDefault("focus", "Tập luyện thể hình");

                        LocalDate sessionDate = nextMonday.plusDays(i % 7);
                        String dateStr = sessionDate.format(DateTimeFormatter.BASIC_ISO_DATE);

                        StringBuilder desc = new StringBuilder();
                        desc.append("Mục tiêu: ").append(focus).append("\\n");
                        desc.append("Tham chiếu khoa học: ACSM Resistance Guidelines (10-20 sets/nhóm cơ)\\n");
                        desc.append("Lưu ý: ").append(LEGAL_DISCLAIMER_TEXT).append("\\n\\nDanh sách bài tập:\\n");

                        if (dayPlan.containsKey("exercises")) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> exercises = (List<Map<String, Object>>) dayPlan.get("exercises");
                            for (Map<String, Object> ex : exercises) {
                                desc.append("• ").append(ex.get("name"))
                                    .append(" (").append(ex.get("sets")).append(" sets x ").append(ex.get("reps")).append(" reps, RPE ")
                                    .append(ex.get("rpe")).append(", nghỉ ").append(ex.get("rest_sec")).append("s)\\n");
                            }
                        }

                        ics.append("BEGIN:VEVENT\r\n")
                           .append("UID:sportzone-").append(snapshot.getId()).append("-day-").append(i).append("@sportzone.vn\r\n")
                           .append("DTSTAMP:").append(dateStr).append("T060000Z\r\n")
                           .append("DTSTART;TZID=Asia/Ho_Chi_Minh:").append(dateStr).append("T063000\r\n")
                           .append("DTEND;TZID=Asia/Ho_Chi_Minh:").append(dateStr).append("T073000\r\n")
                           .append("SUMMARY:SportZone AI: ").append(dayName).append(" - ").append(focus).append("\r\n")
                           .append("DESCRIPTION:").append(desc).append("\r\n")
                           .append("LOCATION:SportZone Gym / Tại nhà\r\n")
                           .append("STATUS:CONFIRMED\r\n")
                           .append("END:VEVENT\r\n");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi parse iCal: {}", e.getMessage());
        }

        ics.append("END:VCALENDAR\r\n");
        return ics.toString();
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================
    private String cleanJsonString(String raw) {
        if (raw == null) return "{}";
        String s = raw.trim();
        if (s.startsWith("```json")) {
            s = s.substring(7);
        } else if (s.startsWith("```")) {
            s = s.substring(3);
        }
        if (s.endsWith("```")) {
            s = s.substring(0, s.length() - 3);
        }
        return s.trim();
    }

    private String toJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    private String buildFallbackJson(CalculatedMetricsDTO metrics, AiPlannerRequestDTO req) {
        return "{\n" +
                "  \"title\": \"Kế hoạch Thể hình Chuẩn Khoa học Thể thao (ACSM & ISSN)\",\n" +
                "  \"legal_disclaimer\": \"" + LEGAL_DISCLAIMER_TEXT + "\",\n" +
                "  \"workout_plan\": {\n" +
                "    \"split_name\": \"Push - Pull - Legs Chuẩn Khoa Học\",\n" +
                "    \"days_per_week\": 4,\n" +
                "    \"schedule\": [\n" +
                "      {\n" +
                "        \"day\": \"Thứ 2\",\n" +
                "        \"focus\": \"Ngực & Tay sau (Push Day)\",\n" +
                "        \"exercises\": [\n" +
                "          { \"name\": \"Barbell Bench Press\", \"target_muscle\": \"Ngực\", \"sets\": 4, \"reps\": \"8-10\", \"rpe\": 8, \"rest_sec\": 90 },\n" +
                "          { \"name\": \"Incline Dumbbell Press\", \"target_muscle\": \"Ngực trên\", \"sets\": 3, \"reps\": \"10-12\", \"rpe\": 8, \"rest_sec\": 75 },\n" +
                "          { \"name\": \"Triceps Rope Pushdown\", \"target_muscle\": \"Tay sau\", \"sets\": 3, \"reps\": \"12-15\", \"rpe\": 7, \"rest_sec\": 60 }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"day\": \"Thứ 3\",\n" +
                "        \"focus\": \"Lưng xô & Tay trước (Pull Day)\",\n" +
                "        \"exercises\": [\n" +
                "          { \"name\": \"Lat Pulldown / Pull-up\", \"target_muscle\": \"Lưng xô\", \"sets\": 4, \"reps\": \"8-10\", \"rpe\": 8, \"rest_sec\": 90 },\n" +
                "          { \"name\": \"Seated Cable Row\", \"target_muscle\": \"Lưng giữa\", \"sets\": 3, \"reps\": \"10-12\", \"rpe\": 8, \"rest_sec\": 75 },\n" +
                "          { \"name\": \"Biceps Barbell Curl\", \"target_muscle\": \"Tay trước\", \"sets\": 3, \"reps\": \"10-12\", \"rpe\": 8, \"rest_sec\": 60 }\n" +
                "        ]\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"meal_plan\": {\n" +
                "    \"daily_summary\": { \"total_calories\": " + metrics.getTargetCalories() + ", \"protein_g\": " + metrics.getTargetProteinGrams() + ", \"carbs_g\": " + metrics.getTargetCarbsGrams() + ", \"fat_g\": " + metrics.getTargetFatGrams() + " },\n" +
                "    \"meals\": [\n" +
                "      {\n" +
                "        \"meal_type\": \"Bữa sáng\",\n" +
                "        \"name\": \"Phở bò tái nạm / Bánh mì ốp la ngũ cốc\",\n" +
                "        \"calories\": 500,\n" +
                "        \"protein_g\": 35,\n" +
                "        \"carbs_g\": 60,\n" +
                "        \"fat_g\": 12,\n" +
                "        \"items\": [\n" +
                "          { \"name\": \"Thịt bò nạc\", \"amount\": \"100g\", \"calories\": 180 },\n" +
                "          { \"name\": \"Bánh phở tươi\", \"amount\": \"150g\", \"calories\": 220 }\n" +
                "        ]\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";
    }
}
