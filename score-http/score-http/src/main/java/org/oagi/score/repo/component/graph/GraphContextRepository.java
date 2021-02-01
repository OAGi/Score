package org.oagi.score.repo.component.graph;

import org.jooq.DSLContext;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsccpManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.BccpManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.CodeListManifestRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public class GraphContextRepository {

    @Autowired
    private DSLContext dslContext;

    public CoreComponentGraphContext buildGraphContext(BigInteger releaseId) {
        return new CoreComponentGraphContext(dslContext, releaseId);
    }

    public CoreComponentGraphContext buildGraphContext(AccManifestRecord accManifest) {
        return buildGraphContext(accManifest.getReleaseId().toBigInteger());
    }

    public CoreComponentGraphContext buildGraphContext(AsccpManifestRecord asccpManifest) {
        return buildGraphContext(asccpManifest.getReleaseId().toBigInteger());
    }

    public CoreComponentGraphContext buildGraphContext(BccpManifestRecord bccpManifest) {
        return buildGraphContext(bccpManifest.getReleaseId().toBigInteger());
    }

    public CodeListGraphContext buildGraphContext(CodeListManifestRecord codeListManifest) {
        return new CodeListGraphContext(dslContext, codeListManifest.getReleaseId().toBigInteger());
    }
}
