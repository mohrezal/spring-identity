package com.github.mohrezal.identity.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        var content =
                new Contact()
                        .name("Mohammadreza Alizadeh")
                        .url("https://github.com/mohrezal/spring-identity");
        var info = new Info().title("Spring Identity").version("1.0.0").contact(content);

        return new OpenAPI().info(info);
    }
}
