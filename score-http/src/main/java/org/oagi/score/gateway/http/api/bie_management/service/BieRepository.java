package org.oagi.score.gateway.http.api.bie_management.service;

import org.jooq.*;
import org.jooq.tools.StringUtils;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScId;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.*;
import org.oagi.score.gateway.http.common.util.ScoreGuidUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.concat;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

@Repository
public class BieRepository {

    @Autowired
    private DSLContext dslContext;

    public TopLevelAsbiepId createTopLevelAsbiep(UserId userId, ReleaseId releaseId, BieState state) {
        return createTopLevelAsbiep(userId, releaseId, null, state, null, null);
    }

    public TopLevelAsbiepId createTopLevelAsbiep(UserId userId, ReleaseId releaseId,
                                                 TopLevelAsbiepId basedTopLevelAsbiepId, BieState state,
                                                 String version, String status) {
        TopLevelAsbiepRecord record = new TopLevelAsbiepRecord();
        LocalDateTime timestamp = LocalDateTime.now();
        record.setOwnerUserId(ULong.valueOf(userId.value()));
        record.setReleaseId(ULong.valueOf(releaseId.value()));
        if (basedTopLevelAsbiepId != null) {
            record.setBasedTopLevelAsbiepId(ULong.valueOf(basedTopLevelAsbiepId.value()));
        }
        record.setState(state.name());
        record.setVersion(version);
        record.setStatus(status);
        record.setLastUpdatedBy(ULong.valueOf(userId.value()));
        record.setLastUpdateTimestamp(timestamp);

        return new TopLevelAsbiepId(
                dslContext.insertInto(TOP_LEVEL_ASBIEP)
                        .set(record)
                        .returning().fetchOne().getTopLevelAsbiepId().toBigInteger()
        );
    }

