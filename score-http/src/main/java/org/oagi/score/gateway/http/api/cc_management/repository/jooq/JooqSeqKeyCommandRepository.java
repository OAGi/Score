package org.oagi.score.gateway.http.api.cc_management.repository.jooq;

import jakarta.annotation.Nullable;
import org.jooq.DSLContext;
import org.jooq.UpdateSetStep;
import org.jooq.UpdateWhereStep;
import org.oagi.score.gateway.http.api.cc_management.model.AsccpOrBccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.MoveTo;
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeyId;
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeySummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.repository.SeqKeyCommandRepository;
import org.oagi.score.gateway.http.api.cc_management.service.SeqKeyHandler;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.SeqKeyRecord;

import static org.jooq.impl.DSL.select;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

public class JooqSeqKeyCommandRepository
        extends JooqBaseRepository
        implements SeqKeyCommandRepository {

    public JooqSeqKeyCommandRepository(DSLContext dslContext,
                                       ScoreUser requester,
                                       RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public SeqKeyId create(AccManifestId fromAccManifestId, AsccManifestId asccManifestId) {
        SeqKeyRecord record = new SeqKeyRecord();
        record.setFromAccManifestId(valueOf(fromAccManifestId));
        record.setAsccManifestId(valueOf(asccManifestId));
        SeqKeyId seqKeyId = new SeqKeyId(
                dslContext().insertInto(SEQ_KEY)
                        .set(record)
                        .returning(SEQ_KEY.SEQ_KEY_ID)
                        .fetchOne().getSeqKeyId().toBigInteger());

        dslContext().update(ASCC_MANIFEST)
                .set(ASCC_MANIFEST.SEQ_KEY_ID, valueOf(seqKeyId))
                .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(valueOf(asccManifestId)))
                .execute();

        return seqKeyId;
    }

    @Override
    public SeqKeyId create(AccManifestId fromAccManifestId, BccManifestId bccManifestId) {
        SeqKeyRecord record = new SeqKeyRecord();
        record.setFromAccManifestId(valueOf(fromAccManifestId));
        record.setBccManifestId(valueOf(bccManifestId));
        SeqKeyId seqKeyId = new SeqKeyId(
                dslContext().insertInto(SEQ_KEY)
                        .set(record)
                        .returning(SEQ_KEY.SEQ_KEY_ID)
                        .fetchOne().getSeqKeyId().toBigInteger());

        dslContext().update(BCC_MANIFEST)
                .set(BCC_MANIFEST.SEQ_KEY_ID, valueOf(seqKeyId))
                .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(valueOf(bccManifestId)))
                .execute();

        return seqKeyId;
    }

    private void setPrev(SeqKeyId key, SeqKeyId prev) {
        if (key != null) {
            if (prev != null) {
                dslContext().update(SEQ_KEY)
                        .set(SEQ_KEY.PREV_SEQ_KEY_ID, valueOf(prev))
                        .where(SEQ_KEY.SEQ_KEY_ID.eq(valueOf(key)))
                        .execute();
            } else {
                dslContext().update(SEQ_KEY)
                        .setNull(SEQ_KEY.PREV_SEQ_KEY_ID)
                        .where(SEQ_KEY.SEQ_KEY_ID.eq(valueOf(key)))
                        .execute();
            }
        }
    }

    private void setNext(SeqKeyId key, SeqKeyId next) {
        if (key != null) {
            if (next != null) {
                dslContext().update(SEQ_KEY)
                        .set(SEQ_KEY.NEXT_SEQ_KEY_ID, valueOf(next))
                        .where(SEQ_KEY.SEQ_KEY_ID.eq(valueOf(key)))
                        .execute();
            } else {
                dslContext().update(SEQ_KEY)
                        .setNull(SEQ_KEY.NEXT_SEQ_KEY_ID)
                        .where(SEQ_KEY.SEQ_KEY_ID.eq(valueOf(key)))
                        .execute();
            }
        }
    }

    private void brokeLinks(SeqKeySummaryRecord seqKey) {
        SeqKeyId prev = seqKey.prevSeqKeyId();
        SeqKeyId next = seqKey.nextSeqKeyId();

        setNext(prev, next);
        setPrev(next, prev);

        setPrev(seqKey.seqKeyId(), null);
        setNext(seqKey.seqKeyId(), null);
    }

    @Override
    public void move(AccManifestId accManifestId,
                     AsccpOrBccpManifestId item, @Nullable AsccpOrBccpManifestId after) {

        var query = repositoryFactory().accQueryRepository(requester());
        SeqKeyHandler seqKeyHandler;
        if (item.asccpManifestId() != null) {
            AsccSummaryRecord ascc = query.getAsccSummary(accManifestId, item.asccpManifestId());
            if (ascc == null) {
                throw new IllegalArgumentException();
            }

            seqKeyHandler = seqKeyHandler(ascc);
        } else {
            BccSummaryRecord bcc = query.getBccSummary(accManifestId, item.bccpManifestId());
            if (bcc == null) {
                throw new IllegalArgumentException();
            }

            seqKeyHandler = seqKeyHandler(bcc);
        }

        if (after == null) {
            seqKeyHandler.moveTo(MoveTo.FIRST);
        } else {
            SeqKeySummaryRecord seqKey;
            if (after.asccpManifestId() != null) {
                AsccpManifestId asccpManifestId = after.asccpManifestId();
                AsccSummaryRecord ascc = query.getAsccSummary(accManifestId, asccpManifestId);
                if (ascc == null) {
                    throw new IllegalArgumentException();
                }

                seqKey = repositoryFactory().seqKeyQueryRepository(requester()).getSeqKeySummary(ascc.seqKeyId());
            } else {
                BccpManifestId bccpManifestId = after.bccpManifestId();
                BccSummaryRecord bcc = query.getBccSummary(accManifestId, bccpManifestId);
                if (bcc == null) {
                    throw new IllegalArgumentException();
                }

                seqKey = repositoryFactory().seqKeyQueryRepository(requester()).getSeqKeySummary(bcc.seqKeyId());
            }

            seqKeyHandler.moveAfter(seqKey);
        }
    }

    public void moveAfter(SeqKeySummaryRecord item, SeqKeySummaryRecord after) {
        if (item == null) {
            throw new IllegalArgumentException("`item` must not be null.");
        }

        if (after == null) {
            throw new IllegalArgumentException("`after` must not be null.");
        }

        if (item.prevSeqKeyId() != null) {
            // if the request is already fulfilled
            if (item.prevSeqKeyId().equals(after.seqKeyId())) {
                return;
            }
        }

        brokeLinks(item);

        // DO NOT change orders of executions.

        SeqKeyId currentSeqKeyId = item.seqKeyId();
        SeqKeyId afterSeqKeyId = after.seqKeyId();
        SeqKeyId prev = after.seqKeyId();
        SeqKeyId next = (after.nextSeqKeyId() != null) ? after.nextSeqKeyId() : null;

        setPrev(currentSeqKeyId, prev);
        if (next != null) {
            setNext(currentSeqKeyId, next);
        }

        setNext(afterSeqKeyId, currentSeqKeyId);
        if (next != null) {
            setPrev(next, currentSeqKeyId);
        }
    }

    @Override
    public boolean updatePrev(SeqKeyId current, SeqKeyId prev) {
        UpdateSetStep step = dslContext().update(SEQ_KEY);
        if (prev == null) {
            step = step.setNull(SEQ_KEY.PREV_SEQ_KEY_ID);
        } else {
            step = step.set(SEQ_KEY.PREV_SEQ_KEY_ID, valueOf(prev));
        }

        int numOfUpdatedRecords = ((UpdateWhereStep) step)
                .where(SEQ_KEY.SEQ_KEY_ID.eq(valueOf(current)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean updateNext(SeqKeyId current, SeqKeyId next) {
        UpdateSetStep step = dslContext().update(SEQ_KEY);
        if (next == null) {
            step = step.setNull(SEQ_KEY.NEXT_SEQ_KEY_ID);
        } else {
            step = step.set(SEQ_KEY.NEXT_SEQ_KEY_ID, valueOf(next));
        }

        int numOfUpdatedRecords = ((UpdateWhereStep) step)
                .where(SEQ_KEY.SEQ_KEY_ID.eq(valueOf(current)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean delete(SeqKeyId seqKeyId) {
        var query = repositoryFactory().seqKeyQueryRepository(requester());
        SeqKeySummaryRecord current = query.getSeqKeySummary(seqKeyId);

        // disconnect links between prev and next
        {
            SeqKeySummaryRecord prev = query.getSeqKeySummary(current.prevSeqKeyId());
            SeqKeySummaryRecord next = query.getSeqKeySummary(current.nextSeqKeyId());

            if (prev != null) {
                updateNext(prev.seqKeyId(), current.nextSeqKeyId());
            }

            if (next != null) {
                updatePrev(next.seqKeyId(), current.prevSeqKeyId());
            }
        }

        if (current.asccManifestId() != null) {
            dslContext().update(ASCC_MANIFEST)
                    .setNull(ASCC_MANIFEST.SEQ_KEY_ID)
                    .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(valueOf(current.asccManifestId())))
                    .execute();
        } else if (current.bccManifestId() != null) {
            dslContext().update(BCC_MANIFEST)
                    .setNull(BCC_MANIFEST.SEQ_KEY_ID)
                    .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(valueOf(current.bccManifestId())))
                    .execute();
        }

        int numOfDeletedRecords = dslContext().deleteFrom(SEQ_KEY)
                .where(SEQ_KEY.SEQ_KEY_ID.eq(valueOf(seqKeyId)))
                .execute();
        return numOfDeletedRecords == 1;
    }

    @Override
    public void delete(ReleaseId releaseId) {
        dslContext().update(ASCC_MANIFEST).setNull(ASCC_MANIFEST.SEQ_KEY_ID)
                .where(ASCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(BCC_MANIFEST).setNull(BCC_MANIFEST.SEQ_KEY_ID)
                .where(BCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(SEQ_KEY.join(ASCC_MANIFEST).on(SEQ_KEY.ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.ASCC_MANIFEST_ID)))
                .setNull(SEQ_KEY.PREV_SEQ_KEY_ID)
                .setNull(SEQ_KEY.NEXT_SEQ_KEY_ID)
                .where(ASCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(SEQ_KEY.join(BCC_MANIFEST).on(SEQ_KEY.BCC_MANIFEST_ID.eq(BCC_MANIFEST.BCC_MANIFEST_ID)))
                .setNull(SEQ_KEY.PREV_SEQ_KEY_ID)
                .setNull(SEQ_KEY.NEXT_SEQ_KEY_ID)
                .where(BCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().deleteFrom(SEQ_KEY).where(SEQ_KEY.ASCC_MANIFEST_ID.in(
                        select(ASCC_MANIFEST.ASCC_MANIFEST_ID)
                                .from(ASCC_MANIFEST)
                                .where(ASCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))))
                .execute();
        dslContext().deleteFrom(SEQ_KEY).where(SEQ_KEY.BCC_MANIFEST_ID.in(
                        select(BCC_MANIFEST.BCC_MANIFEST_ID)
                                .from(BCC_MANIFEST)
                                .where(BCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))))
                .execute();
    }

}
