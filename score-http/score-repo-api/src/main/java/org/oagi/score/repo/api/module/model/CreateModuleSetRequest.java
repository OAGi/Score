package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class CreateModuleSetRequest extends Request {

    private String guid;

    private String name;

    private String description;

    private boolean createModuleSetRelease;

    private BigInteger targetReleaseId;

    private BigInteger targetModuleSetReleaseId;

    public CreateModuleSetRequest(ScoreUser requester) {
        super(requester);
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCreateModuleSetRelease() {
        return createModuleSetRelease;
    }

    public void setCreateModuleSetRelease(boolean createModuleSetRelease) {
        this.createModuleSetRelease = createModuleSetRelease;
    }

    public BigInteger getTargetReleaseId() {
        return targetReleaseId;
    }

    public void setTargetReleaseId(BigInteger targetReleaseId) {
        this.targetReleaseId = targetReleaseId;
    }

    public BigInteger getTargetModuleSetReleaseId() {
        return targetModuleSetReleaseId;
    }

    public void setTargetModuleSetReleaseId(BigInteger targetModuleSetReleaseId) {
        this.targetModuleSetReleaseId = targetModuleSetReleaseId;
    }
}
