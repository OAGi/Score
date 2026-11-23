package org.oagi.score.gateway.http.api.log_management.model;

import java.util.List;

/**
 * A value of a code list or an agency ID list in a {@link ComponentChangeSummary} (issue #1533).
 * Unlike a {@link ComponentChildItem}, a value is not a component of its own; it is an attribute
 * of the list that holds it. Used both for the {@code NEW} state listing and for the added/removed
 * buckets of a {@code REVISED} summary.
 */
public record ComponentValueItem(String name, List<ComponentSummaryField> fields) {
}
