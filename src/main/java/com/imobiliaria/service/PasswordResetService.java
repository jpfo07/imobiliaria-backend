package com.imobiliaria.service;

import com.imobiliaria.entity.User;
import com.imobiliaria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend.url.reset}")
    private String frontendUrl;

    @Value("${MAIL_PASSWORD:}")
    private String resendApiKey;

    /**
     * Gera token e envia e-mail via API REST do Resend (instantâneo).
     * Sempre responde com sucesso para não revelar se o e-mail está cadastrado.
     */
    public void requestReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email.trim().toLowerCase());

        if (userOpt.isEmpty()) {
            log.info("Reset solicitado para e-mail não cadastrado: {}", email);
            return;
        }

        User user = userOpt.get();
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        try {
            sendResetEmail(user.getEmail(), user.getName(), token);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de reset para {}: {}", email, e.getMessage());
        }
    }

    /**
     * Valida token e redefine a senha.
     */
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findAll().stream()
                .filter(u -> token.equals(u.getResetToken()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Token inválido ou expirado"));

        if (user.getResetTokenExpiry() == null ||
                LocalDateTime.now().isAfter(user.getResetTokenExpiry())) {
            throw new RuntimeException("Token expirado. Solicite um novo link.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    private void sendResetEmail(String to, String name, String token) throws Exception {
        String link = frontendUrl + "/redefinir-senha?token=" + token;

        String body = """
            {
                "from": "Moreira & Oliveira Imóveis <noreply@moreiraeoliveiraimoveis.com.br>",
                "to": ["%s"],
                "subject": "Redefinição de senha — Moreira & Oliveira Imóveis",
                "text": "Olá, %s!\\n\\nRecebemos uma solicitação para redefinir a senha da sua conta.\\n\\nClique no link abaixo para criar uma nova senha (válido por 1 hora):\\n\\n%s\\n\\nSe você não fez essa solicitação, pode ignorar este e-mail.\\n\\nAtenciosamente,\\nEquipe Moreira & Oliveira Imóveis"
            }
            """.formatted(to, name, link);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.resend.com/emails"))
                .header("Authorization", "Bearer " + resendApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            log.info("E-mail de reset enviado via Resend API para: {}", to);
        } else {
            log.error("Resend API retornou {}: {}", response.statusCode(), response.body());
            throw new RuntimeException("Falha ao enviar e-mail: " + response.body());
        }
    }
}
