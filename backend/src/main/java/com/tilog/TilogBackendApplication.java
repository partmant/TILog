package com.tilog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TilogBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(TilogBackendApplication.class, args);
	}
}