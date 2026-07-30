package com.tilog;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class getTestPassword {

    @Test
    void getTestPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("1234 암호화 값: " + encoder.encode("1234"));
    }
}