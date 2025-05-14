package org.oagi.score.e2e.impl.api;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.oagi.score.e2e.api.APIFactory;
import org.oagi.score.e2e.api.CoreComponentAPI;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.*;
import org.oagi.score.e2e.obj.*;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.*;
import static org.oagi.score.e2e.obj.ComponentType.*;

public class DSLContextCoreComponentAPIImpl implements CoreComponentAPI {

    private final DSLContext dslContext;

    private final APIFactory apiFactory;

    public DSLContextCoreComponentAPIImpl(DSLContext dslContext, APIFactory apiFactory) {
        this.dslContext = dslContext;
        this.apiFactory = apiFactory;
    }

    @Override
    public ACCObject getACCByManifestId(BigInteger accManifestId) {
        List<Field<?>> fields = new ArrayList();
        fields.add(ACC_MANIFEST.ACC_MANIFEST_ID);
        fields.add(ACC_MANIFEST.BASED_ACC_MANIFEST_ID);
        fields.add(ACC_MANIFEST.RELEASE_ID);
        fields.add(ACC_MANIFEST.DEN);
        fields.addAll(Arrays.asList(ACC.fields()));
        return dslContext.select(fields)
                .from(ACC_MANIFEST)
                .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(accManifestId)))
                .fetchOne(record -> accMapper(record));
    }

    @Override
    public ACCObject getACCByDENAndReleaseNum(LibraryObject library, String den, String releaseNum) {
        ULong releaseId = getReleaseIdByReleaseNum(library, releaseNum);
        List<Field<?>> fields = new ArrayList();
        fields.add(ACC_MANIFEST.ACC_MANIFEST_ID);
        fields.add(ACC_MANIFEST.BASED_ACC_MANIFEST_ID);
        fields.add(ACC_MANIFEST.RELEASE_ID);
        fields.add(ACC_MANIFEST.DEN);
        fields.addAll(Arrays.asList(ACC.fields()));
        return dslContext.select(fields)
                .from(ACC_MANIFEST)
                .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                .where(and(
                        ACC_MANIFEST.RELEASE_ID.eq(releaseId),
                        ACC_MANIFEST.DEN.eq(den)))
                .fetchOne(record -> accMapper(record));
    }

    private ACCObject accMapper(org.jooq.Record record) {
        ACCObject acc = new ACCObject();
        acc.setAccManifestId(record.get(ACC_MANIFEST.ACC_MANIFEST_ID).toBigInteger());
        acc.setReleaseId(record.get(ACC_MANIFEST.RELEASE_ID).toBigInteger());
        acc.setAccId(record.get(ACC.ACC_ID).toBigInteger());
        acc.setGuid(record.get(ACC.GUID));
        acc.setObjectClassTerm(record.get(ACC.OBJECT_CLASS_TERM));
        acc.setDen(record.get(ACC_MANIFEST.DEN));
        acc.setDefinition(record.get(ACC.DEFINITION));
        acc.setDefinitionSource(record.get(ACC.DEFINITION_SOURCE));
        if (record.get(ACC.NAMESPACE_ID) != null) {
            acc.setNamespaceId(record.get(ACC.NAMESPACE_ID).toBigInteger());
        }
        acc.setAbstract(record.get(ACC.IS_ABSTRACT) == 1);
        acc.setDeprecated(record.get(ACC.IS_DEPRECATED) == 1);
        acc.setState(record.get(ACC.STATE));
        String den = record.get(ACC_MANIFEST.DEN);
        if (den.contains("User Extension Group")) {
            acc.setLocalExtension(true);
        }
        acc.setOwnerUserId(record.get(ACC.OWNER_USER_ID).toBigInteger());
        acc.setCreatedBy(record.get(ACC.CREATED_BY).toBigInteger());
        acc.setLastUpdatedBy(record.get(ACC.LAST_UPDATED_BY).toBigInteger());
        acc.setCreationTimestamp(record.get(ACC.CREATION_TIMESTAMP));
        acc.setLastUpdateTimestamp(record.get(ACC.LAST_UPDATE_TIMESTAMP));
        return acc;
    }

    @Override
    public ASCCPObject getASCCPByManifestId(BigInteger asccpManifestId) {
        List<Field<?>> fields = new ArrayList();
        fields.add(ASCCP_MANIFEST.ASCCP_MANIFEST_ID);
        fields.add(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID);
        fields.add(ASCCP_MANIFEST.RELEASE_ID);
        fields.add(ASCCP_MANIFEST.DEN);
        fields.addAll(Arrays.asList(ASCCP.fields()));
        return dslContext.select(fields)
                .from(ASCCP_MANIFEST)
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ULong.valueOf(asccpManifestId)))
                .fetchOne(record -> asccpMapper(record));
    }

    @Override
    public ASCCPObject getASCCPByDENAndReleaseNum(LibraryObject library, String den, String releaseNum) {
        ULong releaseId = getReleaseIdByReleaseNum(library, releaseNum);
        List<Field<?>> fields = new ArrayList();
        fields.add(ASCCP_MANIFEST.ASCCP_MANIFEST_ID);
        fields.add(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID);
        fields.add(ASCCP_MANIFEST.RELEASE_ID);
        fields.add(ASCCP_MANIFEST.DEN);
        fields.addAll(Arrays.asList(ASCCP.fields()));
        return dslContext.select(fields)
                .from(ASCCP_MANIFEST)
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .where(and(
                        ASCCP_MANIFEST.RELEASE_ID.eq(releaseId),
                        ASCCP_MANIFEST.DEN.eq(den)))
                .fetchOne(record -> asccpMapper(record));
    }

    private ASCCPObject asccpMapper(org.jooq.Record record) {
        ASCCPObject asccp = new ASCCPObject();
        asccp.setAsccpManifestId(record.get(ASCCP_MANIFEST.ASCCP_MANIFEST_ID).toBigInteger());
        asccp.setReleaseId(record.get(ASCCP_MANIFEST.RELEASE_ID).toBigInteger());
        asccp.setAsccpId(record.get(ASCCP.ASCCP_ID).toBigInteger());
        asccp.setGuid(record.get(ASCCP.GUID));
        asccp.setPropertyTerm(record.get(ASCCP.PROPERTY_TERM));
        asccp.setDen(record.get(ASCCP_MANIFEST.DEN));
        asccp.setDefinition(record.get(ASCCP.DEFINITION));
        asccp.setDefinitionSource(record.get(ASCCP.DEFINITION_SOURCE));
        asccp.setRoleOfAccManifestId(record.get(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID).toBigInteger());
        if (record.get(ASCCP.NAMESPACE_ID) != null) {
            asccp.setNamespaceId(record.get(ASCCP.NAMESPACE_ID).toBigInteger());
        }
        asccp.setState(record.get(ASCCP.STATE));
        asccp.setDeprecated(record.get(ASCCP.IS_DEPRECATED) == 1);
        asccp.setOwnerUserId(record.get(ASCCP.OWNER_USER_ID).toBigInteger());
        asccp.setCreatedBy(record.get(ASCCP.CREATED_BY).toBigInteger());
        asccp.setLastUpdatedBy(record.get(ASCCP.LAST_UPDATED_BY).toBigInteger());
        asccp.setCreationTimestamp(record.get(ASCCP.CREATION_TIMESTAMP));
        asccp.setLastUpdateTimestamp(record.get(ASCCP.LAST_UPDATE_TIMESTAMP));
        return asccp;
    }

    @Override
    public BCCPObject getBCCPByManifestId(BigInteger bccpManifestId) {
        List<Field<?>> fields = new ArrayList();
        fields.add(BCCP_MANIFEST.BCCP_MANIFEST_ID);
        fields.add(BCCP_MANIFEST.BDT_MANIFEST_ID);
        fields.add(BCCP_MANIFEST.RELEASE_ID);
        fields.add(BCCP_MANIFEST.DEN);
        fields.addAll(Arrays.asList(BCCP.fields()));
        return dslContext.select(fields)
                .from(BCCP_MANIFEST)
                .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(ULong.valueOf(bccpManifestId)))
                .fetchOne(record -> bccpMapper(record));
    }

    @Override
    public BCCPObject getBCCPByDENAndReleaseNum(LibraryObject library, String den, String releaseNum) {
        ULong releaseId = getReleaseIdByReleaseNum(library, releaseNum);
        List<Field<?>> fields = new ArrayList();
        fields.add(BCCP_MANIFEST.BCCP_MANIFEST_ID);
        fields.add(BCCP_MANIFEST.BDT_MANIFEST_ID);
        fields.add(BCCP_MANIFEST.RELEASE_ID);
        fields.add(BCCP_MANIFEST.DEN);
        fields.addAll(Arrays.asList(BCCP.fields()));
        return dslContext.select(fields)
                .from(BCCP_MANIFEST)
                .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                .where(and(
                        BCCP_MANIFEST.RELEASE_ID.eq(releaseId),
                        BCCP_MANIFEST.DEN.eq(den)))
                .fetchOne(record -> bccpMapper(record));
    }

    private BCCPObject bccpMapper(org.jooq.Record record) {
        BCCPObject bccp = new BCCPObject();
        bccp.setBccpManifestId(record.get(BCCP_MANIFEST.BCCP_MANIFEST_ID).toBigInteger());
        bccp.setReleaseId(record.get(BCCP_MANIFEST.RELEASE_ID).toBigInteger());
        bccp.setBccpId(record.get(BCCP.BCCP_ID).toBigInteger());
        bccp.setGuid(record.get(BCCP.GUID));
        bccp.setPropertyTerm(record.get(BCCP.PROPERTY_TERM));
        bccp.setRepresentationTerm(record.get(BCCP.REPRESENTATION_TERM));
        bccp.setDen(record.get(BCCP_MANIFEST.DEN));
        bccp.setDefinition(record.get(BCCP.DEFINITION));
        bccp.setDefinitionSource(record.get(BCCP.DEFINITION_SOURCE));
        if (record.get(BCCP.NAMESPACE_ID) != null) {
            bccp.setNamespaceId(record.get(BCCP.NAMESPACE_ID).toBigInteger());
        }
        bccp.setState(record.get(BCCP.STATE));
        bccp.setDeprecated(record.get(BCCP.IS_DEPRECATED) == 1);
        bccp.setOwnerUserId(record.get(BCCP.OWNER_USER_ID).toBigInteger());
        bccp.setCreatedBy(record.get(BCCP.CREATED_BY).toBigInteger());
        bccp.setLastUpdatedBy(record.get(BCCP.LAST_UPDATED_BY).toBigInteger());
        bccp.setCreationTimestamp(record.get(BCCP.CREATION_TIMESTAMP));
        bccp.setLastUpdateTimestamp(record.get(BCCP.LAST_UPDATE_TIMESTAMP));
        return bccp;
    }

    @Override
    public DTObject getCDTByManifestId(BigInteger dtManifestId) {
        return getDTByManifestId(dtManifestId);
    }

    @Override
    public DTObject getCDTByDENAndReleaseNum(LibraryObject library, String den, String releaseNum) {
        ULong releaseId = getReleaseIdByReleaseNum(library, releaseNum);
        List<Field<?>> fields = new ArrayList();
        fields.add(DT_MANIFEST.DT_MANIFEST_ID);
        fields.add(DT_MANIFEST.BASED_DT_MANIFEST_ID);
        fields.add(DT_MANIFEST.RELEASE_ID);
        fields.add(DT_MANIFEST.DEN);
        fields.addAll(Arrays.asList(DT.fields()));
        return dslContext.select(fields)
                .from(DT_MANIFEST)
                .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .where(and(
                        DT_MANIFEST.BASED_DT_MANIFEST_ID.isNull(),
                        DT_MANIFEST.RELEASE_ID.eq(releaseId),
                        DT_MANIFEST.DEN.eq(den)))
                .fetchOne(record -> dtMapper(record));
    }

    @Override
    public DTObject getBDTByManifestId(BigInteger dtManifestId) {
        return getDTByManifestId(dtManifestId);
    }

    @Override
    public DTObject getBDTByGuidAndReleaseNum(LibraryObject library, String guid, String releaseNum) {
        ULong releaseId = getReleaseIdByReleaseNum(library, releaseNum);
        List<Field<?>> fields = new ArrayList();
        fields.add(DT_MANIFEST.DT_MANIFEST_ID);
        fields.add(DT_MANIFEST.BASED_DT_MANIFEST_ID);
        fields.add(DT_MANIFEST.RELEASE_ID);
        fields.add(DT_MANIFEST.DEN);
        fields.addAll(Arrays.asList(DT.fields()));
        return dslContext.select(fields)
                .from(DT_MANIFEST)
                .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .where(and(
                        DT_MANIFEST.BASED_DT_MANIFEST_ID.isNotNull(),
                        DT_MANIFEST.RELEASE_ID.eq(releaseId),
                        DT.GUID.eq(guid)))
                .fetchOne(record -> dtMapper(record));
    }

    @Override
    public List<DTObject> getBDTByDENAndReleaseNum(LibraryObject library, String den, String releaseNum) {
        ULong releaseId = getReleaseIdByReleaseNum(library, releaseNum);
        List<Field<?>> fields = new ArrayList();
        fields.add(DT_MANIFEST.DT_MANIFEST_ID);
        fields.add(DT_MANIFEST.BASED_DT_MANIFEST_ID);
        fields.add(DT_MANIFEST.RELEASE_ID);
        fields.add(DT_MANIFEST.DEN);
        fields.addAll(Arrays.asList(DT.fields()));
        return dslContext.select(fields)
                .from(DT_MANIFEST)
                .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .where(and(
                        DT_MANIFEST.BASED_DT_MANIFEST_ID.isNotNull(),
                        DT_MANIFEST.RELEASE_ID.eq(releaseId),
                        DT_MANIFEST.DEN.eq(den)))
                .fetch(record -> dtMapper(record));
    }

    private DTObject getDTByManifestId(BigInteger dtManifestId) {
        List<Field<?>> fields = new ArrayList();
        fields.add(DT_MANIFEST.DT_MANIFEST_ID);
        fields.add(DT_MANIFEST.BASED_DT_MANIFEST_ID);
        fields.add(DT_MANIFEST.RELEASE_ID);
        fields.add(DT_MANIFEST.DEN);
        fields.addAll(Arrays.asList(DT.fields()));
        return dslContext.select(fields)
                .from(DT_MANIFEST)
                .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .where(DT_MANIFEST.DT_MANIFEST_ID.eq(ULong.valueOf(dtManifestId)))
                .fetchOne(record -> dtMapper(record));
    }

    public ACCObject createRandomACC(AppUserObject creator, ReleaseObject release,
                                     NamespaceObject namespace, String state,
                                     ComponentType type, String objectClassTerm) {
        LibraryObject library = apiFactory.getLibraryAPI().getLibraryById(release.getLibraryId());
        ACCObject basedAcc = null;
        ACCObject acc = ACCObject.createRandomACC(creator, namespace, state);
        acc.setComponentType(type);
        if (objectClassTerm != null) {
            acc.setObjectClassTerm(objectClassTerm);
            acc.setDen(objectClassTerm + ". Details");
        }
        acc.setReleaseId(release.getReleaseId());
        AccRecord accRecord = new AccRecord();
        accRecord.setGuid(acc.getGuid());
        accRecord.setOagisComponentType(type.getValue());
        if (type == Extension) {
            accRecord.setType("Extension");
            basedAcc = getACCByDENAndReleaseNum(library, "All Extension. Details", release.getReleaseNumber());
        } else {
            accRecord.setType("Default");
        }
        accRecord.setObjectClassTerm(acc.getObjectClassTerm());
        accRecord.setDefinition(acc.getDefinition());
        accRecord.setDefinitionSource(acc.getDefinitionSource());
        accRecord.setNamespaceId(ULong.valueOf(acc.getNamespaceId()));
        accRecord.setIsAbstract((byte) (acc.isAbstract() ? 1 : 0));
        accRecord.setIsDeprecated((byte) (acc.isDeprecated() ? 1 : 0));
        accRecord.setState(acc.getState());
        accRecord.setOwnerUserId(ULong.valueOf(acc.getOwnerUserId()));
        accRecord.setCreatedBy(ULong.valueOf(acc.getCreatedBy()));
        accRecord.setLastUpdatedBy(ULong.valueOf(acc.getLastUpdatedBy()));
        accRecord.setCreationTimestamp(acc.getCreationTimestamp());
        accRecord.setLastUpdateTimestamp(acc.getLastUpdateTimestamp());

        if (basedAcc != null) {
            accRecord.setBasedAccId(ULong.valueOf(basedAcc.getAccId()));
        }

        ULong accId = dslContext.insertInto(ACC)
                .set(accRecord)
                .returning(ACC.ACC_ID)
                .fetchOne().getAccId();
        acc.setAccId(accId.toBigInteger());

        LogRecord dummyLogRecord = new LogRecord();
        dummyLogRecord.setHash(UUID.randomUUID().toString().replaceAll("-", ""));
        dummyLogRecord.setRevisionNum(UInteger.valueOf(1));
        dummyLogRecord.setRevisionTrackingNum(UInteger.valueOf(1));
        dummyLogRecord.setLogAction("Added");
        dummyLogRecord.setReference(acc.getGuid());
        dummyLogRecord.setSnapshot("{\"component\": \"acc\"}");
        dummyLogRecord.setCreatedBy(ULong.valueOf(acc.getCreatedBy()));
        dummyLogRecord.setCreationTimestamp(acc.getCreationTimestamp());

        ULong logId = dslContext.insertInto(LOG)
                .set(dummyLogRecord)
                .returning(LOG.LOG_ID)
                .fetchOne().getLogId();

        AccManifestRecord accManifestRecord = new AccManifestRecord();
        if (basedAcc != null) {
            accManifestRecord.setBasedAccManifestId(ULong.valueOf(basedAcc.getAccManifestId()));
        }
        accManifestRecord.setDen(acc.getDen());
        accManifestRecord.setReleaseId(ULong.valueOf(release.getReleaseId()));
        accManifestRecord.setAccId(accId);
        accManifestRecord.setLogId(logId);

        ULong accManifestId = dslContext.insertInto(ACC_MANIFEST)
                .set(accManifestRecord)
                .returning(ACC_MANIFEST.ACC_MANIFEST_ID)
                .fetchOne().getAccManifestId();
        acc.setAccManifestId(accManifestId.toBigInteger());

        if ("Working".equals(release.getReleaseNumber()) && "Published".equals(state)) {
            accManifestRecord = dslContext.selectFrom(ACC_MANIFEST)
                    .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(accManifestId))
                    .fetchOne();

            ReleaseObject latestRelease = apiFactory.getReleaseAPI().getTheLatestRelease(
                    apiFactory.getLibraryAPI().getLibraryById(release.getLibraryId())
            );
            AccManifestRecord prevAccManifestRecord = accManifestRecord.copy();
            prevAccManifestRecord.setAccManifestId(null);
            prevAccManifestRecord.setAccId(accId);
            prevAccManifestRecord.setReleaseId(ULong.valueOf(latestRelease.getReleaseId()));
            prevAccManifestRecord.setNextAccManifestId(accManifestId);
            prevAccManifestRecord.setAccManifestId(
                    dslContext.insertInto(ACC_MANIFEST)
                            .set(prevAccManifestRecord)
                            .returning(ACC_MANIFEST.ACC_MANIFEST_ID)
                            .fetchOne().getAccManifestId());

            dslContext.update(ACC_MANIFEST)
                    .set(ACC_MANIFEST.PREV_ACC_MANIFEST_ID, prevAccManifestRecord.getAccManifestId())
                    .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(accManifestId))
                    .execute();
        }

        return acc;
    }

    @Override
    public ACCObject createRandomACC(AppUserObject creator, ReleaseObject release,
                                     NamespaceObject namespace, String state) {
        return createRandomACC(creator, release, namespace, state, Semantics, null);
    }

    @Override
    public ACCObject createRandomACCSemanticGroupType(AppUserObject creator, ReleaseObject release,
                                                      NamespaceObject namespace, String state) {
        return createRandomACC(creator, release, namespace, state, SemanticGroup, null);
    }

    @Override
    public DTObject getLatestDTCreated(LibraryObject library, String den, String branch) {
        ULong latestCreatedDT = dslContext.select(DSL.max(DT.DT_ID))
                .from(DT)
                .join(DT_MANIFEST).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .join(RELEASE).on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(
                        RELEASE.RELEASE_NUM.eq(branch),
                        DT_MANIFEST.DEN.eq(den)))
                .fetchOneInto(ULong.class);
        ULong releaseId = getReleaseIdByReleaseNum(library, branch);
        List<Field<?>> fields = new ArrayList();
        fields.add(DT_MANIFEST.DT_MANIFEST_ID);
        fields.add(DT_MANIFEST.BASED_DT_MANIFEST_ID);
        fields.add(DT_MANIFEST.RELEASE_ID);
        fields.add(DT_MANIFEST.DEN);
        fields.addAll(Arrays.asList(DT.fields()));
        return dslContext.select(fields)
                .from(DT_MANIFEST)
                .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .where(DT.DT_ID.eq(latestCreatedDT))
                .fetchOne(record -> dtMapper(record));
    }

    @Override
    public BCCPObject getLatestBCCPCreatedByUser(AppUserObject user, String branch) {
        ULong latestCreatedBCCP = dslContext.select(DSL.max(BCCP.BCCP_ID))
                .from(BCCP)
                .join(BCCP_MANIFEST).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                .join(RELEASE).on(BCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(
                        RELEASE.RELEASE_NUM.eq(branch),
                        BCCP.CREATED_BY.eq(ULong.valueOf(user.getAppUserId()))))
                .fetchOneInto(ULong.class);
        List<Field<?>> fields = new ArrayList();
        fields.add(BCCP_MANIFEST.BCCP_MANIFEST_ID);
        fields.add(BCCP_MANIFEST.BDT_MANIFEST_ID);
        fields.add(BCCP_MANIFEST.RELEASE_ID);
        fields.add(BCCP_MANIFEST.DEN);
        fields.addAll(Arrays.asList(BCCP.fields()));
        return dslContext.select(fields)
                .from(BCCP_MANIFEST)
                .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                .where(BCCP.BCCP_ID.eq(latestCreatedBCCP))
                .fetchOne(record -> bccpMapper(record));
    }

    @Override
    public ASCCPObject createRandomASCCP(ACCObject roleOfAcc, AppUserObject creator,
                                         NamespaceObject namespace, String state) {
        if (roleOfAcc == null) {
            throw new IllegalArgumentException("'roleOfAcc' parameter must not be null.");
        }

        ASCCPObject asccp = ASCCPObject.createRandomASCCP(roleOfAcc, creator, namespace, state);
        asccp.setReleaseId(roleOfAcc.getReleaseId());

        AsccpRecord asccpRecord = new AsccpRecord();
        asccpRecord.setRoleOfAccId(ULong.valueOf(roleOfAcc.getAccId()));
        asccpRecord.setGuid(asccp.getGuid());
        if (roleOfAcc.getComponentType() == Extension) {
            asccpRecord.setType("Extension");
            asccp.setPropertyTerm("Extension");
            asccp.setDen(asccp.getPropertyTerm() + ". " + roleOfAcc.getObjectClassTerm());
        } else {
            asccpRecord.setType("Default");
        }
        asccpRecord.setPropertyTerm(asccp.getPropertyTerm());
        asccpRecord.setDefinition(asccp.getDefinition());
        asccpRecord.setDefinitionSource(asccp.getDefinitionSource());
        asccpRecord.setNamespaceId(ULong.valueOf(asccp.getNamespaceId()));
        asccpRecord.setIsDeprecated((byte) (asccp.isDeprecated() ? 1 : 0));
        asccpRecord.setIsNillable((byte) (asccp.isNillable() ? 1 : 0));
        asccpRecord.setState(asccp.getState());
        asccpRecord.setOwnerUserId(ULong.valueOf(asccp.getOwnerUserId()));
        asccpRecord.setCreatedBy(ULong.valueOf(asccp.getCreatedBy()));
        asccpRecord.setLastUpdatedBy(ULong.valueOf(asccp.getLastUpdatedBy()));
        asccpRecord.setCreationTimestamp(asccp.getCreationTimestamp());
        asccpRecord.setLastUpdateTimestamp(asccp.getLastUpdateTimestamp());

        ULong asccpId = dslContext.insertInto(ASCCP)
                .set(asccpRecord)
                .returning(ASCCP.ASCCP_ID)
                .fetchOne().getAsccpId();
        asccp.setAsccpId(asccpId.toBigInteger());

        LogRecord dummyLogRecord = new LogRecord();
        dummyLogRecord.setHash(UUID.randomUUID().toString().replaceAll("-", ""));
        dummyLogRecord.setRevisionNum(UInteger.valueOf(1));
        dummyLogRecord.setRevisionTrackingNum(UInteger.valueOf(1));
        dummyLogRecord.setLogAction("Added");
        dummyLogRecord.setReference(asccp.getGuid());
        dummyLogRecord.setSnapshot("{\"component\": \"asccp\"}");
        dummyLogRecord.setCreatedBy(ULong.valueOf(asccp.getCreatedBy()));
        dummyLogRecord.setCreationTimestamp(asccp.getCreationTimestamp());

        ULong logId = dslContext.insertInto(LOG)
                .set(dummyLogRecord)
                .returning(LOG.LOG_ID)
                .fetchOne().getLogId();

        AsccpManifestRecord asccpManifestRecord = new AsccpManifestRecord();
        asccpManifestRecord.setReleaseId(ULong.valueOf(roleOfAcc.getReleaseId()));
        asccpManifestRecord.setRoleOfAccManifestId(ULong.valueOf(roleOfAcc.getAccManifestId()));
        asccpManifestRecord.setAsccpId(asccpId);
        asccpManifestRecord.setDen(asccp.getDen());
        asccpManifestRecord.setLogId(logId);

        ULong asccpManifestId = dslContext.insertInto(ASCCP_MANIFEST)
                .set(asccpManifestRecord)
                .returning(ASCCP_MANIFEST.ASCCP_MANIFEST_ID)
                .fetchOne().getAsccpManifestId();
        asccp.setAsccpManifestId(asccpManifestId.toBigInteger());

        ReleaseRecord release = dslContext.selectFrom(RELEASE)
                .where(RELEASE.RELEASE_ID.eq(asccpManifestRecord.getReleaseId()))
                .fetchOne();
        if ("Working".equals(release.getReleaseNum()) && "Published".equals(state)) {
            asccpManifestRecord = dslContext.selectFrom(ASCCP_MANIFEST)
                    .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(asccpManifestId))
                    .fetchOne();

            ReleaseObject latestRelease = apiFactory.getReleaseAPI().getTheLatestRelease(
                    apiFactory.getLibraryAPI().getLibraryById(release.getLibraryId().toBigInteger())
            );
            AsccpManifestRecord prevAsccpManifestRecord = asccpManifestRecord.copy();
            prevAsccpManifestRecord.setAsccpManifestId(null);
            prevAsccpManifestRecord.setReleaseId(ULong.valueOf(latestRelease.getReleaseId()));
            prevAsccpManifestRecord.setRoleOfAccManifestId(dslContext.select(ACC_MANIFEST.ACC_MANIFEST_ID)
                    .from(ACC_MANIFEST)
                    .where(and(
                            ACC_MANIFEST.ACC_ID.eq(ULong.valueOf(roleOfAcc.getAccId())),
                            ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(latestRelease.getReleaseId()))
                    ))
                    .fetchOneInto(ULong.class));
            prevAsccpManifestRecord.setNextAsccpManifestId(asccpManifestId);
            prevAsccpManifestRecord.setAsccpManifestId(
                    dslContext.insertInto(ASCCP_MANIFEST)
                            .set(prevAsccpManifestRecord)
                            .returning(ASCCP_MANIFEST.ASCCP_MANIFEST_ID)
                            .fetchOne().getAsccpManifestId());

            dslContext.update(ASCCP_MANIFEST)
                    .set(ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID, prevAsccpManifestRecord.getAsccpManifestId())
                    .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(asccpManifestId))
                    .execute();
        }

        return asccp;
    }

    @Override
    public BCCPObject createRandomBCCP(ReleaseObject branch, DTObject dataType, AppUserObject creator, NamespaceObject namespace, String state) {
        BCCPObject bccp = BCCPObject.createRandonBCCP(dataType, creator, namespace, state);
        bccp.setReleaseId(branch.getReleaseId());
        BccpRecord bccpRecord = new BccpRecord();
        bccpRecord.setGuid(bccp.getGuid());
        bccpRecord.setPropertyTerm(bccp.getPropertyTerm());
        bccpRecord.setRepresentationTerm(bccp.getRepresentationTerm());
        bccpRecord.setBdtId(ULong.valueOf(bccp.getBdtId()));
        bccpRecord.setDefinition(bccp.getDefinition());
        bccpRecord.setDefinitionSource(bccp.getDefinitionSource());
        bccpRecord.setDefaultValue(bccp.getDefaultValue());
        bccpRecord.setFixedValue(bccp.getFixedValue());
        bccpRecord.setNamespaceId(ULong.valueOf(bccp.getNamespaceId()));
        bccpRecord.setIsDeprecated((byte) (bccp.isDeprecated() ? 1 : 0));
        bccpRecord.setCreatedBy(ULong.valueOf(bccp.getCreatedBy()));
        bccpRecord.setOwnerUserId(ULong.valueOf(bccp.getOwnerUserId()));
        bccpRecord.setLastUpdatedBy(ULong.valueOf(bccp.getLastUpdatedBy()));
        bccpRecord.setCreationTimestamp(bccp.getCreationTimestamp());
        bccpRecord.setLastUpdateTimestamp(bccp.getLastUpdateTimestamp());
        bccpRecord.setState(bccp.getState());
        bccpRecord.setIsNillable((byte) (bccp.isNillable() ? 1 : 0));

        ULong bccpId = dslContext.insertInto(BCCP)
                .set(bccpRecord)
                .returning(BCCP.BCCP_ID)
                .fetchOne().getBccpId();
        bccp.setBccpId(bccpId.toBigInteger());

        LogRecord dummyLogRecord = new LogRecord();
        dummyLogRecord.setHash(UUID.randomUUID().toString().replaceAll("-", ""));
        dummyLogRecord.setRevisionNum(UInteger.valueOf(1));
        dummyLogRecord.setRevisionTrackingNum(UInteger.valueOf(1));
        dummyLogRecord.setLogAction("Added");
        dummyLogRecord.setReference(bccp.getGuid());
        dummyLogRecord.setSnapshot("{\"component\": \"bccp\"}");
        dummyLogRecord.setCreatedBy(ULong.valueOf(bccp.getCreatedBy()));
        dummyLogRecord.setCreationTimestamp(bccp.getCreationTimestamp());

        ULong logId = dslContext.insertInto(LOG)
                .set(dummyLogRecord)
                .returning(LOG.LOG_ID)
                .fetchOne().getLogId();

        BccpManifestRecord bccpManifestRecord = new BccpManifestRecord();

        bccpManifestRecord.setReleaseId(ULong.valueOf(branch.getReleaseId()));
        bccpManifestRecord.setBccpId(bccpId);
        bccpManifestRecord.setDen(bccp.getDen());
        bccpManifestRecord.setLogId(logId);
        bccpManifestRecord.setBdtManifestId(ULong.valueOf(dataType.getDtManifestId()));

        ULong bccpManifestId = dslContext.insertInto(BCCP_MANIFEST)
                .set(bccpManifestRecord)
                .returning(BCCP_MANIFEST.BCCP_MANIFEST_ID)
                .fetchOne().getBccpManifestId();
        bccp.setBccpManifestId(bccpManifestId.toBigInteger());

        ReleaseRecord release = dslContext.selectFrom(RELEASE)
                .where(RELEASE.RELEASE_ID.eq(bccpManifestRecord.getReleaseId()))
                .fetchOne();
        if ("Working".equals(release.getReleaseNum()) && "Published".equals(state)) {
            bccpManifestRecord = dslContext.selectFrom(BCCP_MANIFEST)
                    .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(bccpManifestId))
                    .fetchOne();

            ReleaseObject latestRelease = apiFactory.getReleaseAPI().getTheLatestRelease(
                    apiFactory.getLibraryAPI().getLibraryById(release.getLibraryId().toBigInteger())
            );
            BccpManifestRecord prevBccpManifestRecord = bccpManifestRecord.copy();
            prevBccpManifestRecord.setBccpManifestId(null);
            prevBccpManifestRecord.setReleaseId(ULong.valueOf(latestRelease.getReleaseId()));
            prevBccpManifestRecord.setBdtManifestId(dslContext.select(DT_MANIFEST.DT_MANIFEST_ID)
                    .from(DT_MANIFEST)
                    .where(and(
                            DT_MANIFEST.DT_ID.eq(ULong.valueOf(dataType.getDtId())),
                            DT_MANIFEST.RELEASE_ID.eq(ULong.valueOf(latestRelease.getReleaseId()))
                    ))
                    .fetchOneInto(ULong.class));
            prevBccpManifestRecord.setNextBccpManifestId(bccpManifestId);
            prevBccpManifestRecord.setBccpManifestId(
                    dslContext.insertInto(BCCP_MANIFEST)
                            .set(prevBccpManifestRecord)
                            .returning(BCCP_MANIFEST.BCCP_MANIFEST_ID)
                            .fetchOne().getBccpManifestId());

            dslContext.update(BCCP_MANIFEST)
                    .set(BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID, prevBccpManifestRecord.getBccpManifestId())
                    .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(bccpManifestId))
                    .execute();
        }

        return bccp;
    }

    @Override
    public DTObject createRandomBDT(ReleaseObject release, DTObject baseDataType, AppUserObject creator, NamespaceObject namespace, String state) {
        DTObject bdt = DTObject.createRandomDT(release, baseDataType, creator, namespace, state);
        bdt.setReleaseId(release.getReleaseId());
        DtRecord dtRecord = new DtRecord();
        dtRecord.setGuid(bdt.getGuid());
        dtRecord.setDataTypeTerm(bdt.getDataTypeTerm());
        dtRecord.setRepresentationTerm(bdt.getRepresentationTerm());
        dtRecord.setBasedDtId(ULong.valueOf(bdt.getBasedDtId()));
        dtRecord.setQualifier_(bdt.getQualifier());
        dtRecord.setDefinition(bdt.getDefinition());
        dtRecord.setDefinitionSource(bdt.getDefinitionSource());
        dtRecord.setNamespaceId(ULong.valueOf(bdt.getNamespaceId()));
        dtRecord.setIsDeprecated((byte) (bdt.isDeprecated() ? 1 : 0));
        dtRecord.setCreatedBy(ULong.valueOf(bdt.getCreatedBy()));
        dtRecord.setOwnerUserId(ULong.valueOf(bdt.getOwnerUserId()));
        dtRecord.setLastUpdatedBy(ULong.valueOf(bdt.getLastUpdatedBy()));
        dtRecord.setCreationTimestamp(bdt.getCreationTimestamp());
        dtRecord.setLastUpdateTimestamp(bdt.getLastUpdateTimestamp());
        dtRecord.setState(bdt.getState());

        ULong bdtId = dslContext.insertInto(DT)
                .set(dtRecord)
                .returning(DT.DT_ID)
                .fetchOne().getDtId();
        bdt.setDtId(bdtId.toBigInteger());

        LogRecord dummyLogRecord = new LogRecord();
        dummyLogRecord.setHash(UUID.randomUUID().toString().replaceAll("-", ""));
        dummyLogRecord.setRevisionNum(UInteger.valueOf(1));
        dummyLogRecord.setRevisionTrackingNum(UInteger.valueOf(1));
        dummyLogRecord.setLogAction("Added");
        dummyLogRecord.setReference(bdt.getGuid());
        dummyLogRecord.setSnapshot("{}");
        dummyLogRecord.setCreatedBy(ULong.valueOf(bdt.getCreatedBy()));
        dummyLogRecord.setCreationTimestamp(bdt.getCreationTimestamp());

        ULong logId = dslContext.insertInto(LOG)
                .set(dummyLogRecord)
                .returning(LOG.LOG_ID)
                .fetchOne().getLogId();

        DtManifestRecord bdtManifestRecord = new DtManifestRecord();
        bdtManifestRecord.setReleaseId(ULong.valueOf(bdt.getReleaseId()));
        bdtManifestRecord.setDtId(bdtId);
        bdtManifestRecord.setDen(bdt.getDen());
        bdtManifestRecord.setLogId(logId);
        bdtManifestRecord.setBasedDtManifestId(ULong.valueOf(baseDataType.getDtManifestId()));

        ULong dtManifestId = dslContext.insertInto(DT_MANIFEST)
                .set(bdtManifestRecord)
                .returning(DT_MANIFEST.DT_MANIFEST_ID)
                .fetchOne().getDtManifestId();
        bdt.setDtManifestId(dtManifestId.toBigInteger());

        ReleaseObject baseDataTypeRelease = apiFactory.getReleaseAPI().getReleaseById(baseDataType.getReleaseId());
        LibraryObject baseDataTypeLibrary = apiFactory.getLibraryAPI().getLibraryById(baseDataTypeRelease.getLibraryId());

        List<DtAwdPriRecord> dtAwdPriList = dslContext.selectFrom(DT_AWD_PRI)
                .where(and(
                        DT_AWD_PRI.RELEASE_ID.eq(ULong.valueOf(baseDataTypeRelease.getReleaseId())),
                        DT_AWD_PRI.DT_ID.eq(ULong.valueOf(baseDataType.getDtId()))
                ))
                .fetch();
        for (DtAwdPriRecord dtAwdPri : dtAwdPriList) {
            DtAwdPriRecord copiedDtAwdPri = dtAwdPri.copy();
            copiedDtAwdPri.setDtAwdPriId(null);
            copiedDtAwdPri.setReleaseId(bdtManifestRecord.getReleaseId());
            copiedDtAwdPri.setDtId(bdtManifestRecord.getDtId());
            if (copiedDtAwdPri.getXbtManifestId() != null) {
                copiedDtAwdPri.setXbtManifestId(
                        dslContext.select(XBT_MANIFEST.XBT_MANIFEST_ID)
                                .from(XBT_MANIFEST)
                                .where(and(
                                        XBT_MANIFEST.RELEASE_ID.eq(ULong.valueOf(release.getReleaseId())),
                                        XBT_MANIFEST.XBT_ID.eq(
                                                dslContext.select(XBT_MANIFEST.XBT_ID)
                                                        .from(XBT_MANIFEST)
                                                        .where(XBT_MANIFEST.XBT_MANIFEST_ID.eq(copiedDtAwdPri.getXbtManifestId()))
                                        )
                                ))
                                .fetchOneInto(ULong.class));
            }
            if (copiedDtAwdPri.getCodeListManifestId() != null) {
                copiedDtAwdPri.setCodeListManifestId(
                        dslContext.select(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID)
                                .from(CODE_LIST_MANIFEST)
                                .where(and(
                                        CODE_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(release.getReleaseId())),
                                        CODE_LIST_MANIFEST.CODE_LIST_ID.eq(
                                                dslContext.select(CODE_LIST_MANIFEST.CODE_LIST_ID)
                                                        .from(CODE_LIST_MANIFEST)
                                                        .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(copiedDtAwdPri.getCodeListManifestId()))
                                        )
                                ))
                                .fetchOneInto(ULong.class));
            }
            if (copiedDtAwdPri.getAgencyIdListManifestId() != null) {
                copiedDtAwdPri.setAgencyIdListManifestId(
                        dslContext.select(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID)
                                .from(AGENCY_ID_LIST_MANIFEST)
                                .where(and(
                                        AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(release.getReleaseId())),
                                        AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(
                                                dslContext.select(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID)
                                                        .from(AGENCY_ID_LIST_MANIFEST)
                                                        .where(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(copiedDtAwdPri.getAgencyIdListManifestId()))
                                        )
                                ))
                                .fetchOneInto(ULong.class));
            }
            if (copiedDtAwdPri.getXbtManifestId() == null &&
                copiedDtAwdPri.getCodeListManifestId() == null &&
                copiedDtAwdPri.getAgencyIdListManifestId() == null) {
                continue;
            }
            dslContext.insertInto(DT_AWD_PRI)
                    .set(copiedDtAwdPri)
                    .execute();
        }

        dslContext.selectFrom(DT_SC_MANIFEST)
                .where(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID.eq(ULong.valueOf(baseDataType.getDtManifestId())))
                .fetch().forEach(dtScManifest -> {
                    ULong oldDtScId = dtScManifest.getDtScId();
                    DtScRecord dtSc = dslContext.selectFrom(DT_SC)
                            .where(DT_SC.DT_SC_ID.eq(oldDtScId))
                            .fetchOne();

                    dtSc.setDtScId(null);
                    dtSc.setBasedDtScId(oldDtScId);
                    dtSc.setOwnerDtId(bdtId);
                    dtSc.setCreatedBy(ULong.valueOf(bdt.getCreatedBy()));
                    dtSc.setOwnerUserId(ULong.valueOf(bdt.getOwnerUserId()));
                    dtSc.setLastUpdatedBy(ULong.valueOf(bdt.getLastUpdatedBy()));
                    dtSc.setCreationTimestamp(bdt.getCreationTimestamp());
                    dtSc.setLastUpdateTimestamp(bdt.getLastUpdateTimestamp());
                    dtSc.setDtScId(
                            dslContext.insertInto(DT_SC)
                                    .set(dtSc)
                                    .returning(DT_SC.DT_SC_ID).fetchOne().getDtScId()
                    );

                    ULong oldDtScManifestId = dtScManifest.getDtScManifestId();

                    dtScManifest.setDtScManifestId(null);
                    dtScManifest.setReleaseId(ULong.valueOf(bdt.getReleaseId()));
                    dtScManifest.setBasedDtScManifestId(oldDtScManifestId);
                    dtScManifest.setDtScId(dtSc.getDtScId());
                    dtScManifest.setOwnerDtManifestId(dtManifestId);
                    dtScManifest.setDtScManifestId(
                            dslContext.insertInto(DT_SC_MANIFEST)
                                    .set(dtScManifest)
                                    .returning(DT_SC_MANIFEST.DT_SC_MANIFEST_ID).fetchOne().getDtScManifestId()
                    );

                    List<DtScAwdPriRecord> dtScAwdPriList = dslContext.selectFrom(DT_SC_AWD_PRI)
                            .where(and(
                                    DT_SC_AWD_PRI.RELEASE_ID.eq(ULong.valueOf(baseDataTypeRelease.getReleaseId())),
                                    DT_SC_AWD_PRI.DT_SC_ID.eq(oldDtScId)
                            ))
                            .fetch();
                    for (DtScAwdPriRecord dtScAwdPri : dtScAwdPriList) {
                        DtScAwdPriRecord copiedDtScAwdPri = dtScAwdPri.copy();
                        copiedDtScAwdPri.setDtScAwdPriId(null);
                        copiedDtScAwdPri.setReleaseId(dtScManifest.getReleaseId());
                        copiedDtScAwdPri.setDtScId(dtScManifest.getDtScId());
                        if (copiedDtScAwdPri.getXbtManifestId() != null) {
                            copiedDtScAwdPri.setXbtManifestId(
                                    dslContext.select(XBT_MANIFEST.XBT_MANIFEST_ID)
                                            .from(XBT_MANIFEST)
                                            .where(and(
                                                    XBT_MANIFEST.RELEASE_ID.eq(ULong.valueOf(release.getReleaseId())),
                                                    XBT_MANIFEST.XBT_ID.eq(
                                                            dslContext.select(XBT_MANIFEST.XBT_ID)
                                                                    .from(XBT_MANIFEST)
                                                                    .where(XBT_MANIFEST.XBT_MANIFEST_ID.eq(copiedDtScAwdPri.getXbtManifestId()))
                                                    )
                                            ))
                                            .fetchOneInto(ULong.class));
                        }
                        if (copiedDtScAwdPri.getCodeListManifestId() != null) {
                            copiedDtScAwdPri.setCodeListManifestId(
                                    dslContext.select(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID)
                                            .from(CODE_LIST_MANIFEST)
                                            .where(and(
                                                    CODE_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(release.getReleaseId())),
                                                    CODE_LIST_MANIFEST.CODE_LIST_ID.eq(
                                                            dslContext.select(CODE_LIST_MANIFEST.CODE_LIST_ID)
                                                                    .from(CODE_LIST_MANIFEST)
                                                                    .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(copiedDtScAwdPri.getCodeListManifestId()))
                                                    )
                                            ))
                                            .fetchOneInto(ULong.class));
                        }
                        if (copiedDtScAwdPri.getAgencyIdListManifestId() != null) {
                            copiedDtScAwdPri.setAgencyIdListManifestId(
                                    dslContext.select(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID)
                                            .from(AGENCY_ID_LIST_MANIFEST)
                                            .where(and(
                                                    AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(release.getReleaseId())),
                                                    AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(
                                                            dslContext.select(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID)
                                                                    .from(AGENCY_ID_LIST_MANIFEST)
                                                                    .where(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(copiedDtScAwdPri.getAgencyIdListManifestId()))
                                                    )
                                            ))
                                            .fetchOneInto(ULong.class));
                        }
                        if (copiedDtScAwdPri.getXbtManifestId() == null &&
                            copiedDtScAwdPri.getCodeListManifestId() == null &&
                            copiedDtScAwdPri.getAgencyIdListManifestId() == null) {
                            continue;
                        }
                        dslContext.insertInto(DT_SC_AWD_PRI)
                                .set(copiedDtScAwdPri)
                                .execute();
                    }
                });

        return bdt;
    }

    @Override
    public ACCObject createRevisedACC(ACCObject prevAcc, AppUserObject creator,
                                      ReleaseObject release, String state) {
        ACCObject acc = new ACCObject();
        acc.setReleaseId(release.getReleaseId());
        acc.setGuid(prevAcc.getGuid());
        acc.setObjectClassTerm(prevAcc.getObjectClassTerm());
        acc.setDen(prevAcc.getDen());
        acc.setComponentType(prevAcc.getComponentType());
        acc.setDefinition(prevAcc.getDefinition());
        acc.setDefinitionSource(prevAcc.getDefinitionSource());
        acc.setNamespaceId(prevAcc.getNamespaceId());
        acc.setAbstract(prevAcc.isAbstract());
        acc.setDeprecated(prevAcc.isDeprecated());
        acc.setState(state);
        acc.setOwnerUserId(creator.getAppUserId());
        acc.setCreatedBy(creator.getAppUserId());
        acc.setLastUpdatedBy(creator.getAppUserId());
        acc.setCreationTimestamp(LocalDateTime.now());
        acc.setLastUpdateTimestamp(LocalDateTime.now());

        AccRecord accRecord = new AccRecord();
        accRecord.setGuid(acc.getGuid());
        accRecord.setType("Default");
        accRecord.setObjectClassTerm(acc.getObjectClassTerm());
        accRecord.setDefinition(acc.getDefinition());
        accRecord.setDefinitionSource(acc.getDefinitionSource());
        accRecord.setOagisComponentType(acc.getComponentType().getValue());
        accRecord.setNamespaceId(ULong.valueOf(acc.getNamespaceId()));
        accRecord.setIsAbstract((byte) (acc.isAbstract() ? 1 : 0));
        accRecord.setIsDeprecated((byte) (acc.isDeprecated() ? 1 : 0));
        accRecord.setState(acc.getState());
        accRecord.setOwnerUserId(ULong.valueOf(acc.getOwnerUserId()));
        accRecord.setCreatedBy(ULong.valueOf(acc.getCreatedBy()));
        accRecord.setLastUpdatedBy(ULong.valueOf(acc.getLastUpdatedBy()));
        accRecord.setCreationTimestamp(acc.getCreationTimestamp());
        accRecord.setLastUpdateTimestamp(acc.getLastUpdateTimestamp());
        accRecord.setPrevAccId(ULong.valueOf(prevAcc.getAccId()));

        ULong accId = dslContext.insertInto(ACC)
                .set(accRecord)
                .returning(ACC.ACC_ID)
                .fetchOne().getAccId();
        acc.setAccId(accId.toBigInteger());

        dslContext.update(ACC)
                .set(ACC.NEXT_ACC_ID, accId)
                .where(ACC.ACC_ID.eq(ULong.valueOf(prevAcc.getAccId())))
                .execute();

        LogRecord dummyLogRecord = new LogRecord();
        dummyLogRecord.setHash(UUID.randomUUID().toString().replaceAll("-", ""));
        dummyLogRecord.setRevisionNum(UInteger.valueOf(2));
        dummyLogRecord.setRevisionTrackingNum(UInteger.valueOf(1));
        dummyLogRecord.setLogAction("Revised");
        dummyLogRecord.setReference(acc.getGuid());
        dummyLogRecord.setSnapshot("{\"component\": \"acc\"}");
        dummyLogRecord.setCreatedBy(ULong.valueOf(acc.getCreatedBy()));
        dummyLogRecord.setCreationTimestamp(acc.getCreationTimestamp());

        ULong logId = dslContext.insertInto(LOG)
                .set(dummyLogRecord)
                .returning(LOG.LOG_ID)
                .fetchOne().getLogId();

        AccManifestRecord accManifestRecord = new AccManifestRecord();
        accManifestRecord.setReleaseId(ULong.valueOf(release.getReleaseId()));
        accManifestRecord.setAccId(accId);
        accManifestRecord.setDen(acc.getDen());
        accManifestRecord.setLogId(logId);
        accManifestRecord.setPrevAccManifestId(ULong.valueOf(prevAcc.getAccManifestId()));

        ULong accManifestId = dslContext.insertInto(ACC_MANIFEST)
                .set(accManifestRecord)
                .returning(ACC_MANIFEST.ACC_MANIFEST_ID)
                .fetchOne().getAccManifestId();
        acc.setAccManifestId(accManifestId.toBigInteger());

        dslContext.update(ACC_MANIFEST)
                .set(ACC_MANIFEST.NEXT_ACC_MANIFEST_ID, accManifestId)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(prevAcc.getAccManifestId())))
                .execute();

        return acc;
    }

    @Override
    public ASCCPObject createRevisedASCCP(ASCCPObject prevAsccp, ACCObject roleOfAcc,
                                          AppUserObject creator, ReleaseObject release, String state) {
        ASCCPObject asccp = new ASCCPObject();
        asccp.setGuid(prevAsccp.getGuid());
        String objectClassTerm = roleOfAcc.getObjectClassTerm();
        asccp.setRoleOfAccManifestId(roleOfAcc.getAccManifestId());
        asccp.setPropertyTerm(objectClassTerm);
        asccp.setDen(asccp.getPropertyTerm() + ". " + asccp.getPropertyTerm());
        asccp.setDefinition(asccp.getDefinition());
        asccp.setDefinitionSource(asccp.getDefinitionSource());
        asccp.setNamespaceId(asccp.getNamespaceId());
        asccp.setDeprecated(asccp.isDeprecated());
        asccp.setNillable(asccp.isNillable());
        asccp.setState(state);
        asccp.setNamespaceId(prevAsccp.getNamespaceId());
        asccp.setOwnerUserId(creator.getAppUserId());
        asccp.setCreatedBy(creator.getAppUserId());
        asccp.setLastUpdatedBy(creator.getAppUserId());
        asccp.setCreationTimestamp(LocalDateTime.now());
        asccp.setLastUpdateTimestamp(LocalDateTime.now());
        asccp.setReleaseId(roleOfAcc.getReleaseId());

        AsccpRecord asccpRecord = new AsccpRecord();
        asccpRecord.setRoleOfAccId(ULong.valueOf(roleOfAcc.getAccId()));
        asccpRecord.setGuid(asccp.getGuid());
        asccpRecord.setType("Default");
        asccpRecord.setPropertyTerm(asccp.getPropertyTerm());
        asccpRecord.setDefinition(asccp.getDefinition());
        asccpRecord.setDefinitionSource(asccp.getDefinitionSource());
        asccpRecord.setNamespaceId(ULong.valueOf(asccp.getNamespaceId()));
        asccpRecord.setIsDeprecated((byte) (asccp.isDeprecated() ? 1 : 0));
        asccpRecord.setIsNillable((byte) (asccp.isNillable() ? 1 : 0));
        asccpRecord.setState(asccp.getState());
        asccpRecord.setOwnerUserId(ULong.valueOf(asccp.getOwnerUserId()));
        asccpRecord.setCreatedBy(ULong.valueOf(asccp.getCreatedBy()));
        asccpRecord.setLastUpdatedBy(ULong.valueOf(asccp.getLastUpdatedBy()));
        asccpRecord.setCreationTimestamp(asccp.getCreationTimestamp());
        asccpRecord.setLastUpdateTimestamp(asccp.getLastUpdateTimestamp());

        ULong asccpId = dslContext.insertInto(ASCCP)
                .set(asccpRecord)
                .returning(ASCCP.ASCCP_ID)
                .fetchOne().getAsccpId();
        asccp.setAsccpId(asccpId.toBigInteger());

        LogRecord dummyLogRecord = new LogRecord();
        dummyLogRecord.setHash(UUID.randomUUID().toString().replaceAll("-", ""));
        dummyLogRecord.setRevisionNum(UInteger.valueOf(2));
        dummyLogRecord.setRevisionTrackingNum(UInteger.valueOf(1));
        dummyLogRecord.setLogAction("Revised");
        dummyLogRecord.setReference(asccp.getGuid());
        dummyLogRecord.setSnapshot("{\"component\": \"asccp\"}");
        dummyLogRecord.setCreatedBy(ULong.valueOf(asccp.getCreatedBy()));
        dummyLogRecord.setCreationTimestamp(asccp.getCreationTimestamp());

        ULong logId = dslContext.insertInto(LOG)
                .set(dummyLogRecord)
                .returning(LOG.LOG_ID)
                .fetchOne().getLogId();

        AsccpManifestRecord asccpManifestRecord = new AsccpManifestRecord();
        asccpManifestRecord.setReleaseId(ULong.valueOf(roleOfAcc.getReleaseId()));
        asccpManifestRecord.setRoleOfAccManifestId(ULong.valueOf(roleOfAcc.getAccManifestId()));
        asccpManifestRecord.setAsccpId(asccpId);
        asccpManifestRecord.setDen(asccp.getDen());
        asccpManifestRecord.setLogId(logId);
        asccpManifestRecord.setPrevAsccpManifestId(ULong.valueOf(prevAsccp.getAsccpManifestId()));

        ULong asccpManifestId = dslContext.insertInto(ASCCP_MANIFEST)
                .set(asccpManifestRecord)
                .returning(ASCCP_MANIFEST.ASCCP_MANIFEST_ID)
                .fetchOne().getAsccpManifestId();
        asccp.setAsccpManifestId(asccpManifestId.toBigInteger());

        dslContext.update(ASCCP_MANIFEST)
                .set(ASCCP_MANIFEST.NEXT_ASCCP_MANIFEST_ID, asccpManifestId)
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ULong.valueOf(prevAsccp.getAsccpManifestId())))
                .execute();

        return asccp;
    }

    @Override
    public BCCPObject createRevisedBCCP(BCCPObject prevBccp, DTObject dataType,
                                        AppUserObject creator, ReleaseObject release, String state) {
        if (!dataType.getReleaseId().equals(release.getReleaseId())) {
            throw new IllegalArgumentException("The release IDs are mismatched: [Data Type Release ID: " + dataType.getReleaseId() + ", Release ID: " + release.getReleaseId() + "]");
        }

        BCCPObject bccp = new BCCPObject();
        bccp.setGuid(prevBccp.getGuid());
        bccp.setPropertyTerm(prevBccp.getPropertyTerm());
        bccp.setRepresentationTerm(prevBccp.getRepresentationTerm());
        bccp.setBdtId(dataType.getDtId());
        bccp.setDen(prevBccp.getDen());
        bccp.setDefinition(prevBccp.getDefinition());
        bccp.setDefinitionSource(prevBccp.getDefinitionSource());
        bccp.setDefaultValue(prevBccp.getDefaultValue());
        bccp.setFixedValue(prevBccp.getFixedValue());
        bccp.setNamespaceId(prevBccp.getNamespaceId());
        bccp.setDeprecated(prevBccp.isDeprecated());
        bccp.setState(state);
        bccp.setOwnerUserId(creator.getAppUserId());
        bccp.setCreatedBy(creator.getAppUserId());
        bccp.setLastUpdatedBy(creator.getAppUserId());
        bccp.setCreationTimestamp(LocalDateTime.now());
        bccp.setLastUpdateTimestamp(LocalDateTime.now());
        bccp.setNillable(prevBccp.isNillable());
        bccp.setReleaseId(release.getReleaseId());

        BccpRecord bccpRecord = new BccpRecord();
        bccpRecord.setGuid(bccp.getGuid());
        bccpRecord.setPropertyTerm(bccp.getPropertyTerm());
        bccpRecord.setRepresentationTerm(bccp.getRepresentationTerm());
        bccpRecord.setBdtId(ULong.valueOf(bccp.getBdtId()));
        bccpRecord.setDefinition(bccp.getDefinition());
        bccpRecord.setDefinitionSource(bccp.getDefinitionSource());
        bccpRecord.setNamespaceId(ULong.valueOf(bccp.getNamespaceId()));
        bccpRecord.setIsDeprecated((byte) (bccp.isDeprecated() ? 1 : 0));
        bccpRecord.setCreatedBy(ULong.valueOf(bccp.getCreatedBy()));
        bccpRecord.setOwnerUserId(ULong.valueOf(bccp.getOwnerUserId()));
        bccpRecord.setLastUpdatedBy(ULong.valueOf(bccp.getLastUpdatedBy()));
        bccpRecord.setCreationTimestamp(bccp.getCreationTimestamp());
        bccpRecord.setLastUpdateTimestamp(bccp.getLastUpdateTimestamp());
        bccpRecord.setState(bccp.getState());
        bccpRecord.setIsNillable((byte) (bccp.isNillable() ? 1 : 0));

        ULong bccpId = dslContext.insertInto(BCCP)
                .set(bccpRecord)
                .returning(BCCP.BCCP_ID)
                .fetchOne().getBccpId();
        bccp.setBccpId(bccpId.toBigInteger());

        LogRecord dummyLogRecord = new LogRecord();
        dummyLogRecord.setHash(UUID.randomUUID().toString().replaceAll("-", ""));
        dummyLogRecord.setRevisionNum(UInteger.valueOf(2));
        dummyLogRecord.setRevisionTrackingNum(UInteger.valueOf(1));
        dummyLogRecord.setLogAction("Revised");
        dummyLogRecord.setReference(bccp.getGuid());
        dummyLogRecord.setSnapshot("{\"component\": \"bccp\"}");
        dummyLogRecord.setCreatedBy(ULong.valueOf(bccp.getCreatedBy()));
        dummyLogRecord.setCreationTimestamp(bccp.getCreationTimestamp());

        ULong logId = dslContext.insertInto(LOG)
                .set(dummyLogRecord)
                .returning(LOG.LOG_ID)
                .fetchOne().getLogId();

        BccpManifestRecord bccpManifestRecord = new BccpManifestRecord();

        bccpManifestRecord.setReleaseId(ULong.valueOf(dataType.getReleaseId()));
        bccpManifestRecord.setBccpId(bccpId);
        bccpManifestRecord.setDen(bccp.getDen());
        bccpManifestRecord.setLogId(logId);
        bccpManifestRecord.setBdtManifestId(ULong.valueOf(dataType.getDtManifestId()));

        ULong bccpManifestId = dslContext.insertInto(BCCP_MANIFEST)
                .set(bccpManifestRecord)
                .returning(BCCP_MANIFEST.BCCP_MANIFEST_ID)
                .fetchOne().getBccpManifestId();
        bccp.setBccpManifestId(bccpManifestId.toBigInteger());

        return bccp;
    }

    @Override
    public void updateACC(ACCObject acc) {
        dslContext.update(ACC)
                .set(ACC.OBJECT_CLASS_TERM, acc.getObjectClassTerm())
                .set(ACC.DEFINITION, acc.getDefinition())
                .set(ACC.DEFINITION_SOURCE, acc.getDefinitionSource())
                .set(ACC.NAMESPACE_ID, ULong.valueOf(acc.getNamespaceId()))
                .set(ACC.IS_DEPRECATED, (byte) (acc.isDeprecated() ? 1 : 0))
                .set(ACC.IS_ABSTRACT, (byte) (acc.isAbstract() ? 1 : 0))
                .set(ACC.STATE, acc.getState())
                .set(ACC.OAGIS_COMPONENT_TYPE, acc.getComponentType().getValue())
                .set(ACC.CREATION_TIMESTAMP, acc.getCreationTimestamp())
                .set(ACC.LAST_UPDATE_TIMESTAMP, acc.getLastUpdateTimestamp())
                .where(ACC.ACC_ID.eq(ULong.valueOf(acc.getAccId())))
                .execute();

        dslContext.update(ACC_MANIFEST)
                .set(ACC_MANIFEST.DEN, acc.getObjectClassTerm() + ". Details")
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(acc.getAccManifestId())))
                .execute();
    }

    @Override
    public void updateBasedACC(ACCObject acc, ACCObject basedAcc) {
        dslContext.update(ACC)
                .set(ACC.BASED_ACC_ID, ULong.valueOf(basedAcc.getAccId()))
                .where(ACC.ACC_ID.eq(ULong.valueOf(acc.getAccId())))
                .execute();

        dslContext.update(ACC_MANIFEST)
                .set(ACC_MANIFEST.BASED_ACC_MANIFEST_ID, ULong.valueOf(basedAcc.getAccManifestId()))
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(acc.getAccManifestId())))
                .execute();

        acc.setBasedAccManifestId(basedAcc.getAccManifestId());

        ReleaseRecord release = dslContext.selectFrom(RELEASE)
                .where(RELEASE.RELEASE_ID.eq(ULong.valueOf(acc.getReleaseId())))
                .fetchOne();

        if ("Working".equals(release.getReleaseNum()) && "Published".equals(acc.getState())) {
            AccManifestRecord prevAccManifest = dslContext.selectFrom(ACC_MANIFEST)
                    .where(ACC_MANIFEST.NEXT_ACC_MANIFEST_ID.eq(ULong.valueOf(acc.getAccManifestId())))
                    .fetchOne();

            AccManifestRecord prevBaseAccManifest = dslContext.selectFrom(ACC_MANIFEST)
                    .where(ACC_MANIFEST.NEXT_ACC_MANIFEST_ID.eq(ULong.valueOf(basedAcc.getAccManifestId())))
                    .fetchOne();

            dslContext.update(ACC_MANIFEST)
                    .set(ACC_MANIFEST.BASED_ACC_MANIFEST_ID, prevBaseAccManifest.getAccManifestId())
                    .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(prevAccManifest.getAccManifestId()))
                    .execute();
        }
    }

    @Override
    public ASCCObject appendASCC(ACCObject fromAcc, ASCCPObject toAsccp, String state) {
        if (!fromAcc.getReleaseId().equals(toAsccp.getReleaseId())) {
            throw new IllegalArgumentException();
        }

        ASCCObject ascc = ASCCObject.createRandomASCC(fromAcc, toAsccp, state);

        AsccRecord asccRecord = new AsccRecord();
        asccRecord.setGuid(ascc.getGuid());
        asccRecord.setCardinalityMin(ascc.getCardinalityMin());
        asccRecord.setCardinalityMax(ascc.getCardinalityMax());
        asccRecord.setFromAccId(ULong.valueOf(fromAcc.getAccId()));
        asccRecord.setToAsccpId(ULong.valueOf(toAsccp.getAsccpId()));
        asccRecord.setDefinition(ascc.getDefinition());
        asccRecord.setDefinitionSource(ascc.getDefinitionSource());
        asccRecord.setIsDeprecated((byte) (ascc.isDeprecated() ? 1 : 0));
        asccRecord.setState(ascc.getState());
        asccRecord.setOwnerUserId(ULong.valueOf(ascc.getOwnerUserId()));
        asccRecord.setCreatedBy(ULong.valueOf(ascc.getCreatedBy()));
        asccRecord.setLastUpdatedBy(ULong.valueOf(ascc.getLastUpdatedBy()));
        asccRecord.setCreationTimestamp(ascc.getCreationTimestamp());
        asccRecord.setLastUpdateTimestamp(ascc.getLastUpdateTimestamp());

        ULong asccId = dslContext.insertInto(ASCC)
                .set(asccRecord)
                .returning(ASCC.ASCC_ID)
                .fetchOne().getAsccId();
        ascc.setAsccId(asccId.toBigInteger());

        AsccManifestRecord asccManifestRecord = new AsccManifestRecord();
        asccManifestRecord.setReleaseId(ULong.valueOf(fromAcc.getReleaseId()));
        asccManifestRecord.setAsccId(asccId);
        asccManifestRecord.setDen(ascc.getDen());
        asccManifestRecord.setFromAccManifestId(ULong.valueOf(fromAcc.getAccManifestId()));
        asccManifestRecord.setToAsccpManifestId(ULong.valueOf(toAsccp.getAsccpManifestId()));

        ULong asccManifestId = dslContext.insertInto(ASCC_MANIFEST)
                .set(asccManifestRecord)
                .returning(ASCC_MANIFEST.ASCC_MANIFEST_ID)
                .fetchOne().getAsccManifestId();

        SeqKeyRecord lastSeqKeyRecord = dslContext.selectFrom(SEQ_KEY)
                .where(and(
                        SEQ_KEY.FROM_ACC_MANIFEST_ID.eq(ULong.valueOf(fromAcc.getAccManifestId())),
                        SEQ_KEY.NEXT_SEQ_KEY_ID.isNull()
                ))
                .fetchOne();

        SeqKeyRecord seqKeyRecord = new SeqKeyRecord();
        seqKeyRecord.setFromAccManifestId(ULong.valueOf(fromAcc.getAccManifestId()));
        seqKeyRecord.setAsccManifestId(asccManifestId);
        if (lastSeqKeyRecord != null) {
            seqKeyRecord.setPrevSeqKeyId(lastSeqKeyRecord.getSeqKeyId());
        }
        ULong seqKeyId = dslContext.insertInto(SEQ_KEY)
                .set(seqKeyRecord)
                .returning(SEQ_KEY.SEQ_KEY_ID)
                .fetchOne().getSeqKeyId();
        if (lastSeqKeyRecord != null) {
            dslContext.update(SEQ_KEY)
                    .set(SEQ_KEY.NEXT_SEQ_KEY_ID, seqKeyId)
                    .where(SEQ_KEY.SEQ_KEY_ID.eq(lastSeqKeyRecord.getSeqKeyId()))
                    .execute();
        }

        dslContext.update(ASCC_MANIFEST)
                .set(ASCC_MANIFEST.SEQ_KEY_ID, seqKeyId)
                .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(asccManifestId))
                .execute();

        ReleaseRecord release = dslContext.selectFrom(RELEASE)
                .where(RELEASE.RELEASE_ID.eq(asccManifestRecord.getReleaseId()))
                .fetchOne();
        if ("Working".equals(release.getReleaseNum()) && "Published".equals(state)) {
            asccManifestRecord = dslContext.selectFrom(ASCC_MANIFEST)
                    .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(asccManifestId))
                    .fetchOne();

            ReleaseObject latestRelease = apiFactory.getReleaseAPI().getTheLatestRelease(
                    apiFactory.getLibraryAPI().getLibraryById(release.getLibraryId().toBigInteger())
            );
            AsccManifestRecord prevAsccManifestRecord = asccManifestRecord.copy();
            prevAsccManifestRecord.setAsccManifestId(null);
            prevAsccManifestRecord.setReleaseId(ULong.valueOf(latestRelease.getReleaseId()));
            prevAsccManifestRecord.setFromAccManifestId(dslContext.select(ACC_MANIFEST.ACC_MANIFEST_ID)
                    .from(ACC_MANIFEST)
                    .where(and(
                            ACC_MANIFEST.ACC_ID.eq(ULong.valueOf(fromAcc.getAccId())),
                            ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(latestRelease.getReleaseId()))
                    ))
                    .fetchOneInto(ULong.class));
            prevAsccManifestRecord.setToAsccpManifestId(dslContext.select(ASCCP_MANIFEST.ASCCP_MANIFEST_ID)
                    .from(ASCCP_MANIFEST)
                    .where(and(
                            ASCCP_MANIFEST.ASCCP_ID.eq(ULong.valueOf(toAsccp.getAsccpId())),
                            ASCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(latestRelease.getReleaseId()))
                    ))
                    .fetchOneInto(ULong.class));
            prevAsccManifestRecord.setNextAsccManifestId(asccManifestId);
            prevAsccManifestRecord.setAsccManifestId(
                    dslContext.insertInto(ASCC_MANIFEST)
                            .set(prevAsccManifestRecord)
                            .returning(ASCC_MANIFEST.ASCC_MANIFEST_ID)
                            .fetchOne().getAsccManifestId());

            dslContext.update(ASCC_MANIFEST)
                    .set(ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID, prevAsccManifestRecord.getAsccManifestId())
                    .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(asccManifestId))
                    .execute();

            seqKeyRecord = dslContext.selectFrom(SEQ_KEY)
                    .where(SEQ_KEY.SEQ_KEY_ID.eq(seqKeyId))
                    .fetchOne();
            SeqKeyRecord prevSeqKeyRecord = seqKeyRecord.copy();
            prevSeqKeyRecord.setSeqKeyId(null);
            prevSeqKeyRecord.setFromAccManifestId(prevAsccManifestRecord.getFromAccManifestId());
            prevSeqKeyRecord.setAsccManifestId(prevAsccManifestRecord.getAsccManifestId());

            lastSeqKeyRecord = dslContext.selectFrom(SEQ_KEY)
                    .where(and(
                            SEQ_KEY.FROM_ACC_MANIFEST_ID.eq(prevAsccManifestRecord.getFromAccManifestId()),
                            SEQ_KEY.NEXT_SEQ_KEY_ID.isNull()
                    ))
                    .fetchOne();
            if (lastSeqKeyRecord != null) {
                prevSeqKeyRecord.setPrevSeqKeyId(lastSeqKeyRecord.getSeqKeyId());
            }
            seqKeyId = dslContext.insertInto(SEQ_KEY)
                    .set(prevSeqKeyRecord)
                    .returning(SEQ_KEY.SEQ_KEY_ID)
                    .fetchOne().getSeqKeyId();
            if (lastSeqKeyRecord != null) {
                dslContext.update(SEQ_KEY)
                        .set(SEQ_KEY.NEXT_SEQ_KEY_ID, seqKeyId)
                        .where(SEQ_KEY.SEQ_KEY_ID.eq(lastSeqKeyRecord.getSeqKeyId()))
                        .execute();
            }

            dslContext.update(ASCC_MANIFEST)
                    .set(ASCC_MANIFEST.SEQ_KEY_ID, seqKeyId)
                    .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(prevAsccManifestRecord.getAsccManifestId()))
                    .execute();
        }

        return ascc;
    }

    @Override
    public void updateASCC(ASCCObject ascc) {
        dslContext.update(ASCC)
                .set(ASCC.CARDINALITY_MIN, ascc.getCardinalityMin())
                .set(ASCC.CARDINALITY_MAX, ascc.getCardinalityMax())
                .set(ASCC.DEFINITION, ascc.getDefinition())
                .set(ASCC.DEFINITION_SOURCE, ascc.getDefinitionSource())
                .set(ASCC.IS_DEPRECATED, (byte) (ascc.isDeprecated() ? 1 : 0))
                .set(ASCC.STATE, ascc.getState())
                .where(ASCC.ASCC_ID.eq(ULong.valueOf(ascc.getAsccId())))
                .execute();
    }

    @Override
    public ASCCObject appendExtension(ACCObject fromAcc, AppUserObject creator,
                                      NamespaceObject namespace, String state) {
        ReleaseObject release = apiFactory.getReleaseAPI().getReleaseById(fromAcc.getReleaseId());

        ACCObject extensionAcc = createRandomACC(creator, release, namespace, state,
                Extension, fromAcc.getObjectClassTerm() + " Extension");
        ASCCPObject extensionAsccp = createRandomASCCP(extensionAcc, creator, namespace, state);
        return appendASCC(fromAcc, extensionAsccp, state);
    }

    @Override
    public BCCObject appendBCC(ACCObject fromAcc, BCCPObject toBccp, String state) {
        if (!fromAcc.getReleaseId().equals(toBccp.getReleaseId())) {
            throw new IllegalArgumentException();
        }

        BCCObject bcc = BCCObject.createRandomBCC(fromAcc, toBccp, state);

        BccRecord bccRecord = new BccRecord();
        bccRecord.setGuid(bcc.getGuid());
        bccRecord.setCardinalityMin(bcc.getCardinalityMin());
        bccRecord.setCardinalityMax(bcc.getCardinalityMax());
        bccRecord.setFromAccId(ULong.valueOf(fromAcc.getAccId()));
        bccRecord.setToBccpId(ULong.valueOf(toBccp.getBccpId()));
        bccRecord.setDefinition(bcc.getDefinition());
        bccRecord.setDefinitionSource(bcc.getDefinitionSource());
        bccRecord.setEntityType(bcc.isAttribute() ? 0 : 1);
        bccRecord.setIsDeprecated((byte) (bcc.isDeprecated() ? 1 : 0));
        bccRecord.setIsNillable((byte) (bcc.isNillable() ? 1 : 0));
        bccRecord.setDefaultValue(bcc.getDefaultValue());
        bccRecord.setFixedValue(bcc.getFixedValue());
        bccRecord.setState(bcc.getState());
        bccRecord.setOwnerUserId(ULong.valueOf(bcc.getOwnerUserId()));
        bccRecord.setCreatedBy(ULong.valueOf(bcc.getCreatedBy()));
        bccRecord.setLastUpdatedBy(ULong.valueOf(bcc.getLastUpdatedBy()));
        bccRecord.setCreationTimestamp(bcc.getCreationTimestamp());
        bccRecord.setLastUpdateTimestamp(bcc.getLastUpdateTimestamp());

        ULong bccId = dslContext.insertInto(BCC)
                .set(bccRecord)
                .returning(BCC.BCC_ID)
                .fetchOne().getBccId();
        bcc.setBccId(bccId.toBigInteger());

        BccManifestRecord bccManifestRecord = new BccManifestRecord();
        bccManifestRecord.setReleaseId(ULong.valueOf(fromAcc.getReleaseId()));
        bccManifestRecord.setBccId(bccId);
        bccManifestRecord.setDen(bcc.getDen());
        bccManifestRecord.setFromAccManifestId(ULong.valueOf(fromAcc.getAccManifestId()));
        bccManifestRecord.setToBccpManifestId(ULong.valueOf(toBccp.getBccpManifestId()));

        ULong bccManifestId = dslContext.insertInto(BCC_MANIFEST)
                .set(bccManifestRecord)
                .returning(BCC_MANIFEST.BCC_MANIFEST_ID)
                .fetchOne().getBccManifestId();

        SeqKeyRecord lastSeqKeyRecord = dslContext.selectFrom(SEQ_KEY)
                .where(and(
                        SEQ_KEY.FROM_ACC_MANIFEST_ID.eq(ULong.valueOf(fromAcc.getAccManifestId())),
                        SEQ_KEY.NEXT_SEQ_KEY_ID.isNull()
                ))
                .fetchOne();

        SeqKeyRecord seqKeyRecord = new SeqKeyRecord();
        seqKeyRecord.setFromAccManifestId(ULong.valueOf(fromAcc.getAccManifestId()));
        seqKeyRecord.setBccManifestId(bccManifestId);
        if (lastSeqKeyRecord != null) {
            seqKeyRecord.setPrevSeqKeyId(lastSeqKeyRecord.getSeqKeyId());
        }
        ULong seqKeyId = dslContext.insertInto(SEQ_KEY)
                .set(seqKeyRecord)
                .returning(SEQ_KEY.SEQ_KEY_ID)
                .fetchOne().getSeqKeyId();
        if (lastSeqKeyRecord != null) {
            dslContext.update(SEQ_KEY)
                    .set(SEQ_KEY.NEXT_SEQ_KEY_ID, seqKeyId)
                    .where(SEQ_KEY.SEQ_KEY_ID.eq(lastSeqKeyRecord.getSeqKeyId()))
                    .execute();
        }

        dslContext.update(BCC_MANIFEST)
                .set(BCC_MANIFEST.SEQ_KEY_ID, seqKeyId)
                .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(bccManifestId))
                .execute();

        ReleaseRecord release = dslContext.selectFrom(RELEASE)
                .where(RELEASE.RELEASE_ID.eq(bccManifestRecord.getReleaseId()))
                .fetchOne();
        if ("Working".equals(release.getReleaseNum()) && "Published".equals(state)) {
            bccManifestRecord = dslContext.selectFrom(BCC_MANIFEST)
                    .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(bccManifestId))
                    .fetchOne();

            ReleaseObject latestRelease = apiFactory.getReleaseAPI().getTheLatestRelease(
                    apiFactory.getLibraryAPI().getLibraryById(release.getLibraryId().toBigInteger())
            );
            BccManifestRecord prevBccManifestRecord = bccManifestRecord.copy();
            prevBccManifestRecord.setBccManifestId(null);
            prevBccManifestRecord.setReleaseId(ULong.valueOf(latestRelease.getReleaseId()));
            prevBccManifestRecord.setFromAccManifestId(dslContext.select(ACC_MANIFEST.ACC_MANIFEST_ID)
                    .from(ACC_MANIFEST)
                    .where(and(
                            ACC_MANIFEST.ACC_ID.eq(ULong.valueOf(fromAcc.getAccId())),
                            ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(latestRelease.getReleaseId()))
                    ))
                    .fetchOneInto(ULong.class));
            prevBccManifestRecord.setToBccpManifestId(dslContext.select(BCCP_MANIFEST.BCCP_MANIFEST_ID)
                    .from(BCCP_MANIFEST)
                    .where(and(
                            BCCP_MANIFEST.BCCP_ID.eq(ULong.valueOf(toBccp.getBccpId())),
                            BCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(latestRelease.getReleaseId()))
                    ))
                    .fetchOneInto(ULong.class));
            prevBccManifestRecord.setNextBccManifestId(bccManifestId);
            prevBccManifestRecord.setBccManifestId(
                    dslContext.insertInto(BCC_MANIFEST)
                            .set(prevBccManifestRecord)
                            .returning(BCC_MANIFEST.BCC_MANIFEST_ID)
                            .fetchOne().getBccManifestId());

            dslContext.update(BCC_MANIFEST)
                    .set(BCC_MANIFEST.PREV_BCC_MANIFEST_ID, prevBccManifestRecord.getBccManifestId())
                    .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(bccManifestId))
                    .execute();

            seqKeyRecord = dslContext.selectFrom(SEQ_KEY)
                    .where(SEQ_KEY.SEQ_KEY_ID.eq(seqKeyId))
                    .fetchOne();
            SeqKeyRecord prevSeqKeyRecord = seqKeyRecord.copy();
            prevSeqKeyRecord.setSeqKeyId(null);
            prevSeqKeyRecord.setFromAccManifestId(prevBccManifestRecord.getFromAccManifestId());
            prevSeqKeyRecord.setBccManifestId(prevBccManifestRecord.getBccManifestId());

            lastSeqKeyRecord = dslContext.selectFrom(SEQ_KEY)
                    .where(and(
                            SEQ_KEY.FROM_ACC_MANIFEST_ID.eq(prevBccManifestRecord.getFromAccManifestId()),
                            SEQ_KEY.NEXT_SEQ_KEY_ID.isNull()
                    ))
                    .fetchOne();
            if (lastSeqKeyRecord != null) {
                prevSeqKeyRecord.setPrevSeqKeyId(lastSeqKeyRecord.getSeqKeyId());
            }
            seqKeyId = dslContext.insertInto(SEQ_KEY)
                    .set(prevSeqKeyRecord)
                    .returning(SEQ_KEY.SEQ_KEY_ID)
                    .fetchOne().getSeqKeyId();
            if (lastSeqKeyRecord != null) {
                dslContext.update(SEQ_KEY)
                        .set(SEQ_KEY.NEXT_SEQ_KEY_ID, seqKeyId)
                        .where(SEQ_KEY.SEQ_KEY_ID.eq(lastSeqKeyRecord.getSeqKeyId()))
                        .execute();
            }

            dslContext.update(BCC_MANIFEST)
                    .set(BCC_MANIFEST.SEQ_KEY_ID, seqKeyId)
                    .where(BCC_MANIFEST.BCC_MANIFEST_ID.eq(prevBccManifestRecord.getBccManifestId()))
                    .execute();
        }

        return bcc;
    }

    @Override
    public void updateBCC(BCCObject bcc) {
        dslContext.update(BCC)
                .set(BCC.CARDINALITY_MIN, bcc.getCardinalityMin())
                .set(BCC.CARDINALITY_MAX, bcc.getCardinalityMax())
                .set(BCC.DEFINITION, bcc.getDefinition())
                .set(BCC.DEFINITION_SOURCE, bcc.getDefinitionSource())
                .set(BCC.IS_DEPRECATED, (byte) (bcc.isDeprecated() ? 1 : 0))
                .set(BCC.STATE, bcc.getState())
                .where(BCC.BCC_ID.eq(ULong.valueOf(bcc.getBccId())))
                .execute();
    }

    @Override
    public void updateASCCP(ASCCPObject asccp) {
        dslContext.update(ASCCP)
                .set(ASCCP.DEFINITION, asccp.getDefinition())
                .set(ASCCP.DEFINITION_SOURCE, asccp.getDefinitionSource())
                .set(ASCCP.IS_DEPRECATED, (byte) (asccp.isDeprecated() ? 1 : 0))
                .set(ASCCP.IS_NILLABLE, (byte) (asccp.isNillable() ? 1 : 0))
                .set(ASCCP.REUSABLE_INDICATOR, (byte) (asccp.isReusable() ? 1 : 0))
                .set(ASCCP.STATE, asccp.getState())
                .set(ASCCP.CREATION_TIMESTAMP, asccp.getCreationTimestamp())
                .set(ASCCP.LAST_UPDATE_TIMESTAMP, asccp.getLastUpdateTimestamp())
                .where(ASCCP.ASCCP_ID.eq(ULong.valueOf(asccp.getAsccpId())))
                .execute();
    }

    @Override
    public void updateBCCP(BCCPObject bccp) {
        dslContext.update(BCCP)
                .set(BCCP.DEFINITION, bccp.getDefinition())
                .set(BCCP.DEFINITION_SOURCE, bccp.getDefinitionSource())
                .set(BCCP.DEFAULT_VALUE, bccp.getDefaultValue())
                .set(BCCP.FIXED_VALUE, bccp.getFixedValue())
                .set(BCCP.IS_DEPRECATED, (byte) (bccp.isDeprecated() ? 1 : 0))
                .set(BCCP.IS_NILLABLE, (byte) (bccp.isNillable() ? 1 : 0))
                .set(BCCP.STATE, bccp.getState())
                .set(BCCP.CREATION_TIMESTAMP, bccp.getCreationTimestamp())
                .set(BCCP.LAST_UPDATE_TIMESTAMP, bccp.getLastUpdateTimestamp())
                .where(BCCP.BCCP_ID.eq(ULong.valueOf(bccp.getBccpId())))
                .execute();
    }

    @Override
    public void updateBasedDT(BCCPObject bccp, DTObject dataType) {
        dslContext.update(BCCP)
                .set(BCCP.BDT_ID, ULong.valueOf(dataType.getDtId()))
                .where(BCCP.BCCP_ID.eq(ULong.valueOf(bccp.getBccpId())))
                .execute();

        dslContext.update(BCCP_MANIFEST)
                .set(BCCP_MANIFEST.BDT_MANIFEST_ID, ULong.valueOf(dataType.getDtManifestId()))
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(ULong.valueOf(bccp.getBccpManifestId())))
                .execute();

        bccp.setBdtId(dataType.getDtId());
        bccp.setBdtManifestId(dataType.getDtManifestId());
    }

    private DTObject dtMapper(org.jooq.Record record) {
        DTObject dt = new DTObject();
        dt.setDtManifestId(record.get(DT_MANIFEST.DT_MANIFEST_ID).toBigInteger());
        dt.setReleaseId(record.get(DT_MANIFEST.RELEASE_ID).toBigInteger());
        dt.setDtId(record.get(DT.DT_ID).toBigInteger());
        ULong basedDtManifestId = record.get(DT_MANIFEST.BASED_DT_MANIFEST_ID);
        if (basedDtManifestId != null) {
            dt.setBasedDtManifestId(basedDtManifestId.toBigInteger());
        }
        ULong basedDtId = record.get(DT.BASED_DT_ID);
        if (basedDtId != null) {
            dt.setBasedDtId(basedDtId.toBigInteger());
        }
        dt.setGuid(record.get(DT.GUID));
        dt.setDataTypeTerm(record.get(DT.DATA_TYPE_TERM));
        dt.setRepresentationTerm(record.get(DT.REPRESENTATION_TERM));
        dt.setQualifier(record.get(DT.QUALIFIER));
        dt.setDen(record.get(DT_MANIFEST.DEN));
        dt.setDefinition(record.get(DT.DEFINITION));
        dt.setDefinitionSource(record.get(DT.DEFINITION_SOURCE));
        if (record.get(DT.NAMESPACE_ID) != null) {
            dt.setNamespaceId(record.get(DT.NAMESPACE_ID).toBigInteger());
        }
        dt.setState(record.get(DT.STATE));
        dt.setDeprecated(record.get(DT.IS_DEPRECATED) == 1);
        dt.setOwnerUserId(record.get(DT.OWNER_USER_ID).toBigInteger());
        dt.setCreatedBy(record.get(DT.CREATED_BY).toBigInteger());
        dt.setLastUpdatedBy(record.get(DT.LAST_UPDATED_BY).toBigInteger());
        dt.setCreationTimestamp(record.get(DT.CREATION_TIMESTAMP));
        dt.setLastUpdateTimestamp(record.get(DT.LAST_UPDATE_TIMESTAMP));
        dt.setContentComponentDefinition(record.get(DT.CONTENT_COMPONENT_DEFINITION));
        return dt;
    }

    private ULong getReleaseIdByReleaseNum(LibraryObject library, String releaseNum) {
        return ULong.valueOf(apiFactory.getReleaseAPI().getReleaseByReleaseNumber(library, releaseNum).getReleaseId());
    }

    @Override
    public List<DTSCObject> getSupplementaryComponentsForDT(BigInteger dtID, String release) {
        List<DTSCObject> dtList = new ArrayList<>();
        List<Field<?>> fields = new ArrayList();
        fields.addAll(Arrays.asList(DT_SC.fields()));
        List<Result<Record>> result = dslContext.select(fields)
                .from(DT_SC)
                .join(DT_SC_MANIFEST).on(DT_SC_MANIFEST.DT_SC_ID.eq(DT_SC.DT_SC_ID))
                .join(DT_MANIFEST).on(DT_MANIFEST.DT_MANIFEST_ID.eq(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID))
                .join(RELEASE).on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .where(and(
                        DT.DT_ID.eq(ULong.valueOf(dtID)),
                        RELEASE.RELEASE_NUM.eq(release)
                ))
                .fetchMany();
        for (Result<Record> r : result) {
            for (int i = 0; i < r.size(); i++) {
                DTSCObject dtSC = dtSCMapper(r.get(i));
                dtList.add(dtSC);
            }
        }
        return dtList;
    }

    @Override
    public boolean SCPropertyTermIsUnique(DTObject dataType, String release, String objectClassTerm, String representationTerm, String propertyTerm) {
        List<Field<?>> fields = new ArrayList();
        fields.addAll(Arrays.asList(DT_SC.fields()));
        List<Result<Record>> result = dslContext.select(fields)
                .from(DT_SC)
                .join(DT).on(DT.DT_ID.eq(DT_SC.OWNER_DT_ID))
                .join(DT_MANIFEST).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .join(RELEASE).on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(
                        DT.DT_ID.eq(ULong.valueOf(dataType.getDtId())),
                        RELEASE.RELEASE_NUM.eq(release),
                        DT_SC.OBJECT_CLASS_TERM.eq(objectClassTerm),
                        DT_SC.REPRESENTATION_TERM.eq(representationTerm),
                        DT_SC.PROPERTY_TERM.eq(propertyTerm)
                ))
                .fetchMany();
        if (result.size() > 1) {
            return false;
        } else return true;
    }

    @Override
    public List<String> getRepresentationTermsForCDTs(String release) {
        List<String> representationTerms = new ArrayList<>();

        representationTerms = dslContext.selectDistinct(DT.REPRESENTATION_TERM)
                .from(DT)
                .join(DT_MANIFEST).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .join(RELEASE).on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                .where(and(
                        RELEASE.RELEASE_NUM.eq(release),
                        LIBRARY.NAME.eq("CCTS Data Type Catalogue v3")
                ))
                .fetch(DT.REPRESENTATION_TERM);
        return representationTerms;
    }

    @Override
    public List<String> getValueDomainsByCDTRepresentationTerm(String representationTerm) {
        return dslContext.selectDistinct(CDT_PRI.NAME)
                .from(LIBRARY)
                .join(RELEASE).on(LIBRARY.LIBRARY_ID.eq(RELEASE.LIBRARY_ID))
                .join(DT_MANIFEST).on(RELEASE.RELEASE_ID.eq(DT_MANIFEST.RELEASE_ID))
                .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .join(DT_AWD_PRI).on(and(
                        DT_AWD_PRI.RELEASE_ID.eq(RELEASE.RELEASE_ID),
                        DT_AWD_PRI.DT_ID.eq(DT.DT_ID)
                ))
                .join(CDT_PRI).on(DT_AWD_PRI.CDT_PRI_ID.eq(CDT_PRI.CDT_PRI_ID))
                .join(XBT_MANIFEST).on(DT_AWD_PRI.XBT_MANIFEST_ID.eq(XBT_MANIFEST.XBT_MANIFEST_ID))
                .where(and(
                        LIBRARY.NAME.eq("CCTS Data Type Catalogue v3"),
                        RELEASE.RELEASE_NUM.eq("3.1"),
                        DT.REPRESENTATION_TERM.eq(representationTerm)
                ))
                .fetchInto(String.class);
    }

    @Override
    public String getDefaultValueDomainByCDTRepresentationTerm(String representationTerm) {
        return dslContext.select(CDT_PRI.NAME)
                .from(LIBRARY)
                .join(RELEASE).on(LIBRARY.LIBRARY_ID.eq(RELEASE.LIBRARY_ID))
                .join(DT_MANIFEST).on(RELEASE.RELEASE_ID.eq(DT_MANIFEST.RELEASE_ID))
                .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .join(DT_AWD_PRI).on(and(
                        DT_AWD_PRI.RELEASE_ID.eq(RELEASE.RELEASE_ID),
                        DT_AWD_PRI.DT_ID.eq(DT.DT_ID)
                ))
                .join(CDT_PRI).on(DT_AWD_PRI.CDT_PRI_ID.eq(CDT_PRI.CDT_PRI_ID))
                .join(XBT_MANIFEST).on(DT_AWD_PRI.XBT_MANIFEST_ID.eq(XBT_MANIFEST.XBT_MANIFEST_ID))
                .where(and(
                        LIBRARY.NAME.eq("CCTS Data Type Catalogue v3"),
                        RELEASE.RELEASE_NUM.eq("3.1"),
                        DT.REPRESENTATION_TERM.eq(representationTerm),
                        DT_AWD_PRI.IS_DEFAULT.eq((byte) 1)
                ))
                .fetchOneInto(String.class);
    }

    @Override
    public DTObject getRevisedDT(DTObject previousDT) {
        List<Field<?>> fields = new ArrayList();
        fields.add(DT_MANIFEST.DT_MANIFEST_ID);
        fields.add(DT_MANIFEST.BASED_DT_MANIFEST_ID);
        fields.add(DT_MANIFEST.RELEASE_ID);
        fields.add(DT_MANIFEST.DEN);
        fields.addAll(Arrays.asList(DT.fields()));
        return dslContext.select(fields)
                .from(DT_MANIFEST)
                .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .where(DT.PREV_DT_ID.eq(ULong.valueOf(previousDT.getDtId())))
                .fetchOne(record -> dtMapper(record));
    }

    @Override
    public DTSCObject getNewlyCreatedSCForDT(BigInteger dtId, String releaseNumber) {
        ULong latestSCId = dslContext.select(DSL.max(DT_SC.DT_SC_ID))
                .from(DT_SC)
                .join(DT).on(DT.DT_ID.eq(DT_SC.OWNER_DT_ID))
                .join(DT_MANIFEST).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .join(RELEASE).on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(DT.DT_ID.eq(ULong.valueOf(dtId)),
                        RELEASE.RELEASE_NUM.eq(releaseNumber)))
                .fetchOneInto(ULong.class);
        List<Field<?>> fields = new ArrayList();
        fields.addAll(Arrays.asList(DT_SC.fields()));
        return dslContext.select(fields)
                .from(DT_SC)
                .join(DT).on(DT.DT_ID.eq(DT_SC.OWNER_DT_ID))
                .join(DT_MANIFEST).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .join(RELEASE).on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(DT_SC.DT_SC_ID.eq(latestSCId)))
                .fetchOne(record -> dtSCMapper(record));
    }


    private DTSCObject dtSCMapper(org.jooq.Record record) {
        DTSCObject dtSC = new DTSCObject();
        dtSC.setDtSCId(record.get(DT_SC.DT_SC_ID).toBigInteger());
        dtSC.setOwnerDTId(record.get(DT_SC.OWNER_DT_ID).toBigInteger());
        dtSC.setGuid(record.get(DT_SC.GUID));
        dtSC.setObjectClassTerm(record.get(DT_SC.OBJECT_CLASS_TERM));
        dtSC.setPropertyTerm(record.get(DT_SC.PROPERTY_TERM));
        dtSC.setRepresentationTerm(record.get(DT_SC.REPRESENTATION_TERM));
        dtSC.setDefinition(record.get(DT_SC.DEFINITION));
        dtSC.setDefinitionSource(record.get(DT_SC.DEFINITION_SOURCE));
        dtSC.setOwnerUserId(record.get(DT_SC.OWNER_USER_ID).toBigInteger());
        dtSC.setCreatedBy(record.get(DT_SC.CREATED_BY).toBigInteger());
        dtSC.setLastUpdatedBy(record.get(DT_SC.LAST_UPDATED_BY).toBigInteger());
        return dtSC;
    }

}
