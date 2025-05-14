package org.oagi.score.gateway.http.api.tag_management.repository.jooq;

import org.apache.commons.lang3.tuple.Pair;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.tag_management.model.*;
import org.oagi.score.gateway.http.api.tag_management.repository.TagQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

/**
 * Jooq-based implementation of the {@link TagQueryRepository} for querying tag-related data.
 */
public class JooqTagQueryRepository extends JooqBaseRepository implements TagQueryRepository {

    /**
     * Constructs a new {@link JooqTagQueryRepository}.
     *
     * @param dslContext        The JOOQ DSL context for executing database queries.
     * @param requester         The user making the request.
     * @param repositoryFactory The factory to create other repositories.
     */
    public JooqTagQueryRepository(DSLContext dslContext, ScoreUser requester,
                                  RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public boolean exists(TagId tagId) {
        if (tagId == null) {
            return false;
        }

        return dslContext().selectCount().from(TAG)
                .where(TAG.TAG_ID.eq(valueOf(tagId)))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    @Override
    public List<TagSummaryRecord> getTagSummaryList() {
        var queryBuilder = new GetTagSummaryQueryBuilder();
        return queryBuilder.select()
                .fetch(queryBuilder.mapper());
    }

    public List<TagSummaryRecord> getTagSummaryList(AccManifestId accManifestId) {
        if (accManifestId == null) {
            return Collections.emptyList();
        }

        var queryBuilder = new GetTagSummaryQueryBuilder();
        return queryBuilder.select()
                .join(ACC_MANIFEST_TAG).on(TAG.TAG_ID.eq(ACC_MANIFEST_TAG.TAG_ID))
                .where(ACC_MANIFEST_TAG.ACC_MANIFEST_ID.eq(valueOf(accManifestId)))
                .fetch(queryBuilder.mapper());
    }

    public List<TagSummaryRecord> getTagSummaryList(AsccpManifestId asccpManifestId) {
        if (asccpManifestId == null) {
            return Collections.emptyList();
        }

        var queryBuilder = new GetTagSummaryQueryBuilder();
        return queryBuilder.select()
                .join(ASCCP_MANIFEST_TAG).on(TAG.TAG_ID.eq(ASCCP_MANIFEST_TAG.TAG_ID))
                .where(ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID.eq(valueOf(asccpManifestId)))
                .fetch(queryBuilder.mapper());
    }

    public List<TagSummaryRecord> getTagSummaryList(BccpManifestId bccpManifestId) {
        if (bccpManifestId == null) {
            return Collections.emptyList();
        }

        var queryBuilder = new GetTagSummaryQueryBuilder();
        return queryBuilder.select()
                .join(BCCP_MANIFEST_TAG).on(TAG.TAG_ID.eq(BCCP_MANIFEST_TAG.TAG_ID))
                .where(BCCP_MANIFEST_TAG.BCCP_MANIFEST_ID.eq(valueOf(bccpManifestId)))
                .fetch(queryBuilder.mapper());
    }

    public List<TagSummaryRecord> getTagSummaryList(DtManifestId dtManifestId) {
        if (dtManifestId == null) {
            return Collections.emptyList();
        }

        var queryBuilder = new GetTagSummaryQueryBuilder();
        return queryBuilder.select()
                .join(DT_MANIFEST_TAG).on(TAG.TAG_ID.eq(DT_MANIFEST_TAG.TAG_ID))
                .where(DT_MANIFEST_TAG.DT_MANIFEST_ID.eq(valueOf(dtManifestId)))
                .fetch(queryBuilder.mapper());
    }

    @Override
    public TagSummaryRecord getTagSummary(TagId tagId) {
        if (tagId == null) {
            return null;
        }

        var queryBuilder = new GetTagSummaryQueryBuilder();
        return queryBuilder.select()
                .where(TAG.TAG_ID.eq(valueOf(tagId)))
                .fetchOne(queryBuilder.mapper());
    }

    @Override
    public TagSummaryRecord getTagSummaryByName(String tagName) {
        if (tagName == null) {
            return null;
        }

        var queryBuilder = new GetTagSummaryQueryBuilder();
        return queryBuilder.select()
                .where(TAG.NAME.eq(tagName))
                .fetchOne(queryBuilder.mapper());
    }

    private class GetTagSummaryQueryBuilder {

        SelectJoinStep<? extends Record> select() {
            return dslContext().select(TAG.TAG_ID,
                            TAG.NAME,
                            TAG.TEXT_COLOR,
                            TAG.BACKGROUND_COLOR)
                    .from(TAG);
        }

        RecordMapper<Record, TagSummaryRecord> mapper() {
            return record -> new TagSummaryRecord(
                    new TagId(record.getValue(TAG.TAG_ID).toBigInteger()),
                    record.getValue(TAG.NAME),
                    record.getValue(TAG.TEXT_COLOR),
                    record.getValue(TAG.BACKGROUND_COLOR));
        }
    }

    @Override
    public List<TagDetailsRecord> getTagDetailsList() {
        var queryBuilder = new GetTagDetailsQueryBuilder();
        return queryBuilder.select()
                .fetch(queryBuilder.mapper());
    }

    private class GetTagDetailsQueryBuilder {

        SelectOnConditionStep<? extends org.jooq.Record> select() {
            return dslContext().select(concat(fields(
                            TAG.TAG_ID,
                            TAG.NAME,
                            TAG.DESCRIPTION,
                            TAG.TEXT_COLOR,
                            TAG.BACKGROUND_COLOR,
                            TAG.CREATION_TIMESTAMP,
                            TAG.LAST_UPDATE_TIMESTAMP
                    ), creatorFields(), updaterFields()))
                    .from(TAG)
                    .join(creatorTable()).on(TAG.CREATED_BY.eq(creatorTablePk()))
                    .join(updaterTable()).on(TAG.LAST_UPDATED_BY.eq(updaterTablePk()));
        }

        RecordMapper<Record, TagDetailsRecord> mapper() {
            return record -> new TagDetailsRecord(
                    new TagId(record.getValue(TAG.TAG_ID).toBigInteger()),
                    record.getValue(TAG.NAME),
                    record.getValue(TAG.DESCRIPTION),
                    record.getValue(TAG.TEXT_COLOR),
                    record.getValue(TAG.BACKGROUND_COLOR),
                    new WhoAndWhen(
                            fetchCreatorSummary(record),
                            toDate(record.get(TAG.CREATION_TIMESTAMP))
                    ),
                    new WhoAndWhen(
                            fetchUpdaterSummary(record),
                            toDate(record.get(TAG.LAST_UPDATE_TIMESTAMP))
                    )
            );
        }
    }

    @Override
    public boolean hasTag(TagId tagId, AccManifestId accManifestId) {

        if (tagId == null || accManifestId == null) {
            return false;
        }

        return dslContext().selectCount()
                .from(ACC_MANIFEST_TAG)
                .where(and(
                        ACC_MANIFEST_TAG.ACC_MANIFEST_ID.eq(valueOf(accManifestId)),
                        ACC_MANIFEST_TAG.TAG_ID.eq(valueOf(tagId))
                ))
                .fetchOneInto(Integer.class) > 0;
    }

    @Override
    public boolean hasTag(TagId tagId, AsccpManifestId asccpManifestId) {

        if (tagId == null || asccpManifestId == null) {
            return false;
        }

        return dslContext().selectCount()
                .from(ASCCP_MANIFEST_TAG)
                .where(and(
                        ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID.eq(valueOf(asccpManifestId)),
                        ASCCP_MANIFEST_TAG.TAG_ID.eq(valueOf(tagId))
                ))
                .fetchOneInto(Integer.class) > 0;
    }

    @Override
    public boolean hasTag(TagId tagId, BccpManifestId bccpManifestId) {

        if (tagId == null || bccpManifestId == null) {
            return false;
        }

        return dslContext().selectCount()
                .from(BCCP_MANIFEST_TAG)
                .where(and(
                        BCCP_MANIFEST_TAG.BCCP_MANIFEST_ID.eq(valueOf(bccpManifestId)),
                        BCCP_MANIFEST_TAG.TAG_ID.eq(valueOf(tagId))
                ))
                .fetchOneInto(Integer.class) > 0;
    }

    @Override
    public boolean hasTag(TagId tagId, DtManifestId dtManifestId) {

        if (tagId == null || dtManifestId == null) {
            return false;
        }

        return dslContext().selectCount()
                .from(DT_MANIFEST_TAG)
                .where(and(
                        DT_MANIFEST_TAG.DT_MANIFEST_ID.eq(valueOf(dtManifestId)),
                        DT_MANIFEST_TAG.TAG_ID.eq(valueOf(tagId))
                ))
                .fetchOneInto(Integer.class) > 0;
    }


    @Override
    public List<AccManifestTagSummaryRecord> getAccManifestTagList(Collection<ReleaseId> releaseIdList) {
        return dslContext().select(ACC_MANIFEST_TAG.ACC_MANIFEST_ID, ACC_MANIFEST_TAG.TAG_ID)
                .from(ACC_MANIFEST_TAG)
                .join(ACC_MANIFEST).on(ACC_MANIFEST_TAG.ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                .where(ACC_MANIFEST.RELEASE_ID.in(valueOf(releaseIdList)))
                .fetch(record -> new AccManifestTagSummaryRecord(
                        new AccManifestId(record.get(ACC_MANIFEST_TAG.ACC_MANIFEST_ID).toBigInteger()),
                        new TagId(record.get(ACC_MANIFEST_TAG.TAG_ID).toBigInteger())
                ));
    }

    @Override
    public List<AsccpManifestTagSummaryRecord> getAsccpManifestTagList(Collection<ReleaseId> releaseIdList) {
        return dslContext().select(ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID, ASCCP_MANIFEST_TAG.TAG_ID)
                .from(ASCCP_MANIFEST_TAG)
                .join(ASCCP_MANIFEST).on(ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .where(ASCCP_MANIFEST.RELEASE_ID.in(valueOf(releaseIdList)))
                .fetch(record -> new AsccpManifestTagSummaryRecord(
                        new AsccpManifestId(record.get(ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID).toBigInteger()),
                        new TagId(record.get(ASCCP_MANIFEST_TAG.TAG_ID).toBigInteger())
                ));
    }

    @Override
    public List<BccpManifestTagSummaryRecord> getBccpManifestTagList(Collection<ReleaseId> releaseIdList) {
        return dslContext().select(BCCP_MANIFEST_TAG.BCCP_MANIFEST_ID, BCCP_MANIFEST_TAG.TAG_ID)
                .from(BCCP_MANIFEST_TAG)
                .join(BCCP_MANIFEST).on(BCCP_MANIFEST_TAG.BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.BCCP_MANIFEST_ID))
                .where(BCCP_MANIFEST.RELEASE_ID.in(valueOf(releaseIdList)))
                .fetch(record -> new BccpManifestTagSummaryRecord(
                        new BccpManifestId(record.get(BCCP_MANIFEST_TAG.BCCP_MANIFEST_ID).toBigInteger()),
                        new TagId(record.get(BCCP_MANIFEST_TAG.TAG_ID).toBigInteger())
                ));
    }

    @Override
    public List<DtManifestTagSummaryRecord> getDtManifestTagList(Collection<ReleaseId> releaseIdList) {
        return dslContext().select(DT_MANIFEST_TAG.DT_MANIFEST_ID, DT_MANIFEST_TAG.TAG_ID)
                .from(DT_MANIFEST_TAG)
                .join(DT_MANIFEST).on(DT_MANIFEST_TAG.DT_MANIFEST_ID.eq(DT_MANIFEST.DT_MANIFEST_ID))
                .where(DT_MANIFEST.RELEASE_ID.in(valueOf(releaseIdList)))
                .fetch(record -> new DtManifestTagSummaryRecord(
                        new DtManifestId(record.get(DT_MANIFEST_TAG.DT_MANIFEST_ID).toBigInteger()),
                        new TagId(record.get(DT_MANIFEST_TAG.TAG_ID).toBigInteger())
                ));
    }

    @Override
    public Map<AccManifestId, List<TagSummaryRecord>> getTagAccManifestMap(ReleaseId releaseId) {

        if (releaseId == null) {
            return Collections.emptyMap();
        }

        return dslContext().select(ACC_MANIFEST.ACC_MANIFEST_ID,
                        TAG.TAG_ID, TAG.NAME, TAG.TEXT_COLOR, TAG.BACKGROUND_COLOR)
                .from(TAG)
                .join(ACC_MANIFEST_TAG).on(TAG.TAG_ID.eq(ACC_MANIFEST_TAG.TAG_ID))
                .join(ACC_MANIFEST).on(ACC_MANIFEST_TAG.ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                .where(ACC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .fetchGroups(
                        e -> new AccManifestId(e.get(ACC_MANIFEST.ACC_MANIFEST_ID).toBigInteger()),
                        new GetTagSummaryQueryBuilder().mapper()
                );
    }

    @Override
    public Map<AsccpManifestId, List<TagSummaryRecord>> getTagAsccpManifestMap(ReleaseId releaseId) {

        if (releaseId == null) {
            return Collections.emptyMap();
        }

        return dslContext().select(ASCCP_MANIFEST.ASCCP_MANIFEST_ID,
                        TAG.TAG_ID, TAG.NAME, TAG.TEXT_COLOR, TAG.BACKGROUND_COLOR)
                .from(TAG)
                .join(ASCCP_MANIFEST_TAG).on(TAG.TAG_ID.eq(ASCCP_MANIFEST_TAG.TAG_ID))
                .join(ASCCP_MANIFEST).on(ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .where(ASCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .fetchGroups(
                        e -> new AsccpManifestId(e.get(ASCCP_MANIFEST.ASCCP_MANIFEST_ID).toBigInteger()),
                        new GetTagSummaryQueryBuilder().mapper()
                );
    }

    @Override
    public Map<BccpManifestId, List<TagSummaryRecord>> getTagBccpManifestMap(ReleaseId releaseId) {

        if (releaseId == null) {
            return Collections.emptyMap();
        }

        return dslContext().select(BCCP_MANIFEST.BCCP_MANIFEST_ID,
                        TAG.TAG_ID, TAG.NAME, TAG.TEXT_COLOR, TAG.BACKGROUND_COLOR)
                .from(TAG)
                .join(BCCP_MANIFEST_TAG).on(TAG.TAG_ID.eq(BCCP_MANIFEST_TAG.TAG_ID))
                .join(BCCP_MANIFEST).on(BCCP_MANIFEST_TAG.BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.BCCP_MANIFEST_ID))
                .where(BCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .fetchGroups(
                        e -> new BccpManifestId(e.get(BCCP_MANIFEST.BCCP_MANIFEST_ID).toBigInteger()),
                        new GetTagSummaryQueryBuilder().mapper()
                );
    }

    @Override
    public Map<DtManifestId, List<TagSummaryRecord>> getTagDtManifestMap(ReleaseId releaseId) {

        if (releaseId == null) {
            return Collections.emptyMap();
        }

        return dslContext().select(DT_MANIFEST.DT_MANIFEST_ID,
                        TAG.TAG_ID, TAG.NAME, TAG.TEXT_COLOR, TAG.BACKGROUND_COLOR)
                .from(TAG)
                .join(DT_MANIFEST_TAG).on(TAG.TAG_ID.eq(DT_MANIFEST_TAG.TAG_ID))
                .join(DT_MANIFEST).on(DT_MANIFEST_TAG.DT_MANIFEST_ID.eq(DT_MANIFEST.DT_MANIFEST_ID))
                .where(DT_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .fetchGroups(
                        e -> new DtManifestId(e.get(DT_MANIFEST.DT_MANIFEST_ID).toBigInteger()),
                        new GetTagSummaryQueryBuilder().mapper()
                );
    }

    @Override
    public Map<Pair<CcType, ManifestId>, List<TagSummaryRecord>> getTagSummariesByPairsOfTypeAndManifestId(
            List<Pair<CcType, ManifestId>> pairsOfTypeAndManifestId) {

        if (pairsOfTypeAndManifestId == null) {
            return Collections.emptyMap();
        }

        List<ULong> accManifestIdList = pairsOfTypeAndManifestId.stream().filter(e -> CcType.ACC.equals(e.getKey()))
                .map(e -> valueOf(e.getValue())).collect(Collectors.toList());
        List<ULong> asccpManifestIdList = pairsOfTypeAndManifestId.stream().filter(e -> CcType.ASCCP.equals(e.getKey()))
                .map(e -> valueOf(e.getValue())).collect(Collectors.toList());
        List<ULong> bccpManifestIdList = pairsOfTypeAndManifestId.stream().filter(e -> CcType.BCCP.equals(e.getKey()))
                .map(e -> valueOf(e.getValue())).collect(Collectors.toList());
        List<ULong> dtManifestIdList = pairsOfTypeAndManifestId.stream().filter(e -> CcType.DT.equals(e.getKey()))
                .map(e -> valueOf(e.getValue())).collect(Collectors.toList());

        SelectOrderByStep<Record> select = null;
        if (!accManifestIdList.isEmpty()) {
            SelectConditionStep step = dslContext().select(val("ACC").as("type"),
                            ACC_MANIFEST_TAG.ACC_MANIFEST_ID.as("manifestId"),
                            TAG.TAG_ID, TAG.NAME, TAG.TEXT_COLOR, TAG.BACKGROUND_COLOR)
                    .from(TAG)
                    .join(ACC_MANIFEST_TAG).on(TAG.TAG_ID.eq(ACC_MANIFEST_TAG.TAG_ID))
                    .where(ACC_MANIFEST_TAG.ACC_MANIFEST_ID.in(accManifestIdList));
            select = (select != null) ? select.union(step) : step;
        }
        if (!asccpManifestIdList.isEmpty()) {
            SelectConditionStep step = dslContext().select(val("ASCCP").as("type"),
                            ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID.as("manifestId"),
                            TAG.TAG_ID, TAG.NAME, TAG.TEXT_COLOR, TAG.BACKGROUND_COLOR)
                    .from(TAG)
                    .join(ASCCP_MANIFEST_TAG).on(TAG.TAG_ID.eq(ASCCP_MANIFEST_TAG.TAG_ID))
                    .where(ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID.in(asccpManifestIdList));
            select = (select != null) ? select.union(step) : step;
        }
        if (!bccpManifestIdList.isEmpty()) {
            SelectConditionStep step = dslContext().select(val("BCCP").as("type"),
                            BCCP_MANIFEST_TAG.BCCP_MANIFEST_ID.as("manifestId"),
                            TAG.TAG_ID, TAG.NAME, TAG.TEXT_COLOR, TAG.BACKGROUND_COLOR)
                    .from(TAG)
                    .join(BCCP_MANIFEST_TAG).on(TAG.TAG_ID.eq(BCCP_MANIFEST_TAG.TAG_ID))
                    .where(BCCP_MANIFEST_TAG.BCCP_MANIFEST_ID.in(bccpManifestIdList));
            select = (select != null) ? select.union(step) : step;
        }
        if (!dtManifestIdList.isEmpty()) {
            SelectConditionStep step = dslContext().select(val("DT").as("type"),
                            DT_MANIFEST_TAG.DT_MANIFEST_ID.as("manifestId"),
                            TAG.TAG_ID, TAG.NAME, TAG.TEXT_COLOR, TAG.BACKGROUND_COLOR)
                    .from(TAG)
                    .join(DT_MANIFEST_TAG).on(TAG.TAG_ID.eq(DT_MANIFEST_TAG.TAG_ID))
                    .where(DT_MANIFEST_TAG.DT_MANIFEST_ID.in(dtManifestIdList));
            select = (select != null) ? select.union(step) : step;
        }

        if (select == null) {
            return Collections.emptyMap();
        }

        Map<Pair<CcType, ManifestId>, List<TagSummaryRecord>> tagMap = new HashMap<>();
        for (Record record : select.fetch()) {
            CcType ccType = CcType.valueOf(record.get(field("type"), String.class));
            ManifestId manifestId;
            switch (ccType) {
                case ACC:
                    manifestId = new AccManifestId(record.get(field("manifestId"), ULong.class).toBigInteger());
                    break;
                case ASCCP:
                    manifestId = new AsccpManifestId(record.get(field("manifestId"), ULong.class).toBigInteger());
                    break;
                case BCCP:
                    manifestId = new BccpManifestId(record.get(field("manifestId"), ULong.class).toBigInteger());
                    break;
                case DT:
                    manifestId = new DtManifestId(record.get(field("manifestId"), ULong.class).toBigInteger());
                    break;
                default:
                    throw new IllegalStateException();
            }
            Pair<CcType, ManifestId> key = Pair.of(ccType, manifestId);
            TagSummaryRecord tagSummary = new GetTagSummaryQueryBuilder().mapper().map(record);
            List<TagSummaryRecord> tagList;
            if (tagMap.containsKey(key)) {
                tagList = tagMap.get(key);
            } else {
                tagList = new ArrayList<>();
                tagMap.put(key, tagList);
            }
            tagList.add(tagSummary);
        }

        return tagMap;
    }

}
