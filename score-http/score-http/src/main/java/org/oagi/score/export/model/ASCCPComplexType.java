package org.oagi.score.export.model;

import org.jooq.types.ULong;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsccpRecord;

public class ASCCPComplexType extends ASCCP {

    ASCCPComplexType(AsccpRecord asccp, AccRecord roleOfAcc) {
        super(asccp, roleOfAcc);
    }

}
