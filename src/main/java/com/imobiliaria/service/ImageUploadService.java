package com.imobiliaria.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.File; 
import java.nio.file.*;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class ImageUploadService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.base-url:http://localhost:8080/uploads}")
    private String baseUrl;

    private static final Set<String> ALLOWED_TYPES = Set.of(
        "image/jpeg", "image/jpg", "image/png", "image/webp"
    );
    private static final long MAX_SIZE_BYTES = 10 * 1024 * 1024;

    public String uploadWithWatermark(MultipartFile file) throws IOException {
        validateFile(file);
        BufferedImage original = ImageIO.read(file.getInputStream());
        if (original == null) throw new RuntimeException("Imagem inválida ou corrompida");

        BufferedImage watermarked = applyLogoWatermark(original);
        return saveImage(watermarked);
    }

    public String uploadProfilePhoto(MultipartFile file) throws IOException {
        validateFile(file);
        BufferedImage original = ImageIO.read(file.getInputStream());
        if (original == null) throw new RuntimeException("Imagem inválida ou corrompida");

        BufferedImage resized = resizeIfNeeded(original, 400, 400);
        return saveImage(resized);
    }

    private BufferedImage applyLogoWatermark(BufferedImage original) {
        int w = original.getWidth();
        int h = original.getHeight();

        BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = output.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g2d.drawImage(original, 0, 0, null);

        BufferedImage logo = loadLogo();
        if (logo != null) {
            int logoW = Math.max(80, w / 6);
            int logoH = (int) ((double) logo.getHeight() / logo.getWidth() * logoW);

            int x = w - logoW - 16;
            int y = h - logoH - 16;

            BufferedImage logoTransp = makeBlackTransparent(logo, logoW, logoH);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f));
            g2d.drawImage(logoTransp, x, y, null);
        } else {
            applyTextWatermark(g2d, w, h);
        }

        g2d.dispose();
        return output;
    }

    private BufferedImage loadLogo() {
        try (InputStream is = getClass().getResourceAsStream("/static/logo.png")) {
            if (is == null) {
                log.warn("Logo não encontrado em /static/logo.png");
                return null;
            }
            return ImageIO.read(is);
        } catch (Exception e) {
            log.warn("Erro ao carregar logo: {}", e.getMessage());
            return null;
        }
    }

    private BufferedImage makeBlackTransparent(BufferedImage src, int targetW, int targetH) {
        BufferedImage scaled = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(src, 0, 0, targetW, targetH, null);
        g.dispose();

        for (int y = 0; y < targetH; y++) {
            for (int x = 0; x < targetW; x++) {
                int pixel = scaled.getRGB(x, y);
                int r = (pixel >> 16) & 0xFF;
                int g2 = (pixel >> 8) & 0xFF;
                int b = pixel & 0xFF;
                int brightness = (r + g2 + b) / 3;
                if (brightness < 60) {
                    scaled.setRGB(x, y, 0x00000000);
                }
            }
        }
        return scaled;
    }

    private void applyTextWatermark(Graphics2D g2d, int w, int h) {
        int fontSize = Math.min(36, Math.max(14, w / 30));
        g2d.setFont(new Font("SansSerif", Font.BOLD, fontSize));
        String text = "Moreira & Oliveira";
        FontMetrics fm = g2d.getFontMetrics();
        int tw = fm.stringWidth(text);
        int th = fm.getHeight();
        int pad = fontSize / 2 + 4;
        int x = w - tw - pad;
        int y = h - pad;

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
        g2d.setColor(Color.BLACK);
        g2d.fillRoundRect(x - 8, y - th + 4, tw + 16, th + 4, 8, 8);

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.80f));
        g2d.setColor(Color.WHITE);
        g2d.drawString(text, x, y);
    }

    private BufferedImage resizeIfNeeded(BufferedImage img, int maxW, int maxH) {
        if (img.getWidth() <= maxW && img.getHeight() <= maxH) return img;
        double ratio = Math.min((double) maxW / img.getWidth(), (double) maxH / img.getHeight());
        int nw = (int) (img.getWidth() * ratio);
        int nh = (int) (img.getHeight() * ratio);

        BufferedImage out = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(img, 0, 0, nw, nh, null);
        g.dispose();
        return out;
    }

    private String saveImage(BufferedImage img) throws IOException {
        String filename = UUID.randomUUID() + ".jpg";

        Path uploadPath = Paths.get(uploadDir);
        Files.createDirectories(uploadPath);

        File outputFile = uploadPath.resolve(filename).toFile();

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        ImageWriter writer = writers.next();

        ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile);
        writer.setOutput(ios);

        ImageWriteParam param = writer.getDefaultWriteParam();

        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(1.0f);
        }

        writer.write(null, new javax.imageio.IIOImage(img, null, null), param);

        ios.close();
        writer.dispose();

        log.info("Imagem salva: {}/{}", uploadDir, filename);
        return baseUrl + "/" + filename;
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