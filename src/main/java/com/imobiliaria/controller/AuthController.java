package com.imobiliaria.controller;

import com.imobiliaria.dto.*;
import com.imobiliaria.service.AuthService;
import com.imobiliaria.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validate() {
        return ResponseEntity.ok(Map.of("valid", true));
    }

    /** Solicita link de reset de senha por e-mail. Sempre retorna 200 (não revela se e-mail existe). */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody PasswordResetRequest request) {
        try {
            passwordResetService.requestReset(request.getEmail());
        } catch (Exception ignored) {}
        return ResponseEntity.ok(Map.of(
            "message", "Se esse e-mail estiver cadastrado, você receberá um link em breve."
        ));
    }

    /** Redefine a senha com o token recebido por e-mail. */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody PasswordResetConfirm request) {
        try {
            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Senha redefinida com sucesso!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
