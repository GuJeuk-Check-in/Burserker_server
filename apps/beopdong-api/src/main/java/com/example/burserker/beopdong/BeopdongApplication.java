package com.example.burserker.beopdong;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BeopdongApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeopdongApplication.class, args);
    }
}
