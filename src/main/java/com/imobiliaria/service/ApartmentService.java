package com.imobiliaria.service;

import com.imobiliaria.dto.ApartmentDTO;
import com.imobiliaria.dto.ReorderRequest;
import com.imobiliaria.entity.Apartment;
import com.imobiliaria.entity.Owner;
import com.imobiliaria.entity.PropertyCategory;
import com.imobiliaria.entity.PropertyType;
import com.imobiliaria.repository.ApartmentRepository;
import com.imobiliaria.repository.OwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApartmentService {

    private final ApartmentRepository apartmentRepository;
    private final OwnerRepository ownerRepository;

    public List<Apartment> getAll() {
        return apartmentRepository.findByActiveTrue();
    }

    public List<Apartment> getAllAdmin() {
        return apartmentRepository.findAll();
    }

    public List<Apartment> getFeatured() {
        return apartmentRepository.findByFeaturedTrueAndActiveTrue();
    }

    public Apartment getById(Long id) {
        return apartmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imóvel não encontrado"));
    }

    public List<Apartment> search(String location, String type, String category,
                                   BigDecimal minPrice, BigDecimal maxPrice, Integer bedrooms) {
        PropertyType propertyType = null;
        if (type != null && !type.isBlank()) {
            propertyType = PropertyType.valueOf(type.toUpperCase());
        }
        PropertyCategory propertyCategory = null;
        if (category != null && !category.isBlank()) {
            propertyCategory = PropertyCategory.valueOf(category.toUpperCase());
        }
        return apartmentRepository.searchApartments(location, propertyType, propertyCategory,
                minPrice, maxPrice, bedrooms);
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

    public void delete(Long id) {
        Apartment apartment = getById(id);
        apartment.setActive(false);
        apartmentRepository.save(apartment);
    }

    public void hardDelete(Long id) {
        Apartment apartment = getById(id);
        Owner owner = apartment.getOwner();

        apartment.setOwner(null);
        apartmentRepository.save(apartment);
        apartmentRepository.delete(apartment);

        if (owner != null) {
            boolean stillHasApartments = apartmentRepository.existsByOwnerId(owner.getId());
            if (!stillHasApartments) {
                ownerRepository.delete(owner);
            }
        }
    }

    /**
     * Reordena os cards da home (displayOrder).
     * orderedIds contém todos os IDs na nova ordem desejada.
     */
    @Transactional
    public void reorderHome(ReorderRequest request) {
        List<Long> orderedIds = request.getOrderedIds();
        for (int i = 0; i < orderedIds.size(); i++) {
            final int order = i;
            Long id = orderedIds.get(i);
            apartmentRepository.findById(id).ifPresent(apt -> {
                apt.setDisplayOrder(order);
                apartmentRepository.save(apt);
            });
        }
    }

    /**
     * Reordena o carrossel de destaques (featuredOrder).
     * orderedIds contém apenas os IDs em destaque na nova ordem.
     */
    @Transactional
    public void reorderFeatured(ReorderRequest request) {
        List<Long> orderedIds = request.getOrderedIds();
        for (int i = 0; i < orderedIds.size(); i++) {
            final int order = i;
            Long id = orderedIds.get(i);
            apartmentRepository.findById(id).ifPresent(apt -> {
                apt.setFeaturedOrder(order);
                apartmentRepository.save(apt);
            });
        }
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
        apartment.setCategory(dto.getCategory());

        if (dto.getOwnerName() != null && !dto.getOwnerName().isBlank()) {
            Owner owner = apartment.getOwner() != null ? apartment.getOwner() : new Owner();
            owner.setName(dto.getOwnerName());
            owner.setPhone(dto.getOwnerPhone());
            owner.setEmail(dto.getOwnerEmail());
            owner = ownerRepository.save(owner);
            apartment.setOwner(owner);
        } else {
            apartment.setOwner(null);
        }

        apartment.setFeatured(dto.getFeatured() != null ? dto.getFeatured() : false);
        apartment.setActive(dto.getActive() != null ? dto.getActive() : true);
        apartment.setSold(dto.getSold() != null ? dto.getSold() : false);
        apartment.setFinancing(dto.getFinancing() != null ? dto.getFinancing() : false);

        if (dto.getImages() != null) {
            apartment.setImages(new ArrayList<>(dto.getImages()));
        } else {
            apartment.setImages(new ArrayList<>());
        }

        // Ordens customizadas (opcionais — só sobrescreve se enviado no DTO)
        if (dto.getDisplayOrder() != null) {
            apartment.setDisplayOrder(dto.getDisplayOrder());
        }
        if (dto.getFeaturedOrder() != null) {
            apartment.setFeaturedOrder(dto.getFeaturedOrder());
        }

        return apartment;
    }
}
