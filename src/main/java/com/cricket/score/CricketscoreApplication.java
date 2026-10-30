package com.cricket.score;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CricketscoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(CricketscoreApplication.class, args);
    }
}
