package org.oagi.score.gateway.http.api.bie_management.model.bbie_sc;

import org.oagi.score.gateway.http.api.bie_management.model.bbie.Bbie;

public class WrappedBbieSc {

    private Bbie bbie;

    private BbieSc bbieSc;

    public Bbie getBbie() {
        return bbie;
    }

    public void setBbie(Bbie bbie) {
        this.bbie = bbie;
    }

    public BbieSc getBbieSc() {
        return bbieSc;
    }

    public void setBbieSc(BbieSc bbieSc) {
        this.bbieSc = bbieSc;
    }
}
