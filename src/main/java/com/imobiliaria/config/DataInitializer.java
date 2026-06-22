package com.imobiliaria.config;

import com.imobiliaria.entity.*;
import com.imobiliaria.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ApartmentRepository apartmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        createAdminIfNotExists();
        createSampleApartmentsIfEmpty();
    }

    private void createAdminIfNotExists() {
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .name("Administrador")
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ROLE_ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Admin user created: {}", adminEmail);
        }
    }

    private void createSampleApartmentsIfEmpty() {
        if (apartmentRepository.count() == 0) {
            List<Apartment> apartments = List.of(
                Apartment.builder()
                    .title("Apartamento Moderno no Itaim Bibi")
                    .description("Lindo apartamento reformado, com acabamento premium, varanda gourmet e vista para o parque.")
                    .location("Itaim Bibi, São Paulo - SP")
                    .neighborhood("Itaim Bibi")
                    .city("São Paulo")
                    .state("SP")
                    .price(new BigDecimal("650000"))
                    .bedrooms(3)
                    .bathrooms(2)
                    .parkingSpots(2)
                    .area(new BigDecimal("95"))
                    .type(PropertyType.SALE)
                    .featured(true)
                    .images(List.of(
                        "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800",
                        "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=800",
                        "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=800"
                    ))
                    .build(),
                Apartment.builder()
                    .title("Studio Elegante na Vila Madalena")
                    .description("Studio totalmente mobiliado, localizado no coração da Vila Madalena, próximo a restaurantes e vida cultural.")
                    .location("Vila Madalena, São Paulo - SP")
                    .neighborhood("Vila Madalena")
                    .city("São Paulo")
                    .state("SP")
                    .price(new BigDecimal("2800"))
                    .bedrooms(1)
                    .bathrooms(1)
                    .parkingSpots(1)
                    .area(new BigDecimal("45"))
                    .type(PropertyType.RENT)
                    .featured(true)
                    .images(List.of(
                        "https://images.unsplash.com/photo-1536376072261-38c75010e6c9?w=800",
                        "https://images.unsplash.com/photo-1555636222-cae831e670b3?w=800"
                    ))
                    .build(),
                Apartment.builder()
                    .title("Cobertura Duplex no Alto da Lapa")
                    .description("Cobertura espetacular com piscina privativa, churrasqueira e vista panorâmica da cidade.")
                    .location("Alto da Lapa, São Paulo - SP")
                    .neighborhood("Alto da Lapa")
                    .city("São Paulo")
                    .state("SP")
                    .price(new BigDecimal("2100000"))
                    .bedrooms(4)
                    .bathrooms(4)
                    .parkingSpots(3)
                    .area(new BigDecimal("280"))
                    .type(PropertyType.SALE)
                    .featured(true)
                    .images(List.of(
                        "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?w=800",
                        "https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?w=800"
                    ))
                    .build(),
                Apartment.builder()
                    .title("Apartamento Aconchegante no Brooklin")
                    .description("Ótima localização, condomínio com academia, piscina e salão de festas. Pronto para morar.")
                    .location("Brooklin, São Paulo - SP")
                    .neighborhood("Brooklin")
                    .city("São Paulo")
                    .state("SP")
                    .price(new BigDecimal("4200"))
                    .bedrooms(2)
                    .bathrooms(2)
                    .parkingSpots(1)
                    .area(new BigDecimal("72"))
                    .type(PropertyType.RENT)
                    .featured(false)
                    .images(List.of(
                        "https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800",
                        "https://images.unsplash.com/photo-1484154218962-a197022b5858?w=800"
                    ))
                    .build(),
                Apartment.builder()
                    .title("Flat Executivo na Berrini")
                    .description("Flat com serviços de hotel no maior polo empresarial de SP. Ideal para quem trabalha na região.")
                    .location("Berrini, São Paulo - SP")
                    .neighborhood("Berrini")
                    .city("São Paulo")
                    .state("SP")
                    .price(new BigDecimal("480000"))
                    .bedrooms(1)
                    .bathrooms(1)
                    .parkingSpots(1)
                    .area(new BigDecimal("38"))
                    .type(PropertyType.SALE)
                    .featured(false)
                    .images(List.of(
                        "https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800"
                    ))
                    .build(),
                Apartment.builder()
                    .title("Apartamento Espaçoso no Morumbi")
                    .description("Amplo apartamento em condomínio fechado de alto padrão, com segurança 24h e área de lazer completa.")
                    .location("Morumbi, São Paulo - SP")
                    .neighborhood("Morumbi")
                    .city("São Paulo")
                    .state("SP")
                    .price(new BigDecimal("870000"))
                    .bedrooms(3)
                    .bathrooms(3)
                    .parkingSpots(2)
                    .area(new BigDecimal("130"))
                    .type(PropertyType.SALE)
                    .featured(false)
                    .images(List.of(
                        "https://images.unsplash.com/photo-1567767292278-a4f21aa2d36e?w=800",
                        "https://images.unsplash.com/photo-1618219908412-a29a1bb7b86e?w=800"
                    ))
                    .build()
            );
            apartmentRepository.saveAll(apartments);
            log.info("Sample apartments created.");
        }
    }
}