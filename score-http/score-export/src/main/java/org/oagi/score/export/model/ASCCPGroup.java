package org.oagi.score.export.model;

import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsccpRecord;

public class ASCCPGroup extends ASCCP {

    ASCCPGroup(AsccpRecord asccp, AccRecord roleOfAcc) {
        super(asccp, roleOfAcc);
    }
}
