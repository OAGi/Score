package org.oagi.score.repo.api.businessterm.model;

import org.oagi.score.repo.api.base.Request;

import java.math.BigInteger;
import java.util.List;

public class AssignBusinessTermRequest extends Request {

    private List<BieToAssign> biesToAssign;

    private BigInteger businessTermId;

    private boolean primaryIndicator;

    private String typeCode;

    public AssignBusinessTermRequest() {
    }

    public AssignBusinessTermRequest(List<BieToAssign> biesToAssign, BigInteger businessTermId, String typeCode,
                                     boolean primaryIndicator) {
        this.biesToAssign = biesToAssign;
        this.businessTermId = businessTermId;
        this.typeCode = typeCode;
        this.primaryIndicator = primaryIndicator;
    }

    public List<BieToAssign> getBiesToAssign() {
        return biesToAssign;
    }

    public void setBiesToAssign(List<BieToAssign> biesToAssign) {
        this.biesToAssign = biesToAssign;
    }

    public BigInteger getBusinessTermId() {
        return businessTermId;
    }

    public void setBusinessTermId(BigInteger businessTermId) {
        this.businessTermId = businessTermId;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public boolean isPrimaryIndicator() {
        return primaryIndicator;
    }

    public void setPrimaryIndicator(boolean primaryIndicator) {
        this.primaryIndicator = primaryIndicator;
    }

    @Override
    public String toString() {
        return "AssignBusinessTermRequest{" +
                "biesToAssign=" + biesToAssign +
                ", businessTermId=" + businessTermId +
                ", primaryIndicator=" + primaryIndicator +
                ", typeCode='" + typeCode + '\'' +
                '}';
    }
}
