package org.oagi.score.gateway.http.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/** The Jackson settings of application.yml, applied to the Boot-configured mapper the REST layer uses. */
class JacksonConfigurationTest {

    record Flags(boolean admin, int count) {
    }

    @Test
    void anExplicitNullForAPrimitiveRequestFieldReadsAsTheDefault() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
                .withInitializer(ctx -> {
                    try {
                        new YamlPropertySourceLoader().load("application", new ClassPathResource("application.yml"))
                                .forEach(ps -> ctx.getEnvironment().getPropertySources().addLast(ps));
                    } catch (java.io.IOException e) {
                        throw new IllegalStateException(e);
                    }
                })
                .run(context -> {
                    JsonMapper mapper = context.getBean(JsonMapper.class);
                    assertThat(mapper.readValue("{\"admin\":null,\"count\":null}", Flags.class))
                            .isEqualTo(new Flags(false, 0));
                });
    }
}
