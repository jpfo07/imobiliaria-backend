package com.imobiliaria.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Upload de imagens migrado para Cloudinary.
// Não há mais necessidade de servir arquivos locais.
@Configuration
public class WebConfig implements WebMvcConfigurer {
}
