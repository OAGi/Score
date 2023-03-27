package org.oagi.score.repo.component.tag;

import org.apache.commons.lang3.tuple.Pair;
import org.jooq.Record;
import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.cc_management.data.CcType;
import org.oagi.score.gateway.http.api.tag_management.data.ShortTag;
import org.oagi.score.gateway.http.api.tag_management.data.Tag;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Repository
public class TagRepository {

    @Autowired
    private DSLContext dslContext;

    private RecordMapper<org.jooq.Record, Tag> mapperForTag() {
        return record -> {
            Tag tag = new Tag();
            tag.setTagId(record.get(TAG.TAG_ID).toBigInteger());
            tag.setName(record.get(TAG.NAME));
            tag.setDescription(record.get(TAG.DESCRIPTION));
            tag.setTextColor(record.get(TAG.TEXT_COLOR));
            tag.setBackgroundColor(record.get(TAG.BACKGROUND_COLOR));
            tag.setCreatedBy(record.get(TAG.CREATED_BY).toBigInteger());
            tag.setLastUpdatedBy(record.get(TAG.LAST_UPDATED_BY).toBigInteger());
            tag.setCreationTimestamp(record.get(TAG.CREATION_TIMESTAMP));
            tag.setLastUpdateTimestamp(record.get(TAG.LAST_UPDATE_TIMESTAMP));
            return tag;
        };
    }

    public List<Tag> getTags() {
        return dslContext.selectFrom(TAG)
                .orderBy(TAG.TAG_ID)
                .fetch(mapperForTag());
    }

    public Tag getTagByName(String name) {
        return dslContext.selectFrom(TAG)
                .where(TAG.NAME.eq(name))
                .fetchOne(mapperForTag());
    }

    public List<Tag> getTagsByAccManifestId(BigInteger accManifestId) {
        return dslContext.select(TAG.fields())
                .from(TAG)
                .join(ACC_MANIFEST_TAG).on(TAG.TAG_ID.eq(ACC_MANIFEST_TAG.TAG_ID))
                .where(ACC_MANIFEST_TAG.ACC_MANIFEST_ID.eq(ULong.valueOf(accManifestId)))
                .orderBy(ACC_MANIFEST_TAG.CREATION_TIMESTAMP)
                .fetch(mapperForTag());
    }

    public List<Tag> getTagsByAsccpManifestId(BigInteger asccpManifestId) {
        return dslContext.select(TAG.fields())
                .from(TAG)
                .join(ASCCP_MANIFEST_TAG).on(TAG.TAG_ID.eq(ASCCP_MANIFEST_TAG.TAG_ID))
                .where(ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID.eq(ULong.valueOf(asccpManifestId)))
                .orderBy(ASCCP_MANIFEST_TAG.CREATION_TIMESTAMP)
                .fetch(mapperForTag());
    }

    public List<Tag> getTagsByBccpManifestId(BigInteger bccpManifestId) {
        return dslContext.select(TAG.fields())
                .from(TAG)
                .join(BCCP_MANIFEST_TAG).on(TAG.TAG_ID.eq(BCCP_MANIFEST_TAG.TAG_ID))
                .where(BCCP_MANIFEST_TAG.BCCP_MANIFEST_ID.eq(ULong.valueOf(bccpManifestId)))
                .orderBy(BCCP_MANIFEST_TAG.CREATION_TIMESTAMP)
                .fetch(mapperForTag());
    }

    public List<Tag> getTagsByDtManifestId(BigInteger dtManifestId) {
        return dslContext.select(TAG.fields())
                .from(TAG)
                .join(DT_MANIFEST_TAG).on(TAG.TAG_ID.eq(DT_MANIFEST_TAG.TAG_ID))
                .where(DT_MANIFEST_TAG.DT_MANIFEST_ID.eq(ULong.valueOf(dtManifestId)))
                .orderBy(DT_MANIFEST_TAG.CREATION_TIMESTAMP)
                .fetch(mapperForTag());
    }

