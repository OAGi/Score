package org.oagi.score.gateway.http.api.oas_management.model;

/**
 * Issue #1730: REST request payload to add an API operation (endpoint) that does NOT reference a BIE.
 *
 * Unlike {@link AssignBieForOasDoc}, this carries no {@code topLevelAsbiepId}. The operation is
 * persisted with an empty (BIE-less) message body and an {@code oas_response} carrying an HTTP
 * status code (e.g. 202 Accepted, 204 No Content) without any content schema.
 */
public class AddOperationForOasDoc {

    private OasDocId oasDocId;
    private String verb;
    private String resourceName;
    private String operationId;
    private String tagName;
    private String messageBody;
    private Integer httpStatusCode;
    private String summary;
    private String description;

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

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
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

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
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

    @Override
    public String toString() {
        return "AddOperationForOasDoc{" +
                "oasDocId=" + oasDocId +
                ", verb='" + verb + '\'' +
                ", resourceName='" + resourceName + '\'' +
                ", operationId='" + operationId + '\'' +
                ", tagName='" + tagName + '\'' +
                ", messageBody='" + messageBody + '\'' +
                ", httpStatusCode=" + httpStatusCode +
                ", summary='" + summary + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
