package com.universe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UniVerseApplication {

    public static void main(String[] args) {
        SpringApplication.run(UniVerseApplication.class, args);
        System.out.println(">>> UniVerse Başarıyla Başlatıldı! http://localhost:8080 adresine git. <<<");
    }
}