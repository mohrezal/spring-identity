package com.github.mohrezal.identity.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        var content =
                new Contact()
                        .name("Mohammadreza Alizadeh")
                        .url("https://github.com/mohrezal/spring-identity");
        var info = new Info().title("Spring Identity").version("1.0.0").contact(content);

        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("CSRF"))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "CSRF",
                                        new SecurityScheme()
                                                .name("X-XSRF-TOKEN")
                                                .type(SecurityScheme.Type.APIKEY)
                                                .in(SecurityScheme.In.HEADER)))
                .info(info);
    }

    @Bean
    public OpenApiCustomizer acceptLanguageOpenApiCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) return;

            var param =
                    new Parameter()
                            .in("header")
                            .name(HttpHeaders.ACCEPT_LANGUAGE)
                            .required(false)
                            .schema(new StringSchema()._default("en").addEnumItem("en"));

            openApi.getPaths().values().stream()
                    .flatMap(pathItem -> pathItem.readOperations().stream())
                    .forEach(op -> op.addParametersItem(param));
        };
    }
}
