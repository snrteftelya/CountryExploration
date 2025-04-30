package org.example.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Country Search",
                description = "You can find information about countries"
                        + " and nations and cities,"
                        + " that are connected with this countries",
                version = "1.0.0"
        )
)
public class SwaggerConfig {
}
