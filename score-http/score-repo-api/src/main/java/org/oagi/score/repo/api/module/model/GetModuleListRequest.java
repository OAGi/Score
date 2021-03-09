package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.PaginationRequest;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collection;

public class GetModuleListRequest extends PaginationRequest<Module> {

    public GetModuleListRequest(ScoreUser requester) {
        super(requester, Module.class);
    }

    private BigInteger moduleSetId;

    private String name;

    private Collection<String> updaterUsernameList;
    private LocalDateTime updateStartDate;
    private LocalDateTime updateEndDate;

    public Collection<String> getUpdaterUsernameList() {
        return updaterUsernameList;
    }

    public void setUpdaterUsernameList(Collection<String> updaterUsernameList) {
        this.updaterUsernameList = updaterUsernameList;
    }

    public LocalDateTime getUpdateStartDate() {
        return updateStartDate;
    }

    public void setUpdateStartDate(LocalDateTime updateStartDate) {
        this.updateStartDate = updateStartDate;
    }

    public LocalDateTime getUpdateEndDate() {
        return updateEndDate;
    }

    public void setUpdateEndDate(LocalDateTime updateEndDate) {
        this.updateEndDate = updateEndDate;
    }

    public BigInteger getModuleSetId() {
        return moduleSetId;
    }

    public void setModuleSetId(BigInteger moduleSetId) {
        this.moduleSetId = moduleSetId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
