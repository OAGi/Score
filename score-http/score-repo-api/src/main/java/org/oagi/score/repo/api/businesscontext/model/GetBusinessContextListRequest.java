package org.oagi.score.repo.api.businesscontext.model;

import org.oagi.score.repo.api.base.PaginationRequest;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class GetBusinessContextListRequest extends PaginationRequest<BusinessContext> {

    private Collection<BigInteger> businessContextIdList;
    private Collection<BigInteger> topLevelAsbiepIdList;
    private String name;
    private Collection<String> updaterUsernameList;
    private LocalDateTime updateStartDate;
    private LocalDateTime updateEndDate;
    private Long tenantId;
    private boolean notConnectedToTenant;
    private boolean isBieEditing;
    private List<Long> userTenantIds;

    public GetBusinessContextListRequest(ScoreUser requester) {
        super(requester, BusinessContext.class);
    }

    public Collection<BigInteger> getBusinessContextIdList() {
        return (businessContextIdList == null) ? Collections.emptyList() : businessContextIdList;
    }

    public void setBusinessContextIdList(Collection<BigInteger> businessContextIdList) {
        this.businessContextIdList = businessContextIdList;
    }

    public GetBusinessContextListRequest withBusinessContextIdList(Collection<BigInteger> businessContextIdList) {
        this.setBusinessContextIdList(businessContextIdList);
        return this;
    }

    public Collection<BigInteger> getTopLevelAsbiepIdList() {
        return (topLevelAsbiepIdList == null) ? Collections.emptyList() : topLevelAsbiepIdList;
    }

    public void setTopLevelAsbiepIdList(Collection<BigInteger> topLevelAsbiepIdList) {
        this.topLevelAsbiepIdList = topLevelAsbiepIdList;
    }

    public GetBusinessContextListRequest withTopLevelAsbiepIdList(Collection<BigInteger> topLevelAsbiepIdList) {
        this.setTopLevelAsbiepIdList(topLevelAsbiepIdList);
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GetBusinessContextListRequest withName(String name) {
        this.setName(name);
        return this;
    }

    public Collection<String> getUpdaterUsernameList() {
        return (updaterUsernameList == null) ? Collections.emptyList() : updaterUsernameList;
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

	public Long getTenantId() {
		return tenantId;
	}

	public void setTenantId(Long tenantId) {
		this.tenantId = tenantId;
	}

	public boolean isNotConnectedToTenant() {
		return notConnectedToTenant;
	}

	public void setNotConnectedToTenant(boolean notConnectedToTenant) {
		this.notConnectedToTenant = notConnectedToTenant;
	}

	public boolean isBieEditing() {
		return isBieEditing;
	}

	public void setBieEditing(boolean isBieEditing) {
		this.isBieEditing = isBieEditing;
	}

	public List<Long> getUserTenantIds() {
		return userTenantIds;
	}

	public void setUserTenantIds(List<Long> userTenantIds) {
		this.userTenantIds = userTenantIds;
	}
}
