package org.oagi.score.gateway.http.api.bie_management.model;

import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;

/**
 * A single sibling view-order weight for Issue #1638.
 *
 * <p>The order is keyed by the VIEW parent ({@code fromAccManifestId} — the expanded ACC node
 * under which the child appears as a flattened sibling) and the child association
 * ({@code asccManifestId} XOR {@code bccManifestId}). Exactly one of the two child ids is non-null.
 *
 * <p>The id fields serialize as plain JSON scalars (via {@code Id#value()} {@code @JsonValue}),
 * so the frontend receives e.g. {@code {"fromAccManifestId":1,"asccManifestId":2,"bccManifestId":null,"weight":100}}.
 */
public record BieViewOrderEntry(AccManifestId fromAccManifestId,
                                AsccManifestId asccManifestId,
                                BccManifestId bccManifestId,
                                Integer weight) {
}
