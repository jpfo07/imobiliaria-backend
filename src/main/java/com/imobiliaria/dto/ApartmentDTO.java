package com.imobiliaria.dto;

import com.imobiliaria.entity.PropertyCategory;
import com.imobiliaria.entity.PropertyType;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ApartmentDTO {

    @NotBlank(message = "Título é obrigatório")
    private String title;

    private String description;

    @NotBlank(message = "Localização é obrigatória")
    private String location;

    private String neighborhood;
    private String city;
    private String state;

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço deve ser positivo")
    private BigDecimal price;

    @NotNull(message = "Número de quartos é obrigatório")
    @Min(value = 0, message = "Quartos deve ser >= 0")
    private Integer bedrooms;

    @NotNull(message = "Número de banheiros é obrigatório")
    @Min(value = 1, message = "Banheiros deve ser >= 1")
    private Integer bathrooms;

    private Integer parkingSpots;

    @NotNull(message = "Área é obrigatória")
    @DecimalMin(value = "1", message = "Área deve ser positiva")
    private BigDecimal area;

    @NotNull(message = "Tipo é obrigatório")
    private PropertyType type;

    private PropertyCategory category;

    // Dados do proprietário (apenas admin)
    private String ownerName;
    private String ownerPhone;
    private String ownerEmail;

    private Boolean featured;
    private Boolean active;
    private Boolean sold;
    private Boolean financing;
    private List<String> images;
}
