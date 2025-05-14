package org.oagi.score.gateway.http.api.oas_management.repository.criteria;

import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.oas_management.repository.OasDocCommandRepository;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

public class InsertOasOperationArguments {

    private final OasDocCommandRepository repository;

    private ULong userId;
    private ULong oasResourceId;
    private String verb;
    private String operationId;
    private String summary;
    private String description;
    private boolean deprecated;
    private LocalDateTime timestamp = new Timestamp(System.currentTimeMillis()).toLocalDateTime();

    public InsertOasOperationArguments(OasDocCommandRepository repository) {
        this.repository = repository;
    }

    public InsertOasOperationArguments setUserId(UserId userId) {
        return setUserId(ULong.valueOf(userId.value()));
    }

    public InsertOasOperationArguments setUserId(ULong userId) {
        this.userId = userId;
        return this;
    }

    public ULong getOasResourceId() {
        return oasResourceId;
    }

    public InsertOasOperationArguments setOasResourceId(BigInteger oasResourceId) {
        return setOasResourceId(ULong.valueOf(oasResourceId));
    }

    public InsertOasOperationArguments setOasResourceId(ULong oasResourceId) {
        this.oasResourceId = oasResourceId;
        return this;
    }

    public String getVerb() {
        return verb;
    }

    public InsertOasOperationArguments setVerb(String verb) {
        this.verb = verb;
        return this;
    }

    public String getOperationId() {
        return operationId;
    }

    public InsertOasOperationArguments setOperationId(String operationId) {
        this.operationId = operationId;
        return this;
    }

    public String getSummary() {
        return summary;
    }

    public InsertOasOperationArguments setSummary(String summary) {
        this.summary = summary;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public InsertOasOperationArguments setDescription(String description) {
        this.description = description;
        return this;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public InsertOasOperationArguments setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
        return this;
    }

    public InsertOasOperationArguments setTimestamp(long millis) {
        return setTimestamp(new Timestamp(millis).toLocalDateTime());
    }

    public InsertOasOperationArguments setTimestamp(Date date) {
        return setTimestamp(new Timestamp(date.getTime()).toLocalDateTime());
    }

    public InsertOasOperationArguments setTimestamp(LocalDateTime timestamp) {
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
        return repository.insertOasOperation(this);
    }
}
