package org.oagi.score.gateway.http.api.xbt_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.SelectJoinStep;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.repository.XbtQueryRepository;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

public class JooqXbtQueryRepository extends JooqBaseRepository implements XbtQueryRepository {

    public JooqXbtQueryRepository(DSLContext dslContext,
                                  ScoreUser requester,
                                  RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public XbtSummaryRecord getXbtSummary(XbtManifestId xbtManifestId) {
        if (xbtManifestId == null) {
            return null;
        }

        var queryBuilder = new GetXbtSummaryQueryBuilder();
        return queryBuilder.select()
                .where(XBT_MANIFEST.XBT_MANIFEST_ID.eq(valueOf(xbtManifestId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public List<XbtSummaryRecord> getXbtSummaryList(Collection<ReleaseId> releaseIdList) {
        var queryBuilder = new GetXbtSummaryQueryBuilder();
        return queryBuilder.select()
                .where(XBT_MANIFEST.RELEASE_ID.in(valueOf(releaseIdList)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public List<XbtSummaryRecord> getXbtSummaryList(ReleaseId releaseId) {
        if (releaseId == null) {
            return Collections.emptyList();
        }

        var queryBuilder = new GetXbtSummaryQueryBuilder();
        return queryBuilder.select()
                .where(XBT_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .fetch(queryBuilder.mapper());
    }

    private class GetXbtSummaryQueryBuilder {

        SelectJoinStep<? extends org.jooq.Record> select() {
            return dslContext().select(XBT_MANIFEST.XBT_MANIFEST_ID,
                            XBT.XBT_ID,
                            XBT.SUBTYPE_OF_XBT_ID, XBT_MANIFEST.RELEASE_ID,
                            XBT.GUID,
                            XBT.NAME,
                            XBT.BUILTIN_TYPE,
                            XBT.JBT_DRAFT05_MAP,
                            XBT.OPENAPI30_MAP,
                            XBT.AVRO_MAP,
                            XBT.SCHEMA_DEFINITION)
                    .from(XBT_MANIFEST)
                    .join(XBT).on(XBT_MANIFEST.XBT_ID.eq(XBT.XBT_ID));
        }

        private RecordMapper<Record, XbtSummaryRecord> mapper() {
            return record -> {
                XbtManifestId xbtManifestId = new XbtManifestId(record.get(XBT_MANIFEST.XBT_MANIFEST_ID).toBigInteger());
                XbtId xbtId = new XbtId(record.get(XBT.XBT_ID).toBigInteger());
                XbtId subTypeOfXbtId = (record.get(XBT.SUBTYPE_OF_XBT_ID) != null) ?
                        new XbtId(record.get(XBT.SUBTYPE_OF_XBT_ID).toBigInteger()) : null;
                ReleaseId releaseId = new ReleaseId(record.get(XBT_MANIFEST.RELEASE_ID).toBigInteger());

                return new XbtSummaryRecord(xbtManifestId, xbtId,
                        (subTypeOfXbtId != null) ? dslContext().select(XBT_MANIFEST.XBT_MANIFEST_ID)
                                .from(XBT_MANIFEST)
                                .where(and(
                                        XBT_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                                        XBT_MANIFEST.XBT_ID.eq(valueOf(subTypeOfXbtId))
                                ))
                                .fetchOptional(r -> {
                                    if (r.get(XBT_MANIFEST.XBT_MANIFEST_ID) == null) {
                                        return null;
                                    }
                                    return new XbtManifestId(r.get(XBT_MANIFEST.XBT_MANIFEST_ID).toBigInteger());
                                }).orElse(null) : null,
                        new Guid(record.get(XBT.GUID)),
                        record.get(XBT.NAME),
                        record.get(XBT.BUILTIN_TYPE),

                        record.get(XBT.JBT_DRAFT05_MAP),
                        record.get(XBT.OPENAPI30_MAP),
                        record.get(XBT.AVRO_MAP),

                        record.get(XBT.SCHEMA_DEFINITION));
            };
        }
    }

}
