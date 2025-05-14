package org.oagi.score.gateway.http.api.bie_management.model.bbie;

import org.oagi.score.gateway.http.api.bie_management.model.abie.Abie;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.Bbiep;

public class WrappedBbie {

    private Abie fromAbie;

    private Bbie bbie;

    private Bbiep toBbiep;

    public Abie getFromAbie() {
        return fromAbie;
    }

    public void setFromAbie(Abie fromAbie) {
        this.fromAbie = fromAbie;
    }

    public Bbie getBbie() {
        return bbie;
    }

    public void setBbie(Bbie bbie) {
        this.bbie = bbie;
    }

    public Bbiep getToBbiep() {
        return toBbiep;
    }

    public void setToBbiep(Bbiep toBbiep) {
        this.toBbiep = toBbiep;
    }
}
