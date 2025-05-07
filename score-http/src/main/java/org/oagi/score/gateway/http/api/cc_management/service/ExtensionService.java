package org.oagi.score.gateway.http.api.cc_management.service;

import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.Tables;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.*;
import org.oagi.score.gateway.http.common.util.ScoreGuidUtils;
import org.oagi.score.gateway.http.common.util.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

@Service
@Transactional(readOnly = true)
public class ExtensionService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private CcCommandService ccCommandService;

    @Autowired
    private DSLContext dslContext;

    private AccManifestRecord getExtensionAcc(AccManifestId manifestId) {
        if (manifestId == null) {
            return null;
        }
        return dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(manifestId.value())))
                .fetchOptional().orElse(null);
    }

    private AccRecord createACCForExtension(AccSummaryRecord eAcc, ScoreUser requester) {
        String objectClassTerm = Utility.getUserExtensionGroupObjectClassTerm(eAcc.objectClassTerm());
        ULong userId = ULong.valueOf(requester.userId().value());
        LocalDateTime timestamp = LocalDateTime.now();

        return dslContext.insertInto(Tables.ACC,
                Tables.ACC.GUID,
                Tables.ACC.OBJECT_CLASS_TERM,
                Tables.ACC.DEFINITION,
                Tables.ACC.OAGIS_COMPONENT_TYPE,
                Tables.ACC.CREATED_BY,
                Tables.ACC.LAST_UPDATED_BY,
                Tables.ACC.OWNER_USER_ID,
                Tables.ACC.CREATION_TIMESTAMP,
                Tables.ACC.LAST_UPDATE_TIMESTAMP,
                Tables.ACC.STATE).values(
                ScoreGuidUtils.randomGuid(),
                objectClassTerm,
                "A system created component containing user extension to the " + eAcc.objectClassTerm() + ".",
                OagisComponentType.UserExtensionGroup.getValue(),
                userId,
                userId,
                userId,
                timestamp,
                timestamp,
                CcState.WIP.name()
        ).returning().fetchOne();
    }

    private AccManifestRecord createACCManifestForExtension(AccRecord ueAcc, ReleaseId releaseId) {
        return dslContext.insertInto(ACC_MANIFEST,
                ACC_MANIFEST.ACC_ID,
                ACC_MANIFEST.RELEASE_ID
        ).values(
                ueAcc.getAccId(),
                ULong.valueOf(releaseId.value())
        ).returning().fetchOne();
    }

    private AsccpRecord createASCCPForExtension(AccSummaryRecord eAcc, ScoreUser requester, AccRecord ueAcc) {
        ULong userId = ULong.valueOf(requester.userId().value());
        LocalDateTime timestamp = LocalDateTime.now();

        return dslContext.insertInto(Tables.ASCCP,
                Tables.ASCCP.GUID,
                Tables.ASCCP.PROPERTY_TERM,
                Tables.ASCCP.ROLE_OF_ACC_ID,
                Tables.ASCCP.DEFINITION,
                Tables.ASCCP.REUSABLE_INDICATOR,
                Tables.ASCCP.IS_DEPRECATED,
                Tables.ASCCP.IS_NILLABLE,
                Tables.ASCCP.CREATED_BY,
                Tables.ASCCP.LAST_UPDATED_BY,
                Tables.ASCCP.OWNER_USER_ID,
                Tables.ASCCP.CREATION_TIMESTAMP,
                Tables.ASCCP.LAST_UPDATE_TIMESTAMP,
                Tables.ASCCP.STATE).values(
                ScoreGuidUtils.randomGuid(),
                ueAcc.getObjectClassTerm(),
                ueAcc.getAccId(),
                "A system created component containing user extension to the " + eAcc.objectClassTerm() + ".",
                (byte) 0,
                (byte) 0,
                (byte) 0,
                userId,
                userId,
                userId,
                timestamp,
                timestamp,
                CcState.Production.name()
        ).returning().fetchOne();
    }

    private AsccpManifestRecord createASCCPManifestForExtension(
            AsccpRecord ueAsccp, AccManifestRecord ueAccManifest, ReleaseId releaseId) {
        return dslContext.insertInto(ASCCP_MANIFEST,
                ASCCP_MANIFEST.ASCCP_ID,
                ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID,
                ASCCP_MANIFEST.RELEASE_ID
        ).values(
                ueAsccp.getAsccpId(),
                ueAccManifest.getAccManifestId(),
                ULong.valueOf(releaseId.value())
        ).returning().fetchOne();
    }

    private AsccRecord createASCCForExtension(AccSummaryRecord eAcc, AsccpRecord ueAsccp, ScoreUser requester) {
        ULong userId = ULong.valueOf(requester.userId().value());
        LocalDateTime timestamp = LocalDateTime.now();

        return dslContext.insertInto(Tables.ASCC,
                Tables.ASCC.GUID,
                Tables.ASCC.CARDINALITY_MIN,
                Tables.ASCC.CARDINALITY_MAX,
                Tables.ASCC.SEQ_KEY,
                Tables.ASCC.FROM_ACC_ID,
                Tables.ASCC.TO_ASCCP_ID,
                Tables.ASCC.IS_DEPRECATED,
                Tables.ASCC.CREATED_BY,
                Tables.ASCC.LAST_UPDATED_BY,
                Tables.ASCC.OWNER_USER_ID,
                Tables.ASCC.CREATION_TIMESTAMP,
                Tables.ASCC.LAST_UPDATE_TIMESTAMP,
                Tables.ASCC.STATE).values(
                ScoreGuidUtils.randomGuid(),
                0,
                1,
                1,
                ULong.valueOf(eAcc.accId().value()),
                ueAsccp.getAsccpId(),
                (byte) 0,
                userId,
                userId,
                userId,
                timestamp,
                timestamp,
                CcState.Production.name()
        ).returning().fetchOne();
    }

    private void createASCCManifestForExtension(
            AsccRecord ueAscc, AccManifestRecord eAccManifest, AsccpManifestRecord ueAsccpManifest, ReleaseId releaseId) {
        dslContext.insertInto(ASCC_MANIFEST,
                ASCC_MANIFEST.ASCC_ID,
                ASCC_MANIFEST.FROM_ACC_MANIFEST_ID,
                ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID,
                ASCC_MANIFEST.RELEASE_ID
        ).values(
                ueAscc.getAsccId(),
                eAccManifest.getAccManifestId(),
                ueAsccpManifest.getAsccpManifestId(),
                ULong.valueOf(releaseId.value())
        ).execute();
    }

