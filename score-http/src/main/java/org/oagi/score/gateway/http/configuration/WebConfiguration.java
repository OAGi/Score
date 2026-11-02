package org.oagi.score.gateway.http.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Configuration
@EnableWebMvc
public class WebConfiguration implements WebMvcConfigurer {

    private final JsonMapper jsonMapper;

    public WebConfiguration(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    // @Primary so any unqualified RestTemplate injection resolves here; the GitHub integration uses a
    // dedicated, timeout-configured RestTemplate ("gitHubRestTemplate").
    @Bean
    @Primary
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // @EnableWebMvc makes Spring Boot's HTTP message converter customization back off, so the default
    // converter would serialize with a bare JsonMapper instead of the Boot-configured one (spring.jackson.*).
    // Force the REST layer to use the Boot-configured Jackson 3 JsonMapper so the wire contract is unchanged.
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (int i = 0; i < converters.size(); i++) {
            if (converters.get(i) instanceof JacksonJsonHttpMessageConverter) {
                converters.set(i, new JacksonJsonHttpMessageConverter(jsonMapper));
                return;
            }
        }
        converters.add(new JacksonJsonHttpMessageConverter(jsonMapper));
    }

}
