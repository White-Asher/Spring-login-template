package com.springboot.template.config.properties;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class OpenApiConfig {

    public final static String securitySchemeName = "bearerAuth";
    @Bean
    public OpenAPI openAPI() {


        Info info = new Info().title("TemaplteName API").version("Ver.1.1.0")
                .description("TemaplteName API문서 입니다.")
//                .termsOfService("http://swagger.io/terms/")\
                .contact(new Contact().name("TemaplteName").url("http://localhost:8080").email("TemplateEmail@Email.com"))
                .license(new License().name("MIT Licence").url("https://opensource.org/licenses/MIT"));

        return new OpenAPI()
                .info(info)
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));

    }

}