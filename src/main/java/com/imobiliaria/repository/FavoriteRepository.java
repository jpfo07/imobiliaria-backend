package com.imobiliaria.repository;

import com.imobiliaria.entity.Favorite;
import com.imobiliaria.entity.User;
import com.imobiliaria.entity.Apartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByUser(User user);

    Optional<Favorite> findByUserAndApartment(User user, Apartment apartment);

    boolean existsByUserAndApartment(User user, Apartment apartment);

    @Query("""
    SELECT f FROM Favorite f
    JOIN FETCH f.apartment a
    WHERE f.user = :user
    ORDER BY f.createdAt DESC
""")
List<Favorite> findByUserEager(@Param("user") User user);

    /**
     * CORRIGIDO: JOIN FETCH explícito para carregar user e apartment
     * em uma única query, sem cartesian product e sem N+1.
     * Substitui a query anterior que retornava Object[].
     */
    @Query("""
        SELECT f FROM Favorite f
        JOIN FETCH f.user u
        JOIN FETCH f.apartment a
        ORDER BY f.createdAt DESC
    """)
    List<Favorite> findAllForAdmin();

    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.apartment.id = :apartmentId")
    Long countByApartmentId(@Param("apartmentId") Long apartmentId);
}