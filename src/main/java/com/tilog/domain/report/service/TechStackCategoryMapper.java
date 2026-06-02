package com.tilog.domain.report.service;

import java.util.Map;

public class TechStackCategoryMapper {

    private static final Map<String, TechStackCategory> TAG_CATEGORY_MAP = Map.ofEntries(
            // BACKEND
            Map.entry("Spring",       TechStackCategory.BACKEND),
            Map.entry("Spring Boot",  TechStackCategory.BACKEND),
            Map.entry("Spring MVC",   TechStackCategory.BACKEND),
            Map.entry("JPA",          TechStackCategory.BACKEND),
            Map.entry("Hibernate",    TechStackCategory.BACKEND),
            Map.entry("Java",         TechStackCategory.BACKEND),
            Map.entry("Kotlin",       TechStackCategory.BACKEND),
            Map.entry("Python",       TechStackCategory.BACKEND),
            Map.entry("Django",       TechStackCategory.BACKEND),
            Map.entry("FastAPI",      TechStackCategory.BACKEND),
            Map.entry("Flask",        TechStackCategory.BACKEND),
            Map.entry("Node.js",      TechStackCategory.BACKEND),
            Map.entry("Express",      TechStackCategory.BACKEND),
            Map.entry("Go",           TechStackCategory.BACKEND),
            Map.entry("Rust",         TechStackCategory.BACKEND),
            Map.entry("MySQL",        TechStackCategory.BACKEND),
            Map.entry("PostgreSQL",   TechStackCategory.BACKEND),
            Map.entry("MongoDB",      TechStackCategory.BACKEND),
            Map.entry("Redis",        TechStackCategory.BACKEND),
            Map.entry("Docker",       TechStackCategory.BACKEND),
            Map.entry("Kubernetes",   TechStackCategory.BACKEND),
            Map.entry("AWS",          TechStackCategory.BACKEND),
            Map.entry("Gradle",       TechStackCategory.BACKEND),
            Map.entry("Maven",        TechStackCategory.BACKEND),

            // FRONTEND
            Map.entry("React",        TechStackCategory.FRONTEND),
            Map.entry("Next.js",      TechStackCategory.FRONTEND),
            Map.entry("Vue",          TechStackCategory.FRONTEND),
            Map.entry("Nuxt.js",      TechStackCategory.FRONTEND),
            Map.entry("Angular",      TechStackCategory.FRONTEND),
            Map.entry("Svelte",       TechStackCategory.FRONTEND),
            Map.entry("TypeScript",   TechStackCategory.FRONTEND),
            Map.entry("JavaScript",   TechStackCategory.FRONTEND),
            Map.entry("HTML",         TechStackCategory.FRONTEND),
            Map.entry("CSS",          TechStackCategory.FRONTEND),
            Map.entry("Tailwind",     TechStackCategory.FRONTEND),
            Map.entry("Sass",         TechStackCategory.FRONTEND),

            // SECURITY
            Map.entry("JWT",              TechStackCategory.SECURITY),
            Map.entry("OAuth2",           TechStackCategory.SECURITY),
            Map.entry("Spring Security",  TechStackCategory.SECURITY),
            Map.entry("HTTPS",            TechStackCategory.SECURITY),
            Map.entry("TLS",              TechStackCategory.SECURITY),
            Map.entry("암호화",            TechStackCategory.SECURITY),
            Map.entry("인증",              TechStackCategory.SECURITY),
            Map.entry("인가",              TechStackCategory.SECURITY),

            // CS
            Map.entry("알고리즘",          TechStackCategory.CS),
            Map.entry("자료구조",          TechStackCategory.CS),
            Map.entry("운영체제",          TechStackCategory.CS),
            Map.entry("네트워크",          TechStackCategory.CS),
            Map.entry("데이터베이스",       TechStackCategory.CS),
            Map.entry("디자인패턴",        TechStackCategory.CS),
            Map.entry("Algorithm",        TechStackCategory.CS),
            Map.entry("Data Structure",   TechStackCategory.CS),
            Map.entry("OS",               TechStackCategory.CS),
            Map.entry("Network",          TechStackCategory.CS),
            Map.entry("Design Pattern",   TechStackCategory.CS)
    );

    private TechStackCategoryMapper() {}

    public static TechStackCategory categorize(String tagName) {
        return TAG_CATEGORY_MAP.getOrDefault(tagName, TechStackCategory.OTHER);
    }
}