package com.github.mohrezal.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class SpringIdentityApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringIdentityApplication.class, args);
    }

}
