package com.alquilereventos.seguridad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.alquilereventos.common.entity")
@EnableJpaRepositories("com.alquilereventos.common.repository")
public class SeguridadApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeguridadApplication.class, args);
    }
}
