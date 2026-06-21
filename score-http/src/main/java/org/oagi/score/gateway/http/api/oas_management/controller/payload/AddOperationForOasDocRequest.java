package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.api.oas_management.model.OasDocId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.base.Request;

/**
 * Issue #1730: Service-layer request to add a BIE-less API operation to an OpenAPI document.
 */
public class AddOperationForOasDocRequest extends Request {

    private boolean oasRequest;
    private OasDocId oasDocId;
    private String verb;
    private String path;
    private String ref;
    private String operationId;
    private String tagName;
    private Integer httpStatusCode;
    private String summary;
    private String description;

    public AddOperationForOasDocRequest() {
    }

    public AddOperationForOasDocRequest(ScoreUser requester) {
        super(requester);
    }

    public boolean isOasRequest() {
        return oasRequest;
    }

    public void setOasRequest(boolean oasRequest) {
        this.oasRequest = oasRequest;
    }

    public OasDocId getOasDocId() {
        return oasDocId;
    }

    public void setOasDocId(OasDocId oasDocId) {
        this.oasDocId = oasDocId;
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(Integer httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
