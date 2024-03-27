package org.oagi.score.gateway.http.api.bie_management.data;

import org.oagi.score.repo.api.base.PaginationRequest;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class BieListInBiePackageRequest extends PaginationRequest<BieList> {

    private BigInteger biePackageId;

    private String den;
    private String businessContext;
    private String version;
    private String remark;
    private List<String> ownerLoginIds = Collections.emptyList();
    private List<String> updaterLoginIds = Collections.emptyList();
    private Date updateStartDate;
    private Date updateEndDate;

    public BieListInBiePackageRequest(ScoreUser requester) {
        super(requester, BieList.class);
    }

    public BigInteger getBiePackageId() {
        return biePackageId;
    }

    public void setBiePackageId(BigInteger biePackageId) {
        this.biePackageId = biePackageId;
    }

    public BieListInBiePackageRequest withBiePackageId(BigInteger biePackageId) {
        setBiePackageId(biePackageId);
        return this;
    }

    public String getDen() {
        return den;
    }

    public void setDen(String den) {
        this.den = den;
    }

    public String getBusinessContext() {
        return businessContext;
    }

    public void setBusinessContext(String businessContext) {
        this.businessContext = businessContext;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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

    public Date getUpdateStartDate() {
        return updateStartDate;
    }

    public void setUpdateStartDate(Date updateStartDate) {
        this.updateStartDate = updateStartDate;
    }

    public Date getUpdateEndDate() {
        return updateEndDate;
    }

    public void setUpdateEndDate(Date updateEndDate) {
        this.updateEndDate = updateEndDate;
    }
}
