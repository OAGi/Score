package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.bie_management.model.BieViewOrderEntry;
import org.oagi.score.gateway.http.api.bie_management.repository.BieViewOrderQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.util.List;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.BIE_VIEW_ORDER;

public class JooqBieViewOrderQueryRepository extends JooqBaseRepository implements BieViewOrderQueryRepository {

    public JooqBieViewOrderQueryRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public List<BieViewOrderEntry> findByFromAccManifestId(AccManifestId fromAccManifestId) {
        return dslContext()
                .select(BIE_VIEW_ORDER.FROM_ACC_MANIFEST_ID,
                        BIE_VIEW_ORDER.ASCC_MANIFEST_ID,
                        BIE_VIEW_ORDER.BCC_MANIFEST_ID,
                        BIE_VIEW_ORDER.WEIGHT)
                .from(BIE_VIEW_ORDER)
                .where(BIE_VIEW_ORDER.FROM_ACC_MANIFEST_ID.eq(valueOf(fromAccManifestId)))
                .fetch(record -> {
                    ULong ascc = record.get(BIE_VIEW_ORDER.ASCC_MANIFEST_ID);
                    ULong bcc = record.get(BIE_VIEW_ORDER.BCC_MANIFEST_ID);
                    return new BieViewOrderEntry(
                            new AccManifestId(record.get(BIE_VIEW_ORDER.FROM_ACC_MANIFEST_ID).toBigInteger()),
                            (ascc != null) ? new AsccManifestId(ascc.toBigInteger()) : null,
                            (bcc != null) ? new BccManifestId(bcc.toBigInteger()) : null,
                            record.get(BIE_VIEW_ORDER.WEIGHT));
                });
    }
}
