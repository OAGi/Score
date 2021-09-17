package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.corecomponent.model.CcType;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class CreateModuleManifestRequest extends Request {

    public CreateModuleManifestRequest(ScoreUser requester) {
        super(requester);
    }

    private BigInteger manifestId;

    private CcType type;

    private BigInteger moduleSetReleaseId;

    private BigInteger moduleId;

    private LocalDateTime timestamp;

    public BigInteger getManifestId() {
        return manifestId;
    }

    public void setManifestId(BigInteger manifestId) {
        this.manifestId = manifestId;
    }

    public CcType getType() {
        return type;
    }

    public void setType(CcType type) {
        this.type = type;
    }

    public BigInteger getModuleSetReleaseId() {
        return moduleSetReleaseId;
    }

    public void setModuleSetReleaseId(BigInteger moduleSetReleaseId) {
        this.moduleSetReleaseId = moduleSetReleaseId;
    }

    public BigInteger getModuleId() {
        return moduleId;
    }

    public void setModuleId(BigInteger moduleId) {
        this.moduleId = moduleId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
