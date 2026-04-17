package com.example.backend_side

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.security.SecurityRequirement
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Baby Growth Tracking API")
                    .version("1.0.0")
                    .description("""
                        ## API for Managing Baby Growth, Vaccinations, and Health Records
                        
                        ### Features:
                        - **User Management**: Parent, admin, health worker accounts
                        - **Social Authentication**: Google, Facebook Sign-In
                        - **Baby Profiles**: Complete baby information
                        - **Growth Tracking**: Weight, height monitoring
                        - **Vaccinations**: Schedule and track records
                        - **Memories**: Capture milestones with photos
                        - **Appointments**: Medical appointment management
                        
                        ### Authentication Endpoints:
                        - `POST /api/auth/google` - Google Sign-In
                        - `POST /api/auth/facebook` - Facebook Login
                        - `POST /api/auth/login` - Email/Password Login
                        
                        ### Authentication
                        Include JWT token in Authorization header:
```
                        Authorization: Bearer <your-jwt-token>
```
                    """.trimIndent())
                    .contact(
                        Contact()
                            .name("Baby Growth Support")
                            .email("support@babygrowth.com")
                    )
            )
            .servers(
                listOf(
                    Server().url("http://localhost:8080/api").description("Local Development"),
                    Server().url("http://10.0.2.2:8080/api").description("Android Emulator"),
                    Server().url("http://172.20.10.7:8080/api").description("Physical Device"),
                    Server().url("https://api.babygrowth.com").description("Production")
                )
            )
            .components(
                Components()
                    .addSecuritySchemes("bearerAuth",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                    )
            )
            .addSecurityItem(SecurityRequirement().addList("bearerAuth"))
    }
}