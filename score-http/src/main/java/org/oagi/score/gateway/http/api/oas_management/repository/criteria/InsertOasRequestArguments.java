package org.oagi.score.gateway.http.api.oas_management.repository.criteria;

import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.oas_management.repository.OasDocCommandRepository;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

public class InsertOasRequestArguments {

    private final OasDocCommandRepository repository;

    private ULong userId;
    private ULong oasOperationId;
    private ULong oasMessageBodyId;
    private String description;
    private boolean required;
    private boolean makeArrayIndicator;
    private boolean suppressRootIndicator;
    private boolean includeMetaHeaderIndicator;
    private boolean includePaginationIndicator;
    private LocalDateTime timestamp = new Timestamp(System.currentTimeMillis()).toLocalDateTime();

    public InsertOasRequestArguments(OasDocCommandRepository repository) {
        this.repository = repository;
    }

    public InsertOasRequestArguments setUserId(UserId userId) {
        return setUserId(ULong.valueOf(userId.value()));
    }

    public InsertOasRequestArguments setUserId(ULong userId) {
        this.userId = userId;
        return this;
    }

    public ULong getOasOperationId() {
        return oasOperationId;
    }

    public InsertOasRequestArguments setOasOperationId(BigInteger oasOperationId) {
        return setOasOperationId(ULong.valueOf(oasOperationId));
    }

    public InsertOasRequestArguments setOasOperationId(ULong oasOperationId) {
        this.oasOperationId = oasOperationId;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public InsertOasRequestArguments setDescription(String description) {
        this.description = description;
        return this;
    }

    public boolean isRequired() {
        return required;
    }

    public InsertOasRequestArguments setRequired(boolean required) {
        this.required = required;
        return this;
    }

    public ULong getOasMessageBodyId() {
        return oasMessageBodyId;
    }

    public InsertOasRequestArguments setOasMessageBodyId(ULong oasMessageBodyId) {
        this.oasMessageBodyId = oasMessageBodyId;
        return this;
    }

    public boolean isMakeArrayIndicator() {
        return makeArrayIndicator;
    }

    public InsertOasRequestArguments setMakeArrayIndicator(boolean makeArrayIndicator) {
        this.makeArrayIndicator = makeArrayIndicator;
        return this;
    }

    public boolean isSuppressRootIndicator() {
        return suppressRootIndicator;
    }

    public InsertOasRequestArguments setSuppressRootIndicator(boolean suppressRootIndicator) {
        this.suppressRootIndicator = suppressRootIndicator;
        return this;
    }

    public boolean isIncludeMetaHeaderIndicator() {
        return includeMetaHeaderIndicator;
    }

    public InsertOasRequestArguments setIncludeMetaHeaderIndicator(boolean includeMetaHeaderIndicator) {
        this.includeMetaHeaderIndicator = includeMetaHeaderIndicator;
        return this;
    }

    public boolean isIncludePaginationIndicator() {
        return includePaginationIndicator;
    }

    public InsertOasRequestArguments setIncludePaginationIndicator(boolean includePaginationIndicator) {
        this.includePaginationIndicator = includePaginationIndicator;
        return this;
    }

    public InsertOasRequestArguments setTimestamp(long millis) {
        return setTimestamp(new Timestamp(millis).toLocalDateTime());
    }

    public InsertOasRequestArguments setTimestamp(Date date) {
        return setTimestamp(new Timestamp(date.getTime()).toLocalDateTime());
    }

    public InsertOasRequestArguments setTimestamp(LocalDateTime timestamp) {
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
        return repository.insertOasRequest(this);
    }

}
