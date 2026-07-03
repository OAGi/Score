package org.oagi.score.gateway.http.api.oas_management.repository;

import org.oagi.score.gateway.http.api.oas_management.controller.payload.*;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocId;
import org.oagi.score.gateway.http.api.oas_management.model.OperationErrorResponseAssignment;
import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;

import java.util.List;

public interface BieForOasDocCommandRepository {

    AddBieForOasDocResponse assignBieForOasDoc(
                                               AddBieForOasDocRequest request) throws ScoreDataAccessException;

    //    todo
    UpdateBieForOasDocResponse updateBieForOasDoc(
                                                  UpdateBieForOasDocRequest request) throws ScoreDataAccessException;

    DeleteBieForOasDocResponse deleteBieForOasDoc(
            DeleteBieForOasDocRequest request) throws ScoreDataAccessException;

    // Issue #1347: document-level bulk apply of the per-operation error-response body type. The service
    // resolves which operations to touch (release-matched + bodyless for CONFIRM_MESSAGE, or all for
    // NONE/PROBLEM_DETAILS); this method just persists each assignment.
    BulkUpdateErrorResponseResponse bulkUpdateErrorResponse(
            OasDocId oasDocId,
            List<OperationErrorResponseAssignment> assignments) throws ScoreDataAccessException;
}
