package org.oagi.score.gateway.http.api.bie_management.repository.criteria;


import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.repository.AbieCommandRepository;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

public class InsertAbieArguments {

    private final AbieCommandRepository repository;

    private ULong userId;
    private ULong accManifestId;
    private String path;
    private ULong topLevelAsbiepId;
    private LocalDateTime timestamp = new Timestamp(System.currentTimeMillis()).toLocalDateTime();

    public InsertAbieArguments(AbieCommandRepository repository) {
        this.repository = repository;
    }

    public InsertAbieArguments setUserId(UserId userId) {
        return setUserId(ULong.valueOf(userId.value()));
    }

    public InsertAbieArguments setUserId(ULong userId) {
        this.userId = userId;
        return this;
    }

    public InsertAbieArguments setAccManifestId(AccManifestId accManifestId) {
        return setAccManifestId(ULong.valueOf(accManifestId.value()));
    }

    public InsertAbieArguments setAccManifestId(ULong accManifestId) {
        this.accManifestId = accManifestId;
        return this;
    }

    public String getPath() {
        return path;
    }

    public InsertAbieArguments setPath(String path) {
        this.path = path;
        return this;
    }

    public InsertAbieArguments setTopLevelAsbiepId(TopLevelAsbiepId topLevelAsbiepId) {
        return setTopLevelAsbiepId(ULong.valueOf(topLevelAsbiepId.value()));
    }

    public InsertAbieArguments setTopLevelAsbiepId(ULong topLevelAsbiepId) {
        this.topLevelAsbiepId = topLevelAsbiepId;
        return this;
    }

    public InsertAbieArguments setTimestamp(long millis) {
        return setTimestamp(new Timestamp(millis).toLocalDateTime());
    }

    public InsertAbieArguments setTimestamp(Date date) {
        return setTimestamp(new Timestamp(date.getTime()).toLocalDateTime());
    }

    public InsertAbieArguments setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public ULong getUserId() {
        return userId;
    }

    public ULong getAccManifestId() {
        return accManifestId;
    }

    public ULong getTopLevelAsbiepId() {
        return topLevelAsbiepId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public AbieId execute() {
        return repository.insertAbie(this);
    }

}
