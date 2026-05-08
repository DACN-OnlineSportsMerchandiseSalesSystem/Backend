package com.javaweb.controller;

import com.javaweb.dto.SportDTO;
import com.javaweb.dto.SportRequestDTO;
import com.javaweb.service.SportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sports")
@RequiredArgsConstructor
public class SportController {

    private final SportService sportService;

    @GetMapping
    public ResponseEntity<List<SportDTO>> getAllSports() {
        return ResponseEntity.ok(sportService.getAllSports());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SportDTO> getSportById(@PathVariable Long id) {
        return ResponseEntity.ok(sportService.getSportById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<SportDTO> getSportBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(sportService.getSportBySlug(slug));
    }

    @PostMapping
    public ResponseEntity<SportDTO> createSport(@RequestBody SportRequestDTO requestDTO) {
        return new ResponseEntity<>(sportService.createSport(requestDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SportDTO> updateSport(@PathVariable Long id, @RequestBody SportRequestDTO requestDTO) {
        return ResponseEntity.ok(sportService.updateSport(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSport(@PathVariable Long id) {
        sportService.deleteSport(id);
        return ResponseEntity.noContent().build();
    }
}
