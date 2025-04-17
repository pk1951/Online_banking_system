package com.truebank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OnlineBankingSystemApplication {

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "prod");
        SpringApplication.run(OnlineBankingSystemApplication.class, args);
    }
}
