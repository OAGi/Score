package org.oagi.score.gateway.http.api.bie_management.service;


import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.abie.Abie;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.Asbie;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.Asbiep;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.Bbie;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieSc;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.Bbiep;

public interface BieVisitor {

    default void visitStart(TopLevelAsbiepSummaryRecord topLevelAsbiep, BieVisitContext context) {}

    default void visitEnd(TopLevelAsbiepSummaryRecord topLevelAsbiep, BieVisitContext context) {}

    default void visitAbie(Abie abie, BieVisitContext context) {}

    default void visitAsbie(Asbie asbie, BieVisitContext context) {}

    default void visitBbie(Bbie bbie, BieVisitContext context) {}

    default void visitAsbiep(Asbiep asbiep, BieVisitContext context) {}

    default void visitBbiep(Bbiep bbiep, BieVisitContext context) {}

    default void visitBbieSc(BbieSc bbieSc, BieVisitContext context) {}

}