    public void updateAsbiepIdOnTopLevelAsbiep(AsbiepId asbiepId, TopLevelAsbiepId topLevelAsbiepId) {
        dslContext.update(TOP_LEVEL_ASBIEP)
                .set(TOP_LEVEL_ASBIEP.ASBIEP_ID, ULong.valueOf(asbiepId.value()))
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId.value())))
                .execute();
    }

    public void createBizCtxAssignments(TopLevelAsbiepId topLevelAsbiepId, List<BusinessContextId> bizCtxIds) {
        bizCtxIds.stream().forEach(bizCtxId -> {
            dslContext.insertInto(BIZ_CTX_ASSIGNMENT)
                    .set(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID, ULong.valueOf(topLevelAsbiepId.value()))
                    .set(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID, ULong.valueOf(bizCtxId.value()))
                    .execute();
        });
    }

    public AsbiepRecord createAsbiep(ScoreUser user, AsccpManifestId asccpManifestId, AbieId abieId, TopLevelAsbiepId topLevelAsbiepId) {
        UserId userId = user.userId();
        LocalDateTime timestamp = LocalDateTime.now();

        return dslContext.insertInto(ASBIEP)
                .set(ASBIEP.GUID, ScoreGuidUtils.randomGuid())
                .set(ASBIEP.BASED_ASCCP_MANIFEST_ID, ULong.valueOf(asccpManifestId.value()))
                .set(ASBIEP.ROLE_OF_ABIE_ID, ULong.valueOf(abieId.value()))
                .set(ASBIEP.CREATED_BY, ULong.valueOf(userId.value()))
                .set(ASBIEP.LAST_UPDATED_BY, ULong.valueOf(userId.value()))
                .set(ASBIEP.CREATION_TIMESTAMP, timestamp)
                .set(ASBIEP.LAST_UPDATE_TIMESTAMP, timestamp)
                .set(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(topLevelAsbiepId.value()))
                .returning().fetchOne();
    }

    public AsbieRecord createAsbie(ScoreUser user, AbieId fromAbieId, AsbiepId toAsbiepId,
                                   AsccManifestId basedAsccManifestId,
                                   int seqKey, TopLevelAsbiepId topLevelAsbiepId) {

        UserId userId = user.userId();
        LocalDateTime timestamp = LocalDateTime.now();

        Record2<Integer, Integer> cardinality = dslContext.select(
                ASCC.CARDINALITY_MIN,
                ASCC.CARDINALITY_MAX)
                .from(ASCC)
                .join(ASCC_MANIFEST).on(ASCC.ASCC_ID.eq(ASCC_MANIFEST.ASCC_ID))
                .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(ULong.valueOf(basedAsccManifestId.value())))
                .fetchOne();

        Byte AsccpNillable = dslContext.select(ASCCP.IS_NILLABLE)
                .from(ASCC_MANIFEST)
                .join(ASCCP_MANIFEST).on(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(ULong.valueOf(basedAsccManifestId.value())))
                .fetchOne().getValue(ASCCP.IS_NILLABLE);

        return dslContext.insertInto(ASBIE)
                .set(ASBIE.GUID, ScoreGuidUtils.randomGuid())
                .set(ASBIE.FROM_ABIE_ID, ULong.valueOf(fromAbieId.value()))
                .set(ASBIE.TO_ASBIEP_ID, ULong.valueOf(toAsbiepId.value()))
                .set(ASBIE.BASED_ASCC_MANIFEST_ID, ULong.valueOf(basedAsccManifestId.value()))
                .set(ASBIE.CARDINALITY_MIN, cardinality.get(ASCC.CARDINALITY_MIN))
                .set(ASBIE.CARDINALITY_MAX, cardinality.get(ASCC.CARDINALITY_MAX))
                .set(ASBIE.IS_NILLABLE, AsccpNillable)
                .set(ASBIE.CREATED_BY, ULong.valueOf(userId.value()))
                .set(ASBIE.LAST_UPDATED_BY, ULong.valueOf(userId.value()))
                .set(ASBIE.CREATION_TIMESTAMP, timestamp)
                .set(ASBIE.LAST_UPDATE_TIMESTAMP, timestamp)
                .set(ASBIE.SEQ_KEY, BigDecimal.valueOf(seqKey))
                .set(ASBIE.IS_USED, (byte) (cardinality.get(ASCC.CARDINALITY_MIN) > 0 ? 1 : 0))
                .set(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(topLevelAsbiepId.value()))
                .returning().fetchOne();

    }

    public BbiepRecord createBbiep(ScoreUser user, BccpManifestId basedBccpManifestId, TopLevelAsbiepId topLevelAsbiepId) {
        UserId userId = user.userId();
        LocalDateTime timestamp = LocalDateTime.now();

        return dslContext.insertInto(BBIEP)
                .set(BBIEP.GUID, ScoreGuidUtils.randomGuid())
                .set(BBIEP.BASED_BCCP_MANIFEST_ID, ULong.valueOf(basedBccpManifestId.value()))
                .set(BBIEP.CREATED_BY, ULong.valueOf(userId.value()))
                .set(BBIEP.LAST_UPDATED_BY, ULong.valueOf(userId.value()))
                .set(BBIEP.CREATION_TIMESTAMP, timestamp)
                .set(BBIEP.LAST_UPDATE_TIMESTAMP, timestamp)
                .set(BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(topLevelAsbiepId.value()))
                .returning().fetchOne();
    }

    public BbieRecord createBbie(ScoreUser user, AbieId fromAbieId, BbiepId toBbiepId,
                                 BccManifestId basedBccManifestId,
                                 DtManifestId bdtManifestId,
                                 int seqKey, TopLevelAsbiepId topLevelAsbiepId) {

        UserId userId = user.userId();
        LocalDateTime timestamp = LocalDateTime.now();

        BccRecord bccRecord = dslContext.select(
                BCC.TO_BCCP_ID,
                BCC.CARDINALITY_MIN,
                BCC.CARDINALITY_MAX,
                BCC.DEFAULT_VALUE,
                BCC.FIXED_VALUE,
                BCC.IS_NILLABLE)
                .from(BCC)
                .join(BCC_MANIFEST).on(BCC.BCC_ID.eq(BCC_MANIFEST.BCC_ID))
                .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(ULong.valueOf(basedBccManifestId.value())))
                .fetchOneInto(BccRecord.class);

        BccpRecord bccpRecord = dslContext.select(
                BCCP.DEFAULT_VALUE,
                BCCP.FIXED_VALUE)
                .from(BCCP)
                .where(BCCP.BCCP_ID.eq(bccRecord.getToBccpId()))
                .fetchOneInto(BccpRecord.class);

        return dslContext.insertInto(BBIE)
                .set(BBIE.GUID, ScoreGuidUtils.randomGuid())
                .set(BBIE.FROM_ABIE_ID, ULong.valueOf(fromAbieId.value()))
                .set(BBIE.TO_BBIEP_ID, ULong.valueOf(toBbiepId.value()))
                .set(BBIE.BASED_BCC_MANIFEST_ID, ULong.valueOf(basedBccManifestId.value()))
                .set(BBIE.XBT_MANIFEST_ID, ULong.valueOf(getDefaultXbtManifestIdByDtManifestId(bdtManifestId).value()))
                .set(BBIE.CARDINALITY_MIN, bccRecord.getCardinalityMin())
                .set(BBIE.CARDINALITY_MAX, bccRecord.getCardinalityMax())
                .set(BBIE.IS_NILLABLE, bccRecord.getIsNillable())
                .set(BBIE.IS_NULL, (byte) ((0)))
                .set(BBIE.CREATED_BY, ULong.valueOf(userId.value()))
                .set(BBIE.LAST_UPDATED_BY, ULong.valueOf(userId.value()))
                .set(BBIE.CREATION_TIMESTAMP, timestamp)
                .set(BBIE.LAST_UPDATE_TIMESTAMP, timestamp)
                .set(BBIE.SEQ_KEY, BigDecimal.valueOf(seqKey))
                .set(BBIE.IS_USED, (byte) (bccRecord.getCardinalityMin() > 0 ? 1 : 0))
                .set(BBIE.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(topLevelAsbiepId.value()))
                .set(BBIE.DEFAULT_VALUE, StringUtils.defaultIfEmpty(bccRecord.getDefaultValue(), bccpRecord.getDefaultValue()))
                .set(BBIE.FIXED_VALUE, StringUtils.defaultIfEmpty(bccRecord.getFixedValue(), bccpRecord.getFixedValue()))
                .returning().fetchOne();
    }

    public XbtManifestId getDefaultXbtManifestIdByDtManifestId(DtManifestId bdtManifestId) {
        String bdtDataTypeTerm = dslContext.select(DT.DATA_TYPE_TERM)
                .from(DT)
                .join(DT_MANIFEST).on(DT.DT_ID.eq(DT_MANIFEST.DT_ID))
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(ULong.valueOf(bdtManifestId.value())))
                .fetchOneInto(String.class);

        /*
         * Issue #808
         */
        List<Condition> conds = new ArrayList<>();
        conds.add(DT_MANIFEST.DT_MANIFEST_ID.eq(ULong.valueOf(bdtManifestId.value())));
        if ("Date Time".equals(bdtDataTypeTerm)) {
            conds.add(XBT.NAME.eq("date time"));
        } else if ("Date".equals(bdtDataTypeTerm)) {
            conds.add(XBT.NAME.eq("date"));
        } else if ("Time".equals(bdtDataTypeTerm)) {
            conds.add(XBT.NAME.eq("time"));
        } else {
            conds.add(DT_AWD_PRI.IS_DEFAULT.eq((byte) 1));
        }

        SelectOnConditionStep<Record1<ULong>> step = dslContext.select(
                        DT_AWD_PRI.XBT_MANIFEST_ID)
                .from(DT_AWD_PRI)
                .join(DT_MANIFEST).on(and(
                        DT_AWD_PRI.RELEASE_ID.eq(DT_MANIFEST.RELEASE_ID),
                        DT_AWD_PRI.DT_ID.eq(DT_MANIFEST.DT_ID)
                ))
                .join(DT).on(DT.DT_ID.eq(DT_MANIFEST.DT_ID))
                .join(XBT_MANIFEST).on(DT_AWD_PRI.XBT_MANIFEST_ID.eq(XBT_MANIFEST.XBT_MANIFEST_ID))
                .join(XBT).on(XBT_MANIFEST.XBT_ID.eq(XBT.XBT_ID));
        return step.where(conds)
                .fetchOptionalInto(XbtManifestId.class).orElse(null);
    }

    public BbieScId createBbieSc(ScoreUser user, BbieId bbieId, DtScManifestId dtScManifestId,
                                 TopLevelAsbiepId topLevelAsbiepId) {

        DtScRecord dtScRecord = dslContext.select(DT_SC.fields())
                .from(DT_SC)
                .join(DT_SC_MANIFEST).on(DT_SC.DT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID))
                .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(ULong.valueOf(dtScManifestId.value())))
                .fetchOneInto(DtScRecord.class);

        return new BbieScId(dslContext.insertInto(BBIE_SC)
                .set(BBIE_SC.GUID, ScoreGuidUtils.randomGuid())
                .set(BBIE_SC.BBIE_ID, ULong.valueOf(bbieId.value()))
                .set(BBIE_SC.BASED_DT_SC_MANIFEST_ID, ULong.valueOf(dtScManifestId.value()))
                .set(BBIE_SC.XBT_MANIFEST_ID, ULong.valueOf(getDefaultXbtManifestIdByDtScManifestId(dtScManifestId).value()))
                .set(BBIE_SC.CARDINALITY_MIN, dtScRecord.getCardinalityMin())
                .set(BBIE_SC.CARDINALITY_MAX, dtScRecord.getCardinalityMax())
                .set(BBIE_SC.IS_USED, (byte) (dtScRecord.getCardinalityMin() > 0 ? 1 : 0))
                .set(BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID, ULong.valueOf(topLevelAsbiepId.value()))
                .set(BBIE_SC.DEFAULT_VALUE, dtScRecord.getDefaultValue())
                .set(BBIE_SC.FIXED_VALUE, dtScRecord.getFixedValue())
                .returning().fetchOne().getBbieScId().toBigInteger());
    }

    public XbtManifestId getDefaultXbtManifestIdByDtScManifestId(DtScManifestId dtScManifestId) {
        String bdtScRepresentationTerm = dslContext.select(DT_SC.REPRESENTATION_TERM)
                .from(DT_SC)
                .join(DT_SC_MANIFEST).on(DT_SC.DT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID))
                .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(ULong.valueOf(dtScManifestId.value())))
                .fetchOneInto(String.class);

        /*
         * Issue #808
         */
        List<Condition> conds = new ArrayList<>();
        conds.add(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.eq(ULong.valueOf(dtScManifestId.value())));
        if ("Date Time".equals(bdtScRepresentationTerm)) {
            conds.add(XBT.NAME.eq("date time"));
        } else if ("Date".equals(bdtScRepresentationTerm)) {
            conds.add(XBT.NAME.eq("date"));
        } else if ("Time".equals(bdtScRepresentationTerm)) {
            conds.add(XBT.NAME.eq("time"));
        } else {
            conds.add(DT_SC_AWD_PRI.IS_DEFAULT.eq((byte) 1));
        }

        return dslContext.select(
                        DT_SC_AWD_PRI.XBT_MANIFEST_ID)
                .from(DT_SC_AWD_PRI)
                .join(DT_SC_MANIFEST).on(and(
                        DT_SC_AWD_PRI.RELEASE_ID.eq(DT_SC_MANIFEST.RELEASE_ID),
                        DT_SC_AWD_PRI.DT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID)
                ))
                .join(DT_SC).on(DT_SC.DT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID))
                .join(XBT_MANIFEST).on(DT_SC_AWD_PRI.XBT_MANIFEST_ID.eq(XBT_MANIFEST.XBT_MANIFEST_ID))
                .join(XBT).on(XBT_MANIFEST.XBT_ID.eq(XBT.XBT_ID))
                .where(conds)
                .fetchOptionalInto(XbtManifestId.class).orElse(null);
    }

    public void updateState(TopLevelAsbiepId topLevelAsbiepId, BieState state) {
        dslContext.update(TOP_LEVEL_ASBIEP)
                .set(TOP_LEVEL_ASBIEP.STATE, state.name())
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId.value())))
                .execute();
    }

    public List<BieReuseReport> getBieReuseReport(TopLevelAsbiepId reusedTopLevelAsbiepId) {
        List<Record2<String, String>> propertyTermList = dslContext.select(
                concat("ASCCP-", ASCCP_MANIFEST.ASCCP_MANIFEST_ID.cast(String.class)),
                ASCCP.PROPERTY_TERM)
                .from(ASCCP_MANIFEST)
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .fetch();
        Map<String, String> propertyTermMap = new HashMap<>();
        for (Record2<String, String> item : propertyTermList) {
            propertyTermMap.put(item.value1(), item.value2());
        }

        SelectOnConditionStep step = dslContext.select(
                TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.as("reusingTopLevelAsbiepId"),
                TOP_LEVEL_ASBIEP.STATE.as("reusingState"),
                TOP_LEVEL_ASBIEP.IS_DEPRECATED.as("reusingDeprecated"),
                TOP_LEVEL_ASBIEP.DEPRECATED_REASON.as("reusingDeprecatedReason"),
                TOP_LEVEL_ASBIEP.DEPRECATED_REMARK.as("reusingDeprecatedRemark"),
                ASCCP.as("reusing_asccp").PROPERTY_TERM.as("reusingPropertyTerm"),
                ASBIEP.as("reusing_asbiep").DISPLAY_NAME.as("reusingDisplayName"),
                ASCCP_MANIFEST.as("reusing_asccp_manifest").DEN.as("reusingDen"),
                ABIE.as("reusing_abie").GUID.as("reusingGuid"),
                APP_USER.as("reusing_app_user").LOGIN_ID.as("reusingOwner"),
                TOP_LEVEL_ASBIEP.VERSION.as("reusingVersion"),
                TOP_LEVEL_ASBIEP.STATUS.as("reusingStatus"),
                ASBIEP.as("reusing_asbiep").REMARK.as("reusingRemark"),
                ASBIE.PATH,

                TOP_LEVEL_ASBIEP.as("reused_top_level_asbiep").TOP_LEVEL_ASBIEP_ID.as("reusedTopLevelAsbiepId"),
                TOP_LEVEL_ASBIEP.as("reused_top_level_asbiep").STATE.as("reusedState"),
                TOP_LEVEL_ASBIEP.as("reused_top_level_asbiep").IS_DEPRECATED.as("reusedDeprecated"),
                TOP_LEVEL_ASBIEP.as("reused_top_level_asbiep").DEPRECATED_REASON.as("reusedDeprecatedReason"),
                TOP_LEVEL_ASBIEP.as("reused_top_level_asbiep").DEPRECATED_REMARK.as("reusedDeprecatedRemark"),
                ASCCP.as("reused_asccp").PROPERTY_TERM.as("reusedPropertyTerm"),
                ASBIEP.DISPLAY_NAME.as("reusedDisplayName"),
                ASCCP_MANIFEST.as("reused_asccp_manifest").DEN.as("reusedDen"),
                ABIE.as("reused_abie").GUID.as("reusedGuid"),
                APP_USER.as("reused_app_user").LOGIN_ID.as("reusedOwner"),
                TOP_LEVEL_ASBIEP.as("reused_top_level_asbiep").VERSION.as("reusedVersion"),
                TOP_LEVEL_ASBIEP.as("reused_top_level_asbiep").STATUS.as("reusedStatus"),
                ASBIEP.REMARK.as("reusedRemark"),

                RELEASE.RELEASE_NUM)

                .from(TOP_LEVEL_ASBIEP)
                .join(ASBIE).on(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID))
                .join(ASBIEP).on(ASBIE.TO_ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                .join(TOP_LEVEL_ASBIEP.as("reused_top_level_asbiep")).on(TOP_LEVEL_ASBIEP.as("reused_top_level_asbiep").TOP_LEVEL_ASBIEP_ID.eq(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID))

                .join(ASBIEP.as("reusing_asbiep")).on(TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.as("reusing_asbiep").ASBIEP_ID))
                .join(ABIE.as("reusing_abie")).on(ASBIEP.as("reusing_asbiep").ROLE_OF_ABIE_ID.eq(ABIE.as("reusing_abie").ABIE_ID))
                .join(ASCCP_MANIFEST.as("reusing_asccp_manifest")).on(ASBIEP.as("reusing_asbiep").BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.as("reusing_asccp_manifest").ASCCP_MANIFEST_ID))
                .join(ASCCP.as("reusing_asccp")).on(ASCCP_MANIFEST.as("reusing_asccp_manifest").ASCCP_ID.eq(ASCCP.as("reusing_asccp").ASCCP_ID))
                .join(APP_USER.as("reusing_app_user")).on(TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(APP_USER.as("reusing_app_user").APP_USER_ID))

                .join(ABIE.as("reused_abie")).on(ASBIEP.ROLE_OF_ABIE_ID.eq(ABIE.as("reused_abie").ABIE_ID))
                .join(ASCCP_MANIFEST.as("reused_asccp_manifest")).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.as("reused_asccp_manifest").ASCCP_MANIFEST_ID))
                .join(ASCCP.as("reused_asccp")).on(ASCCP_MANIFEST.as("reused_asccp_manifest").ASCCP_ID.eq(ASCCP.as("reused_asccp").ASCCP_ID))
                .join(APP_USER.as("reused_app_user")).on(TOP_LEVEL_ASBIEP.as("reused_top_level_asbiep").OWNER_USER_ID.eq(APP_USER.as("reused_app_user").APP_USER_ID))
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(ASCCP_MANIFEST.as("reusing_asccp_manifest").RELEASE_ID));

        List<BieReuseReport> reports;

        if (reusedTopLevelAsbiepId != null) {
            reports = step
                    .where(and(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.notEqual(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID),
                            TOP_LEVEL_ASBIEP.as("reused_top_level_asbiep").TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(reusedTopLevelAsbiepId.value()))))
                    .fetchInto(BieReuseReport.class);
        } else {
            reports = step
                    .where(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.notEqual(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID))
                    .fetchInto(BieReuseReport.class);
        }

        reports.forEach(r -> {
            r.setDisplayPath(pathToDisplay(r, propertyTermMap));
        });

        return reports;
    }

    private String pathToDisplay(BieReuseReport report, Map<String, String> nameMap) {
        List<String> tokens = Arrays.stream(report.getPath().split(">")).filter(e -> e.startsWith("ASCCP-")).collect(Collectors.toList());
        StringBuilder displays = new StringBuilder();
        for (String token : tokens) {
            if (nameMap.get(token) != null && !nameMap.get(token).contains("Group")) {
                displays.append("/").append(nameMap.get(token)).append(" ");
            }
        }
        displays.append("/").append(report.getReusedPropertyTerm());
        return displays.toString().replaceAll("\s?/\s?", "/");
    }

}
