package com.imobiliaria.repository;

import com.imobiliaria.entity.Apartment;
import com.imobiliaria.entity.PropertyCategory;
import com.imobiliaria.entity.PropertyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface ApartmentRepository extends JpaRepository<Apartment, Long> {

    List<Apartment> findByActiveTrue();

    List<Apartment> findByFeaturedTrueAndActiveTrue();

    boolean existsByOwnerId(Long ownerId); // <-- NOVO

    @Query("""
        SELECT a FROM Apartment a
        WHERE a.active = true
        AND (:location IS NULL OR LOWER(a.location) LIKE LOWER(CONCAT('%', :location, '%'))
             OR LOWER(a.neighborhood) LIKE LOWER(CONCAT('%', :location, '%'))
             OR LOWER(a.city) LIKE LOWER(CONCAT('%', :location, '%')))
        AND (:type IS NULL OR a.type = :type)
        AND (:category IS NULL OR a.category = :category)
        AND (:minPrice IS NULL OR a.price >= :minPrice)
        AND (:maxPrice IS NULL OR a.price <= :maxPrice)
        AND (:bedrooms IS NULL OR a.bedrooms >= :bedrooms)
        ORDER BY a.createdAt DESC
    """)
    List<Apartment> searchApartments(
        @Param("location") String location,
        @Param("type") PropertyType type,
        @Param("category") PropertyCategory category,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("bedrooms") Integer bedrooms
    );
}