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

    private static final String LOGO_URL =
        "https://res.cloudinary.com/drq3eqr0p/image/upload/v1782248304/logos/moreira-logo.jpg";

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

        String html = """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1.0"></head>
            <body style="margin:0;padding:0;background:#f4f4f5;font-family:Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f4f5;padding:40px 0;">
                <tr><td align="center">
                  <table width="560" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,0.08);">

                    <!-- Header com logo -->
                    <tr>
                      <td align="center" style="background:#1a1a2e;padding:32px 40px;">
                        <img src="%s" alt="Moreira & Oliveira Imóveis" width="160" style="display:block;border-radius:8px;" />
                      </td>
                    </tr>

                    <!-- Corpo -->
                    <tr>
                      <td style="padding:40px;">
                        <h1 style="margin:0 0 16px;font-size:22px;color:#1a1a2e;">Redefinição de senha</h1>
                        <p style="margin:0 0 12px;font-size:15px;color:#444;line-height:1.6;">Olá, <strong>%s</strong>!</p>
                        <p style="margin:0 0 28px;font-size:15px;color:#444;line-height:1.6;">
                          Recebemos uma solicitação para redefinir a senha da sua conta.<br>
                          Clique no botão abaixo para criar uma nova senha. O link é válido por <strong>1 hora</strong>.
                        </p>

                        <!-- Botão -->
                        <table cellpadding="0" cellspacing="0" style="margin:0 0 28px;">
                          <tr>
                            <td align="center" style="background:#c9a84c;border-radius:10px;">
                              <a href="%s" target="_blank"
                                 style="display:inline-block;padding:14px 32px;font-size:15px;font-weight:bold;color:#1a1a2e;text-decoration:none;">
                                Redefinir minha senha
                              </a>
                            </td>
                          </tr>
                        </table>

                        <p style="margin:0 0 8px;font-size:13px;color:#888;line-height:1.5;">
                          Se o botão não funcionar, copie e cole este link no navegador:
                        </p>
                        <p style="margin:0 0 28px;font-size:12px;color:#c9a84c;word-break:break-all;">%s</p>

                        <hr style="border:none;border-top:1px solid #eee;margin:0 0 24px;" />
                        <p style="margin:0;font-size:13px;color:#aaa;line-height:1.5;">
                          Se você não solicitou a redefinição de senha, pode ignorar este e-mail com segurança.
                          Sua senha não será alterada.
                        </p>
                      </td>
                    </tr>

                    <!-- Footer -->
                    <tr>
                      <td align="center" style="background:#f9f9f9;padding:20px 40px;border-top:1px solid #eee;">
                        <p style="margin:0;font-size:12px;color:#aaa;">
                          © 2025 Moreira & Oliveira Imóveis · Todos os direitos reservados
                        </p>
                      </td>
                    </tr>

                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(LOGO_URL, name, link, link);

        // Escapa as aspas para o JSON
        String bodyJson = String.format("""
            {
                "from": "Moreira & Oliveira Imóveis <noreply@moreiraeoliveiraimoveis.com.br>",
                "to": ["%s"],
                "subject": "Redefinição de senha — Moreira & Oliveira Imóveis",
                "html": %s
            }
            """, to, toJsonString(html));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.resend.com/emails"))
                .header("Authorization", "Bearer " + resendApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            log.info("E-mail HTML de reset enviado via Resend para: {}", to);
        } else {
            log.error("Resend API retornou {}: {}", response.statusCode(), response.body());
            throw new RuntimeException("Falha ao enviar e-mail: " + response.body());
        }
    }

    /** Converte string HTML para JSON string escapada */
    private String toJsonString(String html) {
        String escaped = html
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "")
            .replace("\t", "\\t");
        return "\"" + escaped + "\"";
    }
}