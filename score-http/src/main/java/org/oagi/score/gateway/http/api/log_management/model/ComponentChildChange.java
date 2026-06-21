package org.oagi.score.gateway.http.api.log_management.model;

import java.util.List;

/**
 * A changed child element (matched across revisions by GUID) in a {@code REVISED}-type
 * {@link ComponentChangeSummary} (issue #1533), with its field-level changes.
 */
public record ComponentChildChange(String kind, String name, List<ComponentFieldChange> changes) {
}
