package com.rn00n.inhibitor.commons.swagger

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        val bearerSchemeName = "bearerAuth"
        val basicSchemeName = "basicAuth"

        return OpenAPI()
            .components(
                Components()
                    .addSecuritySchemes(
                        bearerSchemeName,
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                    )
                    .addSecuritySchemes(
                        basicSchemeName,
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("basic")
                    )
            )
            .addSecurityItem(
                SecurityRequirement()
                    .addList(bearerSchemeName)
                    .addList(basicSchemeName)
            )
            .info(
                Info()
                    .title("인증 서버 API 문서")
                    .description("OAuth2 인증 흐름을 위한 내부용 API")
                    .version("v1.0.0")
            )
    }

}