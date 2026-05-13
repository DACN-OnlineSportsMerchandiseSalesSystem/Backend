package com.javaweb.service;

import com.javaweb.dto.StorePolicyDTO;
import com.javaweb.dto.StorePolicyRequestDTO;
import java.util.List;

public interface StorePolicyService {
    List<StorePolicyDTO> getAllActivePolicies();
    StorePolicyDTO getPolicyByKey(String key);
    StorePolicyDTO createPolicy(StorePolicyRequestDTO request);
    StorePolicyDTO updatePolicy(String key, StorePolicyRequestDTO request);
    void deletePolicy(String key);
}
