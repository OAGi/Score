package org.oagi.score.gateway.http.api.log_management.model;

/**
 * One field-level change in a {@code REVISED}-type {@link ComponentChangeSummary} (issue #1533),
 * e.g. {@code ("Cardinality", "0..1", "1..1")}. {@code before}/{@code after} are {@code null} when
 * the field was unset on that side; renderers show {@code (none)}.
 */
public record ComponentFieldChange(String label, String before, String after) {
}
