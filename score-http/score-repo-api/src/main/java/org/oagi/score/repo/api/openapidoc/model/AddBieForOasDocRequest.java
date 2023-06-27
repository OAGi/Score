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
    private String req_description;
    private boolean requiredForRequestBody;
    private boolean req_makeArrayIndicator;
    private boolean req_suppressRootIndicator;
    private boolean req_includeMetaHeaderIndicator;
    private boolean req_includePaginationIndicator;
    private String res_description;
    private boolean res_makeArrayIndicator;
    private boolean res_suppressRootIndicator;
    private boolean res_includeMetaHeaderIndicator;
    private boolean res_includePaginationIndicator;

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

    public String getReq_description() {
        return req_description;
    }

    public void setReq_description(String req_description) {
        this.req_description = req_description;
    }

    public boolean isRequiredForRequestBody() {
        return requiredForRequestBody;
    }

    public void setRequiredForRequestBody(boolean requiredForRequestBody) {
        this.requiredForRequestBody = requiredForRequestBody;
    }

    public boolean isReq_makeArrayIndicator() {
        return req_makeArrayIndicator;
    }

    public void setReq_makeArrayIndicator(boolean req_makeArrayIndicator) {
        this.req_makeArrayIndicator = req_makeArrayIndicator;
    }

    public boolean isReq_suppressRootIndicator() {
        return req_suppressRootIndicator;
    }

    public void setReq_suppressRootIndicator(boolean req_suppressRootIndicator) {
        this.req_suppressRootIndicator = req_suppressRootIndicator;
    }

    public boolean isReq_includeMetaHeaderIndicator() {
        return req_includeMetaHeaderIndicator;
    }

    public void setReq_includeMetaHeaderIndicator(boolean req_includeMetaHeaderIndicator) {
        this.req_includeMetaHeaderIndicator = req_includeMetaHeaderIndicator;
    }

    public boolean isReq_includePaginationIndicator() {
        return req_includePaginationIndicator;
    }

    public void setReq_includePaginationIndicator(boolean req_includePaginationIndicator) {
        this.req_includePaginationIndicator = req_includePaginationIndicator;
    }

    public String getRes_description() {
        return res_description;
    }

    public void setRes_description(String res_description) {
        this.res_description = res_description;
    }

    public boolean isRes_makeArrayIndicator() {
        return res_makeArrayIndicator;
    }

    public void setRes_makeArrayIndicator(boolean res_makeArrayIndicator) {
        this.res_makeArrayIndicator = res_makeArrayIndicator;
    }

    public boolean isRes_suppressRootIndicator() {
        return res_suppressRootIndicator;
    }

    public void setRes_suppressRootIndicator(boolean res_suppressRootIndicator) {
        this.res_suppressRootIndicator = res_suppressRootIndicator;
    }

    public boolean isRes_includeMetaHeaderIndicator() {
        return res_includeMetaHeaderIndicator;
    }

    public void setRes_includeMetaHeaderIndicator(boolean res_includeMetaHeaderIndicator) {
        this.res_includeMetaHeaderIndicator = res_includeMetaHeaderIndicator;
    }

    public boolean isRes_includePaginationIndicator() {
        return res_includePaginationIndicator;
    }

    public void setRes_includePaginationIndicator(boolean res_includePaginationIndicator) {
        this.res_includePaginationIndicator = res_includePaginationIndicator;
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
