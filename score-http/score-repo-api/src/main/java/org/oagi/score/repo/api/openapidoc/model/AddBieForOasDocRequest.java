package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collection;

public class AddBieForOasDocRequest extends Request {
    private boolean isOasRequest;
    private BigInteger topLevelAsbiepId;
    private BigInteger oasDocId;
    private String operationId;
    private String path;
    private String ref;
    private String verb;
    private String summary;
    private String descriptionForOperation;
    private boolean deprecatedForOperation;
    private String description;
    private boolean requiredForRequestBody;
    private boolean makeArrayIndicator;
    private boolean suppressRootIndicator;
    private boolean includeMetaHeaderIndicator;
    private boolean includePaginationIndicator;
    private Collection<String> updaterUsernameList;
    private LocalDateTime updateStartDate;
    private LocalDateTime updateEndDate;

    public AddBieForOasDocRequest() {
    }

    public AddBieForOasDocRequest(ScoreUser requester) {
        super(requester);
    }

    public boolean isOasRequest() {
        return isOasRequest;
    }

    public void setOasRequest(boolean oasRequest) {
        isOasRequest = oasRequest;
    }
    public BigInteger getTopLevelAsbiepId() {
        return topLevelAsbiepId;
    }

    public void setTopLevelAsbiepId(BigInteger topLevelAsbiepId) {
        this.topLevelAsbiepId = topLevelAsbiepId;
    }

    public BigInteger getOasDocId() {
        return oasDocId;
    }

    public void setOasDocId(BigInteger oasDocId) {
        this.oasDocId = oasDocId;
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

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescriptionForOperation() {
        return descriptionForOperation;
    }

    public void setDescriptionForOperation(String descriptionForOperation) {
        this.descriptionForOperation = descriptionForOperation;
    }

    public boolean isDeprecatedForOperation() {
        return deprecatedForOperation;
    }

    public void setDeprecatedForOperation(boolean deprecatedForOperation) {
        this.deprecatedForOperation = deprecatedForOperation;
    }

    public boolean isRequiredForRequestBody() {
        return requiredForRequestBody;
    }

    public void setRequiredForRequestBody(boolean requiredForRequestBody) {
        this.requiredForRequestBody = requiredForRequestBody;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isMakeArrayIndicator() {
        return makeArrayIndicator;
    }

    public void setMakeArrayIndicator(boolean makeArrayIndicator) {
        this.makeArrayIndicator = makeArrayIndicator;
    }

    public boolean isSuppressRootIndicator() {
        return suppressRootIndicator;
    }

    public void setSuppressRootIndicator(boolean suppressRootIndicator) {
        this.suppressRootIndicator = suppressRootIndicator;
    }

    public boolean isIncludeMetaHeaderIndicator() {
        return includeMetaHeaderIndicator;
    }

    public void setIncludeMetaHeaderIndicator(boolean includeMetaHeaderIndicator) {
        this.includeMetaHeaderIndicator = includeMetaHeaderIndicator;
    }

    public boolean isIncludePaginationIndicator() {
        return includePaginationIndicator;
    }

    public void setIncludePaginationIndicator(boolean includePaginationIndicator) {
        this.includePaginationIndicator = includePaginationIndicator;
    }

    public Collection<String> getUpdaterUsernameList() {
        return updaterUsernameList;
    }

    public void setUpdaterUsernameList(Collection<String> updaterUsernameList) {
        this.updaterUsernameList = updaterUsernameList;
    }

    public LocalDateTime getUpdateStartDate() {
        return updateStartDate;
    }

    public void setUpdateStartDate(LocalDateTime updateStartDate) {
        this.updateStartDate = updateStartDate;
    }

    public LocalDateTime getUpdateEndDate() {
        return updateEndDate;
    }

    public void setUpdateEndDate(LocalDateTime updateEndDate) {
        this.updateEndDate = updateEndDate;
    }


}
