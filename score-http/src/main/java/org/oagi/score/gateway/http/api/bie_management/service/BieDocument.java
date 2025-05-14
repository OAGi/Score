package org.oagi.score.gateway.http.api.bie_management.service;

import org.oagi.score.gateway.http.api.bie_management.model.BieAssociation;
import org.oagi.score.gateway.http.api.bie_management.model.abie.Abie;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.Asbie;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.Asbiep;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.Bbie;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieSc;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScId;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.Bbiep;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface BieDocument {

    Asbiep getRootAsbiep();

    Abie getAbie(Asbiep asbiep);

    Collection<BieAssociation> getAssociations(Abie abie);

    Asbiep getAsbiep(Asbie asbie);

    Bbie getBbie(BbieId bbieId);

    Bbiep getBbiep(Bbie bbie);

    BbieSc getBbieSc(BbieScId bbieScId);

    List<BbieSc> getBbieScList(Bbie bbie);

    CcDocument getCcDocument();

    void accept(BieVisitor visitor);

    Map<Asbie, Asbiep> getRefAsbieMap();

}
