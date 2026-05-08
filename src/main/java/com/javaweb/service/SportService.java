package com.javaweb.service;

import com.javaweb.dto.SportDTO;
import com.javaweb.dto.SportRequestDTO;
import java.util.List;

public interface SportService {
    List<SportDTO> getAllSports();
    SportDTO getSportById(Long id);
    SportDTO getSportBySlug(String slug);
    SportDTO createSport(SportRequestDTO requestDTO);
    SportDTO updateSport(Long id, SportRequestDTO requestDTO);
    void deleteSport(Long id);
}
