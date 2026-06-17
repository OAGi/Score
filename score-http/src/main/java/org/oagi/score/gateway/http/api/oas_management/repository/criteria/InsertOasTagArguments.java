package org.oagi.score.gateway.http.api.oas_management.repository.criteria;

import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.oas_management.model.OasTagId;
import org.oagi.score.gateway.http.api.oas_management.repository.OasDocCommandRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

public class InsertOasTagArguments {

    private final OasDocCommandRepository repository;

    private UserId userId;
    private String guid;
    private String name;
    private String description;
    private LocalDateTime timestamp = new Timestamp(System.currentTimeMillis()).toLocalDateTime();

    public InsertOasTagArguments(OasDocCommandRepository repository) {
        this.repository = repository;
    }

    public InsertOasTagArguments setUserId(UserId userId) {
        this.userId = userId;
        return this;
    }

    public InsertOasTagArguments setGuid(String guid) {
        this.guid = guid;
        return this;
    }

    public InsertOasTagArguments setName(String name) {
        this.name = name;
        return this;
    }

    public InsertOasTagArguments setDescription(String description) {
        this.description = description;
        return this;
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

    public UserId getUserId() {
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

    public OasTagId execute() {
        return repository.insertOasTag(this);
    }

}
