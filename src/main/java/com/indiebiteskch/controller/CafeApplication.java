package com.indiebiteskch.controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.indiebiteskch")
@EnableJpaRepositories(basePackages = "com.indiebiteskch.repository")
@EntityScan(basePackages = "com.indiebiteskch.entity")
public class CafeApplication {

	public static void main(String[] args) {
		SpringApplication.run(CafeApplication.class, args);
	}

}
