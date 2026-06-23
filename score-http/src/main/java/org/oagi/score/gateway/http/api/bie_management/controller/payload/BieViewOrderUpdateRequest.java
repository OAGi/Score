package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import org.oagi.score.gateway.http.api.bie_management.model.BieViewOrderEntry;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Request body for {@code PUT /bie-view-order/acc/{accManifestId}}.
 *
 * <p>The {@code accManifestId} path variable supplies the VIEW parent; the body carries the
 * affected child entries. A {@code null} {@code weight} means "reset this child to its seq_key
 * position" (the row is deleted). Each entry must carry exactly one of {@code asccManifestId} /
 * {@code bccManifestId}.
 */
public record BieViewOrderUpdateRequest(List<Entry> entries) {

    public record Entry(BigInteger asccManifestId, BigInteger bccManifestId, Integer weight) {
    }

    public List<BieViewOrderEntry> toEntries(AccManifestId fromAccManifestId) {
        if (entries == null) {
            return Collections.emptyList();
        }
        return entries.stream()
                .map(e -> new BieViewOrderEntry(
                        fromAccManifestId,
                        (e.asccManifestId() != null) ? new AsccManifestId(e.asccManifestId()) : null,
                        (e.bccManifestId() != null) ? new BccManifestId(e.bccManifestId()) : null,
                        e.weight()))
                .collect(Collectors.toList());
    }
}
