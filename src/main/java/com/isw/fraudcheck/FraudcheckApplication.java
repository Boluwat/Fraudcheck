package com.isw.fraudcheck;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;

import javax.sql.DataSource;
import java.sql.Connection;


@SpringBootApplication
public class FraudcheckApplication {

    public static void main(String[] args) {
        SpringApplication.run(FraudcheckApplication.class, args);
    }


    @Bean
    CommandLineRunner testFlyway(Flyway flyway) {
        return args -> {
            System.out.println("========================================");
            System.out.println("FLYWAY STATUS");

            MigrationInfo[] migrations = flyway.info().all();

            for (MigrationInfo migration : migrations) {
                System.out.println(
                        "Version: " + migration.getVersion() +
                                " | Description: " + migration.getDescription() +
                                " | State: " + migration.getState()
                );
            }

            System.out.println("========================================");
        };
    }


    @Bean
    CommandLineRunner testConnection(DataSource dataSource) {
        return args -> {
            try (Connection conn = dataSource.getConnection()) {
                System.out.println("========================================");
                System.out.println("DB CONNECTED SUCCESSFULLY");
                System.out.println("Database: " + conn.getCatalog());
                System.out.println("========================================");
                System.out.println("URL:      " + conn.getMetaData().getURL());
                System.out.println("========================================");
            } catch (Exception e) {
                System.err.println("========================================");
                System.err.println("DB CONNECTION FAILED: " + e.getMessage());
            }
        };
    }


}
