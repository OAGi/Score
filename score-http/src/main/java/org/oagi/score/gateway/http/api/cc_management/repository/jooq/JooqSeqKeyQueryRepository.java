package org.oagi.score.gateway.http.api.cc_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.SelectJoinStep;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.EntityType;
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeyId;
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeySummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.repository.SeqKeyQueryRepository;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.AccessControl;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.oagi.score.gateway.http.common.model.ScoreRole.DEVELOPER;
import static org.oagi.score.gateway.http.common.model.ScoreRole.END_USER;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

public class JooqSeqKeyQueryRepository
        extends JooqBaseRepository
        implements SeqKeyQueryRepository {

    public JooqSeqKeyQueryRepository(DSLContext dslContext,
                                     ScoreUser requester,
                                     RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public SeqKeySummaryRecord getSeqKeySummary(SeqKeyId seqKeyId) {
        if (seqKeyId == null) {
            return null;
        }
        var queryBuilder = new GetSeqKeySummaryQueryBuilder();
        return queryBuilder.select()
                .where(SEQ_KEY.SEQ_KEY_ID.eq(valueOf(seqKeyId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public List<SeqKeySummaryRecord> getSeqKeySummaryList(AccManifestId fromAccManifestId) {
        if (fromAccManifestId == null) {
            return null;
        }
        var queryBuilder = new GetSeqKeySummaryQueryBuilder();
        return queryBuilder.select()
                .where(SEQ_KEY.FROM_ACC_MANIFEST_ID.eq(valueOf(fromAccManifestId)))
                .orderBy(SEQ_KEY.SEQ_KEY_ID.asc())
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<SeqKeySummaryRecord> getSeqKeySummaryList(Collection<ReleaseId> releaseIdList) {
        if (releaseIdList == null || releaseIdList.isEmpty()) {
            return Collections.emptyList();
        }
        var queryBuilder = new GetSeqKeySummaryQueryBuilder();
        return queryBuilder.select()
                .where(RELEASE.RELEASE_ID.in(valueOf(releaseIdList)))
                .fetch(queryBuilder.mapper());
    }

    private class GetSeqKeySummaryQueryBuilder {

        SelectJoinStep<? extends Record> select() {
            return dslContext().select(SEQ_KEY.SEQ_KEY_ID,
                            SEQ_KEY.FROM_ACC_MANIFEST_ID,
                            SEQ_KEY.ASCC_MANIFEST_ID,
                            SEQ_KEY.BCC_MANIFEST_ID,
                            BCC.ENTITY_TYPE,
                            SEQ_KEY.PREV_SEQ_KEY_ID,
                            SEQ_KEY.NEXT_SEQ_KEY_ID)
                    .from(SEQ_KEY)
                    .join(ACC_MANIFEST).on(SEQ_KEY.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                    .join(RELEASE).on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .leftJoin(BCC_MANIFEST).on(SEQ_KEY.BCC_MANIFEST_ID.eq(BCC_MANIFEST.BCC_MANIFEST_ID))
                    .leftJoin(BCC).on(BCC_MANIFEST.BCC_ID.eq(BCC.BCC_ID));
        }

        private RecordMapper<Record, SeqKeySummaryRecord> mapper() {
            return record -> {
                AccManifestId fromAccManifestId = new AccManifestId(record.get(SEQ_KEY.FROM_ACC_MANIFEST_ID).toBigInteger());
                AsccManifestId asccManifestId = (record.get(SEQ_KEY.ASCC_MANIFEST_ID) != null) ?
                        new AsccManifestId(record.get(SEQ_KEY.ASCC_MANIFEST_ID).toBigInteger()) : null;
                BccManifestId bccManifestId = (record.get(SEQ_KEY.BCC_MANIFEST_ID) != null) ?
                        new BccManifestId(record.get(SEQ_KEY.BCC_MANIFEST_ID).toBigInteger()) : null;

                return new SeqKeySummaryRecord(
                        new SeqKeyId(record.get(SEQ_KEY.SEQ_KEY_ID).toBigInteger()),
                        fromAccManifestId,
                        asccManifestId,
                        bccManifestId,
                        (record.get(BCC.ENTITY_TYPE) != null) ? EntityType.valueOf(record.get(BCC.ENTITY_TYPE)) : null,
                        (record.get(SEQ_KEY.PREV_SEQ_KEY_ID) != null) ?
                                new SeqKeyId(record.get(SEQ_KEY.PREV_SEQ_KEY_ID).toBigInteger()) : null,
                        (record.get(SEQ_KEY.NEXT_SEQ_KEY_ID) != null) ?
                                new SeqKeyId(record.get(SEQ_KEY.NEXT_SEQ_KEY_ID).toBigInteger()) : null);
            };
        }
    }
}
