package org.oagi.score.gateway.http.api.cc_management.service;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.types.ULong;
import org.oagi.score.data.ACC;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.BieEditAcc;
import org.oagi.score.gateway.http.api.cc_management.data.CcType;
import org.oagi.score.gateway.http.api.cc_management.data.ExtensionUpdateRequest;
import org.oagi.score.gateway.http.api.cc_management.data.ExtensionUpdateResponse;
import org.oagi.score.gateway.http.api.cc_management.data.node.*;
import org.oagi.score.gateway.http.api.cc_management.repository.CcNodeRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.ManifestRepository;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.gateway.http.helper.ScoreGuid;
import org.oagi.score.gateway.http.helper.Utility;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.repo.component.acc.*;
import org.oagi.score.repo.component.ascc.AsccWriteRepository;
import org.oagi.score.repo.component.ascc.CreateAsccRepositoryRequest;
import org.oagi.score.repo.component.ascc.CreateAsccRepositoryResponse;
import org.oagi.score.repo.component.asccp.AsccpWriteRepository;
import org.oagi.score.repo.component.asccp.CreateAsccpRepositoryRequest;
import org.oagi.score.repo.component.asccp.CreateAsccpRepositoryResponse;
import org.oagi.score.service.common.data.AccessPrivilege;
import org.oagi.score.service.common.data.AppUser;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.service.common.data.OagisComponentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Service
@Transactional(readOnly = true)
public class ExtensionService {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private CcNodeRepository repository;

    @Autowired
    private CcNodeService service;

    @Autowired
    private ManifestRepository manifestRepository;

    @Autowired
    private CcListService ccListService;

    @Autowired
    private CcNodeService ccNodeService;

