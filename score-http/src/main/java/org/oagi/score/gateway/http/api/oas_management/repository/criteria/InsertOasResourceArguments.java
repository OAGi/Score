package org.oagi.score.gateway.http.api.oas_management.repository.criteria;

import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.oas_management.repository.OasDocCommandRepository;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

public class InsertOasResourceArguments {

    private final OasDocCommandRepository repository;

    private ULong userId;
    private ULong oasDocId;
    private String path;
    private String ref;
    private LocalDateTime timestamp = new Timestamp(System.currentTimeMillis()).toLocalDateTime();

    public InsertOasResourceArguments(OasDocCommandRepository repository) {
        this.repository = repository;
    }

    public InsertOasResourceArguments setUserId(UserId userId) {
        return setUserId(ULong.valueOf(userId.value()));
    }

    public InsertOasResourceArguments setUserId(ULong userId) {
        this.userId = userId;
        return this;
    }

    public ULong getOasDocId() {
        return oasDocId;
    }

    public InsertOasResourceArguments setOasDocId(ULong oasDocId) {
        this.oasDocId = oasDocId;
        return this;
    }

    public String getPath() {
        return path;
    }

    public InsertOasResourceArguments setOasDocId(BigInteger oasDocId) {
        return setOasDocId(ULong.valueOf(oasDocId));
    }

    public InsertOasResourceArguments setPath(String path) {
        this.path = path;
        return this;
    }

    public String getRef() {
        return ref;
    }

    public InsertOasResourceArguments setRef(String ref) {
        this.ref = ref;
        return this;
    }

    public InsertOasResourceArguments setTimestamp(long millis) {
        return setTimestamp(new Timestamp(millis).toLocalDateTime());
    }

    public InsertOasResourceArguments setTimestamp(Date date) {
        return setTimestamp(new Timestamp(date.getTime()).toLocalDateTime());
    }

    public InsertOasResourceArguments setTimestamp(LocalDateTime timestamp) {
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
        return repository.insertOasResource(this);
    }

}
