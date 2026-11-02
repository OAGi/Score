package org.oagi.score.gateway.http.security.secret;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Binds {@code score.security.secret-encryption.*}, the application secret key ring. */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SecretEncryptionProperties.class)
public class ApplicationSecretConfiguration {
}
