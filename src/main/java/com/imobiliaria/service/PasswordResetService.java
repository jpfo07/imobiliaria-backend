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

    @Value("${spring.mail.username:}")
    private String mailFrom;

    /**
     * Gera token de reset e envia e-mail.
     * Sempre retorna sucesso (não revela se o e-mail existe).
     */
    public void requestReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
            userRepository.save(user);

            sendResetEmail(user.getEmail(), user.getName(), token);
        });
    }

    /**
     * Valida o token e redefine a senha.
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
        try {
            String link = frontendUrl + "/redefinir-senha?token=" + token;

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailFrom);
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
        } catch (Exception e) {
            log.error("Erro ao enviar e-mail de reset para {}: {}", to, e.getMessage());
        }
    }
}
