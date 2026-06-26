package com.imobiliaria.service;

import com.imobiliaria.dto.OwnerDTO;
import com.imobiliaria.entity.Apartment;
import com.imobiliaria.entity.Owner;
import com.imobiliaria.repository.ApartmentRepository;
import com.imobiliaria.repository.OwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerService {

    private final OwnerRepository ownerRepository;
    private final ApartmentRepository apartmentRepository;

    public Owner updateOwner(Long id, OwnerDTO dto) {
        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proprietário não encontrado"));
        owner.setName(dto.getName());
        owner.setPhone(dto.getPhone());
        owner.setEmail(dto.getEmail());
        return ownerRepository.save(owner);
    }

    /**
     * Vincula um imóvel a um proprietário existente.
     */
    @Transactional
    public void linkApartment(Long ownerId, Long apartmentId) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Proprietário não encontrado"));
        Apartment apt = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new RuntimeException("Imóvel não encontrado"));
        apt.setOwner(owner);
        apartmentRepository.save(apt);
    }

    /**
     * Desvincula um imóvel de seu proprietário.
     */
    @Transactional
    public void unlinkApartment(Long apartmentId) {
        Apartment apt = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new RuntimeException("Imóvel não encontrado"));
        apt.setOwner(null);
        apartmentRepository.save(apt);
    }

    public List<Map<String, Object>> getAllOwnersWithApartments() {
        List<Owner> owners = ownerRepository.findAll();
        List<Apartment> apartments = apartmentRepository.findAll();

        return owners.stream().map(owner -> {
            List<Map<String, Object>> ownerApts = apartments.stream()
                .filter(a -> a.getOwner() != null && a.getOwner().getId().equals(owner.getId()))
                .map(a -> Map.<String, Object>of(
                    "id",    a.getId(),
                    "title", a.getTitle(),
                    "city",  a.getCity() != null ? a.getCity() : "",
                    "type",  a.getType() != null ? a.getType().name() : "",
                    "price", a.getPrice()
                ))
                .collect(Collectors.toList());

            return Map.<String, Object>of(
                "id",         owner.getId(),
                "name",       owner.getName(),
                "phone",      owner.getPhone() != null ? owner.getPhone() : "",
                "email",      owner.getEmail() != null ? owner.getEmail() : "",
                "createdAt",  owner.getCreatedAt().toString(),
                "apartments", ownerApts
            );
        }).collect(Collectors.toList());
    }

    /** Retorna todos os imóveis sem proprietário vinculado. */
    public List<Map<String, Object>> getApartmentsWithoutOwner() {
        return apartmentRepository.findAll().stream()
                .filter(a -> a.getOwner() == null)
                .map(a -> Map.<String, Object>of(
                    "id",    a.getId(),
                    "title", a.getTitle(),
                    "city",  a.getCity() != null ? a.getCity() : "",
                    "type",  a.getType() != null ? a.getType().name() : ""
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteOwner(Long id) {
        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proprietário não encontrado"));

        List<Apartment> apartments = apartmentRepository.findAll().stream()
                .filter(a -> a.getOwner() != null && a.getOwner().getId().equals(id))
                .collect(Collectors.toList());
        apartments.forEach(a -> a.setOwner(null));
        apartmentRepository.saveAll(apartments);

        ownerRepository.delete(owner);
    }
}
