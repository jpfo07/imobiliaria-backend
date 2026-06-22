package com.imobiliaria.dto;

import lombok.Data;

@Data
public class UserDTO {
    private String name;
    private String phone;
    private String photoUrl;
    private String newPassword;
    private String currentPassword;
}