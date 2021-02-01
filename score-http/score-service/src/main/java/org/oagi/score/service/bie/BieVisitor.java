package org.oagi.score.service.bie;

import org.oagi.score.repo.api.bie.model.*;

public interface BieVisitor {

    default void visitStart(TopLevelAsbiep topLevelAsbiep, BieVisitContext context) {}

    default void visitEnd(TopLevelAsbiep topLevelAsbiep, BieVisitContext context) {}

    default void visitAbie(Abie abie, BieVisitContext context) {}

    default void visitAsbie(Asbie asbie, BieVisitContext context) {}

    default void visitBbie(Bbie bbie, BieVisitContext context) {}

    default void visitAsbiep(Asbiep asbiep, BieVisitContext context) {}

    default void visitBbiep(Bbiep bbiep, BieVisitContext context) {}

    default void visitBbieSc(BbieSc bbieSc, BieVisitContext context) {}

}
