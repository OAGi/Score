package org.oagi.score.gateway.http.change;

/** Published after every read of the change log: it was read to its end ({@code succeeded}), or it could not be. */
public record ChangeLogReadEvent(boolean succeeded) {
}
