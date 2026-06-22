package com.imobiliaria.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(unique = true)
    private String cpf;

    // Nunca expor a senha na serialização JSON
    @JsonIgnore
    @Column(nullable = false)
    private String password;

    private String phone;
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ★ CORREÇÃO PRINCIPAL: @JsonIgnore impede o loop infinito
    // User → favorites → User → favorites → ...
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Favorite> favorites;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}