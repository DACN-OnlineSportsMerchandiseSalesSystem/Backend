package com.javaweb.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class ProductMetadataMigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public ProductMetadataMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> [Product Metadata Migration] Kiểm tra và cập nhật metadata thể thao & status cho sản phẩm...");

        try {
            // 1. Đồng bộ status thành 'active' cho các sản phẩm hiện có
            jdbcTemplate.update("UPDATE products SET status = 'active' WHERE status IS NULL OR LOWER(status) = 'active'");

            // 2. Cập nhật metadata chi tiết cho từng sản phẩm theo ID (nếu chưa có hoặc đang rỗng)
            updateProductMeta(1001L, "pickleball", "intermediate,advanced", "unisex",
                    "Thi đấu chuyên nghiệp, tăng lực xoáy spin",
                    "Carbon Charged Surface, trợ lực cổ tay, giảm chấn",
                    "vot pickleball joola ben johns perseus");

            updateProductMeta(1002L, "running", "beginner,intermediate", "women",
                    "Chạy bộ hàng ngày 5-10km, đi bộ, đi làm",
                    "Đệm EVA êm ái, trợ lực bước chạy, thoáng khí",
                    "giay the thao bitis hunter evo nu chay bo");

            updateProductMeta(1003L, "running,gym", "beginner", "men",
                    "Chạy bộ cự ly ngắn, tập cardio phòng gym",
                    "Siêu nhẹ, đế chống trơn trượt, thoáng khí",
                    "giay chay bo nam nhe thoai mai run one gym");

            updateProductMeta(1004L, "gym,badminton,running,football", "all", "unisex",
                    "Bảo vệ khớp gối, trợ lực dây chằng, giảm đau mỏi khi vận động mạnh",
                    "Nén cơ 3D đa chiều, đệm silicon chống va đập, bảo vệ sụn gối",
                    "bang bao ve khop goi powermax pro phu kien gym cau long");

            updateProductMeta(1005L, "gym,badminton,pickleball,running", "all", "men",
                    "Tập luyện thể thao cường độ cao, thấm hút mồ hôi nhanh",
                    "Vải sợi thoáng khí nhanh khô 100 Dry, co giãn 4 chiều",
                    "ao thun the thao nam thoang khi 100 dry gym pickleball");

            updateProductMeta(1006L, "badminton", "intermediate,advanced", "unisex",
                    "Tấn công đập cầu uy lực cuối sân, smash cắm sân",
                    "Thân vợt cứng, điểm ngọt mở rộng, công nghệ Rotational Generator System",
                    "vot cau long yonex astrox 88d pro tan cong");

            updateProductMeta(1007L, "football", "beginner,intermediate", "men",
                    "Đá bóng sân cỏ nhân tạo 5-7 người",
                    "Da KA-FIBER êm mềm, đinh dăm TF bám sân chống trơn, kiểm soát bóng tốt",
                    "giay bong da kamito artista kl tf san co nhan tao");

            updateProductMeta(1008L, "football", "all", "unisex",
                    "Tập luyện và thi đấu bóng đá phong trào",
                    "Độ nảy chuẩn FIFA, da PU bền bỉ chống mài mòn, đường may tỉ mỉ",
                    "qua bong da dong luc uhv 205 san co");

            updateProductMeta(1009L, "basketball", "intermediate", "men",
                    "Bật nhảy ném rổ, bứt tốc trên sân bê tông và sàn gỗ",
                    "Đệm khí giảm chấn bảo vệ cổ chân, đế cao su chịu mài mòn cao",
                    "giay bong ro jogarbola comet star nam");

            updateProductMeta(1010L, "running", "intermediate,advanced", "men",
                    "Chạy cự ly dài bán Marathon và Marathon, người có bàn chân sấp",
                    "Hệ thống 4D Guidance System ổn định sải chân, đệm PureGEL siêu êm giảm chấn",
                    "giay chay bo asics gel kayano 31 marathon");

            updateProductMeta(1011L, "swimming", "all", "unisex",
                    "Bơi lội hồ bơi trong nhà và ngoài trời",
                    "Chống đọng sương Anti-Fog, chống tia UV, đệm mắt silicone êm ái chống tràn nước",
                    "kinh boi nhat ban view v610 boi loi");

            updateProductMeta(1012L, "yoga", "all", "women",
                    "Tập Yoga định tuyến, Pilates tại nhà và phòng tập",
                    "Chất liệu TPE 2 lớp độ bám cao, hoa văn định tuyến chuẩn xác, chống trượt",
                    "tham tap yoga icado cao cap dinh tuyen");

            updateProductMeta(1013L, "boxing,martial_arts", "beginner,intermediate", "unisex",
                    "Tập đấm bao cát, luyện phản xạ và đấu tập đối kháng",
                    "Lớp đệm mút bảo vệ khớp xương bàn tay, đai dán cổ tay chắc chắn, thoáng khí",
                    "gang tay boxing everlast pro style vo thuat");

            updateProductMeta(1014L, "cycling", "all", "unisex",
                    "Đạp xe địa hình MTB, đạp xe thể thao đường trường",
                    "Vỏ Polystyrene giảm chấn đạt chuẩn CE, 17 lỗ thoáng khí khí động học",
                    "mu bao hiem xe dap decathlon rockrider the thao");

            updateProductMeta(1015L, "pickleball,badminton", "all", "men",
                    "Thi đấu và tập luyện Pickleball lịch sự, thoáng mát",
                    "Chất liệu Poly Spandex co giãn nhẹ, chống nhăn, kháng khuẩn và chống tia UV",
                    "ao polo pickleball icado ht603 the thao");

            updateProductMeta(1016L, "running,gym", "all", "men",
                    "Chạy bộ hàng ngày, tập gym, thời trang thể thao năng động",
                    "Đế Light Boost hoàn trả năng lượng tối đa, thân dệt Primeknit ôm chân",
                    "giay chay bo adidas ultraboost 5 nam the thao");

            updateProductMeta(1017L, "badminton", "intermediate,advanced", "unisex",
                    "Lối đánh công thủ toàn diện, phản tạt linh hoạt tốc độ cao",
                    "Sợi Med Carbon đàn hồi cao, đũa vợt kiểm soát cầu chính xác",
                    "vot cau long lining halbertec 7000 cong thu toan dien");

            updateProductMeta(1018L, "running,hiking", "all", "unisex",
                    "Chạy trail địa hình, dã ngoại cuối tuần, chứa đồ tập thể thao",
                    "Dung tích 23L với nhiều ngăn thông minh, quai đeo êm ái thoáng khí",
                    "balo da ngoai quechua nh escape 500 23l");

            updateProductMeta(1019L, "running,gym", "all", "men",
                    "Chạy bộ thời tiết nóng bức, tập luyện cường độ cao",
                    "Quần lót trong chống cọ xát bẹn, chất vải siêu nhẹ nhanh khô, túi khóa zip",
                    "quan short chay bo kiprun run 100 nam gym");

            updateProductMeta(1020L, "basketball", "all", "unisex",
                    "Chơi bóng rổ sân bê tông ngoài trời và sân trong nhà",
                    "Cao su bề mặt bám dính tốt, rãnh sâu dễ cầm nắm và kiểm soát đường chuyền",
                    "qua bong ro dong luc hunter ngoai troi");

            updateProductMeta(1021L, "running", "beginner,intermediate,advanced", "men",
                    "Chạy bộ hàng ngày, luyện tập cự ly 10km đến 21km",
                    "Đệm ReactX foam mới tăng 13% độ phản hồi lực, 2 túi khí Air Zoom trước sau",
                    "giay chay bo nam nike pegasus 41 hang ngay");

            updateProductMeta(1022L, "badminton", "advanced", "unisex",
                    "Vận động viên tấn công đập cầu uy lực liên tục (smash)",
                    "Công nghệ WES 2.0 đàn hồi cao, đũa Pyrofil trợ lực tối đa",
                    "vot cau long victor thruster k ryuga ii smash lee zii jia");

            System.out.println(">>> [Product Metadata Migration] Hoàn tất cập nhật metadata cho toàn bộ 22 sản phẩm.");
        } catch (Exception e) {
            System.err.println(">>> [Product Metadata Migration] Lỗi cập nhật metadata: " + e.getMessage());
        }
    }

    private void updateProductMeta(Long id, String targetSport, String fitnessLevel, String genderTarget,
                                   String suitableFor, String benefits, String searchTag) {
        String sql = "UPDATE products SET " +
                "target_sport = ?, " +
                "fitness_level = ?, " +
                "gender_target = ?, " +
                "suitable_for = ?, " +
                "benefits = ?, " +
                "search_tag = ? " +
                "WHERE id = ? AND (target_sport IS NULL OR target_sport = '')";
        jdbcTemplate.update(sql, targetSport, fitnessLevel, genderTarget, suitableFor, benefits, searchTag, id);
    }
}
