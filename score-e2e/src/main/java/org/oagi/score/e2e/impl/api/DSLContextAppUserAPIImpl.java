package org.oagi.score.e2e.impl.api;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.e2e.api.AppUserAPI;
import org.oagi.score.e2e.impl.api.helper.BCryptPasswordEncoder;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.AppUserRecord;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.DtManifestRecord;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.DtScManifestRecord;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.ReleaseRecord;
import org.oagi.score.e2e.obj.AppUserObject;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.or;
import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.*;

public class DSLContextAppUserAPIImpl implements AppUserAPI {

    private final DSLContext dslContext;

    public DSLContextAppUserAPIImpl(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public List<AppUserObject> getAppUsers() {
        return dslContext.selectFrom(APP_USER).fetch(record -> mapper(record));
    }

    @Override
    public AppUserObject getAppUserByLoginID(String loginID) {
        AppUserRecord appUserRecord = dslContext.selectFrom(APP_USER)
                .where(APP_USER.LOGIN_ID.eq(loginID))
                .fetchOptional().orElse(null);
        return mapper(appUserRecord);
    }

    @Override
    public AppUserObject getAppUserByID(BigInteger appUserId) {
        AppUserRecord appUserRecord = dslContext.selectFrom(APP_USER)
                .where(APP_USER.APP_USER_ID.eq(ULong.valueOf(appUserId)))
                .fetchOptional().orElse(null);
        return mapper(appUserRecord);
    }

    private AppUserObject mapper(AppUserRecord record) {
        if (record == null) {
            return null;
        }

        AppUserObject user = new AppUserObject();
        user.setAppUserId(record.getAppUserId().toBigInteger());
        user.setLoginId(record.getLoginId());
        user.setName(record.getName());
        user.setOrganization(record.getOrganization());
        user.setDeveloper(record.getIsDeveloper() == 1);
        user.setAdmin(record.getIsAdmin() == 1);
        user.setEnabled(record.getIsEnabled() == 1);
        return user;
    }

    @Override
    public BigInteger createAppUser(AppUserObject appUser) {
        AppUserRecord appUserRecord = new AppUserRecord();

        appUserRecord.setLoginId(appUser.getLoginId());
        appUserRecord.setPassword(new BCryptPasswordEncoder().encode(appUser.getPassword()));
        appUserRecord.setName(appUser.getName());
        appUserRecord.setOrganization(appUser.getOrganization());
        appUserRecord.setIsDeveloper((byte) (appUser.isDeveloper() ? 1 : 0));
        appUserRecord.setIsAdmin((byte) (appUser.isAdmin() ? 1 : 0));
        appUserRecord.setIsEnabled((byte) (appUser.isEnabled() ? 1 : 0));

        BigInteger appUserId = dslContext.insertInto(APP_USER)
                .set(appUserRecord)
                .returning(APP_USER.APP_USER_ID)
                .fetchOne().getAppUserId().toBigInteger();
        appUser.setAppUserId(appUserId);
        return appUserId;
    }

    @Override
    public AppUserObject createRandomDeveloperAccount(boolean admin) {
        AppUserObject appUser = new AppUserObject();
        appUser.setLoginId("dev_" + randomAlphanumeric(5, 10));
        appUser.setPassword("dev_" + randomAlphanumeric(5, 10));
        appUser.setName(appUser.getLoginId());
        appUser.setOrganization("Test User-Agent");
        appUser.setDeveloper(true);
        appUser.setAdmin(admin);
        appUser.setEnabled(true);

        createAppUser(appUser);
        return appUser;
    }

    @Override
    public AppUserObject createRandomEndUserAccount(boolean admin) {
        AppUserObject appUser = new AppUserObject();
        appUser.setLoginId("eu_" + randomAlphanumeric(5, 10));
        appUser.setPassword("eu_" + randomAlphanumeric(5, 10));
        appUser.setName(appUser.getLoginId());
        appUser.setOrganization("Test User-Agent");
        appUser.setDeveloper(false);
        appUser.setAdmin(admin);
        appUser.setEnabled(true);

        createAppUser(appUser);
        return appUser;
    }

    @Override
    public void disableAccount(AppUserObject appUser) {
        dslContext.update(APP_USER)
                .set(APP_USER.IS_ENABLED, (byte) 0)
                .where(APP_USER.LOGIN_ID.eq(appUser.getLoginId()))
                .execute();
    }

    @Override
    public void deleteAppUserByLoginId(String loginId) {
        ULong appUserId = dslContext.select(APP_USER.APP_USER_ID)
                .from(APP_USER)
                .where(APP_USER.LOGIN_ID.eq(loginId))
                .fetchOneInto(ULong.class);

        if (appUserId != null) {
            deleteAppUserById(appUserId.toBigInteger());
        }
    }

    @Override
    public void deleteAppUserById(BigInteger appUserIdBigInteger) {
        ULong appUserId = ULong.valueOf(appUserIdBigInteger);

        ReleaseRecord publishedRelease = dslContext.selectFrom(RELEASE)
                .where(and(
                        RELEASE.CREATED_BY.eq(appUserId),
                        RELEASE.STATE.eq("Published")
                ))
                .orderBy(RELEASE.LAST_UPDATE_TIMESTAMP.desc())
                .fetchAny();
        if (publishedRelease != null) {
            revertWorkingRelease(appUserId, publishedRelease);
        }

        dslContext.transaction(conf -> {
            DSLContext txContext = conf.dsl();
            txContext.execute("SET FOREIGN_KEY_CHECKS = 0");

            deleteOpenAPIDocumentByAppUserId(txContext, appUserId);
            deleteBusinessTermByAppUserId(txContext, appUserId);
            deleteBusinessInformationEntityByAppUserId(txContext, appUserId);
            deleteCoreComponentByAppUserId(txContext, appUserId);
            deleteCodeListByAppUserId(txContext, appUserId);
            deleteAgencyIDListListByAppUserId(txContext, appUserId);
            deleteBusinessContextByAppUserId(txContext, appUserId);
            deleteContextSchemeByAppUserId(txContext, appUserId);
            deleteContextCategoryByAppUserId(txContext, appUserId);
            deleteModuleSetReleaseByAppUserId(txContext, appUserId);
            deleteModuleSetByAppUserId(txContext, appUserId);
            deleteNamespaceByAppUserId(txContext, appUserId);
            deleteReleaseByAppUserId(txContext, appUserId);

            txContext.deleteFrom(APP_USER)
                    .where(APP_USER.APP_USER_ID.eq(appUserId))
                    .execute();

            txContext.execute("SET FOREIGN_KEY_CHECKS = 1");
        });
    }

    private void deleteOpenAPIDocumentByAppUserId(DSLContext dslContext, ULong appUserId) {
        List<ULong> oasDocIdList = dslContext.select(OAS_DOC.OAS_DOC_ID)
                .from(OAS_DOC)
                .where(OAS_DOC.CREATED_BY.eq(appUserId))
                .fetchInto(ULong.class);
        if (oasDocIdList.isEmpty()) {
            return;
        }

        List<ULong> oasResourceIdList = dslContext.select(OAS_RESOURCE.OAS_RESOURCE_ID)
                .from(OAS_RESOURCE)
                .where(OAS_RESOURCE.OAS_DOC_ID.in(oasDocIdList))
                .fetchInto(ULong.class);
        if (!oasResourceIdList.isEmpty()) {
            List<ULong> oasOperationIdList = dslContext.select(OAS_OPERATION.OAS_OPERATION_ID)
                    .from(OAS_OPERATION)
                    .where(OAS_OPERATION.OAS_RESOURCE_ID.in(oasResourceIdList))
                    .fetchInto(ULong.class);
            if (!oasOperationIdList.isEmpty()) {
                List<ULong> oasRequestIdList = dslContext.select(OAS_REQUEST.OAS_REQUEST_ID)
                        .from(OAS_REQUEST)
                        .where(OAS_REQUEST.OAS_OPERATION_ID.in(oasOperationIdList))
                        .fetchInto(ULong.class);
                if (!oasRequestIdList.isEmpty()) {
                    dslContext.deleteFrom(OAS_REQUEST_PARAMETER)
                            .where(OAS_REQUEST_PARAMETER.OAS_REQUEST_ID.in(oasRequestIdList))
                            .execute();
                    dslContext.deleteFrom(OAS_REQUEST)
                            .where(OAS_REQUEST.OAS_REQUEST_ID.in(oasRequestIdList))
                            .execute();
                }

                List<ULong> oasResponseIdList = dslContext.select(OAS_RESPONSE.OAS_RESPONSE_ID)
                        .from(OAS_RESPONSE)
                        .where(OAS_RESPONSE.OAS_OPERATION_ID.in(oasOperationIdList))
                        .fetchInto(ULong.class);
                if (!oasResponseIdList.isEmpty()) {
                    dslContext.deleteFrom(OAS_RESPONSE_HEADERS)
                            .where(OAS_RESPONSE_HEADERS.OAS_RESPONSE_ID.in(oasResponseIdList))
                            .execute();
                    dslContext.deleteFrom(OAS_RESPONSE)
                            .where(OAS_RESPONSE.OAS_RESPONSE_ID.in(oasResponseIdList))
                            .execute();
                }

                List<ULong> oasTagIdList = dslContext.select(OAS_RESOURCE_TAG.OAS_TAG_ID)
                        .from(OAS_RESOURCE_TAG)
                        .where(OAS_RESOURCE_TAG.OAS_OPERATION_ID.in(oasOperationIdList))
                        .fetchInto(ULong.class);
                if (!oasTagIdList.isEmpty()) {
                    dslContext.deleteFrom(OAS_TAG)
                            .where(OAS_TAG.OAS_TAG_ID.in(oasTagIdList))
                            .execute();
                }

                dslContext.deleteFrom(OAS_RESOURCE_TAG)
                        .where(OAS_RESOURCE_TAG.OAS_OPERATION_ID.in(oasOperationIdList))
                        .execute();

                dslContext.deleteFrom(OAS_OPERATION)
                        .where(OAS_OPERATION.OAS_OPERATION_ID.in(oasOperationIdList))
                        .execute();
            }

            dslContext.deleteFrom(OAS_RESOURCE)
                    .where(OAS_RESOURCE.OAS_RESOURCE_ID.in(oasResourceIdList))
                    .execute();
        }

        List<ULong> oasTagIdList = dslContext.select(OAS_DOC_TAG.OAS_TAG_ID)
                .from(OAS_DOC_TAG)
                .where(OAS_DOC_TAG.OAS_DOC_ID.in(oasDocIdList))
                .fetchInto(ULong.class);
        if (!oasTagIdList.isEmpty()) {
            dslContext.deleteFrom(OAS_TAG)
                    .where(OAS_TAG.OAS_TAG_ID.in(oasTagIdList))
                    .execute();
        }

        dslContext.deleteFrom(OAS_DOC_TAG)
                .where(OAS_DOC_TAG.OAS_DOC_ID.in(oasDocIdList))
                .execute();

        dslContext.deleteFrom(OAS_DOC)
                .where(OAS_DOC.OAS_DOC_ID.in(oasDocIdList))
                .execute();
    }

    private void deleteBusinessTermByAppUserId(DSLContext dslContext, ULong appUserId) {
        List<ULong> businessTermIdList = dslContext.select(BUSINESS_TERM.BUSINESS_TERM_ID)
                .from(BUSINESS_TERM)
                .where(BUSINESS_TERM.CREATED_BY.eq(appUserId))
                .fetchInto(ULong.class);
        if (businessTermIdList.isEmpty()) {
            return;
        }

        List<ULong> asccBiztermIdList = dslContext.select(ASCC_BIZTERM.ASCC_BIZTERM_ID)
                .from(ASCC_BIZTERM)
                .where(ASCC_BIZTERM.BUSINESS_TERM_ID.in(businessTermIdList))
                .fetchInto(ULong.class);
        if (!asccBiztermIdList.isEmpty()) {
            dslContext.deleteFrom(ASBIE_BIZTERM)
                    .where(ASBIE_BIZTERM.ASCC_BIZTERM_ID.in(asccBiztermIdList))
                    .execute();
            dslContext.deleteFrom(ASCC_BIZTERM)
                    .where(ASCC_BIZTERM.ASCC_BIZTERM_ID.in(asccBiztermIdList))
                    .execute();
        }

        List<ULong> bccBiztermIdList = dslContext.select(BCC_BIZTERM.BCC_BIZTERM_ID)
                .from(BCC_BIZTERM)
                .where(BCC_BIZTERM.BUSINESS_TERM_ID.in(businessTermIdList))
                .fetchInto(ULong.class);
        if (!bccBiztermIdList.isEmpty()) {
            dslContext.deleteFrom(BBIE_BIZTERM)
                    .where(BBIE_BIZTERM.BCC_BIZTERM_ID.in(bccBiztermIdList))
                    .execute();
            dslContext.deleteFrom(BCC_BIZTERM)
                    .where(BCC_BIZTERM.BCC_BIZTERM_ID.in(bccBiztermIdList))
                    .execute();
        }

        dslContext.deleteFrom(BUSINESS_TERM)
                .where(BUSINESS_TERM.BUSINESS_TERM_ID.in(businessTermIdList))
                .execute();
    }

    private void deleteBusinessInformationEntityByAppUserId(DSLContext dslContext, ULong appUserId) {
        List<ULong> topLevelAsbiepIdList = dslContext.select(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID)
                .from(TOP_LEVEL_ASBIEP)
                .where(TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(appUserId))
                .fetchInto(ULong.class);
        if (topLevelAsbiepIdList.isEmpty()) {
            return;
        }

        dslContext.deleteFrom(BBIE_SC)
                .where(BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIdList))
                .execute();
        dslContext.deleteFrom(BBIE)
                .where(BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIdList))
                .execute();
        dslContext.deleteFrom(ASBIE)
                .where(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIdList))
                .execute();
        dslContext.deleteFrom(BBIEP)
                .where(BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIdList))
                .execute();
        dslContext.update(TOP_LEVEL_ASBIEP)
                .setNull(TOP_LEVEL_ASBIEP.ASBIEP_ID)
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIdList))
                .execute();
        dslContext.deleteFrom(ASBIEP)
                .where(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIdList))
                .execute();
        dslContext.deleteFrom(ABIE)
                .where(ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIdList))
                .execute();
        dslContext.deleteFrom(BIZ_CTX_ASSIGNMENT)
                .where(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIdList))
                .execute();
        dslContext.deleteFrom(TOP_LEVEL_ASBIEP)
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(topLevelAsbiepIdList))
                .execute();
    }

    private void deleteDTSCByAppUserId(DSLContext dslContext, ULong appUserId) {
        List<ULong> dtScIdList = dslContext.select(DT_SC.DT_SC_ID)
                .from(DT_SC)
                .where(or(
                        DT_SC.OWNER_USER_ID.eq(appUserId),
                        DT_SC.CREATED_BY.eq(appUserId)
                ))
                .fetchInto(ULong.class);
        if (dtScIdList.isEmpty()) {
            return;
        }

        List<DtScManifestRecord> dtScManifestList = dslContext.selectFrom(DT_SC_MANIFEST)
                .where(DT_SC_MANIFEST.DT_SC_ID.in(dtScIdList))
                .fetch();
        if (!dtScManifestList.isEmpty()) {
            for (DtScManifestRecord dtScManifest : dtScManifestList) {
                dslContext.deleteFrom(DT_SC_AWD_PRI)
                        .where(and(
                                DT_SC_AWD_PRI.RELEASE_ID.eq(dtScManifest.getReleaseId()),
                                DT_SC_AWD_PRI.DT_SC_ID.eq(dtScManifest.getDtScId())
                        ))
                        .execute();
            }
        }

        dslContext.deleteFrom(DT_SC_MANIFEST)
                .where(DT_SC_MANIFEST.DT_SC_MANIFEST_ID.in(
                        dtScManifestList.stream().map(e -> e.getDtScManifestId()).collect(Collectors.toSet())
                ))
                .execute();
        dslContext.deleteFrom(DT_SC)
                .where(DT_SC.DT_SC_ID.in(dtScIdList))
                .execute();
    }

    private void deleteDTByAppUserId(DSLContext dslContext, ULong appUserId) {
        List<ULong> dtIdList = dslContext.select(DT.DT_ID)
                .from(DT)
                .where(or(
                        DT.OWNER_USER_ID.eq(appUserId),
                        DT.CREATED_BY.eq(appUserId)
                ))
                .fetchInto(ULong.class);
        if (dtIdList.isEmpty()) {
            return;
        }

        List<DtManifestRecord> dtManifestList = dslContext.selectFrom(DT_MANIFEST)
                .where(DT_MANIFEST.DT_ID.in(dtIdList))
                .fetch();
        if (!dtManifestList.isEmpty()) {
            for (DtManifestRecord dtManifest : dtManifestList) {
                dslContext.deleteFrom(DT_AWD_PRI)
                        .where(and(
                                DT_AWD_PRI.RELEASE_ID.eq(dtManifest.getReleaseId()),
                                DT_AWD_PRI.DT_ID.eq(dtManifest.getDtId())
                        ))
                        .execute();
            }
        }

        List<String> dtGuidList = dslContext.selectDistinct(DT.GUID)
                .from(DT)
                .where(DT.DT_ID.in(dtIdList))
                .fetchInto(String.class);
        dslContext.update(DT_MANIFEST)
                .setNull(DT_MANIFEST.LOG_ID)
                .where(DT_MANIFEST.DT_MANIFEST_ID.in(
                        dtManifestList.stream().map(e -> e.getDtManifestId()).collect(Collectors.toSet())
                ))
                .execute();
        dslContext.deleteFrom(LOG)
                .where(LOG.REFERENCE.in(dtGuidList))
                .execute();

        dslContext.deleteFrom(DT_MANIFEST)
                .where(DT_MANIFEST.DT_MANIFEST_ID.in(
                        dtManifestList.stream().map(e -> e.getDtManifestId()).collect(Collectors.toSet())
                ))
                .execute();
        dslContext.deleteFrom(DT)
                .where(DT.DT_ID.in(dtIdList))
                .execute();
    }

    private void deleteBCCPByAppUserId(DSLContext dslContext, ULong appUserId) {
        List<ULong> bccpIdList = dslContext.select(BCCP.BCCP_ID)
                .from(BCCP)
                .where(or(
                        BCCP.OWNER_USER_ID.eq(appUserId),
                        BCCP.CREATED_BY.eq(appUserId)
                ))
                .fetchInto(ULong.class);
        if (bccpIdList.isEmpty()) {
            return;
        }

        List<ULong> bccpManifestIdList = dslContext.select(BCCP_MANIFEST.BCCP_MANIFEST_ID)
                .from(BCCP_MANIFEST)
                .where(BCCP_MANIFEST.BCCP_ID.in(bccpIdList))
                .fetchInto(ULong.class);

        List<String> bccpGuidList = dslContext.selectDistinct(BCCP.GUID)
                .from(BCCP)
                .where(BCCP.BCCP_ID.in(bccpIdList))
                .fetchInto(String.class);
        dslContext.update(BCCP_MANIFEST)
                .setNull(BCCP_MANIFEST.LOG_ID)
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.in(bccpManifestIdList))
                .execute();
        dslContext.deleteFrom(LOG)
                .where(LOG.REFERENCE.in(bccpGuidList))
                .execute();

        dslContext.deleteFrom(BCCP_MANIFEST)
                .where(BCCP_MANIFEST.BCCP_MANIFEST_ID.in(bccpManifestIdList))
                .execute();
        dslContext.deleteFrom(BCCP)
                .where(BCCP.BCCP_ID.in(bccpIdList))
                .execute();
    }

    private void deleteASCCPByAppUserId(DSLContext dslContext, ULong appUserId) {
        List<ULong> asccpIdList = dslContext.select(ASCCP.ASCCP_ID)
                .from(ASCCP)
                .where(or(
                        ASCCP.OWNER_USER_ID.eq(appUserId),
                        ASCCP.CREATED_BY.eq(appUserId)
                ))
                .fetchInto(ULong.class);
        if (asccpIdList.isEmpty()) {
            return;
        }

        List<ULong> asccpManifestIdList = dslContext.select(ASCCP_MANIFEST.ASCCP_MANIFEST_ID)
                .from(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.ASCCP_ID.in(asccpIdList))
                .fetchInto(ULong.class);

        List<String> asccpGuidList = dslContext.selectDistinct(ASCCP.GUID)
                .from(ASCCP)
                .where(ASCCP.ASCCP_ID.in(asccpIdList))
                .fetchInto(String.class);
        dslContext.update(ASCCP_MANIFEST)
                .setNull(ASCCP_MANIFEST.LOG_ID)
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.in(asccpManifestIdList))
                .execute();
        dslContext.deleteFrom(LOG)
                .where(LOG.REFERENCE.in(asccpGuidList))
                .execute();

        dslContext.deleteFrom(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.in(asccpManifestIdList))
                .execute();
        dslContext.deleteFrom(ASCCP)
                .where(ASCCP.ASCCP_ID.in(asccpIdList))
                .execute();
    }

    private void deleteASCCByAppUserId(DSLContext dslContext, ULong appUserId) {
        List<ULong> asccIdList = dslContext.select(ASCC.ASCC_ID)
                .from(ASCC)
                .where(or(
                        ASCC.OWNER_USER_ID.eq(appUserId),
                        ASCC.CREATED_BY.eq(appUserId)
                ))
                .fetchInto(ULong.class);
        if (asccIdList.isEmpty()) {
            return;
        }

        List<ULong> asccManifestIdList = dslContext.select(ASCC_MANIFEST.ASCC_MANIFEST_ID)
                .from(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.ASCC_ID.in(asccIdList))
                .fetchInto(ULong.class);

        dslContext.update(ASCC_MANIFEST)
                .setNull(ASCC_MANIFEST.SEQ_KEY_ID)
                .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.in(asccManifestIdList))
                .execute();
        dslContext.deleteFrom(SEQ_KEY)
                .where(SEQ_KEY.ASCC_MANIFEST_ID.in(asccManifestIdList))
                .execute();
        dslContext.deleteFrom(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.ASCC_MANIFEST_ID.in(asccManifestIdList))
                .execute();
        dslContext.deleteFrom(ASCC)
                .where(ASCC.ASCC_ID.in(asccIdList))
                .execute();
    }

    private void deleteBCCByAppUserId(DSLContext dslContext, ULong appUserId) {
        List<ULong> bccIdList = dslContext.select(BCC.BCC_ID)
                .from(BCC)
                .where(or(
                        BCC.OWNER_USER_ID.eq(appUserId),
                        BCC.CREATED_BY.eq(appUserId)
                ))
                .fetchInto(ULong.class);
        if (bccIdList.isEmpty()) {
            return;
        }

        List<ULong> bccManifestIdList = dslContext.select(BCC_MANIFEST.BCC_MANIFEST_ID)
                .from(BCC_MANIFEST)
                .where(BCC_MANIFEST.BCC_ID.in(bccIdList))
                .fetchInto(ULong.class);

        dslContext.update(BCC_MANIFEST)
                .setNull(BCC_MANIFEST.SEQ_KEY_ID)
                .where(BCC_MANIFEST.BCC_MANIFEST_ID.in(bccManifestIdList))
                .execute();
        dslContext.deleteFrom(SEQ_KEY)
                .where(SEQ_KEY.BCC_MANIFEST_ID.in(bccManifestIdList))
                .execute();
        dslContext.deleteFrom(BCC_MANIFEST)
                .where(BCC_MANIFEST.BCC_MANIFEST_ID.in(bccManifestIdList))
                .execute();
        dslContext.deleteFrom(BCC)
                .where(BCC.BCC_ID.in(bccIdList))
                .execute();
    }

    private void deleteACCByAppUserId(DSLContext dslContext, ULong appUserId) {
        List<ULong> accIdList = dslContext.select(ACC.ACC_ID)
                .from(ACC)
                .where(or(
                        ACC.OWNER_USER_ID.eq(appUserId),
                        ACC.CREATED_BY.eq(appUserId)
                ))
                .fetchInto(ULong.class);
        if (accIdList.isEmpty()) {
            return;
        }

        List<ULong> accManifestIdList = dslContext.select(ACC_MANIFEST.ACC_MANIFEST_ID)
                .from(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_ID.in(accIdList))
                .fetchInto(ULong.class);

        dslContext.update(ASCC_MANIFEST)
                .setNull(ASCC_MANIFEST.SEQ_KEY_ID)
                .where(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.in(accManifestIdList))
                .execute();
        dslContext.update(BCC_MANIFEST)
                .setNull(BCC_MANIFEST.SEQ_KEY_ID)
                .where(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.in(accManifestIdList))
                .execute();
        dslContext.update(SEQ_KEY)
                .setNull(SEQ_KEY.PREV_SEQ_KEY_ID)
                .setNull(SEQ_KEY.NEXT_SEQ_KEY_ID)
                .where(SEQ_KEY.FROM_ACC_MANIFEST_ID.in(accManifestIdList))
                .execute();
        dslContext.deleteFrom(SEQ_KEY)
                .where(SEQ_KEY.FROM_ACC_MANIFEST_ID.in(accManifestIdList))
                .execute();
        dslContext.deleteFrom(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.in(accManifestIdList))
                .execute();
        dslContext.deleteFrom(BCC_MANIFEST)
                .where(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.in(accManifestIdList))
                .execute();

        List<String> accGuidList = dslContext.selectDistinct(ACC.GUID)
                .from(ACC)
                .where(ACC.ACC_ID.in(accIdList))
                .fetchInto(String.class);
        dslContext.update(ACC_MANIFEST)
                .setNull(ACC_MANIFEST.LOG_ID)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.in(accManifestIdList))
                .execute();
        dslContext.deleteFrom(LOG)
                .where(LOG.REFERENCE.in(accGuidList))
                .execute();

        dslContext.deleteFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.in(accManifestIdList))
                .execute();
        dslContext.deleteFrom(ACC)
                .where(ACC.ACC_ID.in(accIdList))
                .execute();
    }

    private void deleteCoreComponentByAppUserId(DSLContext dslContext, ULong appUserId) {
        deleteDTSCByAppUserId(dslContext, appUserId);
        deleteDTByAppUserId(dslContext, appUserId);
        deleteBCCPByAppUserId(dslContext, appUserId);
        deleteASCCPByAppUserId(dslContext, appUserId);
        deleteACCByAppUserId(dslContext, appUserId);

        deleteASCCByAppUserId(dslContext, appUserId);
        deleteBCCByAppUserId(dslContext, appUserId);
    }

    private void deleteCodeListByAppUserId(DSLContext dslContext, ULong appUserId) {
        List<ULong> codeListIdList = dslContext.select(CODE_LIST.CODE_LIST_ID)
                .from(CODE_LIST)
                .where(or(
                        CODE_LIST.OWNER_USER_ID.eq(appUserId),
                        CODE_LIST.CREATED_BY.eq(appUserId)
                ))
                .fetchInto(ULong.class);
        if (codeListIdList.isEmpty()) {
            return;
        }

        List<String> codeListGuidList = dslContext.select(CODE_LIST.GUID)
                .from(CODE_LIST)
                .where(CODE_LIST.CODE_LIST_ID.in(codeListIdList))
                .fetchInto(String.class);

        List<ULong> codeListManifestIdList = dslContext.select(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID)
                .from(CODE_LIST_MANIFEST)
                .where(CODE_LIST_MANIFEST.CODE_LIST_ID.in(codeListIdList))
                .fetchInto(ULong.class);

        dslContext.update(CODE_LIST_MANIFEST)
                .setNull(CODE_LIST_MANIFEST.LOG_ID)
                .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.in(codeListManifestIdList))
                .execute();
        dslContext.deleteFrom(LOG)
                .where(LOG.REFERENCE.in(codeListGuidList))
                .execute();
        dslContext.deleteFrom(CODE_LIST_VALUE_MANIFEST)
                .where(CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID.in(codeListManifestIdList))
                .execute();
        dslContext.deleteFrom(CODE_LIST_MANIFEST)
                .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.in(codeListManifestIdList))
                .execute();
        dslContext.deleteFrom(CODE_LIST_VALUE)
                .where(CODE_LIST_VALUE.CODE_LIST_ID.in(codeListIdList))
                .execute();
        dslContext.deleteFrom(CODE_LIST)
                .where(CODE_LIST.CODE_LIST_ID.in(codeListIdList))
                .execute();
    }

    private void deleteAgencyIDListListByAppUserId(DSLContext dslContext, ULong appUserId) {
        List<ULong> agencyIDListIdList = dslContext.select(AGENCY_ID_LIST.AGENCY_ID_LIST_ID)
                .from(AGENCY_ID_LIST)
                .where(or(
                        AGENCY_ID_LIST.OWNER_USER_ID.eq(appUserId),
                        AGENCY_ID_LIST.CREATED_BY.eq(appUserId)
                ))
                .fetchInto(ULong.class);
        if (agencyIDListIdList.isEmpty()) {
            return;
        }

        List<String> agencyIDListListGuidList = dslContext.select(AGENCY_ID_LIST.GUID)
                .from(AGENCY_ID_LIST)
                .where(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.in(agencyIDListIdList))
                .fetchInto(String.class);

        List<ULong> agencyIDListListManifestIdList = dslContext.select(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID)
                .from(AGENCY_ID_LIST_MANIFEST)
                .where(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.in(agencyIDListIdList))
                .fetchInto(ULong.class);

        dslContext.update(AGENCY_ID_LIST_MANIFEST)
                .setNull(AGENCY_ID_LIST_MANIFEST.LOG_ID)
                .where(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.in(agencyIDListListManifestIdList))
                .execute();
        dslContext.deleteFrom(LOG)
                .where(LOG.REFERENCE.in(agencyIDListListGuidList))
                .execute();
        dslContext.deleteFrom(AGENCY_ID_LIST_VALUE_MANIFEST)
                .where(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.in(agencyIDListListManifestIdList))
                .execute();
        dslContext.deleteFrom(AGENCY_ID_LIST_MANIFEST)
                .where(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.in(agencyIDListListManifestIdList))
                .execute();
        dslContext.deleteFrom(AGENCY_ID_LIST_VALUE)
                .where(AGENCY_ID_LIST_VALUE.OWNER_LIST_ID.in(agencyIDListIdList))
                .execute();
        dslContext.deleteFrom(AGENCY_ID_LIST)
                .where(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.in(agencyIDListIdList))
                .execute();
    }

    private void deleteBusinessContextByAppUserId(DSLContext dslContext, ULong appUserId) {
        List<ULong> bizCtxIdList = dslContext.select(BIZ_CTX.BIZ_CTX_ID)
                .from(BIZ_CTX)
                .where(BIZ_CTX.CREATED_BY.eq(appUserId))
                .fetchInto(ULong.class);
        if (bizCtxIdList.isEmpty()) {
            return;
        }

        dslContext.deleteFrom(BIZ_CTX_ASSIGNMENT)
                .where(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID.in(bizCtxIdList))
                .execute();
        dslContext.deleteFrom(BIZ_CTX_VALUE)
                .where(BIZ_CTX_VALUE.BIZ_CTX_ID.in(bizCtxIdList))
                .execute();
        dslContext.deleteFrom(BIZ_CTX)
                .where(BIZ_CTX.BIZ_CTX_ID.in(bizCtxIdList))
                .execute();
    }

    private void deleteContextSchemeByAppUserId(DSLContext dslContext, ULong appUserId) {
        List<ULong> ctxSchemeIdList = dslContext.select(CTX_SCHEME.CTX_SCHEME_ID)
                .from(CTX_SCHEME)
                .where(CTX_SCHEME.CREATED_BY.eq(appUserId))
                .fetchInto(ULong.class);
        if (ctxSchemeIdList.isEmpty()) {
            return;
        }

        List<ULong> ctxSchemeValueIdList = dslContext.select(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID)
                .from(CTX_SCHEME_VALUE)
                .where(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID.in(ctxSchemeIdList))
                .fetchInto(ULong.class);
        if (!ctxSchemeValueIdList.isEmpty()) {
            dslContext.deleteFrom(BIZ_CTX_VALUE)
                    .where(BIZ_CTX_VALUE.CTX_SCHEME_VALUE_ID.in(ctxSchemeValueIdList))
                    .execute();
            dslContext.deleteFrom(CTX_SCHEME_VALUE)
                    .where(CTX_SCHEME_VALUE.CTX_SCHEME_VALUE_ID.in(ctxSchemeValueIdList))
                    .execute();
        }

        dslContext.deleteFrom(CTX_SCHEME_VALUE)
                .where(CTX_SCHEME_VALUE.OWNER_CTX_SCHEME_ID.in(ctxSchemeIdList))
                .execute();
        dslContext.deleteFrom(CTX_SCHEME)
                .where(CTX_SCHEME.CTX_SCHEME_ID.in(ctxSchemeIdList))
                .execute();
    }

    private void deleteContextCategoryByAppUserId(DSLContext dslContext, ULong appUserId) {
        dslContext.deleteFrom(CTX_CATEGORY)
                .where(CTX_CATEGORY.CREATED_BY.eq(appUserId))
                .execute();
    }

    private void deleteModuleSetReleaseByAppUserId(DSLContext dslContext, ULong appUserId) {
        List<ULong> moduleSetReleaseIdList = dslContext.select(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID)
                .from(MODULE_SET_RELEASE)
                .where(MODULE_SET_RELEASE.CREATED_BY.eq(appUserId))
                .fetchInto(ULong.class);
        if (moduleSetReleaseIdList != null && !moduleSetReleaseIdList.isEmpty()) {
            dslContext.deleteFrom(MODULE_ACC_MANIFEST)
                    .where(MODULE_ACC_MANIFEST.MODULE_SET_RELEASE_ID.in(moduleSetReleaseIdList))
                    .execute();
            dslContext.deleteFrom(MODULE_AGENCY_ID_LIST_MANIFEST)
                    .where(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_SET_RELEASE_ID.in(moduleSetReleaseIdList))
                    .execute();
            dslContext.deleteFrom(MODULE_ASCCP_MANIFEST)
                    .where(MODULE_ASCCP_MANIFEST.MODULE_SET_RELEASE_ID.in(moduleSetReleaseIdList))
                    .execute();
            dslContext.deleteFrom(MODULE_BCCP_MANIFEST)
                    .where(MODULE_BCCP_MANIFEST.MODULE_SET_RELEASE_ID.in(moduleSetReleaseIdList))
                    .execute();
            dslContext.deleteFrom(MODULE_BLOB_CONTENT_MANIFEST)
                    .where(MODULE_BLOB_CONTENT_MANIFEST.MODULE_SET_RELEASE_ID.in(moduleSetReleaseIdList))
                    .execute();
            dslContext.deleteFrom(MODULE_CODE_LIST_MANIFEST)
                    .where(MODULE_CODE_LIST_MANIFEST.MODULE_SET_RELEASE_ID.in(moduleSetReleaseIdList))
                    .execute();
            dslContext.deleteFrom(MODULE_DT_MANIFEST)
                    .where(MODULE_DT_MANIFEST.MODULE_SET_RELEASE_ID.in(moduleSetReleaseIdList))
                    .execute();
            dslContext.deleteFrom(MODULE_XBT_MANIFEST)
                    .where(MODULE_XBT_MANIFEST.MODULE_SET_RELEASE_ID.in(moduleSetReleaseIdList))
                    .execute();
            dslContext.deleteFrom(MODULE_SET_RELEASE)
                    .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.in(moduleSetReleaseIdList))
                    .execute();
        }
    }

    private void deleteModuleSetByAppUserId(DSLContext dslContext, ULong appUserId) {
        List<ULong> moduleSetIdList = dslContext.select(MODULE_SET.MODULE_SET_ID)
                .from(MODULE_SET)
                .where(MODULE_SET.CREATED_BY.eq(appUserId))
                .fetchInto(ULong.class);
        if (moduleSetIdList != null && !moduleSetIdList.isEmpty()) {
            dslContext.deleteFrom(MODULE_SET)
                    .where(MODULE_SET.MODULE_SET_ID.in(moduleSetIdList))
                    .execute();
        }
    }

    private void deleteNamespaceByAppUserId(DSLContext dslContext, ULong appUserId) {
        List<ULong> namespaceIdList = dslContext.select(NAMESPACE.NAMESPACE_ID)
                .from(NAMESPACE)
                .where(NAMESPACE.CREATED_BY.eq(appUserId))
                .fetchInto(ULong.class);
        if (namespaceIdList != null && !namespaceIdList.isEmpty()) {
            dslContext.deleteFrom(NAMESPACE)
                    .where(NAMESPACE.NAMESPACE_ID.in(namespaceIdList))
                    .execute();
        }
    }

    private void deleteReleaseByAppUserId(DSLContext dslContext, ULong appUserId) {
        List<ULong> releaseIdList = dslContext.select(RELEASE.RELEASE_ID)
                .from(RELEASE)
                .where(RELEASE.CREATED_BY.eq(appUserId))
                .fetchInto(ULong.class);
        if (releaseIdList != null && !releaseIdList.isEmpty()) {
            dslContext.deleteFrom(RELEASE)
                    .where(RELEASE.RELEASE_ID.in(releaseIdList))
                    .execute();
        }
    }

    private void revertWorkingRelease(ULong appUserId, ReleaseRecord publishedRelease) {
        dslContext.execute("SET FOREIGN_KEY_CHECKS = 0");

        ULong adminId = dslContext.select(APP_USER.APP_USER_ID)
                .from(APP_USER)
                .where(and(
                        APP_USER.IS_ADMIN.eq((byte) 1),
                        APP_USER.APP_USER_ID.greaterThan(ULong.valueOf(0L))
                ))
                .orderBy(APP_USER.APP_USER_ID.asc())
                .fetchAnyInto(ULong.class);
        ULong releaseId = publishedRelease.getReleaseId();

        // ACCs
        dslContext.update(ACC_MANIFEST
                        .join(ACC_MANIFEST.as("working"))
                        .on(ACC_MANIFEST.NEXT_ACC_MANIFEST_ID.eq(ACC_MANIFEST.as("working").ACC_MANIFEST_ID))
                        .join(ACC_MANIFEST.as("prev"))
                        .on(ACC_MANIFEST.PREV_ACC_MANIFEST_ID.eq(ACC_MANIFEST.as("prev").ACC_MANIFEST_ID)))
                .set(ACC_MANIFEST.as("working").PREV_ACC_MANIFEST_ID, ACC_MANIFEST.PREV_ACC_MANIFEST_ID)
                .set(ACC_MANIFEST.as("prev").NEXT_ACC_MANIFEST_ID, ACC_MANIFEST.NEXT_ACC_MANIFEST_ID)
                .where(ACC_MANIFEST.RELEASE_ID.eq(releaseId))
                .execute();
        dslContext.deleteFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.RELEASE_ID.eq(releaseId))
                .execute();
        dslContext.update(ACC)
                .set(ACC.OWNER_USER_ID, ACC.LAST_UPDATED_BY)
                .where(and(
                        ACC.OWNER_USER_ID.eq(appUserId),
                        ACC.LAST_UPDATED_BY.ne(appUserId)
                ))
                .execute();

        // ASCCs
        dslContext.update(ASCC_MANIFEST
                        .join(ASCC_MANIFEST.as("working"))
                        .on(ASCC_MANIFEST.NEXT_ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.as("working").ASCC_MANIFEST_ID))
                        .join(ASCC_MANIFEST.as("prev"))
                        .on(ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.as("prev").ASCC_MANIFEST_ID)))
                .set(ASCC_MANIFEST.as("working").PREV_ASCC_MANIFEST_ID, ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID)
                .set(ASCC_MANIFEST.as("prev").NEXT_ASCC_MANIFEST_ID, ASCC_MANIFEST.NEXT_ASCC_MANIFEST_ID)
                .where(ASCC_MANIFEST.RELEASE_ID.eq(releaseId))
                .execute();
        dslContext.deleteFrom(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.RELEASE_ID.eq(releaseId))
                .execute();
        dslContext.update(ASCC)
                .set(ASCC.OWNER_USER_ID, ASCC.LAST_UPDATED_BY)
                .where(and(
                        ASCC.OWNER_USER_ID.eq(appUserId),
                        ASCC.LAST_UPDATED_BY.ne(appUserId)
                ))
                .execute();

        // BCCs
        dslContext.update(BCC_MANIFEST
                        .join(BCC_MANIFEST.as("working"))
                        .on(BCC_MANIFEST.NEXT_BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("working").BCC_MANIFEST_ID))
                        .join(BCC_MANIFEST.as("prev"))
                        .on(BCC_MANIFEST.PREV_BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("prev").BCC_MANIFEST_ID)))
                .set(BCC_MANIFEST.as("working").PREV_BCC_MANIFEST_ID, BCC_MANIFEST.PREV_BCC_MANIFEST_ID)
                .set(BCC_MANIFEST.as("prev").NEXT_BCC_MANIFEST_ID, BCC_MANIFEST.NEXT_BCC_MANIFEST_ID)
                .where(BCC_MANIFEST.RELEASE_ID.eq(releaseId))
                .execute();
        dslContext.deleteFrom(BCC_MANIFEST)
                .where(BCC_MANIFEST.RELEASE_ID.eq(releaseId))
                .execute();
        dslContext.update(BCC)
                .set(BCC.OWNER_USER_ID, BCC.LAST_UPDATED_BY)
                .where(and(
                        BCC.OWNER_USER_ID.eq(appUserId),
                        BCC.LAST_UPDATED_BY.ne(appUserId)
                ))
                .execute();

        // ASCCPs
        dslContext.update(ASCCP_MANIFEST
                        .join(ASCCP_MANIFEST.as("working"))
                        .on(ASCCP_MANIFEST.NEXT_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.as("working").ASCCP_MANIFEST_ID))
                        .join(ASCCP_MANIFEST.as("prev"))
                        .on(ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.as("prev").ASCCP_MANIFEST_ID)))
                .set(ASCCP_MANIFEST.as("working").PREV_ASCCP_MANIFEST_ID, ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID)
                .set(ASCCP_MANIFEST.as("prev").NEXT_ASCCP_MANIFEST_ID, ASCCP_MANIFEST.NEXT_ASCCP_MANIFEST_ID)
                .where(ASCCP_MANIFEST.RELEASE_ID.eq(releaseId))
                .execute();
        dslContext.deleteFrom(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.RELEASE_ID.eq(releaseId))
                .execute();
        dslContext.update(ASCCP)
                .set(ASCCP.OWNER_USER_ID, ASCCP.LAST_UPDATED_BY)
                .where(and(
                        ASCCP.OWNER_USER_ID.eq(appUserId),
                        ASCCP.LAST_UPDATED_BY.ne(appUserId)
                ))
                .execute();

        // BCCPs
        dslContext.update(BCCP_MANIFEST
                        .join(BCCP_MANIFEST.as("working"))
                        .on(BCCP_MANIFEST.NEXT_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.as("working").BCCP_MANIFEST_ID))
                        .join(BCCP_MANIFEST.as("prev"))
                        .on(BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.as("prev").BCCP_MANIFEST_ID)))
                .set(BCCP_MANIFEST.as("working").PREV_BCCP_MANIFEST_ID, BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID)
                .set(BCCP_MANIFEST.as("prev").NEXT_BCCP_MANIFEST_ID, BCCP_MANIFEST.NEXT_BCCP_MANIFEST_ID)
                .where(BCCP_MANIFEST.RELEASE_ID.eq(releaseId))
                .execute();
        dslContext.deleteFrom(BCCP_MANIFEST)
                .where(BCCP_MANIFEST.RELEASE_ID.eq(releaseId))
                .execute();
        dslContext.update(BCCP)
                .set(BCCP.OWNER_USER_ID, BCCP.LAST_UPDATED_BY)
                .where(and(
                        BCCP.OWNER_USER_ID.eq(appUserId),
                        BCCP.LAST_UPDATED_BY.ne(appUserId)
                ))
                .execute();

        // CODE_LISTs
        dslContext.update(CODE_LIST_MANIFEST
                        .join(CODE_LIST_MANIFEST.as("working"))
                        .on(CODE_LIST_MANIFEST.NEXT_CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.as("working").CODE_LIST_MANIFEST_ID))
                        .join(CODE_LIST_MANIFEST.as("prev"))
                        .on(CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.as("prev").CODE_LIST_MANIFEST_ID)))
                .set(CODE_LIST_MANIFEST.as("working").PREV_CODE_LIST_MANIFEST_ID, CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID)
                .set(CODE_LIST_MANIFEST.as("prev").NEXT_CODE_LIST_MANIFEST_ID, CODE_LIST_MANIFEST.NEXT_CODE_LIST_MANIFEST_ID)
                .where(CODE_LIST_MANIFEST.RELEASE_ID.eq(releaseId))
                .execute();
        dslContext.deleteFrom(CODE_LIST_MANIFEST)
                .where(CODE_LIST_MANIFEST.RELEASE_ID.eq(releaseId))
                .execute();
        dslContext.update(CODE_LIST)
                .set(CODE_LIST.OWNER_USER_ID, CODE_LIST.LAST_UPDATED_BY)
                .where(and(
                        CODE_LIST.OWNER_USER_ID.eq(appUserId),
                        CODE_LIST.LAST_UPDATED_BY.ne(appUserId)
                ))
                .execute();

        // CODE_LIST_VALUEs
        dslContext.update(CODE_LIST_VALUE_MANIFEST
                        .join(CODE_LIST_VALUE_MANIFEST.as("working"))
                        .on(CODE_LIST_VALUE_MANIFEST.NEXT_CODE_LIST_VALUE_MANIFEST_ID.eq(CODE_LIST_VALUE_MANIFEST.as("working").CODE_LIST_VALUE_MANIFEST_ID))
                        .join(CODE_LIST_VALUE_MANIFEST.as("prev"))
                        .on(CODE_LIST_VALUE_MANIFEST.PREV_CODE_LIST_VALUE_MANIFEST_ID.eq(CODE_LIST_VALUE_MANIFEST.as("prev").CODE_LIST_VALUE_MANIFEST_ID)))
                .set(CODE_LIST_VALUE_MANIFEST.as("working").PREV_CODE_LIST_VALUE_MANIFEST_ID, CODE_LIST_VALUE_MANIFEST.PREV_CODE_LIST_VALUE_MANIFEST_ID)
                .set(CODE_LIST_VALUE_MANIFEST.as("prev").NEXT_CODE_LIST_VALUE_MANIFEST_ID, CODE_LIST_VALUE_MANIFEST.NEXT_CODE_LIST_VALUE_MANIFEST_ID)
                .where(CODE_LIST_VALUE_MANIFEST.RELEASE_ID.eq(releaseId))
                .execute();
        dslContext.deleteFrom(CODE_LIST_VALUE_MANIFEST)
                .where(CODE_LIST_VALUE_MANIFEST.RELEASE_ID.eq(releaseId))
                .execute();
        dslContext.update(CODE_LIST_VALUE)
                .set(CODE_LIST_VALUE.OWNER_USER_ID, CODE_LIST_VALUE.LAST_UPDATED_BY)
                .where(and(
                        CODE_LIST_VALUE.OWNER_USER_ID.eq(appUserId),
                        CODE_LIST_VALUE.LAST_UPDATED_BY.ne(appUserId)
                ))
                .execute();

        // DTs
        dslContext.update(DT_MANIFEST
                        .join(DT_MANIFEST.as("working"))
                        .on(DT_MANIFEST.NEXT_DT_MANIFEST_ID.eq(DT_MANIFEST.as("working").DT_MANIFEST_ID))
                        .join(DT_MANIFEST.as("prev"))
                        .on(DT_MANIFEST.PREV_DT_MANIFEST_ID.eq(DT_MANIFEST.as("prev").DT_MANIFEST_ID)))
                .set(DT_MANIFEST.as("working").PREV_DT_MANIFEST_ID, DT_MANIFEST.PREV_DT_MANIFEST_ID)
                .set(DT_MANIFEST.as("prev").NEXT_DT_MANIFEST_ID, DT_MANIFEST.NEXT_DT_MANIFEST_ID)
                .where(DT_MANIFEST.RELEASE_ID.eq(releaseId))
                .execute();
        dslContext.deleteFrom(DT_MANIFEST)
                .where(DT_MANIFEST.RELEASE_ID.eq(releaseId))
                .execute();
        dslContext.update(DT)
                .set(DT.OWNER_USER_ID, DT.LAST_UPDATED_BY)
                .where(and(
                        DT.OWNER_USER_ID.eq(appUserId),
                        DT.LAST_UPDATED_BY.ne(appUserId)
                ))
                .execute();

        //DT_SCs
        dslContext.update(DT_SC_MANIFEST
                        .join(DT_SC_MANIFEST.as("working"))
                        .on(DT_SC_MANIFEST.NEXT_DT_SC_MANIFEST_ID.eq(DT_SC_MANIFEST.as("working").DT_SC_MANIFEST_ID))
                        .join(DT_SC_MANIFEST.as("prev"))
                        .on(DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID.eq(DT_SC_MANIFEST.as("prev").DT_SC_MANIFEST_ID)))
                .set(DT_SC_MANIFEST.as("working").PREV_DT_SC_MANIFEST_ID, DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID)
                .set(DT_SC_MANIFEST.as("prev").NEXT_DT_SC_MANIFEST_ID, DT_SC_MANIFEST.NEXT_DT_SC_MANIFEST_ID)
                .where(DT_SC_MANIFEST.RELEASE_ID.eq(releaseId))
                .execute();
        dslContext.deleteFrom(DT_SC_MANIFEST)
                .where(DT_SC_MANIFEST.RELEASE_ID.eq(releaseId))
                .execute();
        dslContext.update(DT_SC)
                .set(DT_SC.OWNER_USER_ID, DT_SC.LAST_UPDATED_BY)
                .where(and(
                        DT_SC.OWNER_USER_ID.eq(appUserId),
                        DT_SC.LAST_UPDATED_BY.ne(appUserId)
                ))
                .execute();

        //XBTs
        dslContext.update(XBT_MANIFEST
                        .join(XBT_MANIFEST.as("working"))
                        .on(XBT_MANIFEST.NEXT_XBT_MANIFEST_ID.eq(XBT_MANIFEST.as("working").XBT_MANIFEST_ID))
                        .join(XBT_MANIFEST.as("prev"))
                        .on(XBT_MANIFEST.PREV_XBT_MANIFEST_ID.eq(XBT_MANIFEST.as("prev").XBT_MANIFEST_ID)))
                .set(XBT_MANIFEST.as("working").PREV_XBT_MANIFEST_ID, XBT_MANIFEST.PREV_XBT_MANIFEST_ID)
                .set(XBT_MANIFEST.as("prev").NEXT_XBT_MANIFEST_ID, XBT_MANIFEST.NEXT_XBT_MANIFEST_ID)
                .where(XBT_MANIFEST.RELEASE_ID.eq(releaseId))
                .execute();
        dslContext.deleteFrom(XBT_MANIFEST)
                .where(XBT_MANIFEST.RELEASE_ID.eq(releaseId))
                .execute();
        dslContext.update(XBT)
                .set(XBT.OWNER_USER_ID, XBT.LAST_UPDATED_BY)
                .where(and(
                        XBT.OWNER_USER_ID.eq(appUserId),
                        XBT.LAST_UPDATED_BY.ne(appUserId)
                ))
                .execute();

        // AGENCY_ID_LIST
        dslContext.update(AGENCY_ID_LIST_MANIFEST
                        .join(AGENCY_ID_LIST_MANIFEST.as("working"))
                        .on(AGENCY_ID_LIST_MANIFEST.NEXT_AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.as("working").AGENCY_ID_LIST_MANIFEST_ID))
                        .join(AGENCY_ID_LIST_MANIFEST.as("prev"))
                        .on(AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.as("prev").AGENCY_ID_LIST_MANIFEST_ID)))
                .set(AGENCY_ID_LIST_MANIFEST.as("working").PREV_AGENCY_ID_LIST_MANIFEST_ID, AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID)
                .set(AGENCY_ID_LIST_MANIFEST.as("prev").NEXT_AGENCY_ID_LIST_MANIFEST_ID, AGENCY_ID_LIST_MANIFEST.NEXT_AGENCY_ID_LIST_MANIFEST_ID)
                .where(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(releaseId))
                .execute();
        dslContext.deleteFrom(AGENCY_ID_LIST_MANIFEST)
                .where(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(releaseId))
                .execute();
        dslContext.update(AGENCY_ID_LIST)
                .set(AGENCY_ID_LIST.OWNER_USER_ID, AGENCY_ID_LIST.LAST_UPDATED_BY)
                .where(and(
                        AGENCY_ID_LIST.OWNER_USER_ID.eq(appUserId),
                        AGENCY_ID_LIST.LAST_UPDATED_BY.ne(appUserId)
                ))
                .execute();

        // AGENCY_ID_LIST_VALUE
        dslContext.update(AGENCY_ID_LIST_VALUE_MANIFEST
                        .join(AGENCY_ID_LIST_VALUE_MANIFEST.as("working"))
                        .on(AGENCY_ID_LIST_VALUE_MANIFEST.NEXT_AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.as("working").AGENCY_ID_LIST_VALUE_MANIFEST_ID))
                        .join(AGENCY_ID_LIST_VALUE_MANIFEST.as("prev"))
                        .on(AGENCY_ID_LIST_VALUE_MANIFEST.PREV_AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.as("prev").AGENCY_ID_LIST_VALUE_MANIFEST_ID)))
                .set(AGENCY_ID_LIST_VALUE_MANIFEST.as("working").PREV_AGENCY_ID_LIST_VALUE_MANIFEST_ID, AGENCY_ID_LIST_VALUE_MANIFEST.PREV_AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                .set(AGENCY_ID_LIST_VALUE_MANIFEST.as("prev").NEXT_AGENCY_ID_LIST_VALUE_MANIFEST_ID, AGENCY_ID_LIST_VALUE_MANIFEST.NEXT_AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                .where(AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(releaseId))
                .execute();
        dslContext.deleteFrom(AGENCY_ID_LIST_VALUE_MANIFEST)
                .where(AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(releaseId))
                .execute();
        dslContext.update(AGENCY_ID_LIST_VALUE)
                .set(AGENCY_ID_LIST_VALUE.OWNER_USER_ID, AGENCY_ID_LIST_VALUE.LAST_UPDATED_BY)
                .where(and(
                        AGENCY_ID_LIST_VALUE.OWNER_USER_ID.eq(appUserId),
                        AGENCY_ID_LIST_VALUE.LAST_UPDATED_BY.ne(appUserId)
                ))
                .execute();

        dslContext.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

}
