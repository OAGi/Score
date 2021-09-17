package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.PaginationRequest;
import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collection;

public class GetModuleElementRequest extends Request {

    public GetModuleElementRequest(ScoreUser requester) {
        super(requester);
    }

    private BigInteger moduleSetId;

    private BigInteger moduleDirId;

    public BigInteger getModuleSetId() {
        return moduleSetId;
    }

    public void setModuleSetId(BigInteger moduleSetId) {
        this.moduleSetId = moduleSetId;
    }

    public BigInteger getModuleDirId() {
        return moduleDirId;
    }

    public void setModuleDirId(BigInteger moduleDirId) {
        this.moduleDirId = moduleDirId;
    }
}
