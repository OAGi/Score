package org.oagi.score.gateway.http.security.secret;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationSecretConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfiguration.class);

    @Test
    void startsWithoutAKeyButRejectsMalformedConfiguredKeys() {
        contextRunner.run(context -> assertThat(context).hasNotFailed());

        contextRunner.withPropertyValues(
                        "score.security.secret-encryption.active-key-id=primary",
                        "score.security.secret-encryption.keys.primary=not-base64")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseMessage("Application secret encryption keys must be Base64 encoded.");
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(SecretEncryptionProperties.class)
    static class TestConfiguration {
        @Bean
        ApplicationSecretCrypto applicationSecretCrypto(SecretEncryptionProperties properties) {
            return new ApplicationSecretCrypto(properties);
        }
    }
}
