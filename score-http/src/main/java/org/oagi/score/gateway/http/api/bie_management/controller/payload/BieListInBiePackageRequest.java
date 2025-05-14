package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import org.oagi.score.gateway.http.api.bie_management.model.BieList;
import org.oagi.score.gateway.http.api.bie_management.model.BiePackageId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.base.PaginationRequest;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class BieListInBiePackageRequest extends PaginationRequest<BieList> {

    private BiePackageId biePackageId;

    private String den;
    private String businessContext;
    private String version;
    private String remark;
    private List<String> ownerLoginIdList = Collections.emptyList();
    private List<String> updaterLoginIdList = Collections.emptyList();
    private Date updateStartDate;
    private Date updateEndDate;

    public BieListInBiePackageRequest(ScoreUser requester) {
        super(requester, BieList.class);
    }

    public BiePackageId getBiePackageId() {
        return biePackageId;
    }

    public void setBiePackageId(BiePackageId biePackageId) {
        this.biePackageId = biePackageId;
    }

    public BieListInBiePackageRequest withBiePackageId(BiePackageId biePackageId) {
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

    public List<String> getOwnerLoginIdList() {
        return ownerLoginIdList;
    }

    public void setOwnerLoginIdList(List<String> ownerLoginIdList) {
        this.ownerLoginIdList = ownerLoginIdList;
    }

    public List<String> getUpdaterLoginIdList() {
        return updaterLoginIdList;
    }

    public void setUpdaterLoginIdList(List<String> updaterLoginIdList) {
        this.updaterLoginIdList = updaterLoginIdList;
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
