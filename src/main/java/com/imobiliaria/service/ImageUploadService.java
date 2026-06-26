package com.imobiliaria.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class ImageUploadService {

    private final Cloudinary cloudinary;

    // Public ID do logo no Cloudinary — faça upload do logo uma vez e coloque o public_id aqui
    // Ex: se o logo está em "logos/moreira-logo", coloque "logos:moreira-logo"
    // (Cloudinary usa ":" como separador de pasta no overlay)
    @Value("${cloudinary.logo.public-id:logos:moreira-logo}")
    private String logoPublicId;

    private static final Set<String> ALLOWED_TYPES = Set.of(
        "image/jpeg", "image/jpg", "image/png", "image/webp"
    );
    private static final long MAX_SIZE_BYTES = 10 * 1024 * 1024; // 10MB

    public ImageUploadService(@Value("${cloudinary.url}") String cloudinaryUrl) {
        this.cloudinary = new Cloudinary(cloudinaryUrl);
        this.cloudinary.config.secure = true;
    }

    /**
     * Upload de imagem de imóvel:
     * - Preserva proporção original (c_pad com background branco) → sem corte, sem distorção
     * - Aplica o logo da imobiliária como marca d'água no canto inferior direito
     */
    public String uploadWithWatermark(MultipartFile file) throws IOException {
        validateFile(file);

        String publicId = "imoveis/" + UUID.randomUUID();

        Map uploadResult = cloudinary.uploader().upload(
            file.getBytes(),
            ObjectUtils.asMap(
                "public_id", publicId,
                "transformation", new Transformation()
                    // Redimensiona mantendo proporção — preenche espaço vazio com branco
                    .width(1200).height(800)
                    .crop("pad")
                    .background("white")
                    .quality("auto")
                    .fetchFormat("auto")
                    // Marca d'água: logo centralizado na parte inferior
                    .chain()
                    .overlay(logoPublicId)
                    .width(180)
                    .crop("scale")
                    .gravity("south")
                    .y(15)
                    .opacity(85)
            )
        );

        String url = (String) uploadResult.get("secure_url");
        log.info("Imagem de imóvel enviada para Cloudinary: {}", url);
        return url;
    }

    /**
     * Upload de foto de perfil — sem marca d'água, crop inteligente no rosto
     */
    public String uploadProfilePhoto(MultipartFile file) throws IOException {
        validateFile(file);

        String publicId = "perfis/" + UUID.randomUUID();

        Map uploadResult = cloudinary.uploader().upload(
            file.getBytes(),
            ObjectUtils.asMap(
                "public_id", publicId,
                "transformation", new Transformation()
                    .width(400).height(400)
                    .crop("fill")
                    .gravity("face")
                    .quality("auto")
                    .fetchFormat("auto")
            )
        );

        String url = (String) uploadResult.get("secure_url");
        log.info("Foto de perfil enviada para Cloudinary: {}", url);
        return url;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new RuntimeException("Arquivo não pode estar vazio");
        if (file.getSize() > MAX_SIZE_BYTES)
            throw new RuntimeException("Arquivo muito grande. Máximo: 10MB");
        String ct = file.getContentType();
        if (ct == null || !ALLOWED_TYPES.contains(ct.toLowerCase()))
            throw new RuntimeException("Tipo inválido. Use JPEG, PNG ou WebP");
    }
}
