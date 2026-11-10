package org.oagi.score.gateway.http.common.async;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AsyncJobConfiguration {

    /** Gives back what fails, unless the application has a policy of its own. */
    @Bean
    @ConditionalOnMissingBean(RetryPolicy.class)
    public RetryPolicy retryPolicy() {
        return new GiveBackRetryPolicy();
    }
}
