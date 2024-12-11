package org.oagi.score.gateway.http.api.bie_management.data;

import org.oagi.score.repo.api.base.PaginationRequest;
import org.oagi.score.repo.api.bie.model.BieState;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class BiePackageListRequest extends PaginationRequest<BiePackage> {

    private BigInteger libraryId;
    private String versionId;
    private String versionName;
    private String description;
    private String den;
    private String businessTerm;
    private String version;
    private String remark;
    private List<BieState> states = Collections.emptyList();
    private List<String> ownerLoginIds = Collections.emptyList();
    private List<String> updaterLoginIds = Collections.emptyList();
    private List<BigInteger> releaseIds = Collections.emptyList();
    private List<BigInteger> biePackageIds = Collections.emptyList();
    private Date updateStartDate;
    private Date updateEndDate;

    public BiePackageListRequest(ScoreUser requester) {
        super(requester, BiePackage.class);
    }

    public BigInteger getLibraryId() {
        return libraryId;
    }

    public void setLibraryId(BigInteger libraryId) {
        this.libraryId = libraryId;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDen() {
        return den;
    }

    public void setDen(String den) {
        this.den = den;
    }

    public String getBusinessTerm() {
        return businessTerm;
    }

    public void setBusinessTerm(String businessTerm) {
        this.businessTerm = businessTerm;
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

    public List<BieState> getStates() {
        return (states != null) ? states : Collections.emptyList();
    }

    public void setStates(List<BieState> states) {
        this.states = states;
    }

    public List<String> getOwnerLoginIds() {
        return (ownerLoginIds != null) ? ownerLoginIds : Collections.emptyList();
    }

    public void setOwnerLoginIds(List<String> ownerLoginIds) {
        this.ownerLoginIds = ownerLoginIds;
    }

    public List<String> getUpdaterLoginIds() {
        return (updaterLoginIds != null) ? updaterLoginIds : Collections.emptyList();
    }

    public void setUpdaterLoginIds(List<String> updaterLoginIds) {
        this.updaterLoginIds = updaterLoginIds;
    }

    public List<BigInteger> getReleaseIds() {
        return (releaseIds != null) ? releaseIds : Collections.emptyList();
    }

    public void setReleaseIds(List<BigInteger> releaseIds) {
        this.releaseIds = releaseIds;
    }

    public List<BigInteger> getBiePackageIds() {
        return (biePackageIds != null) ? biePackageIds : Collections.emptyList();
    }

    public void setBiePackageIds(List<BigInteger> biePackageIds) {
        this.biePackageIds = biePackageIds;
    }

    public BiePackageListRequest withBiePackageIdList(List<BigInteger> biePackageIds) {
        setBiePackageIds(biePackageIds);
        return this;
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
