package com.imobiliaria.service;

import com.imobiliaria.entity.*;
import com.imobiliaria.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final ApartmentRepository apartmentRepository;

    @Transactional(readOnly = true)
public List<Map<String, Object>> getUserFavorites(String email) {
    User user = userRepository.findByEmail(email).orElseThrow();
    return favoriteRepository.findByUserEager(user)
            .stream()
            .map(fav -> {
                Apartment apt = fav.getApartment();
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id",           apt.getId());
                m.put("title",        apt.getTitle());
                m.put("description",  apt.getDescription());
                m.put("location",     apt.getLocation());
                m.put("neighborhood", apt.getNeighborhood());
                m.put("city",         apt.getCity());
                m.put("state",        apt.getState());
                m.put("price",        apt.getPrice());
                m.put("bedrooms",     apt.getBedrooms());
                m.put("bathrooms",    apt.getBathrooms());
                m.put("parkingSpots", apt.getParkingSpots());
                m.put("area",         apt.getArea());
                m.put("type",         apt.getType());
                m.put("featured",     apt.getFeatured());
                m.put("active",       apt.getActive());
                // força carregamento do ElementCollection dentro da transação
                List<String> imgs = apt.getImages();
                m.put("images", imgs != null ? new ArrayList<>(imgs) : new ArrayList<>());
                return m;
            })
            .collect(Collectors.toList());
}

// ADICIONA logo abaixo (novo método — para o Home usar):
@Transactional(readOnly = true)
public List<Long> getUserFavoriteIds(String email) {
    User user = userRepository.findByEmail(email).orElseThrow();
    return favoriteRepository.findByUserEager(user)
            .stream()
            .map(fav -> fav.getApartment().getId())
            .collect(Collectors.toList());
}

    @Transactional
    public boolean toggleFavorite(String email, Long apartmentId) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new RuntimeException("Imóvel não encontrado"));

        return favoriteRepository.findByUserAndApartment(user, apartment)
                .map(fav -> {
                    favoriteRepository.delete(fav);
                    favoriteRepository.flush();
                    return false;
                })
                .orElseGet(() -> {
                    favoriteRepository.save(
                        Favorite.builder().user(user).apartment(apartment).build()
                    );
                    return true;
                });
    }

    @Transactional(readOnly = true)
    public boolean isFavorited(String email, Long apartmentId) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Apartment apartment = apartmentRepository.findById(apartmentId).orElseThrow();
        return favoriteRepository.existsByUserAndApartment(user, apartment);
    }

    /**
     * CORRIGIDO: Usa query dedicada sem Object[] para evitar problemas
     * de mapeamento e retorna DTOs limpos para o admin.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllFavoritesAdmin() {
        return favoriteRepository.findAllForAdmin()
                .stream()
                .map(fav -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("favoriteId",      fav.getId());
                    item.put("userId",          fav.getUser().getId());
                    item.put("userName",        fav.getUser().getName());
                    item.put("userEmail",       fav.getUser().getEmail());
                    item.put("apartmentId",     fav.getApartment().getId());
                    item.put("apartmentTitle",  fav.getApartment().getTitle());
                    item.put("apartmentType",   fav.getApartment().getType());
                    item.put("favoritedAt",     fav.getCreatedAt());
                    return item;
                })
                .collect(Collectors.toList());
    }
}