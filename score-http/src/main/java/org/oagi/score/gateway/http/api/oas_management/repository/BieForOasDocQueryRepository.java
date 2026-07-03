package org.oagi.score.gateway.http.api.oas_management.repository;

import org.oagi.score.gateway.http.api.oas_management.controller.payload.GetAssignedOasTagRequest;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.GetAssignedOasTagResponse;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.GetBieForOasDocRequest;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.GetBieForOasDocResponse;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocId;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocReleaseSummary;
import org.oagi.score.gateway.http.api.oas_management.model.OperationErrorResponseSummary;
import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;

import java.util.List;

public interface BieForOasDocQueryRepository {

    GetBieForOasDocResponse getBieForOasDoc(GetBieForOasDocRequest request) throws ScoreDataAccessException;

    GetAssignedOasTagResponse getAssignedOasTag(GetAssignedOasTagRequest request) throws ScoreDataAccessException;

    // Issue #1347: lightweight per-body rows (operation id, release, error-response body type + confirm
    // BIE) for a whole document — used to compute bulk-apply targeting and new-operation inheritance
    // without the heavyweight per-row security / ConfirmMessage-DEN subqueries of getBieForOasDoc.
    List<OperationErrorResponseSummary> getOperationErrorResponseSummaries(OasDocId oasDocId)
            throws ScoreDataAccessException;

    // The distinct releases among the document's BIE-backed bodies (Request + Response), derived in a
    // single SELECT DISTINCT query. Feeds the Error Response "apply to all" ConfirmMessage Branch selector
    // so the whole paginated BIE list is no longer fetched only to combine releases client-side. Bodyless
    // operations (no BIE, no release) are excluded.
    List<OasDocReleaseSummary> getDistinctReleases(OasDocId oasDocId)
            throws ScoreDataAccessException;

}
