package com.imobiliaria.service;

import com.imobiliaria.dto.OwnerDTO;
import com.imobiliaria.entity.Apartment;
import com.imobiliaria.entity.Owner;
import com.imobiliaria.repository.ApartmentRepository;
import com.imobiliaria.repository.OwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerService {

    private final OwnerRepository ownerRepository;
    private final ApartmentRepository apartmentRepository;

    /**
     * Atualiza nome, telefone e e-mail de um proprietário existente.
     */
    public Owner updateOwner(Long id, OwnerDTO dto) {
        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proprietário não encontrado"));
        owner.setName(dto.getName());
        owner.setPhone(dto.getPhone());
        owner.setEmail(dto.getEmail());
        return ownerRepository.save(owner);
    }

    /**
     * Retorna todos os proprietários com seus imóveis vinculados.
     */
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

    public void deleteOwner(Long id) {
    Owner owner = ownerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Proprietário não encontrado"));

    // Desvincula o owner dos imóveis antes de deletar
    List<Apartment> apartments = apartmentRepository.findAll().stream()
            .filter(a -> a.getOwner() != null && a.getOwner().getId().equals(id))
            .collect(Collectors.toList());
    apartments.forEach(a -> a.setOwner(null));
    apartmentRepository.saveAll(apartments);

    ownerRepository.delete(owner);
}
}