package com.javaweb.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressDTO {
    private Long id;
    private String street;
    private String city; 	
    private String state;
    private Boolean isDefault;
    private String receiverName;
    private String phone;
}
