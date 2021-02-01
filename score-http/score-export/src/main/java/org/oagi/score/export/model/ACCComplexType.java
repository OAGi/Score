package org.oagi.score.export.model;

import org.oagi.score.provider.ImportedDataProvider;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccRecord;

public class ACCComplexType extends ACC {

    AccManifestRecord accManifest;

    ACCComplexType(AccRecord acc, ACC basedAcc, AccManifestRecord accManifestRecord,
                   ImportedDataProvider importedDataProvider) {
        super(acc, basedAcc, importedDataProvider);
        this.accManifest = accManifestRecord;
    }

    public AccManifestRecord getAccManifest() {
        return accManifest;
    }

}
