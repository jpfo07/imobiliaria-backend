package com.imobiliaria.controller;

import com.imobiliaria.dto.ApartmentDTO;
import com.imobiliaria.dto.OwnerDTO;
import com.imobiliaria.dto.ReorderRequest;
import com.imobiliaria.entity.Apartment;
import com.imobiliaria.entity.Owner;
import com.imobiliaria.entity.User;
import com.imobiliaria.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final ApartmentService   apartmentService;
    private final UserService        userService;
    private final FavoriteService    favoriteService;
    private final ImageUploadService imageUploadService;
    private final OwnerService       ownerService;

    // ── Imóveis ────────────────────────────────────────────
    @GetMapping("/apartments")
    public ResponseEntity<List<Apartment>> getAllApartments() {
        return ResponseEntity.ok(apartmentService.getAllAdmin());
    }

    @PostMapping("/apartments")
    public ResponseEntity<Apartment> createApartment(@Valid @RequestBody ApartmentDTO dto) {
        return ResponseEntity.ok(apartmentService.create(dto));
    }

    @PutMapping("/apartments/{id}")
    public ResponseEntity<Apartment> updateApartment(
            @PathVariable Long id,
            @Valid @RequestBody ApartmentDTO dto) {
        return ResponseEntity.ok(apartmentService.update(id, dto));
    }

    @DeleteMapping("/apartments/{id}")
    public ResponseEntity<Map<String, String>> deleteApartment(@PathVariable Long id) {
        apartmentService.hardDelete(id);
        return ResponseEntity.ok(Map.of("message", "Imóvel excluído com sucesso"));
    }

    // ── Reordenação ────────────────────────────────────────

    /**
     * Reordena a exibição dos cards na home.
     * Body: { "orderedIds": [3, 1, 5, 2, ...] }
     */
    @PutMapping("/apartments/reorder/home")
    public ResponseEntity<Map<String, String>> reorderHome(@RequestBody ReorderRequest request) {
        apartmentService.reorderHome(request);
        return ResponseEntity.ok(Map.of("message", "Ordem da home atualizada com sucesso"));
    }

    /**
     * Reordena a exibição do carrossel de destaques.
     * Body: { "orderedIds": [5, 3, 1, ...] }  (apenas IDs em destaque)
     */
    @PutMapping("/apartments/reorder/featured")
    public ResponseEntity<Map<String, String>> reorderFeatured(@RequestBody ReorderRequest request) {
        apartmentService.reorderFeatured(request);
        return ResponseEntity.ok(Map.of("message", "Ordem do carrossel atualizada com sucesso"));
    }

    // ── Upload de imagem com marca d'água ──────────────────
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file) {
        try {
            String url = imageUploadService.uploadWithWatermark(file);
            return ResponseEntity.ok(Map.of("url", url, "message", "Upload realizado com sucesso"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Usuários ───────────────────────────────────────────
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // ── Favoritos ──────────────────────────────────────────
    @GetMapping("/favorites")
    public ResponseEntity<List<Map<String, Object>>> getAllFavorites() {
        return ResponseEntity.ok(favoriteService.getAllFavoritesAdmin());
    }

    // ── Proprietários ──────────────────────────────────────
    @PostMapping("/owners")
    public ResponseEntity<Map<String, Object>> createOwner(@Valid @RequestBody OwnerDTO dto) {
        Owner owner = ownerService.createOwner(dto);
        return ResponseEntity.ok(Map.of(
            "id", owner.getId(),
            "name", owner.getName(),
            "message", "Proprietário criado com sucesso"
        ));
    }

    @GetMapping("/owners")
    public ResponseEntity<List<Map<String, Object>>> getAllOwners() {
        return ResponseEntity.ok(ownerService.getAllOwnersWithApartments());
    }

    @PutMapping("/owners/{id}")
    public ResponseEntity<Map<String, String>> updateOwner(
            @PathVariable Long id,
            @Valid @RequestBody OwnerDTO dto) {
        ownerService.updateOwner(id, dto);
        return ResponseEntity.ok(Map.of("message", "Proprietário atualizado com sucesso"));
    }

    @DeleteMapping("/owners/{id}")
    public ResponseEntity<Map<String, String>> deleteOwner(@PathVariable Long id) {
        ownerService.deleteOwner(id);
        return ResponseEntity.ok(Map.of("message", "Proprietário excluído com sucesso"));
    }

    /** Imóveis sem proprietário — para o select de vincular */
    @GetMapping("/apartments/without-owner")
    public ResponseEntity<List<Map<String, Object>>> getApartmentsWithoutOwner() {
        return ResponseEntity.ok(ownerService.getApartmentsWithoutOwner());
    }

    /** Vincula um imóvel existente a um proprietário */
    @PostMapping("/owners/{ownerId}/apartments/{apartmentId}")
    public ResponseEntity<Map<String, String>> linkApartment(
            @PathVariable Long ownerId, @PathVariable Long apartmentId) {
        ownerService.linkApartment(ownerId, apartmentId);
        return ResponseEntity.ok(Map.of("message", "Imóvel vinculado com sucesso"));
    }

    /** Desvincula um imóvel do seu proprietário */
    @DeleteMapping("/owners/apartments/{apartmentId}/unlink")
    public ResponseEntity<Map<String, String>> unlinkApartment(@PathVariable Long apartmentId) {
        ownerService.unlinkApartment(apartmentId);
        return ResponseEntity.ok(Map.of("message", "Imóvel desvinculado com sucesso"));
    }
}