    private AccManifestRecord getExtensionAcc(BigInteger manifestId) {
        return dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(manifestId)))
                .fetchOptional().orElse(null);
    }

    public CcAccNode getExtensionNode(AuthenticatedPrincipal user, BigInteger manifestId) {
        AccManifestRecord extensionAcc = getExtensionAcc(manifestId);
        CcAccNode ueAcc = repository.getAccNodeByAccManifestId(user, extensionAcc.getAccManifestId().toBigInteger());

        AppUser requester = sessionService.getAppUserByUsername(user);
        BigInteger ownerUserId = dslContext.select(ACC.OWNER_USER_ID).from(ACC)
                .where(ACC.ACC_ID.eq(ULong.valueOf(ueAcc.getAccId()))).fetchOneInto(BigInteger.class);
        AppUser owner = sessionService.getAppUserByUserId(ownerUserId);
        boolean isWorkingRelease = "Working".equals(ueAcc.getReleaseNum());
        ueAcc.setWorkingRelease(isWorkingRelease);
        AccessPrivilege accessPrivilege = AccessPrivilege.toAccessPrivilege(requester, owner, ueAcc.getState(), isWorkingRelease);
        ueAcc.setAccess(accessPrivilege);
        return ueAcc;
    }

    public ACC getExistsUserExtension(BigInteger accManifestId) {
        List<Field> fields = new ArrayList();
        fields.add(ACC_MANIFEST.ACC_MANIFEST_ID);
        fields.addAll(Arrays.asList(ACC.fields()));

        ACC ueAcc =
                dslContext.select(fields)
                        .from(ACC.as("eAcc"))
                        .join(ACC_MANIFEST.as("eACCRM")).on(ACC.as("eAcc").ACC_ID.eq(ACC_MANIFEST.as("eACCRM").ACC_ID))
                        .join(ASCC_MANIFEST).on(ACC_MANIFEST.as("eACCRM").ACC_MANIFEST_ID.eq(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID))
                        .join(ASCCP_MANIFEST).on(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                        .join(ACC_MANIFEST).on(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID))
                        .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                        .where(and(
                                ACC_MANIFEST.as("eACCRM").ACC_MANIFEST_ID.eq(ULong.valueOf(accManifestId)),
                                ACC.OAGIS_COMPONENT_TYPE.eq(OagisComponentType.UserExtensionGroup.getValue())
                        )).fetchOneInto(ACC.class);
        return ueAcc;
    }

    @Transactional
    public BigInteger appendUserExtension(BieEditAcc eAcc, ACC ueAcc,
                                          BigInteger releaseId, AuthenticatedPrincipal user) {
        AppUser appUser = sessionService.getAppUserByUsername(user);
        if (appUser.isDeveloper()) {
            throw new IllegalArgumentException("Developer cannot create User Extension.");
        }

        if (ueAcc != null) {
            if (ueAcc.getState() == CcState.Production) {
                AccManifestRecord accManifest = repository.getAccManifestByAcc(ueAcc.getAccId(), releaseId);
                ReviseAccRepositoryRequest reviseAccRepositoryRequest =
                        new ReviseAccRepositoryRequest(user, accManifest.getAccManifestId().toBigInteger());
                ReviseAccRepositoryResponse reviseAccRepositoryResponse =
                        accWriteRepository.reviseAcc(reviseAccRepositoryRequest);
                return reviseAccRepositoryResponse.getAccManifestId();
            } else {
                AccManifestRecord ueAccManifest = repository.getAccManifestByAcc(ueAcc.getAccId(), releaseId);
                return ueAccManifest.getAccManifestId().toBigInteger();
            }
        } else {
            return createNewUserExtensionGroupACC(ccListService.getAcc(eAcc.getAccManifestId()), releaseId, user);
        }
    }

    @Autowired
    private AccWriteRepository accWriteRepository;

    @Autowired
    private AsccpWriteRepository asccpWriteRepository;

    @Autowired
    private AsccWriteRepository asccWriteRepository;

    private BigInteger createNewUserExtensionGroupACC(ACC eAcc, BigInteger releaseId, AuthenticatedPrincipal user) {
        LocalDateTime timestamp = LocalDateTime.now();
        CreateAccRepositoryRequest createUeAccRequest = new CreateAccRepositoryRequest(user, timestamp, releaseId);

        String objectClassTerm = Utility.getUserExtensionGroupObjectClassTerm(eAcc.getObjectClassTerm());
        createUeAccRequest.setInitialObjectClassTerm(objectClassTerm);
        createUeAccRequest.setInitialComponentType(OagisComponentType.UserExtensionGroup);
        createUeAccRequest.setInitialDefinition("A system created component containing user extension to the " + eAcc.getObjectClassTerm() + ".");

        CreateAccRepositoryResponse createUeAccResponse =
                accWriteRepository.createAcc(createUeAccRequest);

        CreateAsccpRepositoryRequest createAsccpRepositoryRequest = new CreateAsccpRepositoryRequest(
                user, timestamp, createUeAccResponse.getAccManifestId(), releaseId);

        createAsccpRepositoryRequest.setInitialPropertyTerm(objectClassTerm);
        createAsccpRepositoryRequest.setReusable(false);
        createAsccpRepositoryRequest.setDefinition("A system created component containing user extension to the " + eAcc.getObjectClassTerm() + ".");
        createAsccpRepositoryRequest.setInitialState(CcState.Production);
        CreateAsccpRepositoryResponse createUeAsccpRepositoryResponse =
                asccpWriteRepository.createAsccp(createAsccpRepositoryRequest);

        CreateAsccRepositoryRequest createAsccRepositoryRequest = new CreateAsccRepositoryRequest(
                user, timestamp, releaseId,
                eAcc.getAccManifestId(), createUeAsccpRepositoryResponse.getAsccpManifestId()
        );
        createAsccRepositoryRequest.setInitialState(CcState.Production);
        createAsccRepositoryRequest.setCardinalityMin(1);
        createAsccRepositoryRequest.setCardinalityMax(1);

        CreateAsccRepositoryResponse createAsccRepositoryResponse =
                asccWriteRepository.createAscc(createAsccRepositoryRequest);

        return createUeAccResponse.getAccManifestId();
    }

    private AccRecord createACCForExtension(ACC eAcc, AuthenticatedPrincipal user) {
        String objectClassTerm = Utility.getUserExtensionGroupObjectClassTerm(eAcc.getObjectClassTerm());
        ULong userId = ULong.valueOf(sessionService.userId(user));
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
                ScoreGuid.randomGuid(),
                objectClassTerm,
                "A system created component containing user extension to the " + eAcc.getObjectClassTerm() + ".",
                OagisComponentType.UserExtensionGroup.getValue(),
                userId,
                userId,
                userId,
                timestamp,
                timestamp,
                CcState.WIP.name()
        ).returning().fetchOne();
    }

    private AccManifestRecord createACCManifestForExtension(AccRecord ueAcc, BigInteger releaseId) {
        return dslContext.insertInto(ACC_MANIFEST,
                ACC_MANIFEST.ACC_ID,
                ACC_MANIFEST.RELEASE_ID
        ).values(
                ueAcc.getAccId(),
                ULong.valueOf(releaseId)
        ).returning().fetchOne();
    }

    private AsccpRecord createASCCPForExtension(ACC eAcc, AuthenticatedPrincipal user, AccRecord ueAcc) {
        ULong userId = ULong.valueOf(sessionService.userId(user));
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
                ScoreGuid.randomGuid(),
                ueAcc.getObjectClassTerm(),
                ueAcc.getAccId(),
                "A system created component containing user extension to the " + eAcc.getObjectClassTerm() + ".",
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
            AsccpRecord ueAsccp, AccManifestRecord ueAccManifest, BigInteger releaseId) {
        return dslContext.insertInto(ASCCP_MANIFEST,
                ASCCP_MANIFEST.ASCCP_ID,
                ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID,
                ASCCP_MANIFEST.RELEASE_ID
        ).values(
                ueAsccp.getAsccpId(),
                ueAccManifest.getAccManifestId(),
                ULong.valueOf(releaseId)
        ).returning().fetchOne();
    }

    private AsccRecord createASCCForExtension(ACC eAcc, AsccpRecord ueAsccp, AuthenticatedPrincipal user) {
        ULong userId = ULong.valueOf(sessionService.userId(user));
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
                ScoreGuid.randomGuid(),
                0,
                1,
                1,
                ULong.valueOf(eAcc.getAccId()),
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
            AsccRecord ueAscc, AccManifestRecord eAccManifest, AsccpManifestRecord ueAsccpManifest, BigInteger releaseId) {
        dslContext.insertInto(ASCC_MANIFEST,
                ASCC_MANIFEST.ASCC_ID,
                ASCC_MANIFEST.FROM_ACC_MANIFEST_ID,
                ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID,
                ASCC_MANIFEST.RELEASE_ID
        ).values(
                ueAscc.getAsccId(),
                eAccManifest.getAccManifestId(),
                ueAsccpManifest.getAsccpManifestId(),
                ULong.valueOf(releaseId)
        ).execute();
    }

    @Transactional
    public void appendAsccp(AuthenticatedPrincipal user, BigInteger manifestId, BigInteger asccpManifestId) {
        AccManifestRecord extensionAcc = getExtensionAcc(manifestId);
        AsccpManifestRecord asccpManifestRecord =
                dslContext.selectFrom(ASCCP_MANIFEST)
                        .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ULong.valueOf(asccpManifestId)))
                        .fetchOne();

        ccNodeService.appendAsccp(user, asccpManifestRecord.getReleaseId().toBigInteger(),
                extensionAcc.getAccManifestId().toBigInteger(),
                asccpManifestRecord.getAsccpManifestId().toBigInteger(), -1);
    }

    @Transactional
    public void appendBccp(AuthenticatedPrincipal user, BigInteger manifestId, BigInteger bccpManifestId) {
        AccManifestRecord extensionAcc = getExtensionAcc(manifestId);
        BccpManifestRecord bccpManifestRecord =
                dslContext.selectFrom(BCCP_MANIFEST)
                        .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(ULong.valueOf(bccpManifestId)))
                        .fetchOne();

        ccNodeService.appendBccp(user, bccpManifestRecord.getReleaseId().toBigInteger(),
                extensionAcc.getAccManifestId().toBigInteger(),
                bccpManifestRecord.getBccpManifestId().toBigInteger(), -1);
    }

    @Transactional
    public void updateState(AuthenticatedPrincipal user, BigInteger manifestId, CcState state) {
        ccNodeService.updateAccState(user, manifestId, state);
    }

    @Transactional
    public void purgeExtension(AuthenticatedPrincipal user, BigInteger manifestId) {

        AccManifestRecord extensionAccManifestRecord = dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(manifestId)))
                .fetchOne();

        AsccpManifestRecord groupAsccpManifestRecord = dslContext.selectFrom(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID.eq(extensionAccManifestRecord.getAccManifestId()))
                .fetchOne();

        AsccManifestRecord asccManifestRecord = dslContext.selectFrom(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(groupAsccpManifestRecord.getAsccpManifestId()))
                .fetchOne();

        ccNodeService.deleteAscc(user, asccManifestRecord.getAsccManifestId().toBigInteger(), true);

        ccNodeService.purgeAsccp(user, groupAsccpManifestRecord.getAsccpManifestId().toBigInteger(), false, true);

        ccNodeService.purgeAcc(user, manifestId);
    }

    @Transactional
    public ExtensionUpdateResponse updateDetails(AuthenticatedPrincipal user, ExtensionUpdateRequest request) {
        ExtensionUpdateResponse response = new ExtensionUpdateResponse();

        AccManifestRecord extensionAcc = getExtensionAcc(request.getManifestId());
        ULong userId = ULong.valueOf(sessionService.userId(user));
        LocalDateTime timestamp = LocalDateTime.now();

        List<CcAsccpNodeDetail.Ascc> asccList = request.getAsccpDetails().stream()
                .map(asccpDetail -> asccpDetail.getAscc())
                .collect(Collectors.toList());

        for (CcAsccpNodeDetail.Ascc ascc : asccList) {
            response.getAsccResults().put(ascc.getAsccId().longValue(),
                    updateAscc(extensionAcc, ascc, userId, timestamp)
            );
        }

        List<CcBccpNodeDetail.Bcc> bccList = request.getBccpDetails().stream()
                .map(bccpDetail -> bccpDetail.getBcc())
                .collect(Collectors.toList());

        for (CcBccpNodeDetail.Bcc bcc : bccList) {
            response.getBccResults().put(bcc.getBccId().longValue(),
                    updateBcc(extensionAcc, bcc, userId, timestamp)
            );
        }

        return response;
    }

    private boolean updateAscc(AccManifestRecord extensionAcc,
                               CcAsccpNodeDetail.Ascc ascc,
                               ULong userId, LocalDateTime timestamp) {

        String guid = dslContext.select(ASCC.GUID).from(ASCC)
                .where(ASCC.ASCC_ID.eq(ULong.valueOf(ascc.getAsccId())))
                .fetchOneInto(String.class);

        ULong asccId = dslContext.select(ASCC.ASCC_ID).from(ASCC)
                .where(ASCC.GUID.eq(guid))
                .orderBy(ASCC.ASCC_ID.desc()).limit(1).fetchOneInto(ULong.class);

        AsccRecord history = dslContext.selectFrom(Tables.ASCC)
                .where(ASCC.ASCC_ID.eq(asccId))
                .fetchOne();

        history.setAsccId(null);
        history.setCardinalityMin(ascc.getCardinalityMin());
        history.setCardinalityMax(ascc.getCardinalityMax());
        history.setIsDeprecated((byte) ((ascc.isDeprecated()) ? 1 : 0));
        history.setDefinition(ascc.getDefinition());
        history.setDefinitionSource(ascc.getDefinitionSource());
        history.setCreatedBy(userId);
        history.setLastUpdatedBy(userId);
        history.setCreationTimestamp(timestamp);
        history.setLastUpdateTimestamp(timestamp);

        history = dslContext.insertInto(ASCC).set(history).returning().fetchOne();
        int result = dslContext.update(ASCC_MANIFEST)
                .set(ASCC_MANIFEST.ASCC_ID, history.getAsccId())
                .where(and(
                        ASCC_MANIFEST.ASCC_ID.eq(ULong.valueOf(ascc.getAsccId())),
                        ASCC_MANIFEST.RELEASE_ID.eq(extensionAcc.getReleaseId())
                )).execute();

        return (result == 1);
    }

    private boolean updateBcc(AccManifestRecord extensionAcc,
                              CcBccpNodeDetail.Bcc bcc,
                              ULong userId, LocalDateTime timestamp) {

        String guid = dslContext.select(BCC.GUID).from(BCC)
                .where(BCC.BCC_ID.eq(ULong.valueOf(bcc.getBccId())))
                .fetchOneInto(String.class);

        ULong bccId = dslContext.select(BCC.BCC_ID).from(BCC)
                .where(BCC.GUID.eq(guid))
                .orderBy(BCC.BCC_ID.desc()).limit(1).fetchOneInto(ULong.class);

        BccRecord history = dslContext.selectFrom(Tables.BCC)
                .where(BCC.BCC_ID.eq(bccId))
                .fetchOne();

        history.setBccId(null);
        if (bcc.getEntityType() != null) {
            history.setEntityType(bcc.getEntityType());
        }
        history.setCardinalityMin(bcc.getCardinalityMin());
        history.setCardinalityMax(bcc.getCardinalityMax());
        history.setIsDeprecated((byte) ((bcc.isDeprecated()) ? 1 : 0));
        history.setDefaultValue(bcc.getDefaultValue());
        history.setDefinition(bcc.getDefinition());
        history.setDefinitionSource(bcc.getDefinitionSource());
        history.setCreatedBy(userId);
        history.setLastUpdatedBy(userId);
        history.setCreationTimestamp(timestamp);
        history.setLastUpdateTimestamp(timestamp);

        history = dslContext.insertInto(BCC).set(history).returning().fetchOne();
        int result = dslContext.update(BCC_MANIFEST)
                .set(BCC_MANIFEST.BCC_ID, history.getBccId())
                .where(and(
                        BCC_MANIFEST.BCC_ID.eq(ULong.valueOf(bcc.getBccId())),
                        BCC_MANIFEST.RELEASE_ID.eq(extensionAcc.getReleaseId())
                )).execute();

        return (result == 1);
    }

    @Transactional
    public void transferOwnership(AuthenticatedPrincipal user, long accManifestId, String targetLoginId) {
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
        ULong userId = ULong.valueOf(sessionService.userId(user));
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

    public CcNode getLastRevisionCc(AuthenticatedPrincipal user, String type, BigInteger manifestId) {

        if (type.equals(CcType.ASCC.name())) {
            AsccManifestRecord asccManifest = manifestRepository.getAsccManifestById(manifestId);
            if (asccManifest == null) {
                return null;
            }
            String guid = dslContext.select(ASCC.GUID).from(ASCC)
                    .where(ASCC.ASCC_ID.eq(asccManifest.getAsccId()))
                    .fetchOneInto(String.class);
            return dslContext.select(
                    ASCC.ASCC_ID,
                    ASCC.GUID,
                    ASCC.CARDINALITY_MIN,
                    ASCC.CARDINALITY_MAX).from(ASCC)
                    .where(and(ASCC.GUID.eq(guid), ASCC.STATE.eq(CcState.Published.name())))
                    .orderBy(ASCC.ASCC_ID.desc()).limit(1)
                    .fetchOneInto(CcAsccNode.class);
        } else if (type.equals(CcType.BCC.name())) {
            BccManifestRecord bccManifest = manifestRepository.getBccManifestById(manifestId);
            if (bccManifest == null) {
                return null;
            }
            String guid = dslContext.select(BCC.GUID).from(BCC)
                    .where(BCC.BCC_ID.eq(bccManifest.getBccId()))
                    .fetchOneInto(String.class);
            return dslContext.select(BCC.BCC_ID,
                    BCC.GUID,
                    BCC.CARDINALITY_MIN,
                    BCC.CARDINALITY_MAX,
                    BCC.IS_NILLABLE.as("nillable")).from(BCC)
                    .where(and(BCC.GUID.eq(guid), BCC.STATE.eq(CcState.Published.name())))
                    .orderBy(BCC.BCC_ID.desc()).limit(1)
                    .fetchOneInto(CcBccNode.class);
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
