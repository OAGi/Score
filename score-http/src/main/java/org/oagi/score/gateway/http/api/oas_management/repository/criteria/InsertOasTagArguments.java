package org.oagi.score.gateway.http.api.oas_management.repository.criteria;

import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.oas_management.repository.OasDocCommandRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

public class InsertOasTagArguments {

    private final OasDocCommandRepository repository;

    private ULong userId;
    private String guid;
    private String name;
    private String description;
    private LocalDateTime timestamp = new Timestamp(System.currentTimeMillis()).toLocalDateTime();

    public InsertOasTagArguments(OasDocCommandRepository repository) {
        this.repository = repository;
    }

    public InsertOasTagArguments setUserId(UserId userId) {
        return setUserId(ULong.valueOf(userId.value()));
    }

    public InsertOasTagArguments setUserId(ULong userId) {
        this.userId = userId;
        return this;
    }

    public InsertOasTagArguments setGuid(String guid) {
        return setGuid(guid);
    }

    public InsertOasTagArguments setName(String name) {
        return setName(name);
    }

    public InsertOasTagArguments setDescription(String description) {
        return setName(description);
    }

    public InsertOasTagArguments setTimestamp(long millis) {
        return setTimestamp(new Timestamp(millis).toLocalDateTime());
    }

    public InsertOasTagArguments setTimestamp(Date date) {
        return setTimestamp(new Timestamp(date.getTime()).toLocalDateTime());
    }

    public InsertOasTagArguments setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public ULong getUserId() {
        return userId;
    }

    public String getGuid() {
        return guid;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public ULong execute() {
        return repository.insertOasTag(this);
    }

}
