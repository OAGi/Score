package org.oagi.score.gateway.http.api.oas_management.repository.criteria;

import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.oas_management.model.OasOperationId;
import org.oagi.score.gateway.http.api.oas_management.model.OasResourceTagId;
import org.oagi.score.gateway.http.api.oas_management.model.OasTagId;
import org.oagi.score.gateway.http.api.oas_management.repository.OasDocCommandRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

public class InsertOasResourceTagArguments {

    private final OasDocCommandRepository repository;

    private UserId userId;
    private OasOperationId oasOperationId;
    private OasTagId oasTagId;
    private LocalDateTime timestamp = new Timestamp(System.currentTimeMillis()).toLocalDateTime();

    public InsertOasResourceTagArguments(OasDocCommandRepository repository) {
        this.repository = repository;
    }

    public InsertOasResourceTagArguments setUserId(UserId userId) {
        this.userId = userId;
        return this;
    }

    public InsertOasResourceTagArguments setOasOperationId(OasOperationId oasOperationId) {
        this.oasOperationId = oasOperationId;
        return this;
    }

    public InsertOasResourceTagArguments setOasTagId(OasTagId oasTagId) {
        this.oasTagId = oasTagId;
        return this;
    }

    public InsertOasResourceTagArguments setTimestamp(long millis) {
        return setTimestamp(new Timestamp(millis).toLocalDateTime());
    }

    public InsertOasResourceTagArguments setTimestamp(Date date) {
        return setTimestamp(new Timestamp(date.getTime()).toLocalDateTime());
    }

    public InsertOasResourceTagArguments setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public UserId getUserId() {
        return userId;
    }

    public OasOperationId getOasOperationId() {
        return oasOperationId;
    }

    public OasTagId getOasTagId() {
        return oasTagId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public OasResourceTagId execute() {
        return repository.insertOasResourceTag(this);
    }

}