    private RecordMapper<org.jooq.Record, ShortTag> mapperForShortTag() {
        return record -> {
            ShortTag shortTag = new ShortTag();
            shortTag.setTagId(record.get(TAG.TAG_ID).toBigInteger());
            shortTag.setName(record.get(TAG.NAME));
            shortTag.setTextColor(record.get(TAG.TEXT_COLOR));
            shortTag.setBackgroundColor(record.get(TAG.BACKGROUND_COLOR));
            return shortTag;
        };
    }

    public List<ShortTag> getShortTagsByAccManifestId(BigInteger accManifestId) {
        return dslContext.select(TAG.TAG_ID, TAG.NAME, TAG.TEXT_COLOR, TAG.BACKGROUND_COLOR)
                .from(TAG)
                .join(ACC_MANIFEST_TAG).on(TAG.TAG_ID.eq(ACC_MANIFEST_TAG.TAG_ID))
                .where(ACC_MANIFEST_TAG.ACC_MANIFEST_ID.eq(ULong.valueOf(accManifestId)))
                .orderBy(ACC_MANIFEST_TAG.CREATION_TIMESTAMP)
                .fetch(mapperForShortTag());
    }

    public List<ShortTag> getShortTagsByAsccpManifestId(BigInteger asccpManifestId) {
        return dslContext.select(TAG.TAG_ID, TAG.NAME, TAG.TEXT_COLOR, TAG.BACKGROUND_COLOR)
                .from(TAG)
                .join(ASCCP_MANIFEST_TAG).on(TAG.TAG_ID.eq(ASCCP_MANIFEST_TAG.TAG_ID))
                .where(ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID.eq(ULong.valueOf(asccpManifestId)))
                .orderBy(ASCCP_MANIFEST_TAG.CREATION_TIMESTAMP)
                .fetch(mapperForShortTag());
    }

    public List<ShortTag> getShortTagsByBccpManifestId(BigInteger bccpManifestId) {
        return dslContext.select(TAG.TAG_ID, TAG.NAME, TAG.TEXT_COLOR, TAG.BACKGROUND_COLOR)
                .from(TAG)
                .join(BCCP_MANIFEST_TAG).on(TAG.TAG_ID.eq(BCCP_MANIFEST_TAG.TAG_ID))
                .where(BCCP_MANIFEST_TAG.BCCP_MANIFEST_ID.eq(ULong.valueOf(bccpManifestId)))
                .orderBy(BCCP_MANIFEST_TAG.CREATION_TIMESTAMP)
                .fetch(mapperForShortTag());
    }

    public List<ShortTag> getShortTagsByDtManifestId(BigInteger dtManifestId) {
        return dslContext.select(TAG.TAG_ID, TAG.NAME, TAG.TEXT_COLOR, TAG.BACKGROUND_COLOR)
                .from(TAG)
                .join(DT_MANIFEST_TAG).on(TAG.TAG_ID.eq(DT_MANIFEST_TAG.TAG_ID))
                .where(DT_MANIFEST_TAG.DT_MANIFEST_ID.eq(ULong.valueOf(dtManifestId)))
                .orderBy(DT_MANIFEST_TAG.CREATION_TIMESTAMP)
                .fetch(mapperForShortTag());
    }