//    @Transactional
//    public void appendAsccp(ScoreUser requester, AccManifestId manifestId, AsccpManifestId asccpManifestId) {
//        AccManifestRecord extensionAcc = getExtensionAcc(manifestId);
//        AsccpManifestRecord asccpManifestRecord =
//                dslContext.selectFrom(ASCCP_MANIFEST)
//                        .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ULong.valueOf(asccpManifestId.value())))
//                        .fetchOne();
//
//        ccNodeService.appendAsccp(requester, new ReleaseId(asccpManifestRecord.getReleaseId().toBigInteger()),
//                new AccManifestId(extensionAcc.getAccManifestId().toBigInteger()),
//                new AsccpManifestId(asccpManifestRecord.getAsccpManifestId().toBigInteger()), -1);
//    }
//
//    @Transactional
//    public void appendBccp(ScoreUser requester, AccManifestId manifestId, BccpManifestId bccpManifestId) {
//        AccManifestRecord extensionAcc = getExtensionAcc(manifestId);
//        BccpManifestRecord bccpManifestRecord =
//                dslContext.selectFrom(BCCP_MANIFEST)
//                        .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(ULong.valueOf(bccpManifestId.value())))
//                        .fetchOne();
//
//        ccNodeService.appendBccp(requester, new ReleaseId(bccpManifestRecord.getReleaseId().toBigInteger()),
//                new AccManifestId(extensionAcc.getAccManifestId().toBigInteger()),
//                new BccpManifestId(bccpManifestRecord.getBccpManifestId().toBigInteger()), -1);
//    }

    @Transactional
    public void purgeExtension(ScoreUser requester, AccManifestId manifestId) {

//        AccManifestRecord extensionAccManifestRecord = dslContext.selectFrom(ACC_MANIFEST)
//                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(manifestId.value())))
//                .fetchOne();
//
//        AsccpManifestRecord groupAsccpManifestRecord = dslContext.selectFrom(ASCCP_MANIFEST)
//                .where(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID.eq(extensionAccManifestRecord.getAccManifestId()))
//                .fetchOne();
//
//        AsccManifestRecord asccManifestRecord = dslContext.selectFrom(ASCC_MANIFEST)
//                .where(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(groupAsccpManifestRecord.getAsccpManifestId()))
//                .fetchOne();
//
//        ccNodeService.deleteAscc(requester, new AsccManifestId(asccManifestRecord.getAsccManifestId().toBigInteger()), true);
//
//        ccNodeService.purgeAsccp(requester, new AsccpManifestId(groupAsccpManifestRecord.getAsccpManifestId().toBigInteger()), false, true);
//
//        ccNodeService.purgeAcc(requester, manifestId);
    }

