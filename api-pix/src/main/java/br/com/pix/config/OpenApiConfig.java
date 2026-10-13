package br.com.pix.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI pixOpenApi() {
        return new OpenAPI().info(new Info()
                .title("API de Transferências PIX")
                .version("v1")
                .description("API para abertura de contas e transferências PIX entre contas ativas."));
    }
}