    public Map<Pair<CcType, BigInteger>, List<ShortTag>> getShortTagsByPairsOfTypeAndManifestId(
            List<Pair<CcType, BigInteger>> pairsOfTypeAndManifestId) {

        List<ULong> accManifestIdList = pairsOfTypeAndManifestId.stream().filter(e -> CcType.ACC.equals(e.getKey()))
                .map(e -> ULong.valueOf(e.getValue())).collect(Collectors.toList());
        List<ULong> asccpManifestIdList = pairsOfTypeAndManifestId.stream().filter(e -> CcType.ASCCP.equals(e.getKey()))
                .map(e -> ULong.valueOf(e.getValue())).collect(Collectors.toList());
        List<ULong> bccpManifestIdList = pairsOfTypeAndManifestId.stream().filter(e -> CcType.BCCP.equals(e.getKey()))
                .map(e -> ULong.valueOf(e.getValue())).collect(Collectors.toList());
        List<ULong> dtManifestIdList = pairsOfTypeAndManifestId.stream().filter(e -> CcType.DT.equals(e.getKey()))
                .map(e -> ULong.valueOf(e.getValue())).collect(Collectors.toList());

        SelectOrderByStep<Record> select = null;
        if (!accManifestIdList.isEmpty()) {
            SelectConditionStep step = dslContext.select(val("ACC").as("type"), ACC_MANIFEST_TAG.ACC_MANIFEST_ID.as("manifestId"),
                            TAG.TAG_ID, TAG.NAME, TAG.TEXT_COLOR, TAG.BACKGROUND_COLOR)
                    .from(TAG)
                    .join(ACC_MANIFEST_TAG).on(TAG.TAG_ID.eq(ACC_MANIFEST_TAG.TAG_ID))
                    .where(ACC_MANIFEST_TAG.ACC_MANIFEST_ID.in(accManifestIdList));
            select = (select != null) ? select.union(step) : step;
        }
        if (!asccpManifestIdList.isEmpty()) {
            SelectConditionStep step = dslContext.select(val("ASCCP").as("type"), ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID.as("manifestId"),
                            TAG.TAG_ID, TAG.NAME, TAG.TEXT_COLOR, TAG.BACKGROUND_COLOR)
                    .from(TAG)
                    .join(ASCCP_MANIFEST_TAG).on(TAG.TAG_ID.eq(ASCCP_MANIFEST_TAG.TAG_ID))
                    .where(ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID.in(asccpManifestIdList));
            select = (select != null) ? select.union(step) : step;
        }
        if (!bccpManifestIdList.isEmpty()) {
            SelectConditionStep step = dslContext.select(val("BCCP").as("type"), BCCP_MANIFEST_TAG.BCCP_MANIFEST_ID.as("manifestId"),
                            TAG.TAG_ID, TAG.NAME, TAG.TEXT_COLOR, TAG.BACKGROUND_COLOR)
                    .from(TAG)
                    .join(BCCP_MANIFEST_TAG).on(TAG.TAG_ID.eq(BCCP_MANIFEST_TAG.TAG_ID))
                    .where(BCCP_MANIFEST_TAG.BCCP_MANIFEST_ID.in(bccpManifestIdList));
            select = (select != null) ? select.union(step) : step;
        }
        if (!dtManifestIdList.isEmpty()) {
            SelectConditionStep step = dslContext.select(val("DT").as("type"), DT_MANIFEST_TAG.DT_MANIFEST_ID.as("manifestId"),
                            TAG.TAG_ID, TAG.NAME, TAG.TEXT_COLOR, TAG.BACKGROUND_COLOR)
                    .from(TAG)
                    .join(DT_MANIFEST_TAG).on(TAG.TAG_ID.eq(DT_MANIFEST_TAG.TAG_ID))
                    .where(DT_MANIFEST_TAG.DT_MANIFEST_ID.in(dtManifestIdList));
            select = (select != null) ? select.union(step) : step;
        }

        if (select == null) {
            return Collections.emptyMap();
        }

        Map<Pair<CcType, BigInteger>, List<ShortTag>> tagMap = new HashMap<>();
        for (Record record : select.fetch()) {
            Pair<CcType, BigInteger> key = Pair.of(
                    CcType.valueOf(record.get(field("type"), String.class)),
                    record.get(field("manifestId"), ULong.class).toBigInteger()
            );
            ShortTag shortTag = mapperForShortTag().map(record);
            List<ShortTag> tagList;
            if (tagMap.containsKey(key)) {
                tagList = tagMap.get(key);
            } else {
                tagList = new ArrayList<>();
                tagMap.put(key, tagList);
            }
            tagList.add(shortTag);
        }

        return tagMap;
    }

