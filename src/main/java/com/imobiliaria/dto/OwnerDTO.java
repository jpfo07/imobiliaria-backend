package com.imobiliaria.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OwnerDTO {

    @NotBlank(message = "Nome do proprietário é obrigatório")
    private String name;

    private String phone;
    private String email;
}
