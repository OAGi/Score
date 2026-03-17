package org.oagi.score.gateway.http.api.oas_management.repository.criteria;

import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.oas_management.model.OasMessageBodyId;
import org.oagi.score.gateway.http.api.oas_management.repository.OasDocCommandRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

public class InsertOasMessageBodyArguments {

    private final OasDocCommandRepository repository;

    private UserId userId;
    private TopLevelAsbiepId topLevelAsbiepId;
    private LocalDateTime timestamp = new Timestamp(System.currentTimeMillis()).toLocalDateTime();

    public InsertOasMessageBodyArguments(OasDocCommandRepository repository) {
        this.repository = repository;
    }

    public InsertOasMessageBodyArguments setUserId(UserId userId) {
        this.userId = userId;
        return this;
    }

    public InsertOasMessageBodyArguments setTopLevelAsbiepId(TopLevelAsbiepId topLevelAsbiepId) {
        this.topLevelAsbiepId = topLevelAsbiepId;
        return this;
    }

    public InsertOasMessageBodyArguments setTimestamp(long millis) {
        return setTimestamp(new Timestamp(millis).toLocalDateTime());
    }

    public InsertOasMessageBodyArguments setTimestamp(Date date) {
        return setTimestamp(new Timestamp(date.getTime()).toLocalDateTime());
    }

    public InsertOasMessageBodyArguments setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public UserId getUserId() {
        return userId;
    }

    public TopLevelAsbiepId getTopLevelAsbiepId() {
        return topLevelAsbiepId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public OasMessageBodyId execute() {
        return repository.insertOasMessageBody(this);
    }
}
