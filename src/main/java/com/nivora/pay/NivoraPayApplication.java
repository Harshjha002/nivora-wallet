package com.nivora.pay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class NivoraPayApplication {

    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.configure()
                .directory("./")   // 🔥 force root directory
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(e ->
                System.setProperty(e.getKey(), e.getValue())
        );

        // ✅ DEBUG (very important)
        System.out.println("DB_PORT = " + System.getProperty("DB_PORT"));
        System.out.println("DB_HOST = " + System.getProperty("DB_HOST"));

        SpringApplication.run(NivoraPayApplication.class, args);

        System.out.println("I Am running");
    }
}