package com.imobiliaria.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "apartments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Apartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String location;

    private String neighborhood;
    private String city;
    private String state;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer bedrooms;

    @Column(nullable = false)
    private Integer bathrooms;

    private Integer parkingSpots;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal area;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyType type;

    @Enumerated(EnumType.STRING)
    private PropertyCategory category;

    // Proprietário do imóvel — visível apenas no admin
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "owner_id")
    private Owner owner;

    private Boolean featured;
    private Boolean active;
    private Boolean sold;
    private Boolean financing;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "apartment_images", joinColumns = @JoinColumn(name = "apartment_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> images = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.active == null) this.active = true;
        if (this.featured == null) this.featured = false;
        if (this.sold == null) this.sold = false;
        if (this.financing == null) this.financing = false;
        if (this.images == null) this.images = new ArrayList<>();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
