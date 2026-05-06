package com.javaweb.service;

import com.javaweb.dto.VoucherDTO;
import com.javaweb.dto.VoucherRequestDTO;

import java.util.List;

public interface VoucherService {
    VoucherDTO createVoucher(VoucherRequestDTO requestDTO);
    VoucherDTO updateVoucher(Long id, VoucherRequestDTO requestDTO);
    void deleteVoucher(Long id);
    List<VoucherDTO> getAllVouchers();
    List<VoucherDTO> getValidVouchers();
    VoucherDTO getVoucherById(Long id);
    VoucherDTO checkVoucher(String code, java.math.BigDecimal orderValue);
}
