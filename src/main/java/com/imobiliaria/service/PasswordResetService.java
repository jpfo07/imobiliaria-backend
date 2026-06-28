package com.imobiliaria.service;

import com.imobiliaria.entity.User;
import com.imobiliaria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${app.frontend.url.reset}")
    private String frontendUrl;

    /**
     * Gera token e envia e-mail. Sempre responde com sucesso
     * para não revelar se o e-mail está cadastrado.
     */
    public void requestReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email.trim().toLowerCase());

        if (userOpt.isEmpty()) {
            // Não revela que o e-mail não existe — retorna silenciosamente
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
            // Log mas não lança exceção — a resposta HTTP sempre é 200
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

    private void sendResetEmail(String to, String name, String token) {
        String link = frontendUrl + "/redefinir-senha?token=" + token;

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("noreply@moreiraeoliveiraimoveis.com.br");
        msg.setTo(to);
        msg.setSubject("Redefinição de senha — Moreira & Oliveira Imóveis");
        msg.setText(
            "Olá, " + name + "!\n\n" +
            "Recebemos uma solicitação para redefinir a senha da sua conta.\n\n" +
            "Clique no link abaixo para criar uma nova senha (válido por 1 hora):\n\n" +
            link + "\n\n" +
            "Se você não fez essa solicitação, pode ignorar este e-mail.\n\n" +
            "Atenciosamente,\nEquipe Moreira & Oliveira Imóveis"
        );
        mailSender.send(msg);
        log.info("E-mail de reset enviado para: {}", to);
    }
}
