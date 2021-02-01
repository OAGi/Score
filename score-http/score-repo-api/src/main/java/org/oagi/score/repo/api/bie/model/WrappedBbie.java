package org.oagi.score.repo.api.bie.model;

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
