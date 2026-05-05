package com.javaweb.service.impl;

import com.javaweb.dto.VoucherDTO;
import com.javaweb.dto.VoucherRequestDTO;
import com.javaweb.entity.Voucher;
import com.javaweb.exception.ResouceNotFoundException;
import com.javaweb.repository.VoucherRepository;
import com.javaweb.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;

    @Override
    public VoucherDTO createVoucher(VoucherRequestDTO requestDTO) {
        if (voucherRepository.findByCode(requestDTO.getCode()).isPresent()) {
            throw new RuntimeException("Voucher code already exists: " + requestDTO.getCode());
        }

        Voucher voucher = new Voucher();
        voucher.setCode(requestDTO.getCode());
        voucher.setDiscountAmount(requestDTO.getDiscountAmount());
        voucher.setMinOrderValue(requestDTO.getMinOrderValue());
        voucher.setUsageLimit(requestDTO.getUsageLimit());
        voucher.setExpiryDate(requestDTO.getExpiryDate());
        voucher.setUsedCount(0);

        Voucher savedVoucher = voucherRepository.save(voucher);
        return mapToDTO(savedVoucher);
    }

    @Override
    public VoucherDTO updateVoucher(Long id, VoucherRequestDTO requestDTO) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResouceNotFoundException("Voucher not found with id: " + id));

        // Nếu cập nhật mã code khác, kiểm tra trùng lặp
        if (!voucher.getCode().equals(requestDTO.getCode()) && 
            voucherRepository.findByCode(requestDTO.getCode()).isPresent()) {
            throw new RuntimeException("Voucher code already exists: " + requestDTO.getCode());
        }

        voucher.setCode(requestDTO.getCode());
        voucher.setDiscountAmount(requestDTO.getDiscountAmount());
        voucher.setMinOrderValue(requestDTO.getMinOrderValue());
        voucher.setUsageLimit(requestDTO.getUsageLimit());
        voucher.setExpiryDate(requestDTO.getExpiryDate());

        Voucher updatedVoucher = voucherRepository.save(voucher);
        return mapToDTO(updatedVoucher);
    }

    @Override
    public void deleteVoucher(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResouceNotFoundException("Voucher not found with id: " + id));
        voucherRepository.delete(voucher);
    }

    @Override
    public List<VoucherDTO> getAllVouchers() {
        return voucherRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<VoucherDTO> getValidVouchers() {
        return voucherRepository.findValidVouchers().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public VoucherDTO getVoucherById(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResouceNotFoundException("Voucher not found with id: " + id));
        return mapToDTO(voucher);
    }

    private VoucherDTO mapToDTO(Voucher voucher) {
        VoucherDTO dto = new VoucherDTO();
        dto.setId(voucher.getId());
        dto.setCode(voucher.getCode());
        dto.setDiscountAmount(voucher.getDiscountAmount());
        dto.setMinOrderValue(voucher.getMinOrderValue());
        dto.setUsageLimit(voucher.getUsageLimit());
        dto.setUsedCount(voucher.getUsedCount());
        dto.setExpiryDate(voucher.getExpiryDate());
        dto.setCreatedAt(voucher.getCreatedAt());
        return dto;
    }
}
