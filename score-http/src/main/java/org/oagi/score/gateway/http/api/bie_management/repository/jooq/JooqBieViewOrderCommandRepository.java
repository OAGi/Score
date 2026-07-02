package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.bie_management.model.BieViewOrderEntry;
import org.oagi.score.gateway.http.api.bie_management.repository.BieViewOrderCommandRepository;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.AccManifest;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.AsccManifest;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.BccManifest;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.BieViewOrderRecord;

import java.time.LocalDateTime;
import java.util.List;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

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

    // ----- Release carry-over (issue #1638) -----
    //
    // View order is authored on the 'Working' release; a drafted-release manifest records the Working
    // manifest it was copied from in its NEXT_*_MANIFEST_ID column (see JooqCcCommandRepository
    // #copyWorkingManifests). A view-order row is re-anchored onto the new release by remapping BOTH its
    // view-parent ACC (from_acc_manifest_id) and its ASCC/BCC child through that pointer. This mirrors how
    // GitHub issue links are carried over (issue #1533), but needs two joins because a row references two
    // manifests. The audit columns are copied from the source row to preserve authorship/timestamps.

    @Override
    public void copyFromWorking(ReleaseId targetReleaseId) {
        ULong target = valueOf(targetReleaseId);
        AccManifest targetAcc = ACC_MANIFEST.as("target_acc");
        AsccManifest targetAscc = ASCC_MANIFEST.as("target_ascc");
        BccManifest targetBcc = BCC_MANIFEST.as("target_bcc");

        // ASCC-typed rows. The join to the target ASCC (NULL never equals) already excludes BCC rows;
        // the bcc_manifest_id column is omitted from the insert so it defaults to NULL.
        dslContext().insertInto(BIE_VIEW_ORDER,
                        BIE_VIEW_ORDER.FROM_ACC_MANIFEST_ID,
                        BIE_VIEW_ORDER.ASCC_MANIFEST_ID,
                        BIE_VIEW_ORDER.WEIGHT,
                        BIE_VIEW_ORDER.CREATED_BY,
                        BIE_VIEW_ORDER.LAST_UPDATED_BY,
                        BIE_VIEW_ORDER.CREATION_TIMESTAMP,
                        BIE_VIEW_ORDER.LAST_UPDATE_TIMESTAMP)
                .select(dslContext().select(
                                targetAcc.ACC_MANIFEST_ID,
                                targetAscc.ASCC_MANIFEST_ID,
                                BIE_VIEW_ORDER.WEIGHT,
                                BIE_VIEW_ORDER.CREATED_BY,
                                BIE_VIEW_ORDER.LAST_UPDATED_BY,
                                BIE_VIEW_ORDER.CREATION_TIMESTAMP,
                                BIE_VIEW_ORDER.LAST_UPDATE_TIMESTAMP)
                        .from(BIE_VIEW_ORDER)
                        .join(targetAcc).on(and(
                                targetAcc.NEXT_ACC_MANIFEST_ID.eq(BIE_VIEW_ORDER.FROM_ACC_MANIFEST_ID),
                                targetAcc.RELEASE_ID.eq(target)))
                        .join(targetAscc).on(and(
                                targetAscc.NEXT_ASCC_MANIFEST_ID.eq(BIE_VIEW_ORDER.ASCC_MANIFEST_ID),
                                targetAscc.RELEASE_ID.eq(target)))
                        .where(BIE_VIEW_ORDER.ASCC_MANIFEST_ID.isNotNull()))
                .execute();

        // BCC-typed rows (symmetric; ascc_manifest_id omitted -> NULL).
        dslContext().insertInto(BIE_VIEW_ORDER,
                        BIE_VIEW_ORDER.FROM_ACC_MANIFEST_ID,
                        BIE_VIEW_ORDER.BCC_MANIFEST_ID,
                        BIE_VIEW_ORDER.WEIGHT,
                        BIE_VIEW_ORDER.CREATED_BY,
                        BIE_VIEW_ORDER.LAST_UPDATED_BY,
                        BIE_VIEW_ORDER.CREATION_TIMESTAMP,
                        BIE_VIEW_ORDER.LAST_UPDATE_TIMESTAMP)
                .select(dslContext().select(
                                targetAcc.ACC_MANIFEST_ID,
                                targetBcc.BCC_MANIFEST_ID,
                                BIE_VIEW_ORDER.WEIGHT,
                                BIE_VIEW_ORDER.CREATED_BY,
                                BIE_VIEW_ORDER.LAST_UPDATED_BY,
                                BIE_VIEW_ORDER.CREATION_TIMESTAMP,
                                BIE_VIEW_ORDER.LAST_UPDATE_TIMESTAMP)
                        .from(BIE_VIEW_ORDER)
                        .join(targetAcc).on(and(
                                targetAcc.NEXT_ACC_MANIFEST_ID.eq(BIE_VIEW_ORDER.FROM_ACC_MANIFEST_ID),
                                targetAcc.RELEASE_ID.eq(target)))
                        .join(targetBcc).on(and(
                                targetBcc.NEXT_BCC_MANIFEST_ID.eq(BIE_VIEW_ORDER.BCC_MANIFEST_ID),
                                targetBcc.RELEASE_ID.eq(target)))
                        .where(BIE_VIEW_ORDER.BCC_MANIFEST_ID.isNotNull()))
                .execute();
    }

    @Override
    public void deleteByRelease(ReleaseId releaseId) {
        ULong release = valueOf(releaseId);
        // Delete every row referencing ANY manifest in this release. The three refs of a row live in the
        // same release, so matching from_acc alone would suffice, but covering all three is unambiguously
        // FK-safe against the ACC/ASCC/BCC manifest deletes that follow.
        dslContext().deleteFrom(BIE_VIEW_ORDER)
                .where(BIE_VIEW_ORDER.FROM_ACC_MANIFEST_ID.in(
                                dslContext().select(ACC_MANIFEST.ACC_MANIFEST_ID).from(ACC_MANIFEST)
                                        .where(ACC_MANIFEST.RELEASE_ID.eq(release)))
                        .or(BIE_VIEW_ORDER.ASCC_MANIFEST_ID.in(
                                dslContext().select(ASCC_MANIFEST.ASCC_MANIFEST_ID).from(ASCC_MANIFEST)
                                        .where(ASCC_MANIFEST.RELEASE_ID.eq(release))))
                        .or(BIE_VIEW_ORDER.BCC_MANIFEST_ID.in(
                                dslContext().select(BCC_MANIFEST.BCC_MANIFEST_ID).from(BCC_MANIFEST)
                                        .where(BCC_MANIFEST.RELEASE_ID.eq(release)))))
                .execute();
    }
}
