package com.javaweb.service;

import com.javaweb.dto.AddressDTO;
import java.util.List;

public interface AddressService {
    List<AddressDTO> getMyAddresses(String email);
    AddressDTO createAddress(String email, AddressDTO addressDTO);
    AddressDTO updateAddress(Long addressId, String email, AddressDTO addressDTO);
    void deleteAddress(Long addressId, String email);
    AddressDTO setDefaultAddress(Long addressId, String email);
}
