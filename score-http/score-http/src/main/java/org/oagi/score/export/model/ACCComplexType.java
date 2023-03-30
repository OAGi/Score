package org.oagi.score.export.model;

import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccRecord;
import org.oagi.score.repository.provider.DataProvider;

public class ACCComplexType extends ACC {

    AccManifestRecord accManifest;

    ACCComplexType(AccRecord acc, ACC basedAcc, AccManifestRecord accManifestRecord,
                   DataProvider dataProvider) {
        super(acc, basedAcc, dataProvider);
        this.accManifest = accManifestRecord;
    }

    public AccManifestRecord getAccManifest() {
        return accManifest;
    }

}
