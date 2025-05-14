package org.oagi.score.gateway.http.api.business_term_management.model;

import org.oagi.score.gateway.http.common.model.base.Auditable;

import java.math.BigInteger;
import java.util.Objects;

public class BieToAssign extends Auditable {

    private BigInteger bieId;

    private String bieType;

    public BieToAssign() {
    }

    public BieToAssign(BigInteger bieId, String bieType) {
        this.bieId = bieId;
        this.bieType = bieType;
    }

    public BigInteger getBieId() {
        return bieId;
    }

    public void setBieId(BigInteger bieId) {
        this.bieId = bieId;
    }

    public String getBieType() {
        return bieType;
    }

    public void setBieType(String bieType) {
        this.bieType = bieType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BieToAssign that = (BieToAssign) o;
        return bieId == that.bieId && bieType.equals(that.bieType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bieId, bieType);
    }

    @Override
    public String toString() {
        return "BieToAssign{" +
                "bieId=" + bieId +
                ", bieType='" + bieType + '\'' +
                '}';
    }
}
