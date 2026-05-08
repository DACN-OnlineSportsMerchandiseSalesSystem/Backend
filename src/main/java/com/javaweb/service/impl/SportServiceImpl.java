package com.javaweb.service.impl;

import com.javaweb.dto.SportDTO;
import com.javaweb.dto.SportRequestDTO;
import com.javaweb.entity.Sport;
import com.javaweb.exception.ResouceNotFoundException;
import com.javaweb.repository.SportRepository;
import com.javaweb.service.SportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SportServiceImpl implements SportService {

    private final SportRepository sportRepository;

    @Override
    public List<SportDTO> getAllSports() {
        return sportRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SportDTO getSportById(Long id) {
        Sport sport = sportRepository.findById(id)
                .orElseThrow(() -> new ResouceNotFoundException("Sport not found with id: " + id));
        return mapToDTO(sport);
    }

    @Override
    public SportDTO getSportBySlug(String slug) {
        Sport sport = sportRepository.findBySlug(slug)
                .orElseThrow(() -> new ResouceNotFoundException("Sport not found with slug: " + slug));
        return mapToDTO(sport);
    }

    @Override
    public SportDTO createSport(SportRequestDTO requestDTO) {
        Sport sport = new Sport();
        sport.setName(requestDTO.getName());
        sport.setSlug(requestDTO.getSlug());
        sport.setRating(requestDTO.getRating());
        sport.setStatus(requestDTO.getStatus());
        sport.setDiscount(requestDTO.getDiscount());
        Sport savedSport = sportRepository.save(sport);
        return mapToDTO(savedSport);
    }

    @Override
    public SportDTO updateSport(Long id, SportRequestDTO requestDTO) {
        Sport sport = sportRepository.findById(id)
                .orElseThrow(() -> new ResouceNotFoundException("Sport not found with id: " + id));
        sport.setName(requestDTO.getName());
        sport.setSlug(requestDTO.getSlug());
        sport.setRating(requestDTO.getRating());
        sport.setStatus(requestDTO.getStatus());
        sport.setDiscount(requestDTO.getDiscount());
        Sport updatedSport = sportRepository.save(sport);
        return mapToDTO(updatedSport);
    }

    @Override
    public void deleteSport(Long id) {
        Sport sport = sportRepository.findById(id)
                .orElseThrow(() -> new ResouceNotFoundException("Sport not found with id: " + id));
        // Soft delete
        sport.setStatus("INACTIVE");
        sportRepository.save(sport);
    }

    private SportDTO mapToDTO(Sport sport) {
        SportDTO dto = new SportDTO();
        dto.setId(sport.getId());
        dto.setName(sport.getName());
        dto.setSlug(sport.getSlug());
        dto.setRating(sport.getRating());
        dto.setStatus(sport.getStatus());
        dto.setDiscount(sport.getDiscount());
        return dto;
    }
}
