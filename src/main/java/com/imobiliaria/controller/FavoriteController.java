package com.imobiliaria.controller;


import com.imobiliaria.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

   @GetMapping
public ResponseEntity<List<Map<String, Object>>> getMyFavorites(@AuthenticationPrincipal UserDetails user) {
    return ResponseEntity.ok(favoriteService.getUserFavorites(user.getUsername()));
}

@GetMapping("/ids")
public ResponseEntity<List<Long>> getMyFavoriteIds(@AuthenticationPrincipal UserDetails user) {
    return ResponseEntity.ok(favoriteService.getUserFavoriteIds(user.getUsername()));
}

    @PostMapping("/toggle/{apartmentId}")
    public ResponseEntity<Map<String, Object>> toggle(
            @PathVariable Long apartmentId,
            @AuthenticationPrincipal UserDetails user) {
        boolean added = favoriteService.toggleFavorite(user.getUsername(), apartmentId);
        return ResponseEntity.ok(Map.of(
                "favorited", added,
                "message", added ? "Adicionado aos favoritos" : "Removido dos favoritos"
        ));
    }

    @GetMapping("/check/{apartmentId}")
    public ResponseEntity<Map<String, Boolean>> check(
            @PathVariable Long apartmentId,
            @AuthenticationPrincipal UserDetails user) {
        boolean favorited = favoriteService.isFavorited(user.getUsername(), apartmentId);
        return ResponseEntity.ok(Map.of("favorited", favorited));
    }
}