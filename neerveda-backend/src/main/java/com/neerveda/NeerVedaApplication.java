package com.neerveda;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 🚀 NeerVedaApplication — Main entry point.
 *
 *  💧 NeerVeda: Smart Water Safety & Disease Prevention System
 *  Team: CORE_401 | Problem ID: SIH25001
 *  Smart India Hackathon 2025
 *
 *  Neer = Water | Veda = Knowledge/Wisdom
 */
@Slf4j
@SpringBootApplication
public class NeerVedaApplication {

    public static void main(String[] args) {
        SpringApplication.run(NeerVedaApplication.class, args);

        log.info("  ______  __                                           ______                          ");
        log.info(" /      \\|  \\                                         /      \\                         ");
        log.info("|  $$$$$$| $$  ______ ______   ______ _____          |  $$$$$$\\  ______  ______        ");
        log.info("| $$  | $| $$ /      \\      \\ /      \\     \\         | $$ __\\$$ /      \\/      \\       ");
        log.info("| $$  | $| $$|  $$$$$$\\$$$$$$|  $$$$$$\\$$$$$        | $$|    \\|  $$$$$$\\  $$$$$$\\      ");
        log.info("| $$  | $| $$| $$    $$      | $$  | $$ | $$        | $$ \\$$$$| $$    $$ $$    $$      ");
        log.info("| $$__/ $| $$| $$$$$$$$$$$$$$| $$__/ $$ | $$        | $$__| $$| $$$$$$$$ $$$$$$$$      ");
        log.info(" \\$$    $| $$ \\$$     \\      | $$    $$ | $$         \\$$    $$ \\$$     \\\\$$     \\      ");
        log.info("  \\$$$$$$ \\$$  \\$$$$$$$\\$$$$$$ \\$$$$$$$  \\$$          \\$$$$$$   \\$$$$$$$ \\$$$$$$$      ");
        log.info("");
        log.info("  💧 NeerVeda Backend Started Successfully!");
        log.info("  📖 API Docs: http://localhost:8080/swagger-ui");
        log.info("  🔍 Health:   http://localhost:8080/actuator/health");
        log.info("  🏆 Team CORE_401 | Problem ID: SIH25001");
    }
}
