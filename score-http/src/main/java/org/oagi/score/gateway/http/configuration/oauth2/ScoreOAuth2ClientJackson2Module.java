package org.oagi.score.gateway.http.configuration.oauth2;

import org.springframework.security.oauth2.client.jackson2.OAuth2ClientJackson2Module;

import java.util.Collections;

final class ScoreOAuth2ClientJackson2Module extends OAuth2ClientJackson2Module {

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        context.setMixInAnnotations(Collections.unmodifiableSet(Collections.emptySet()).getClass(), UnmodifiableSetMixin.class);
    }
}
