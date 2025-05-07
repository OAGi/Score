package org.oagi.score.gateway.http.api.oas_management.repository.criteria;

import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.oas_management.repository.OasDocCommandRepository;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

public class InsertOasResourceTagArguments {

    private final OasDocCommandRepository repository;

    private ULong userId;
    private ULong oasOperationId;
    private ULong oasTagId;
    private LocalDateTime timestamp = new Timestamp(System.currentTimeMillis()).toLocalDateTime();

    public InsertOasResourceTagArguments(OasDocCommandRepository repository) {
        this.repository = repository;
    }

    public InsertOasResourceTagArguments setUserId(UserId userId) {
        return setUserId(ULong.valueOf(userId.value()));
    }

    public InsertOasResourceTagArguments setUserId(ULong userId) {
        this.userId = userId;
        return this;
    }

    public InsertOasResourceTagArguments setOasOperationId(BigInteger oasOperationId) {
        return setOasOperationId(ULong.valueOf(oasOperationId));
    }

    public InsertOasResourceTagArguments setOasOperationId(ULong oasOperationId) {
        this.oasOperationId = oasOperationId;
        return this;
    }

    public InsertOasResourceTagArguments setOasTagId(BigInteger oasTagId) {
        return setOasTagId(ULong.valueOf(oasTagId));
    }

    public InsertOasResourceTagArguments setOasTagId(ULong oasTagId) {
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

    public ULong getUserId() {
        return userId;
    }

    public ULong getOasOperationId() {
        return oasOperationId;
    }

    public ULong getOasTagId() {
        return oasTagId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public ULong execute() {
        return repository.insertOasResourceTag(this);
    }

}
