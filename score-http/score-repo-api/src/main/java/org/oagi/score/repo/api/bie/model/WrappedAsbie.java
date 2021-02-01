package org.oagi.score.repo.api.bie.model;

import java.math.BigInteger;

public class WrappedAsbie {

    private Abie fromAbie;

    private Asbie asbie;

    private WrappedAsbiep toAsbiep;

    private BigInteger refTopLevelAsbiepId;

    public Abie getFromAbie() {
        return fromAbie;
    }

    public void setFromAbie(Abie fromAbie) {
        this.fromAbie = fromAbie;
    }

    public Asbie getAsbie() {
        return asbie;
    }

    public void setAsbie(Asbie asbie) {
        this.asbie = asbie;
    }

    public WrappedAsbiep getToAsbiep() {
        return toAsbiep;
    }

    public void setToAsbiep(WrappedAsbiep toAsbiep) {
        this.toAsbiep = toAsbiep;
    }

    public BigInteger getRefTopLevelAsbiepId() {
        return refTopLevelAsbiepId;
    }

    public void setRefTopLevelAsbiepId(BigInteger refTopLevelAsbiepId) {
        this.refTopLevelAsbiepId = refTopLevelAsbiepId;
    }
}
