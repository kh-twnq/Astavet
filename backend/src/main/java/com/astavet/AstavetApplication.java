package com.astavet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AstavetApplication {

    public static void main(String[] args) {
        SpringApplication.run(AstavetApplication.class, args);
    }
}

