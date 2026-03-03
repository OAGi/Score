package org.oagi.score.gateway.http.api.xbt_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.SelectJoinStep;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.log_management.model.LogId;
import org.oagi.score.gateway.http.api.log_management.model.LogSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtDetailsRecord;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.repository.XbtQueryRepository;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
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
                            XBT.JBT_202012_MAP,
                            XBT.OPENAPI30_MAP,
                            XBT.OPENAPI31_MAP,
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
                        record.get(XBT.JBT_202012_MAP),
                        record.get(XBT.OPENAPI30_MAP),
                        record.get(XBT.OPENAPI31_MAP),
                        record.get(XBT.AVRO_MAP),

                        record.get(XBT.SCHEMA_DEFINITION));
            };
        }
    }

    @Override
    public XbtDetailsRecord getXbtDetails(XbtManifestId xbtManifestId) {
        if (xbtManifestId == null) {
            return null;
        }

        var queryBuilder = new GetXbtDetailsQueryBuilder();
        return queryBuilder.select()
                .where(XBT_MANIFEST.XBT_MANIFEST_ID.eq(valueOf(xbtManifestId)))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetXbtDetailsQueryBuilder {

        SelectJoinStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(XBT_MANIFEST.XBT_MANIFEST_ID,
                            XBT.XBT_ID,
                            XBT.SUBTYPE_OF_XBT_ID, XBT_MANIFEST.RELEASE_ID,
                            XBT.GUID,
                            XBT.NAME,
                            XBT.BUILTIN_TYPE,
                            XBT.JBT_DRAFT05_MAP,
                            XBT.JBT_202012_MAP,
                            XBT.OPENAPI30_MAP,
                            XBT.OPENAPI31_MAP,
                            XBT.AVRO_MAP,
                            XBT.SCHEMA_DEFINITION,
                            XBT.REVISION_DOC,
                            XBT.IS_DEPRECATED,
                            XBT.CREATION_TIMESTAMP,
                            XBT.LAST_UPDATE_TIMESTAMP,

                            LOG.LOG_ID,
                            LOG.REVISION_NUM,
                            LOG.REVISION_TRACKING_NUM,

                            XBT_MANIFEST.PREV_XBT_MANIFEST_ID,
                            XBT_MANIFEST.NEXT_XBT_MANIFEST_ID
                    ), libraryFields(), releaseFields(), ownerFields(), creatorFields(), updaterFields()))
                    .from(XBT_MANIFEST)
                    .join(XBT).on(XBT_MANIFEST.XBT_ID.eq(XBT.XBT_ID))
                    .join(releaseTable()).on(releaseTablePk().eq(XBT_MANIFEST.RELEASE_ID))
                    .join(libraryTable()).on(libraryTablePk().eq(releaseTable().LIBRARY_ID))
                    .join(ownerTable()).on(ownerTablePk().eq(XBT.OWNER_USER_ID))
                    .join(creatorTable()).on(creatorTablePk().eq(XBT.CREATED_BY))
                    .join(updaterTable()).on(updaterTablePk().eq(XBT.LAST_UPDATED_BY))
                    .leftJoin(LOG).on(XBT_MANIFEST.LOG_ID.eq(LOG.LOG_ID));
        }

        private RecordMapper<Record, XbtDetailsRecord> mapper() {
            return record -> {
                XbtManifestId xbtManifestId = new XbtManifestId(record.get(XBT_MANIFEST.XBT_MANIFEST_ID).toBigInteger());
                XbtId xbtId = new XbtId(record.get(XBT.XBT_ID).toBigInteger());
                XbtId subTypeOfXbtId = (record.get(XBT.SUBTYPE_OF_XBT_ID) != null) ?
                        new XbtId(record.get(XBT.SUBTYPE_OF_XBT_ID).toBigInteger()) : null;
                LibrarySummaryRecord library = fetchLibrarySummary(record);
                ReleaseSummaryRecord release = fetchReleaseSummary(record);

                XbtManifestId subTypeOfXbtManifestId = null;
                if (subTypeOfXbtId != null) {
                    subTypeOfXbtManifestId = new XbtManifestId(
                            dslContext().select(XBT_MANIFEST.XBT_MANIFEST_ID)
                                    .from(XBT_MANIFEST)
                                    .where(and(
                                            XBT_MANIFEST.RELEASE_ID.eq(valueOf(release.releaseId())),
                                            XBT_MANIFEST.XBT_ID.eq(valueOf(subTypeOfXbtId))
                                    ))
                                    .fetchOneInto(BigInteger.class)
                    );
                }

                return new XbtDetailsRecord(
                        library, release,
                        xbtManifestId, xbtId,
                        (subTypeOfXbtManifestId != null) ? getXbtSummary(subTypeOfXbtManifestId) : null,

                        new Guid(record.get(XBT.GUID)),
                        record.get(XBT.NAME),
                        record.get(XBT.BUILTIN_TYPE),

                        record.get(XBT.JBT_DRAFT05_MAP),
                        record.get(XBT.JBT_202012_MAP),
                        record.get(XBT.OPENAPI30_MAP),
                        record.get(XBT.OPENAPI31_MAP),
                        record.get(XBT.AVRO_MAP),

                        record.get(XBT.SCHEMA_DEFINITION),
                        record.get(XBT.REVISION_DOC),

                        (byte) 1 == record.get(XBT.IS_DEPRECATED),
                        CcState.Published,

                        (record.get(LOG.LOG_ID) != null) ? new LogSummaryRecord(
                                new LogId(record.get(LOG.LOG_ID).toBigInteger()),
                                record.get(LOG.REVISION_NUM).intValue(),
                                record.get(LOG.REVISION_TRACKING_NUM).intValue()) : null,

                        fetchOwnerSummary(record),
                        new WhoAndWhen(
                                fetchCreatorSummary(record),
                                toDate(record.get(XBT.CREATION_TIMESTAMP))
                        ),
                        new WhoAndWhen(
                                fetchUpdaterSummary(record),
                                toDate(record.get(XBT.LAST_UPDATE_TIMESTAMP))
                        ),
                        (record.get(XBT_MANIFEST.PREV_XBT_MANIFEST_ID) != null) ?
                                new XbtManifestId(record.get(XBT_MANIFEST.PREV_XBT_MANIFEST_ID).toBigInteger()) : null,
                        (record.get(XBT_MANIFEST.NEXT_XBT_MANIFEST_ID) != null) ?
                                new XbtManifestId(record.get(XBT_MANIFEST.NEXT_XBT_MANIFEST_ID).toBigInteger()) : null);
            };
        }
    }

}
