package com.javaweb.controller;

import com.javaweb.dto.aiplanner.AiPlannerRefineRequestDTO;
import com.javaweb.dto.aiplanner.AiPlannerRequestDTO;
import com.javaweb.dto.aiplanner.AiPlannerResponseDTO;
import com.javaweb.entity.AiPlanSnapshot;
import com.javaweb.service.AiPlannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-planner")
@CrossOrigin("*")
@RequiredArgsConstructor
@Tag(name = "AI Workout & Meal Planner", description = "Endpoints for generating personalized workout routines and nutrition plans using 6-module deterministic & LLM hybrid pipeline")
public class AiPlannerController {

    private final AiPlannerService aiPlannerService;

    @PostMapping("/generate")
    @Operation(summary = "Generate 6-Module Workout & Meal Plan")
    public ResponseEntity<AiPlannerResponseDTO> generatePlan(
            @RequestBody AiPlannerRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            request.setUserEmail(userDetails.getUsername());
        }
        AiPlannerResponseDTO response = aiPlannerService.generatePlan(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refine")
    @Operation(summary = "Differential Multi-turn Delta Update")
    public ResponseEntity<AiPlannerResponseDTO> refinePlan(
            @RequestBody AiPlannerRefineRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            request.setUserEmail(userDetails.getUsername());
        }
        AiPlannerResponseDTO response = aiPlannerService.refinePlan(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/save")
    @Operation(summary = "Save Snapshot to Database")
    public ResponseEntity<AiPlanSnapshot> saveSnapshot(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = (userDetails != null) ? userDetails.getUsername() : (String) body.get("userEmail");
        String title = (String) body.getOrDefault("title", "Kế hoạch Thể hình & Dinh dưỡng");
        String planType = (String) body.getOrDefault("planType", "FULL");
        
        // Deserialize response payload
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        AiPlannerResponseDTO planData = mapper.convertValue(body.get("planData"), AiPlannerResponseDTO.class);

        AiPlanSnapshot saved = aiPlannerService.saveSnapshot(title, planType, planData, userEmail);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/saved")
    @Operation(summary = "Get All Saved Snapshots")
    public ResponseEntity<List<AiPlanSnapshot>> getSavedPlans(
            @RequestParam(required = false) String email,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = (userDetails != null) ? userDetails.getUsername() : email;
        List<AiPlanSnapshot> list = aiPlannerService.getSavedPlans(userEmail);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/saved/{id}")
    @Operation(summary = "Get Snapshot Detail by ID")
    public ResponseEntity<AiPlanSnapshot> getPlanById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = (userDetails != null) ? userDetails.getUsername() : null;
        AiPlanSnapshot plan = aiPlannerService.getPlanById(id, userEmail);
        if (plan == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(plan);
    }

    @PutMapping("/saved/{id}/active")
    @Operation(summary = "Set Snapshot as Active Plan")
    public ResponseEntity<Void> setActivePlan(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = (userDetails != null) ? userDetails.getUsername() : null;
        aiPlannerService.setActivePlan(id, userEmail);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/saved/{id}")
    @Operation(summary = "Delete Saved Snapshot")
    public ResponseEntity<Void> deletePlan(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = (userDetails != null) ? userDetails.getUsername() : null;
        aiPlannerService.deletePlan(id, userEmail);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export/ics/{id}")
    @Operation(summary = "Export Plan as iCalendar (.ics)")
    public ResponseEntity<String> exportIcs(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = (userDetails != null) ? userDetails.getUsername() : null;
        String icsContent = aiPlannerService.exportIcs(id, userEmail);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"workout_plan_" + id + ".ics\"")
                .contentType(MediaType.parseMediaType("text/calendar; charset=utf-8"))
                .body(icsContent);
    }
}
