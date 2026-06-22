package com.imobiliaria.dto;

import lombok.*;

@Data
@AllArgsConstructor
@Builder
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String name;
    private String email;
    private String role;
    private String photoUrl;

    public JwtResponse(String token, Long id, String name, String email, String role, String photoUrl) {
        this.token = token;
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.photoUrl = photoUrl;
    }
}