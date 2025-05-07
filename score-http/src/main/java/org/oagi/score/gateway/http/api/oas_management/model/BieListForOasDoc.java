package org.oagi.score.gateway.http.api.oas_management.model;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class BieListForOasDoc {

    private BigInteger oasDocId;

    private List<BieForOasDoc> bieList = new ArrayList<BieForOasDoc>();

    public BieListForOasDoc(BigInteger oasDocId) {
        this.oasDocId = oasDocId;
    }

    public BigInteger getOasDocId() {
        return oasDocId;
    }

    public void setOasDocId(BigInteger oasDocId) {
        this.oasDocId = oasDocId;
    }

    public List<BieForOasDoc> getBieList() {
        return bieList;
    }

    public void setBieList(List<BieForOasDoc> bieList) {
        this.bieList = bieList;
    }

}
