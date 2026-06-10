package org.oagi.score.gateway.http.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfiguration implements WebMvcConfigurer {

    // @Primary so any unqualified RestTemplate injection resolves here; the GitHub integration uses a
    // dedicated, timeout-configured RestTemplate ("gitHubRestTemplate").
    @Bean
    @Primary
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
