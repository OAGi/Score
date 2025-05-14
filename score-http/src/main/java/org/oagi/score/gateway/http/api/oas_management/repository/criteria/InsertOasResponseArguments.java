package org.oagi.score.gateway.http.api.oas_management.repository.criteria;

import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.oas_management.repository.OasDocCommandRepository;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

public class InsertOasResponseArguments {

    private final OasDocCommandRepository repository;

    private ULong userId;
    private ULong oasOperationId;
    private ULong oasMessageBodyId;
    private String description;
    private String httpStatusCode;
    private boolean makeArrayIndicator;
    private boolean suppressRootIndicator;
    private boolean includeMetaHeaderIndicator;
    private boolean includePaginationIndicator;
    private boolean includeConfirmIndicator;
    private LocalDateTime timestamp = new Timestamp(System.currentTimeMillis()).toLocalDateTime();

    public InsertOasResponseArguments(OasDocCommandRepository repository) {
        this.repository = repository;
    }

    public InsertOasResponseArguments setUserId(UserId userId) {
        return setUserId(ULong.valueOf(userId.value()));
    }

    public InsertOasResponseArguments setUserId(ULong userId) {
        this.userId = userId;
        return this;
    }

    public ULong getOasOperationId() {
        return oasOperationId;
    }

    public InsertOasResponseArguments setOasOperationId(BigInteger oasOperationId) {
        return setOasOperationId(ULong.valueOf(oasOperationId));
    }

    public InsertOasResponseArguments setOasOperationId(ULong oasOperationId) {
        this.oasOperationId = oasOperationId;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public InsertOasResponseArguments setDescription(String description) {
        this.description = description;
        return this;
    }

    public ULong getOasMessageBodyId() {
        return oasMessageBodyId;
    }

    public InsertOasResponseArguments setOasMessageBodyId(ULong oasMessageBodyId) {
        this.oasMessageBodyId = oasMessageBodyId;
        return this;
    }

    public boolean isMakeArrayIndicator() {
        return makeArrayIndicator;
    }

    public InsertOasResponseArguments setMakeArrayIndicator(boolean makeArrayIndicator) {
        this.makeArrayIndicator = makeArrayIndicator;
        return this;
    }

    public boolean isSuppressRootIndicator() {
        return suppressRootIndicator;
    }

    public InsertOasResponseArguments setSuppressRootIndicator(boolean suppressRootIndicator) {
        this.suppressRootIndicator = suppressRootIndicator;
        return this;
    }

    public boolean isIncludeMetaHeaderIndicator() {
        return includeMetaHeaderIndicator;
    }

    public InsertOasResponseArguments setIncludeMetaHeaderIndicator(boolean includeMetaHeaderIndicator) {
        this.includeMetaHeaderIndicator = includeMetaHeaderIndicator;
        return this;
    }

    public boolean isIncludePaginationIndicator() {
        return includePaginationIndicator;
    }

    public InsertOasResponseArguments setIncludePaginationIndicator(boolean includePaginationIndicator) {
        this.includePaginationIndicator = includePaginationIndicator;
        return this;
    }

    public String getHttpStatusCode() {
        return httpStatusCode;
    }

    public InsertOasResponseArguments setHttpStatusCode(String httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
        return this;
    }

    public boolean isIncludeConfirmIndicator() {
        return includeConfirmIndicator;
    }

    public InsertOasResponseArguments setIncludeConfirmIndicator(boolean includeConfirmIndicator) {
        this.includeConfirmIndicator = includeConfirmIndicator;
        return this;
    }

    public InsertOasResponseArguments setTimestamp(long millis) {
        return setTimestamp(new Timestamp(millis).toLocalDateTime());
    }

    public InsertOasResponseArguments setTimestamp(Date date) {
        return setTimestamp(new Timestamp(date.getTime()).toLocalDateTime());
    }

    public InsertOasResponseArguments setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public ULong getUserId() {
        return userId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public ULong execute() {
        return repository.insertOasResponse(this);
    }

}
