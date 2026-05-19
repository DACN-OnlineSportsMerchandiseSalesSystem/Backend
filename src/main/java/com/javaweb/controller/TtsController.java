package com.javaweb.controller;

import com.javaweb.service.TtsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Text to Speech (TTS)", description = "Endpoints for translating chatbot answers or texts into synthetic audio speeches using FPT TTS APIs")
public class TtsController {

    private final TtsService ttsService;

    @GetMapping
    @Operation(summary = "Convert text to audio speech url", description = "Submit a plain text prompt to get back a hosted audio URL of the synthetically spoken text.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully generated speech audio file URL")
    })
    public ResponseEntity<?> getTts(
            @Parameter(description = "Plain text string to synthesize", example = "Chào bạn, tôi có thể giúp gì cho bạn?", required = true)
            @RequestParam String text) {
        String audioUrl = ttsService.getTextToSpeechUrl(text);
        Map<String, String> response = new HashMap<>();
        response.put("audioUrl", audioUrl);
        return ResponseEntity.ok(response);
    }
}
