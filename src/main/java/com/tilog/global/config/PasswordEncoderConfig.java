package com.tilog.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * PasswordEncoder Bean 설정
 *
 * SecurityConfig와 분리한 이유:
 * - 향후 SecurityConfig에서 PasswordEncoder를 사용하는 컴포넌트(예: UserDetailsService 등)를
 *   주입받을 가능성이 생기면 순환 참조가 발생하기 쉬움.
 * - 별도 @Configuration 으로 빼면 안전.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
