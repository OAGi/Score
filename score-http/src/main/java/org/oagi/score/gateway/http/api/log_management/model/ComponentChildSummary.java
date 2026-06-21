package org.oagi.score.gateway.http.api.log_management.model;

import java.util.List;

/**
 * A child element of a component in a {@link ComponentChangeSummary} (issue #1533): an ACC
 * association ({@code kind} = "ASCC"/"BCC"), a DT supplementary component or value domain, or a
 * code/agency list value. Used both for the {@code NEW} state listing and for the added/removed
 * buckets of a {@code REVISED} summary.
 */
public record ComponentChildSummary(String kind, String name, List<ComponentSummaryField> fields) {
}
