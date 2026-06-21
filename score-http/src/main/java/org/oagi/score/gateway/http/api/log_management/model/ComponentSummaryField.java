package org.oagi.score.gateway.http.api.log_management.model;

/**
 * One labeled value in a {@code NEW}-type {@link ComponentChangeSummary} (issue #1533),
 * e.g. {@code ("Object Class Term", "Purchase Order")}. Labels follow the editor UI field names.
 */
public record ComponentSummaryField(String label, String value) {
}
