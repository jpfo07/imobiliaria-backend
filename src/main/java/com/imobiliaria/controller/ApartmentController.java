package com.imobiliaria.controller;

import com.imobiliaria.dto.ApartmentDTO;
import com.imobiliaria.entity.Apartment;
import com.imobiliaria.service.ApartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/apartments")
@RequiredArgsConstructor
public class ApartmentController {

    private final ApartmentService apartmentService;

    @GetMapping
    public ResponseEntity<List<Apartment>> getAll() {
        return ResponseEntity.ok(apartmentService.getAll());
    }

    @GetMapping("/featured")
    public ResponseEntity<List<Apartment>> getFeatured() {
        return ResponseEntity.ok(apartmentService.getFeatured());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Apartment> getById(@PathVariable Long id) {
        return ResponseEntity.ok(apartmentService.getById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Apartment>> search(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer bedrooms) {
        return ResponseEntity.ok(apartmentService.search(location, type, category, minPrice, maxPrice, bedrooms));
    }
}
