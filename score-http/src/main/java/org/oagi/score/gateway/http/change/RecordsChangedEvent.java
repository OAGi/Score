package org.oagi.score.gateway.http.change;

import java.util.List;

/**
 * The rows of the change log that were read, in the order they were written. A listener that throws makes the read fail and be
 * repeated with the same rows, so a listener must accept the same rows twice.
 */
public record RecordsChangedEvent(List<ChangeLogEntry> changes) {
}