    public void toggleTagByAccManifestId(ScoreUser requester, BigInteger accManifestId, Tag tag) {
        boolean exists = dslContext.selectCount()
                .from(ACC_MANIFEST_TAG)
                .where(and(
                        ACC_MANIFEST_TAG.ACC_MANIFEST_ID.eq(ULong.valueOf(accManifestId)),
                        ACC_MANIFEST_TAG.TAG_ID.eq(ULong.valueOf(tag.getTagId()))
                ))
                .fetchOneInto(Integer.class) > 0;
        if (exists) {
            dslContext.deleteFrom(ACC_MANIFEST_TAG)
                    .where(and(
                            ACC_MANIFEST_TAG.ACC_MANIFEST_ID.eq(ULong.valueOf(accManifestId)),
                            ACC_MANIFEST_TAG.TAG_ID.eq(ULong.valueOf(tag.getTagId()))
                    ))
                    .execute();
        } else {
            AccManifestTagRecord accManifestTagRecord = new AccManifestTagRecord();
            accManifestTagRecord.setAccManifestId(ULong.valueOf(accManifestId));
            accManifestTagRecord.setTagId(ULong.valueOf(tag.getTagId()));
            accManifestTagRecord.setCreatedBy(ULong.valueOf(requester.getUserId()));
            accManifestTagRecord.setCreationTimestamp(LocalDateTime.now());
            dslContext.insertInto(ACC_MANIFEST_TAG)
                    .set(accManifestTagRecord)
                    .execute();
        }
    }

    public void toggleTagByAsccpManifestId(ScoreUser requester, BigInteger asccpManifestId, Tag tag) {
        boolean exists = dslContext.selectCount()
                .from(ASCCP_MANIFEST_TAG)
                .where(and(
                        ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID.eq(ULong.valueOf(asccpManifestId)),
                        ASCCP_MANIFEST_TAG.TAG_ID.eq(ULong.valueOf(tag.getTagId()))
                ))
                .fetchOneInto(Integer.class) > 0;
        if (exists) {
            dslContext.deleteFrom(ASCCP_MANIFEST_TAG)
                    .where(and(
                            ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID.eq(ULong.valueOf(asccpManifestId)),
                            ASCCP_MANIFEST_TAG.TAG_ID.eq(ULong.valueOf(tag.getTagId()))
                    ))
                    .execute();
        } else {
            AsccpManifestTagRecord asccpManifestTagRecord = new AsccpManifestTagRecord();
            asccpManifestTagRecord.setAsccpManifestId(ULong.valueOf(asccpManifestId));
            asccpManifestTagRecord.setTagId(ULong.valueOf(tag.getTagId()));
            asccpManifestTagRecord.setCreatedBy(ULong.valueOf(requester.getUserId()));
            asccpManifestTagRecord.setCreationTimestamp(LocalDateTime.now());
            dslContext.insertInto(ASCCP_MANIFEST_TAG)
                    .set(asccpManifestTagRecord)
                    .execute();
        }
    }

    public void toggleTagByBccpManifestId(ScoreUser requester, BigInteger bccpManifestId, Tag tag) {
        boolean exists = dslContext.selectCount()
                .from(BCCP_MANIFEST_TAG)
                .where(and(
                        BCCP_MANIFEST_TAG.BCCP_MANIFEST_ID.eq(ULong.valueOf(bccpManifestId)),
                        BCCP_MANIFEST_TAG.TAG_ID.eq(ULong.valueOf(tag.getTagId()))
                ))
                .fetchOneInto(Integer.class) > 0;
        if (exists) {
            dslContext.deleteFrom(BCCP_MANIFEST_TAG)
                    .where(and(
                            BCCP_MANIFEST_TAG.BCCP_MANIFEST_ID.eq(ULong.valueOf(bccpManifestId)),
                            BCCP_MANIFEST_TAG.TAG_ID.eq(ULong.valueOf(tag.getTagId()))
                    ))
                    .execute();
        } else {
            BccpManifestTagRecord bccpManifestTagRecord = new BccpManifestTagRecord();
            bccpManifestTagRecord.setBccpManifestId(ULong.valueOf(bccpManifestId));
            bccpManifestTagRecord.setTagId(ULong.valueOf(tag.getTagId()));
            bccpManifestTagRecord.setCreatedBy(ULong.valueOf(requester.getUserId()));
            bccpManifestTagRecord.setCreationTimestamp(LocalDateTime.now());
            dslContext.insertInto(BCCP_MANIFEST_TAG)
                    .set(bccpManifestTagRecord)
                    .execute();
        }
    }

