package org.oagi.score.export.model;

import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsccpManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsccpRecord;

public class ASCCPGroup extends ASCCP {

    ASCCPGroup(AsccpManifestRecord asccpManifest, AsccpRecord asccp,
               AccManifestRecord rolfOfAccManifest, AccRecord roleOfAcc) {
        super(asccpManifest, asccp, rolfOfAccManifest, roleOfAcc);
    }

}
