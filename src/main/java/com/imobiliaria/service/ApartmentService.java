package com.imobiliaria.service;

import com.imobiliaria.dto.ApartmentDTO;
import com.imobiliaria.entity.Apartment;
import com.imobiliaria.entity.PropertyType;
import com.imobiliaria.repository.ApartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApartmentService {

    private final ApartmentRepository apartmentRepository;

    public List<Apartment> getAll() {
        return apartmentRepository.findByActiveTrue();
    }

    public List<Apartment> getFeatured() {
        return apartmentRepository.findByFeaturedTrueAndActiveTrue();
    }

    public Apartment getById(Long id) {
        return apartmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imóvel não encontrado"));
    }

    public List<Apartment> search(String location, String type, BigDecimal minPrice,
                                   BigDecimal maxPrice, Integer bedrooms) {
        PropertyType propertyType = null;
        if (type != null && !type.isBlank()) {
            propertyType = PropertyType.valueOf(type.toUpperCase());
        }
        return apartmentRepository.searchApartments(location, propertyType, minPrice, maxPrice, bedrooms);
    }

    public Apartment create(ApartmentDTO dto) {
        Apartment apartment = mapToEntity(dto, new Apartment());
        return apartmentRepository.save(apartment);
    }

    public Apartment update(Long id, ApartmentDTO dto) {
        Apartment apartment = getById(id);
        mapToEntity(dto, apartment);
        return apartmentRepository.save(apartment);
    }

    // Método para desativação lógica (usado no site comum)
    public void delete(Long id) {
        Apartment apartment = getById(id);
        apartment.setActive(false);
        apartmentRepository.save(apartment);
    }

    // Método para exclusão real (exigido pelo AdminController)
    public void hardDelete(Long id) {
        Apartment apartment = getById(id);
        apartmentRepository.delete(apartment);
    }

    private Apartment mapToEntity(ApartmentDTO dto, Apartment apartment) {
        apartment.setTitle(dto.getTitle());
        apartment.setDescription(dto.getDescription());
        apartment.setLocation(dto.getLocation());
        apartment.setNeighborhood(dto.getNeighborhood());
        apartment.setCity(dto.getCity());
        apartment.setState(dto.getState());
        apartment.setPrice(dto.getPrice());
        apartment.setBedrooms(dto.getBedrooms());
        apartment.setBathrooms(dto.getBathrooms());
        apartment.setParkingSpots(dto.getParkingSpots());
        apartment.setArea(dto.getArea());
        apartment.setType(dto.getType());
        
        // Garante que valores booleanos não sejam nulos
        apartment.setFeatured(dto.getFeatured() != null ? dto.getFeatured() : false);
        apartment.setActive(dto.getActive() != null ? dto.getActive() : true);
        apartment.setSold(dto.getSold() != null ? dto.getSold() : false);
        apartment.setFinancing(dto.getFinancing() != null ? dto.getFinancing() : false);
        
        // Proteção crucial: se a lista de imagens for nula no DTO, cria uma lista vazia
        if (dto.getImages() != null) {
            apartment.setImages(new ArrayList<>(dto.getImages()));
        } else {
            apartment.setImages(new ArrayList<>());
        }
        
        return apartment;
    }
}