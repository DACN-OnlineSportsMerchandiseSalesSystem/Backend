package com.javaweb.service.impl;

import com.javaweb.dto.StorePolicyDTO;
import com.javaweb.dto.StorePolicyRequestDTO;
import com.javaweb.entity.StorePolicy;
import com.javaweb.exception.ResouceNotFoundException;
import com.javaweb.repository.StorePolicyRepository;
import com.javaweb.service.StorePolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StorePolicyServiceImpl implements StorePolicyService {

    private final StorePolicyRepository storePolicyRepository;

    @Override
    public List<StorePolicyDTO> getAllActivePolicies() {
        return storePolicyRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public StorePolicyDTO getPolicyByKey(String key) {
        StorePolicy policy = storePolicyRepository.findByPolicyKey(key)
                .orElseThrow(() -> new ResouceNotFoundException("Không tìm thấy chính sách với key: " + key));
        return mapToDTO(policy);
    }

    @Override
    public StorePolicyDTO createPolicy(StorePolicyRequestDTO request) {
        StorePolicy policy = new StorePolicy();
        policy.setPolicyKey(request.getPolicyKey());
        policy.setTitle(request.getTitle());
        policy.setContent(request.getContent());
        policy.setCategory(request.getCategory());
        policy.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        policy.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        policy.setUpdatedAt(LocalDateTime.now());
        policy.setIsVectorized(false); // Cần được AI học thuộc
        return mapToDTO(storePolicyRepository.save(policy));
    }

    @Override
    public StorePolicyDTO updatePolicy(String key, StorePolicyRequestDTO request) {
        StorePolicy policy = storePolicyRepository.findByPolicyKey(key)
                .orElseThrow(() -> new ResouceNotFoundException("Không tìm thấy chính sách với key: " + key));

        if (request.getTitle() != null) policy.setTitle(request.getTitle());
        if (request.getContent() != null) policy.setContent(request.getContent());
        if (request.getCategory() != null) policy.setCategory(request.getCategory());
        if (request.getIsActive() != null) policy.setIsActive(request.getIsActive());
        if (request.getDisplayOrder() != null) policy.setDisplayOrder(request.getDisplayOrder());

        // Reset lại cờ vectorized để AI đọc lại nội dung mới nhất vào đêm nay
        policy.setIsVectorized(false);
        policy.setUpdatedAt(LocalDateTime.now());

        return mapToDTO(storePolicyRepository.save(policy));
    }

    @Override
    public void deletePolicy(String key) {
        StorePolicy policy = storePolicyRepository.findByPolicyKey(key)
                .orElseThrow(() -> new ResouceNotFoundException("Không tìm thấy chính sách với key: " + key));
        storePolicyRepository.delete(policy);
    }

    private StorePolicyDTO mapToDTO(StorePolicy policy) {
        StorePolicyDTO dto = new StorePolicyDTO();
        dto.setId(policy.getId());
        dto.setPolicyKey(policy.getPolicyKey());
        dto.setTitle(policy.getTitle());
        dto.setContent(policy.getContent());
        dto.setCategory(policy.getCategory());
        dto.setIsActive(policy.getIsActive());
        dto.setDisplayOrder(policy.getDisplayOrder());
        dto.setUpdatedAt(policy.getUpdatedAt());
        return dto;
    }
}