//    @Transactional
//    public ExtensionUpdateResponse updateDetails(ScoreUser requester, ExtensionUpdateRequest request) {
//        ExtensionUpdateResponse response = new ExtensionUpdateResponse();
//
//        AccManifestRecord extensionAcc = getExtensionAcc(request.getManifestId());
//        ULong userId = ULong.valueOf(requester.userId().value());
//        LocalDateTime timestamp = LocalDateTime.now();
//
//        List<CcAsccpNodeInfo.Ascc> asccList = request.getAsccpDetails().stream()
//                .map(asccpDetail -> asccpDetail.getAscc())
//                .collect(Collectors.toList());
//
//        for (CcAsccpNodeInfo.Ascc ascc : asccList) {
//            response.getAsccResults().put(ascc.getAsccId(),
//                    updateAscc(extensionAcc, ascc, userId, timestamp)
//            );
//        }
//
//        List<CcBccpNodeInfo.Bcc> bccList = request.getBccpDetails().stream()
//                .map(bccpDetail -> bccpDetail.getBcc())
//                .collect(Collectors.toList());
//
//        for (CcBccpNodeInfo.Bcc bcc : bccList) {
//            response.getBccResults().put(bcc.getBccId(),
//                    updateBcc(extensionAcc, bcc, userId, timestamp)
//            );
//        }
//
//        return response;
//    }
//
//    private boolean updateAscc(AccManifestRecord extensionAcc,
//                               CcAsccpNodeInfo.Ascc ascc,
//                               ULong userId, LocalDateTime timestamp) {
//
//        String guid = dslContext.select(ASCC.GUID).from(ASCC)
//                .where(ASCC.ASCC_ID.eq(ULong.valueOf(ascc.getAsccId().value())))
//                .fetchOneInto(String.class);
//
//        AsccId asccId = new AsccId(dslContext.select(ASCC.ASCC_ID).from(ASCC)
//                .where(ASCC.GUID.eq(guid))
//                .orderBy(ASCC.ASCC_ID.desc()).limit(1).fetchOneInto(BigInteger.class));
//
//        AsccRecord history = dslContext.selectFrom(Tables.ASCC)
//                .where(ASCC.ASCC_ID.eq(ULong.valueOf(asccId.value())))
//                .fetchOne();
//
//        history.setAsccId(null);
//        history.setCardinalityMin(ascc.getCardinalityMin());
//        history.setCardinalityMax(ascc.getCardinalityMax());
//        history.setIsDeprecated((byte) ((ascc.isDeprecated()) ? 1 : 0));
//        history.setDefinition(ascc.getDefinition());
//        history.setDefinitionSource(ascc.getDefinitionSource());
//        history.setCreatedBy(userId);
//        history.setLastUpdatedBy(userId);
//        history.setCreationTimestamp(timestamp);
//        history.setLastUpdateTimestamp(timestamp);
//
//        history = dslContext.insertInto(ASCC).set(history).returning().fetchOne();
//        int result = dslContext.update(ASCC_MANIFEST)
//                .set(ASCC_MANIFEST.ASCC_ID, history.getAsccId())
//                .where(and(
//                        ASCC_MANIFEST.ASCC_ID.eq(ULong.valueOf(ascc.getAsccId().value())),
//                        ASCC_MANIFEST.RELEASE_ID.eq(extensionAcc.getReleaseId())
//                )).execute();
//
//        return (result == 1);
//    }
//
//    private boolean updateBcc(AccManifestRecord extensionAcc,
//                              CcBccpNodeInfo.Bcc bcc,
//                              ULong userId, LocalDateTime timestamp) {
//
//        String guid = dslContext.select(BCC.GUID).from(BCC)
//                .where(BCC.BCC_ID.eq(ULong.valueOf(bcc.getBccId().value())))
//                .fetchOneInto(String.class);
//
//        BccId bccId = new BccId(dslContext.select(BCC.BCC_ID).from(BCC)
//                .where(BCC.GUID.eq(guid))
//                .orderBy(BCC.BCC_ID.desc()).limit(1).fetchOneInto(BigInteger.class));
//
//        BccRecord history = dslContext.selectFrom(Tables.BCC)
//                .where(BCC.BCC_ID.eq(ULong.valueOf(bccId.value())))
//                .fetchOne();
//
//        history.setBccId(null);
//        if (bcc.getEntityType() != null) {
//            history.setEntityType(bcc.getEntityType());
//        }
//        history.setCardinalityMin(bcc.getCardinalityMin());
//        history.setCardinalityMax(bcc.getCardinalityMax());
//        history.setIsDeprecated((byte) ((bcc.isDeprecated()) ? 1 : 0));
//        history.setDefaultValue(bcc.getDefaultValue());
//        history.setDefinition(bcc.getDefinition());
//        history.setDefinitionSource(bcc.getDefinitionSource());
//        history.setCreatedBy(userId);
//        history.setLastUpdatedBy(userId);
//        history.setCreationTimestamp(timestamp);
//        history.setLastUpdateTimestamp(timestamp);
//
//        history = dslContext.insertInto(BCC).set(history).returning().fetchOne();
//        int result = dslContext.update(BCC_MANIFEST)
//                .set(BCC_MANIFEST.BCC_ID, history.getBccId())
//                .where(and(
//                        BCC_MANIFEST.BCC_ID.eq(ULong.valueOf(bcc.getBccId().value())),
//                        BCC_MANIFEST.RELEASE_ID.eq(extensionAcc.getReleaseId())
//                )).execute();
//
//        return (result == 1);
//    }

    @Transactional
    public void transferOwnership(ScoreUser requester, long accManifestId, String targetLoginId) {
        long targetAppUserId = dslContext.select(APP_USER.APP_USER_ID)
                .from(APP_USER)
                .where(APP_USER.LOGIN_ID.equalIgnoreCase(targetLoginId))
                .fetchOptionalInto(Long.class).orElse(0L);
        if (targetAppUserId == 0L) {
            throw new IllegalArgumentException("Not found a target user.");
        }

        AccManifestRecord accManifest =
                dslContext.selectFrom(ACC_MANIFEST)
                        .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(accManifestId)))
                        .fetchOptional().orElse(null);
        if (accManifest == null) {
            throw new IllegalArgumentException("Not found a target ACC.");
        }

        ULong target = ULong.valueOf(targetAppUserId);
        ULong userId = ULong.valueOf(requester.userId().value());
        LocalDateTime timestamp = LocalDateTime.now();

        updateAsccOwnerUserId(accManifest, target, userId, timestamp);
        updateBccOwnerUserId(accManifest, target, userId, timestamp);
        updateAccOwnerUserId(accManifest, target, userId, timestamp);
    }

    private void updateAccOwnerUserId(AccManifestRecord accManifest,
                                      ULong targetAppUserId,
                                      ULong userId, LocalDateTime timestamp) {

        AccRecord history = dslContext.selectFrom(Tables.ACC)
                .where(ACC.ACC_ID.eq(accManifest.getAccId()))
                .fetchOne();

        history.setAccId(null);
        history.setCreatedBy(userId);
        history.setLastUpdatedBy(userId);
        history.setCreationTimestamp(timestamp);
        history.setLastUpdateTimestamp(timestamp);
        history.setOwnerUserId(targetAppUserId);

        history = dslContext.insertInto(Tables.ACC).set(history).returning().fetchOne();
        dslContext.update(ACC_MANIFEST)
                .set(ACC_MANIFEST.ACC_ID, history.getAccId())
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(accManifest.getAccManifestId()))
                .execute();
    }

    private void updateAsccOwnerUserId(AccManifestRecord accManifest,
                                       ULong targetAppUserId,
                                       ULong userId, LocalDateTime timestamp) {

        Map<ULong, AsccManifestRecord> asccManifestRecordMap =
                dslContext.select(ASCC_MANIFEST.fields()).from(ASCC_MANIFEST)
                        .join(ACC_MANIFEST).on(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                        .where(ACC_MANIFEST.ACC_ID.eq(accManifest.getAccId()))
                        .fetchInto(AsccManifestRecord.class)
                        .stream().collect(Collectors.toMap(AsccManifestRecord::getAsccId, Function.identity()));

        if (asccManifestRecordMap.isEmpty()) {
            return;
        }

        Result<AsccRecord> asccRecordResult = dslContext.selectFrom(ASCC)
                .where(ASCC.ASCC_ID.in(asccManifestRecordMap.keySet()))
                .fetch();

        for (AsccRecord history : asccRecordResult) {
            AsccManifestRecord asccManifestRecord =
                    asccManifestRecordMap.get(history.getAsccId());

            history.setAsccId(null);
            history.setCreatedBy(userId);
            history.setLastUpdatedBy(userId);
            history.setCreationTimestamp(timestamp);
            history.setLastUpdateTimestamp(timestamp);
            history.setOwnerUserId(targetAppUserId);

            history = dslContext.insertInto(ASCC).set(history).returning().fetchOne();
            dslContext.update(ASCC_MANIFEST)
                    .set(ASCC_MANIFEST.ASCC_ID, history.getAsccId())
                    .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(asccManifestRecord.getAsccManifestId()))
                    .execute();
        }
    }

    private void updateBccOwnerUserId(AccManifestRecord accManifest,
                                      ULong targetAppUserId,
                                      ULong userId, LocalDateTime timestamp) {

        Map<ULong, BccManifestRecord> bccManifestRecordMap =
                dslContext.select(BCC_MANIFEST.fields()).from(BCC_MANIFEST)
                        .join(ACC_MANIFEST).on(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                        .where(ACC_MANIFEST.ACC_ID.eq(accManifest.getAccId()))
                        .fetchInto(BccManifestRecord.class)
                        .stream().collect(Collectors.toMap(BccManifestRecord::getBccId, Function.identity()));

        if (bccManifestRecordMap.isEmpty()) {
            return;
        }

        Result<BccRecord> bccRecordResult = dslContext.selectFrom(BCC)
                .where(BCC.BCC_ID.in(bccManifestRecordMap.keySet()))
                .fetch();

        for (BccRecord history : bccRecordResult) {
            BccManifestRecord bccManifestRecord =
                    bccManifestRecordMap.get(history.getBccId());

            history.setBccId(null);
            history.setCreatedBy(userId);
            history.setLastUpdatedBy(userId);
            history.setCreationTimestamp(timestamp);
            history.setLastUpdateTimestamp(timestamp);
            history.setOwnerUserId(targetAppUserId);

            history = dslContext.insertInto(BCC).set(history).returning().fetchOne();
            dslContext.update(BCC_MANIFEST)
                    .set(BCC_MANIFEST.BCC_ID, history.getBccId())
                    .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(bccManifestRecord.getBccManifestId()))
                    .execute();
        }
    }

}
