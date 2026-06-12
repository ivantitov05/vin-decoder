package com.vin.decoder.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("VIN Decoder API")
                        .version("1.0")
                        .description("API для расшифровки VIN-кодов автомобилей\n\n" +
                                "## Аутентификация\n" +
                                "1. Зарегистрируйтесь через `/api/auth/register`\n" +
                                "2. Получите токен через `/api/auth/login`\n" +
                                "3. Нажмите кнопку **Authorize** и введите токен\n" +
                                "4. Используйте защищенные эндпоинты"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Введите JWT токен, полученный при логине")));
    }
}