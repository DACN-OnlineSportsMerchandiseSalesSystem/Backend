package com.javaweb.service.impl;

import com.javaweb.dto.AddressDTO;
import com.javaweb.entity.Address;
import com.javaweb.entity.User;
import com.javaweb.exception.ResouceNotFoundException;
import com.javaweb.repository.AddressRepository;
import com.javaweb.repository.UserRepository;
import com.javaweb.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public List<AddressDTO> getMyAddresses(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResouceNotFoundException("User not found"));
        return addressRepository.findByUserId(user.getId()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AddressDTO createAddress(String email, AddressDTO request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResouceNotFoundException("User not found"));

        List<Address> currentAddresses = addressRepository.findByUserId(user.getId());
        
        Address newAddress = new Address();
        newAddress.setUser(user);
        newAddress.setStreet(request.getStreet());
        newAddress.setCity(request.getCity());
        newAddress.setState(request.getState());
        newAddress.setReceiverName(request.getReceiverName());
        newAddress.setPhone(request.getPhone());

        // Nếu là địa chỉ đầu tiên hoặc được chỉ định làm Default, ta reset cái cũ
        boolean shouldBeDefault = currentAddresses.isEmpty() || (request.getIsDefault() != null && request.getIsDefault());
        
        if (shouldBeDefault && !currentAddresses.isEmpty()) {
            for (Address addr : currentAddresses) {
                addr.setIsDefault(false);
            }
            addressRepository.saveAll(currentAddresses);
        }
        
        newAddress.setIsDefault(shouldBeDefault);
        return mapToDTO(addressRepository.save(newAddress));
    }

    @Override
    public AddressDTO updateAddress(Long addressId, String email, AddressDTO request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResouceNotFoundException("User not found"));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResouceNotFoundException("Address not found"));

        // Kiểm tra quyền sở hữu
        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền sửa địa chỉ này!");
        }

        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setReceiverName(request.getReceiverName());
        address.setPhone(request.getPhone());

        return mapToDTO(addressRepository.save(address));
    }

    @Override
    public void deleteAddress(Long addressId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResouceNotFoundException("User not found"));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResouceNotFoundException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền xóa địa chỉ này!");
        }

        addressRepository.delete(address);
    }

    @Override
    public AddressDTO setDefaultAddress(Long addressId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResouceNotFoundException("User not found"));

        List<Address> currentAddresses = addressRepository.findByUserId(user.getId());
        Address targetAddress = null;

        for (Address addr : currentAddresses) {
            if (addr.getId().equals(addressId)) {
                addr.setIsDefault(true);
                targetAddress = addr;
            } else {
                addr.setIsDefault(false);
            }
        }

        if (targetAddress == null) {
            throw new ResouceNotFoundException("Address not found or does not belong to you");
        }

        addressRepository.saveAll(currentAddresses);
        return mapToDTO(targetAddress);
    }

    private AddressDTO mapToDTO(Address address) {
        AddressDTO dto = new AddressDTO();
        dto.setId(address.getId());
        dto.setStreet(address.getStreet());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setReceiverName(address.getReceiverName());
        dto.setPhone(address.getPhone());
        dto.setIsDefault(address.getIsDefault());
        return dto;
    }
}
