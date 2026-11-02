package org.oagi.score.gateway.http.change.propagation;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** The engine runs every {@link PropagationHandler} that is a bean, whichever part of the application it belongs to. */
@Configuration
public class PropagationConfiguration {

    @Bean
    public PropagationEngine propagationEngine(ObjectProvider<PropagationHandler> handlers) {
        return new PropagationEngine(handlers.orderedStream().toList());
    }
}
