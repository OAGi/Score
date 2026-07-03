package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.api.oas_management.model.OasDocId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.base.Request;

import java.math.BigInteger;

/**
 * Issue #1347: internal service request for the document-level "apply Error Response Body Type to all
 * operations" action (see {@link BulkErrorResponseRequest} for the wire shape).
 */
public class BulkUpdateErrorResponseRequest extends Request {

    private OasDocId oasDocId;
    private String errorResponseBodyType;
    private BigInteger confirmTopLevelAsbiepId;
    private BigInteger releaseId;

    public BulkUpdateErrorResponseRequest(ScoreUser requester) {
        super(requester);
    }

    public OasDocId getOasDocId() {
        return oasDocId;
    }

    public void setOasDocId(OasDocId oasDocId) {
        this.oasDocId = oasDocId;
    }

    public String getErrorResponseBodyType() {
        return errorResponseBodyType;
    }

    public void setErrorResponseBodyType(String errorResponseBodyType) {
        this.errorResponseBodyType = errorResponseBodyType;
    }

    public BigInteger getConfirmTopLevelAsbiepId() {
        return confirmTopLevelAsbiepId;
    }

    public void setConfirmTopLevelAsbiepId(BigInteger confirmTopLevelAsbiepId) {
        this.confirmTopLevelAsbiepId = confirmTopLevelAsbiepId;
    }

    public BigInteger getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(BigInteger releaseId) {
        this.releaseId = releaseId;
    }
}
