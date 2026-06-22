package com.imobiliaria.controller;

import com.imobiliaria.dto.UserDTO;
import com.imobiliaria.entity.User;
import com.imobiliaria.service.ImageUploadService;
import com.imobiliaria.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService        userService;
    private final ImageUploadService imageUploadService;

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getByEmail(userDetails.getUsername());
        return ResponseEntity.ok(Map.of(
                "id",        user.getId(),
                "name",      user.getName(),
                "email",     user.getEmail(),
                "phone",     user.getPhone()    != null ? user.getPhone()    : "",
                "photoUrl",  user.getPhotoUrl() != null ? user.getPhotoUrl() : "",
                "role",      user.getRole().name(),
                "createdAt", user.getCreatedAt()
        ));
    }

    @PutMapping("/me")
    public ResponseEntity<Map<String, Object>> updateMe(
            @RequestBody UserDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.updateProfile(userDetails.getUsername(), dto);
        return ResponseEntity.ok(Map.of(
                "id",       user.getId(),
                "name",     user.getName(),
                "email",    user.getEmail(),
                "phone",    user.getPhone()    != null ? user.getPhone()    : "",
                "photoUrl", user.getPhotoUrl() != null ? user.getPhotoUrl() : ""
        ));
    }

    /**
     * Upload direto de foto de perfil — sem marca d'água
     */
    @PostMapping("/me/photo")
    public ResponseEntity<Map<String, Object>> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Reutiliza o mesmo serviço de upload, sem marca d'água para foto de perfil
            String url = imageUploadService.uploadProfilePhoto(file);

            // Já salva no banco
            UserDTO dto = new UserDTO();
            dto.setPhotoUrl(url);
            User user = userService.updateProfile(userDetails.getUsername(), dto);

            return ResponseEntity.ok(Map.of(
                    "photoUrl", user.getPhotoUrl(),
                    "message",  "Foto atualizada com sucesso"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}