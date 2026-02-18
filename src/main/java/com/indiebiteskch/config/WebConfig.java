package com.indiebiteskch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        /* MVC config: frontend at Vercel, with backend in Render; allow for platform integration
           without it may trigger CORS (Cross-Origin Resource Sharing) that's a 
           browser-based security mechanism that restricts web pages from making requests 
           to different domain than the one that served the page */
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "https://indiebitescafe.vercel.app") 
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
