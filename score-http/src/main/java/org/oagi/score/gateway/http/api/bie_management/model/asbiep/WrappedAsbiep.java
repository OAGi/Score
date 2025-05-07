package org.oagi.score.gateway.http.api.bie_management.model.asbiep;

import org.oagi.score.gateway.http.api.bie_management.model.abie.Abie;

public class WrappedAsbiep {

    private Asbiep asbiep;

    private Abie roleOfAbie;

    public Asbiep getAsbiep() {
        return asbiep;
    }

    public void setAsbiep(Asbiep asbiep) {
        this.asbiep = asbiep;
    }

    public Abie getRoleOfAbie() {
        return roleOfAbie;
    }

    public void setRoleOfAbie(Abie roleOfAbie) {
        this.roleOfAbie = roleOfAbie;
    }
}
