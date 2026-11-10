package org.oagi.score.gateway.http.configuration.initializer;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Runs what is scheduled: the recovery of the releases that are in the middle of a transition for too long. */
@Configuration
@EnableScheduling
public class ReleaseProgressConfiguration {
}
