package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.PaginationRequest;
import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collection;

public class GetAssignableCCListRequest extends Request {

    public GetAssignableCCListRequest(ScoreUser requester) {
        super(requester);
    }

    BigInteger moduleSetReleaseId;
    BigInteger releaseId;

    public BigInteger getModuleSetReleaseId() {
        return moduleSetReleaseId;
    }

    public void setModuleSetReleaseId(BigInteger moduleSetReleaseId) {
        this.moduleSetReleaseId = moduleSetReleaseId;
    }

    public BigInteger getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(BigInteger releaseId) {
        this.releaseId = releaseId;
    }
}