    public void toggleTagByDtManifestId(ScoreUser requester, BigInteger dtManifestId, Tag tag) {
        boolean exists = dslContext.selectCount()
                .from(DT_MANIFEST_TAG)
                .where(and(
                        DT_MANIFEST_TAG.DT_MANIFEST_ID.eq(ULong.valueOf(dtManifestId)),
                        DT_MANIFEST_TAG.TAG_ID.eq(ULong.valueOf(tag.getTagId()))
                ))
                .fetchOneInto(Integer.class) > 0;
        if (exists) {
            dslContext.deleteFrom(DT_MANIFEST_TAG)
                    .where(and(
                            DT_MANIFEST_TAG.DT_MANIFEST_ID.eq(ULong.valueOf(dtManifestId)),
                            DT_MANIFEST_TAG.TAG_ID.eq(ULong.valueOf(tag.getTagId()))
                    ))
                    .execute();
        } else {
            DtManifestTagRecord dtManifestTagRecord = new DtManifestTagRecord();
            dtManifestTagRecord.setDtManifestId(ULong.valueOf(dtManifestId));
            dtManifestTagRecord.setTagId(ULong.valueOf(tag.getTagId()));
            dtManifestTagRecord.setCreatedBy(ULong.valueOf(requester.getUserId()));
            dtManifestTagRecord.setCreationTimestamp(LocalDateTime.now());
            dslContext.insertInto(DT_MANIFEST_TAG)
                    .set(dtManifestTagRecord)
                    .execute();
        }
    }

    public void add(ScoreUser requester, Tag tag) {
        TagRecord labelRecord = new TagRecord();
        labelRecord.setName(tag.getName());
        labelRecord.setTextColor(tag.getTextColor());
        labelRecord.setBackgroundColor(tag.getBackgroundColor());
        labelRecord.setDescription(tag.getDescription());
        labelRecord.setCreatedBy(ULong.valueOf(requester.getUserId()));
        labelRecord.setLastUpdatedBy(labelRecord.getCreatedBy());
        labelRecord.setCreationTimestamp(LocalDateTime.now());
        labelRecord.setLastUpdateTimestamp(labelRecord.getCreationTimestamp());

        dslContext.insertInto(TAG)
                .set(labelRecord)
                .execute();
    }

    public void update(ScoreUser requester, Tag tag) {
        dslContext.update(TAG)
                .set(TAG.NAME, tag.getName())
                .set(TAG.TEXT_COLOR, tag.getTextColor())
                .set(TAG.BACKGROUND_COLOR, tag.getBackgroundColor())
                .set(TAG.DESCRIPTION, tag.getDescription())
                .set(TAG.LAST_UPDATED_BY, ULong.valueOf(requester.getUserId()))
                .set(TAG.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                .where(TAG.TAG_ID.eq(ULong.valueOf(tag.getTagId())))
                .execute();
    }

    public void discard(ScoreUser requester, BigInteger tagId) {
        ULong id = ULong.valueOf(tagId);
        dslContext.deleteFrom(ACC_MANIFEST_TAG)
                .where(ACC_MANIFEST_TAG.TAG_ID.eq(id))
                .execute();
        dslContext.deleteFrom(ASCCP_MANIFEST_TAG)
                .where(ASCCP_MANIFEST_TAG.TAG_ID.eq(id))
                .execute();
        dslContext.deleteFrom(BCCP_MANIFEST_TAG)
                .where(BCCP_MANIFEST_TAG.TAG_ID.eq(id))
                .execute();
        dslContext.deleteFrom(DT_MANIFEST_TAG)
                .where(DT_MANIFEST_TAG.TAG_ID.eq(id))
                .execute();
        dslContext.deleteFrom(TAG)
                .where(TAG.TAG_ID.eq(id))
                .execute();
    }

}
