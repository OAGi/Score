package org.oagi.score.gateway.http.change.propagation;

import org.oagi.score.gateway.http.change.ChangeEvent;

/** Because of {@code cause}, the record of {@code event} is affected. */
public record Effect(ChangeEvent cause, ChangeEvent event) {
}
