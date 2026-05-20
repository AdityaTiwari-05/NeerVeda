package com.neerveda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 💧 NeerVeda - Smart Water Safety & Disease Prevention System
 *
 * This is the main entry point of the Spring Boot application.
 * When you run this file, the entire backend server starts.
 *
 * @author Team CORE_401
 */
@SpringBootApplication
public class NeerVedaApplication {

    public static void main(String[] args) {
        SpringApplication.run(NeerVedaApplication.class, args);
        System.out.println("====================================");
        System.out.println("  💧 NeerVeda Backend is RUNNING!  ");
        System.out.println("  http://localhost:8080            ");
        System.out.println("====================================");
    }
}
