package org.oagi.score.repo.api.agency.model;

import org.oagi.score.repo.api.base.PaginationRequest;
import org.oagi.score.repo.api.corecomponent.model.CcState;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class GetAgencyIdListListRequest extends PaginationRequest<AgencyIdList> {
    public GetAgencyIdListListRequest(ScoreUser requester) {
        super(requester, AgencyIdList.class);
    }

    private String name;
    private String definition;
    private String module;
    private BigInteger releaseId;
    private Collection<String> updaterUsernameList;
    private LocalDateTime updateStartDate;
    private LocalDateTime updateEndDate;
    private List<CcState> states;
    private List<String> ownerLoginIds;
    private List<String> updaterLoginIds;

    public Boolean getDeprecated() {
        return deprecated;
    }

    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

    private Boolean deprecated;

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public List<CcState> getStates() {
        return states;
    }

    public void setStates(List<CcState> states) {
        this.states = states;
    }

    public List<String> getOwnerLoginIds() {
        return ownerLoginIds;
    }

    public void setOwnerLoginIds(List<String> ownerLoginIds) {
        this.ownerLoginIds = ownerLoginIds;
    }

    public List<String> getUpdaterLoginIds() {
        return updaterLoginIds;
    }

    public void setUpdaterLoginIds(List<String> updaterLoginIds) {
        this.updaterLoginIds = updaterLoginIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public Collection<String> getUpdaterUsernameList() {
        return Objects.requireNonNullElse(updaterUsernameList, Collections.emptyList());
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

    public BigInteger getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(BigInteger releaseId) {
        this.releaseId = releaseId;
    }
}
