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

    /**
     * Retorna imóveis ativos ordenados por:
     * 1. displayOrder ASC (nulos vão para o final)
     * 2. createdAt DESC como fallback
     */
    @Query("""
        SELECT a FROM Apartment a
        WHERE a.active = true
        ORDER BY
            CASE WHEN a.displayOrder IS NULL THEN 1 ELSE 0 END ASC,
            a.displayOrder ASC,
            a.createdAt DESC
    """)
    List<Apartment> findByActiveTrue();

    /**
     * Retorna imóveis em destaque ordenados por featuredOrder ASC,
     * com fallback para createdAt DESC.
     */
    @Query("""
        SELECT a FROM Apartment a
        WHERE a.featured = true AND a.active = true
        ORDER BY
            CASE WHEN a.featuredOrder IS NULL THEN 1 ELSE 0 END ASC,
            a.featuredOrder ASC,
            a.createdAt DESC
    """)
    List<Apartment> findByFeaturedTrueAndActiveTrue();

    boolean existsByOwnerId(Long ownerId);

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
        ORDER BY
            CASE WHEN a.displayOrder IS NULL THEN 1 ELSE 0 END ASC,
            a.displayOrder ASC,
            a.createdAt DESC
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
