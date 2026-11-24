package org.oagi.score.gateway.http.api.info_management.model;

import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

/** How many User Extension Groups a user has in a state in a release: what the list of that release shows. */
public record SummaryCcExtCount(
        ReleaseId releaseId,
        String releaseNum,
        String ownerUsername,
        CcState state,
        int count) {
}
