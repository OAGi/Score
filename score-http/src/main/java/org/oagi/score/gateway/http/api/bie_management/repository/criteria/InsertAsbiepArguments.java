package org.oagi.score.gateway.http.api.bie_management.repository.criteria;

import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.bie_management.repository.AsbiepCommandRepository;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

public class InsertAsbiepArguments {

    private final AsbiepCommandRepository repository;

    private ULong asccpManifestId;
    private ULong roleOfAbieId;
    private ULong topLevelAsbiepId;
    private String path;
    private ULong userId;
    private LocalDateTime timestamp = new Timestamp(System.currentTimeMillis()).toLocalDateTime();

    public InsertAsbiepArguments(AsbiepCommandRepository repository) {
        this.repository = repository;
    }

    public InsertAsbiepArguments setAsccpManifestId(AsccpManifestId asccpManifestId) {
        return setAsccpManifestId(ULong.valueOf(asccpManifestId.value()));
    }

    public InsertAsbiepArguments setAsccpManifestId(ULong asccpManifestId) {
        this.asccpManifestId = asccpManifestId;
        return this;
    }

    public InsertAsbiepArguments setRoleOfAbieId(AbieId roleOfAbieId) {
        return setRoleOfAbieId(ULong.valueOf(roleOfAbieId.value()));
    }

    public InsertAsbiepArguments setRoleOfAbieId(ULong roleOfAbieId) {
        this.roleOfAbieId = roleOfAbieId;
        return this;
    }

    public InsertAsbiepArguments setTopLevelAsbiepId(TopLevelAsbiepId topLevelAsbiepId) {
        return setTopLevelAsbiepId(ULong.valueOf(topLevelAsbiepId.value()));
    }

    public InsertAsbiepArguments setTopLevelAsbiepId(ULong topLevelAsbiepId) {
        this.topLevelAsbiepId = topLevelAsbiepId;
        return this;
    }

    public String getPath() {
        return path;
    }

    public InsertAsbiepArguments setPath(String path) {
        this.path = path;
        return this;
    }

    public InsertAsbiepArguments setUserId(UserId userId) {
        return setUserId(ULong.valueOf(userId.value()));
    }

    public InsertAsbiepArguments setUserId(ULong userId) {
        this.userId = userId;
        return this;
    }

    public InsertAsbiepArguments setTimestamp(long millis) {
        return setTimestamp(new Timestamp(millis).toLocalDateTime());
    }

    public InsertAsbiepArguments setTimestamp(Date date) {
        return setTimestamp(new Timestamp(date.getTime()).toLocalDateTime());
    }

    public InsertAsbiepArguments setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public ULong getAsccpManifestId() {
        return asccpManifestId;
    }

    public ULong getRoleOfAbieId() {
        return roleOfAbieId;
    }

    public ULong getTopLevelAsbiepId() {
        return topLevelAsbiepId;
    }

    public ULong getUserId() {
        return userId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public AsbiepId execute() {
        return repository.insertAsbiep(this);
    }
    
}
