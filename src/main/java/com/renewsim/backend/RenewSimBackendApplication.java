package com.renewsim.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

import org.springframework.boot.CommandLineRunner;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;


@SpringBootApplication
@EnableFeignClients(basePackages = "com.renewsim.backend")
@EnableMethodSecurity
@EnableAspectJAutoProxy
@EnableCaching 
@ConfigurationPropertiesScan
public class RenewSimBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(RenewSimBackendApplication.class, args);
    }

    @Bean
    @Profile("!test") 
    CommandLineRunner checkDatabase(DataSource dataSource) {
        return args -> {
            try (Connection connection = dataSource.getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();
                System.out.println("✅ Conectado a la base de datos: " + metaData.getURL());
            }
        };
    }

}