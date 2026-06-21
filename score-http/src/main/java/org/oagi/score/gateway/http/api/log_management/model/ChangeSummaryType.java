package org.oagi.score.gateway.http.api.log_management.model;

/**
 * Discriminator for a {@link ComponentChangeSummary} (issue #1533).
 * <p>
 * {@code NEW} — the component is at revision 1 (or has no resolvable prior revision); the summary
 * describes its current state. {@code REVISED} — the component is at revision 2+; the summary
 * describes what changed since the last revision (the state frozen when Revise/Amend was invoked).
 */
public enum ChangeSummaryType {
    NEW,
    REVISED
}
