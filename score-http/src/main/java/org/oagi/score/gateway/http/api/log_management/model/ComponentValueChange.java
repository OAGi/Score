package org.oagi.score.gateway.http.api.log_management.model;

import java.util.List;

/**
 * A changed value (matched across revisions by GUID) of a code list or an agency ID list in a
 * {@code REVISED}-type {@link ComponentChangeSummary} (issue #1533), with its field-level changes.
 */
public record ComponentValueChange(String name, List<ComponentFieldChange> changes) {
}
