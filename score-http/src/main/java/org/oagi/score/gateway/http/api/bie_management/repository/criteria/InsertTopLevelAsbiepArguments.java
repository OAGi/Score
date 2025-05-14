package org.oagi.score.gateway.http.api.bie_management.repository.criteria;


import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.repository.TopLevelAsbiepCommandRepository;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

import static org.oagi.score.gateway.http.api.bie_management.model.BieState.WIP;

public class InsertTopLevelAsbiepArguments {

    private final TopLevelAsbiepCommandRepository repository;

    private ULong releaseId;
    private ULong userId;
    private BieState bieState = WIP;
    private String version;
    private String status;

    private boolean inverseMode;
    private LocalDateTime timestamp = new Timestamp(System.currentTimeMillis()).toLocalDateTime();

    private ULong basedTopLevelAsbiepId;

    private ULong sourceTopLevelAsbiepId;
    private String sourceAction;
    private LocalDateTime sourceTimestamp;

    public InsertTopLevelAsbiepArguments(TopLevelAsbiepCommandRepository repository) {
        this.repository = repository;
    }

    public InsertTopLevelAsbiepArguments setReleaseId(ReleaseId releaseId) {
        return setReleaseId(ULong.valueOf(releaseId.value()));
    }

    public InsertTopLevelAsbiepArguments setReleaseId(ULong releaseId) {
        this.releaseId = releaseId;
        return this;
    }

    public InsertTopLevelAsbiepArguments setBieState(BieState bieState) {
        this.bieState = bieState;
        return this;
    }

    public InsertTopLevelAsbiepArguments setVersion(String version) {
        this.version = version;
        return this;
    }

    public InsertTopLevelAsbiepArguments setStatus(String status) {
        this.status = status;
        return this;
    }

    public InsertTopLevelAsbiepArguments setInverseMode(boolean inverseMode) {
        this.inverseMode = inverseMode;
        return this;
    }

    public InsertTopLevelAsbiepArguments setUserId(UserId userId) {
        return setUserId(ULong.valueOf(userId.value()));
    }

    public InsertTopLevelAsbiepArguments setUserId(ULong userId) {
        this.userId = userId;
        return this;
    }

    public InsertTopLevelAsbiepArguments setTimestamp(long millis) {
        return setTimestamp(new Timestamp(millis).toLocalDateTime());
    }

    public InsertTopLevelAsbiepArguments setTimestamp(Date date) {
        return setTimestamp(new Timestamp(date.getTime()).toLocalDateTime());
    }

    public InsertTopLevelAsbiepArguments setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public InsertTopLevelAsbiepArguments setBasedTopLevelAsbiepId(TopLevelAsbiepId basedTopLevelAsbiepId) {
        if (basedTopLevelAsbiepId != null) {
            return setBasedTopLevelAsbiepId(ULong.valueOf(basedTopLevelAsbiepId.value()));
        }
        return this;
    }

    public InsertTopLevelAsbiepArguments setBasedTopLevelAsbiepId(ULong basedTopLevelAsbiepId) {
        this.basedTopLevelAsbiepId = basedTopLevelAsbiepId;
        return this;
    }

    public InsertTopLevelAsbiepArguments setSource(TopLevelAsbiepId sourceTopLevelAsbiepId, String sourceAction) {
        this.sourceTopLevelAsbiepId = ULong.valueOf(sourceTopLevelAsbiepId.value());
        this.sourceAction = sourceAction;
        return this;
    }

    public ULong getReleaseId() {
        return releaseId;
    }

    public BieState getBieState() {
        return bieState;
    }

    public String getVersion() {
        return version;
    }

    public String getStatus() {
        return status;
    }

    public boolean isInverseMode() {
        return inverseMode;
    }

    public ULong getUserId() {
        return userId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public ULong getBasedTopLevelAsbiepId() {
        return basedTopLevelAsbiepId;
    }

    public ULong getSourceTopLevelAsbiepId() {
        return sourceTopLevelAsbiepId;
    }

    public String getSourceAction() {
        return sourceAction;
    }

    public LocalDateTime getSourceTimestamp() {
        return (sourceTimestamp != null) ? sourceTimestamp : getTimestamp();
    }

    public TopLevelAsbiepId execute() {
        return repository.insertTopLevelAsbiep(this);
    }

}
