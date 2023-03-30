package org.oagi.score.export.model;

import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccRecord;
import org.oagi.score.repository.provider.DataProvider;

public class ACCComplexType extends ACC {

    ACCComplexType(AccManifestRecord accManifest, AccRecord acc, ACC basedAcc,
                   DataProvider dataProvider) {
        super(accManifest, acc, basedAcc, dataProvider);
    }

}
