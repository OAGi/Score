package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.bie_management.model.BieViewOrderEntry;
import org.oagi.score.gateway.http.api.bie_management.repository.BieViewOrderCommandRepository;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.BieViewOrderRecord;

import java.time.LocalDateTime;
import java.util.List;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.BIE_VIEW_ORDER;

public class JooqBieViewOrderCommandRepository extends JooqBaseRepository implements BieViewOrderCommandRepository {

    public JooqBieViewOrderCommandRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public void upsert(AccManifestId fromAccManifestId, List<BieViewOrderEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return;
        }
        ULong fromAccId = valueOf(fromAccManifestId);
        ULong requesterId = valueOf(requester().userId());
        LocalDateTime timestamp = LocalDateTime.now();

        for (BieViewOrderEntry entry : entries) {
            ULong ascc = valueOf(entry.asccManifestId());
            ULong bcc = valueOf(entry.bccManifestId());

            // Invariant: exactly one of ascc/bcc is non-null (enforced in code, not the DB).
            if ((ascc == null) == (bcc == null)) {
                throw new IllegalArgumentException(
                        "Exactly one of asccManifestId / bccManifestId must be provided for a view-order entry.");
            }

            Condition childCondition = (ascc != null)
                    ? BIE_VIEW_ORDER.ASCC_MANIFEST_ID.eq(ascc)
                    : BIE_VIEW_ORDER.BCC_MANIFEST_ID.eq(bcc);

            // A null weight resets this child back to its seq_key position.
            if (entry.weight() == null) {
                dslContext().deleteFrom(BIE_VIEW_ORDER)
                        .where(BIE_VIEW_ORDER.FROM_ACC_MANIFEST_ID.eq(fromAccId).and(childCondition))
                        .execute();
                continue;
            }

            BieViewOrderRecord record = new BieViewOrderRecord();
            record.setFromAccManifestId(fromAccId);
            record.setAsccManifestId(ascc);
            record.setBccManifestId(bcc);
            record.setWeight(entry.weight());
            record.setCreatedBy(requesterId);
            record.setLastUpdatedBy(requesterId);
            record.setCreationTimestamp(timestamp);
            record.setLastUpdateTimestamp(timestamp);

            // Upsert keyed by the (from_acc, ascc) / (from_acc, bcc) unique key; created_* is preserved on update.
            dslContext().insertInto(BIE_VIEW_ORDER)
                    .set(record)
                    .onDuplicateKeyUpdate()
                    .set(BIE_VIEW_ORDER.WEIGHT, entry.weight())
                    .set(BIE_VIEW_ORDER.LAST_UPDATED_BY, requesterId)
                    .set(BIE_VIEW_ORDER.LAST_UPDATE_TIMESTAMP, timestamp)
                    .execute();
        }
    }

    @Override
    public void deleteByFromAccManifestId(AccManifestId fromAccManifestId) {
        dslContext().deleteFrom(BIE_VIEW_ORDER)
                .where(BIE_VIEW_ORDER.FROM_ACC_MANIFEST_ID.eq(valueOf(fromAccManifestId)))
                .execute();
    }
}
