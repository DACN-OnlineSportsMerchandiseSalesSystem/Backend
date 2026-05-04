package com.javaweb.service;

import com.javaweb.dto.CreateReturnRequestDTO;
import com.javaweb.dto.ReturnRequestDTO;

import java.util.List;

public interface ReturnService {
    ReturnRequestDTO createReturnRequest(Long userId, CreateReturnRequestDTO requestDTO);
    List<ReturnRequestDTO> getReturnRequestsByUser(Long userId);
    List<ReturnRequestDTO> getAllReturnRequests();
    ReturnRequestDTO processReturnRequest(Long requestId, String action); // action: APPROVED or REJECTED
}
