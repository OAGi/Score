package org.oagi.score.export.model;

import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccRecord;
import org.oagi.score.repository.provider.DataProvider;

public class ACCGroup extends ACC {

    ACCGroup(AccRecord acc, ACC basedAcc,
             DataProvider dataProvider) {
        super(acc, basedAcc, dataProvider);
    }
}
