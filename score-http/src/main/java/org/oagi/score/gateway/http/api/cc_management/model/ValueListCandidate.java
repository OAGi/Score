package org.oagi.score.gateway.http.api.cc_management.model;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.common.model.Guid;

/**
 * What it takes to decide whether a Code List or an Agency ID List can be offered for a node, and which list of
 * another release it matches: no values, no definition. One of these is read for each list of a release with one query.
 *
 * @param manifestId           the manifest of the list in its release
 * @param agencyIdListValueName the name of the agency identification of an Agency ID List (null for a Code List), which
 *                             takes part in matching it by identifiers
 * @param basedManifestId      the manifest of the list this one is derived from
 * @param endUserOwned         whether the owner of the list is an end user
 */
public record ValueListCandidate<I extends ManifestId>(
        I manifestId,
        Guid guid,
        String name, String listId, String versionId,
        @Nullable String agencyIdListValueName,
        CcState state,
        @Nullable I basedManifestId,
        boolean endUserOwned) {
}
