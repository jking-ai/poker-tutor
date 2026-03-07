package com.jkingai.pokertutor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PokerTutorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PokerTutorApplication.class, args);
    }
}
