package org.oagi.score.gateway.http.api.bie_management.model.asbie;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.Abie;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.WrappedAsbiep;

public class WrappedAsbie {

    private Abie fromAbie;

    private Asbie asbie;

    private WrappedAsbiep toAsbiep;

    private TopLevelAsbiepId refTopLevelAsbiepId;

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

    public TopLevelAsbiepId getRefTopLevelAsbiepId() {
        return refTopLevelAsbiepId;
    }

    public void setRefTopLevelAsbiepId(TopLevelAsbiepId refTopLevelAsbiepId) {
        this.refTopLevelAsbiepId = refTopLevelAsbiepId;
    }
}
