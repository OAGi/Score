package org.oagi.score.gateway.http.api.bie_management.model;

import org.oagi.score.gateway.http.api.bie_management.model.abie.Abie;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.Asbie;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.Asbiep;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.Bbie;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieSc;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.Bbiep;

import java.util.Collections;
import java.util.List;

public class BieSet {

    private TopLevelAsbiepSummaryRecord topLevelAsbiep;

    private List<Abie> abieList;

    private List<Asbie> asbieList;

    private List<Bbie> bbieList;

    private List<Asbiep> asbiepList;

    private List<Bbiep> bbiepList;

    private List<BbieSc> bbieScList;

    public TopLevelAsbiepSummaryRecord getTopLevelAsbiep() {
        return topLevelAsbiep;
    }

    public void setTopLevelAsbiep(TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        this.topLevelAsbiep = topLevelAsbiep;
    }

    public List<Abie> getAbieList() {
        return (abieList != null) ? abieList : Collections.emptyList();
    }

    public void setAbieList(List<Abie> abieList) {
        this.abieList = abieList;
    }

    public List<Asbie> getAsbieList() {
        return (asbieList != null) ? asbieList : Collections.emptyList();
    }

    public void setAsbieList(List<Asbie> asbieList) {
        this.asbieList = asbieList;
    }

    public List<Bbie> getBbieList() {
        return (bbieList != null) ? bbieList : Collections.emptyList();
    }

    public void setBbieList(List<Bbie> bbieList) {
        this.bbieList = bbieList;
    }

    public List<Asbiep> getAsbiepList() {
        return (asbiepList != null) ? asbiepList : Collections.emptyList();
    }

    public void setAsbiepList(List<Asbiep> asbiepList) {
        this.asbiepList = asbiepList;
    }

    public List<Bbiep> getBbiepList() {
        return (bbiepList != null) ? bbiepList : Collections.emptyList();
    }

    public void setBbiepList(List<Bbiep> bbiepList) {
        this.bbiepList = bbiepList;
    }

    public List<BbieSc> getBbieScList() {
        return (bbieScList != null) ? bbieScList : Collections.emptyList();
    }

    public void setBbieScList(List<BbieSc> bbieScList) {
        this.bbieScList = bbieScList;
    }
}
