package org.oagi.score.gateway.http.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Score HTTP Gateway",
                version = "3.4",
                description = "Score HTTP Gateway",
                contact = @Contact(name = "Support", email = "member.services@oagi.org"),
                license = @License(name = "MIT License", url = "https://github.com/OAGi/Score/blob/master/LICENSE.txt")
        )
)
public class SwaggerConfig {

}
