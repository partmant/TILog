package com.tilog.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

// FE-BE CORS 및 정적 리소스 설정

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${file.upload-path}")
    private String uploadPath;

    // 업로드 이미지 정적 리소스 접근 경로 설정
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/post/**")
                .addResourceLocations(Paths.get(uploadPath).toUri().toString());

        registry.addResourceHandler("/uploads/profile/**")
                .addResourceLocations(Paths.get(uploadPath, "profile").toUri().toString());
    }
}
