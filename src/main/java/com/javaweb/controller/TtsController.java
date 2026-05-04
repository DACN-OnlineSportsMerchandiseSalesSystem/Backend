package com.javaweb.controller;

import com.javaweb.service.TtsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TtsController {

    private final TtsService ttsService;

    @GetMapping
    public ResponseEntity<?> getTts(@RequestParam String text) {
        String audioUrl = ttsService.getTextToSpeechUrl(text);
        Map<String, String> response = new HashMap<>();
        response.put("audioUrl", audioUrl);
        return ResponseEntity.ok(response);
    }
}
