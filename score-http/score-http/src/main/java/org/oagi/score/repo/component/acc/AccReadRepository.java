package org.oagi.score.repo.component.acc;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.cc_management.data.CcACCType;
import org.oagi.score.gateway.http.api.cc_management.data.CcList;
import org.oagi.score.gateway.http.api.cc_management.data.CcType;
import org.oagi.score.repo.api.impl.jooq.entity.tables.AppUser;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccManifestRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AccRecord;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.ModuleSetReleaseRecord;
import org.oagi.score.service.common.data.CcState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Repository
public class AccReadRepository {

    @Autowired
    private DSLContext dslContext;

    public AccRecord getAccByManifestId(BigInteger accManifestId) {
        return dslContext.select(ACC.fields())
                .from(ACC)
                .join(ACC_MANIFEST).on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(accManifestId)))
                .fetchOptionalInto(AccRecord.class).orElse(null);
    }

    public AccManifestRecord getAllExtensionAccManifest(BigInteger releaseId) {
        return dslContext.select(ACC_MANIFEST.fields())
                .from(ACC_MANIFEST)
                .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                .where(and(ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        ACC.TYPE.eq(CcACCType.AllExtension.name())))
                .fetchOptionalInto(AccManifestRecord.class).orElse(null);
    }

    public AccManifestRecord getAccManifest(BigInteger accManifestId) {
        return dslContext.selectFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(accManifestId)))
                .fetchOptionalInto(AccManifestRecord.class).orElse(null);
    }

    public List<CcList> getBaseAccList(BigInteger accManifestId, BigInteger releaseId) {

        ULong defaultModuleSetReleaseId = null;
        ModuleSetReleaseRecord defaultModuleSetRelease = dslContext.selectFrom(MODULE_SET_RELEASE)
                .where(and(MODULE_SET_RELEASE.IS_DEFAULT.eq((byte) 1), MODULE_SET_RELEASE.RELEASE_ID.eq(ULong.valueOf(releaseId))))
                .fetchOne();
        if (defaultModuleSetRelease != null) {
            defaultModuleSetReleaseId = defaultModuleSetRelease.getModuleSetReleaseId();
        }

        List<AccManifestRecord> accManifestRecordList = dslContext.selectFrom(ACC_MANIFEST).where(ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))).fetch();

        AccManifestRecord accManifestRecord = accManifestRecordList.stream().filter(e -> e.getAccManifestId().equals(ULong.valueOf(accManifestId))).findFirst().orElse(null);

        if (accManifestRecord == null) {
            throw new IllegalArgumentException("Can not found CoreComponent.");
        }

        List<ULong> accManifestIdList = new ArrayList<>();

        while(accManifestRecord.getBasedAccManifestId() != null) {
            ULong cur = accManifestRecord.getBasedAccManifestId();
            accManifestIdList.add(cur);
            accManifestRecord = accManifestRecordList.stream().filter(e -> e.getAccManifestId().equals(cur)).findFirst().orElse(null);
        }

        Collections.reverse(accManifestIdList);

        AppUser appUserOwner = APP_USER.as("owner");
        AppUser appUserUpdater = APP_USER.as("updater");


        return dslContext.select(ACC_MANIFEST.ACC_MANIFEST_ID,
                ACC.ACC_ID,
                ACC.GUID,
                ACC_MANIFEST.DEN,
                ACC.DEFINITION,
                ACC.DEFINITION_SOURCE,
                ACC.OBJECT_CLASS_TERM,
                ACC.OAGIS_COMPONENT_TYPE,
                ACC.STATE,
                ACC.IS_DEPRECATED,
                ACC.LAST_UPDATE_TIMESTAMP,
                MODULE.PATH,
                MODULE.NAME,
                appUserOwner.LOGIN_ID,
                appUserUpdater.LOGIN_ID,
                LOG.REVISION_NUM,
                LOG.REVISION_TRACKING_NUM,
                RELEASE.RELEASE_NUM).from(ACC_MANIFEST)
                .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                .join(LOG)
                .on(ACC_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .join(RELEASE)
                .on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(appUserOwner)
                .on(ACC.OWNER_USER_ID.eq(appUserOwner.APP_USER_ID))
                .join(appUserUpdater)
                .on(ACC.LAST_UPDATED_BY.eq(appUserUpdater.APP_USER_ID))
                .leftJoin(MODULE_ACC_MANIFEST)
                .on(and(ACC_MANIFEST.ACC_MANIFEST_ID.eq(MODULE_ACC_MANIFEST.ACC_MANIFEST_ID), MODULE_ACC_MANIFEST.MODULE_SET_RELEASE_ID.eq(defaultModuleSetReleaseId)))
                .leftJoin(MODULE)
                .on(MODULE_ACC_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .where(ACC_MANIFEST.ACC_MANIFEST_ID.in(accManifestIdList))
                .fetch().map(e -> {
                    CcList ccList = new CcList();
                    ccList.setType(CcType.ACC);
                    ccList.setManifestId(e.get(ACC_MANIFEST.ACC_MANIFEST_ID).toBigInteger());
                    ccList.setGuid(e.get(ACC.GUID));
                    ccList.setDen(e.get(ACC_MANIFEST.DEN));
                    ccList.setDefinition(e.get(ACC.DEFINITION));
                    ccList.setModule(e.get(MODULE.NAME) == null ? "" : e.get(MODULE.PATH) + "\\" + e.get(MODULE.NAME));
                    ccList.setName(e.get(ACC.OBJECT_CLASS_TERM));
                    ccList.setDefinitionSource(e.get(ACC.DEFINITION_SOURCE));
                    ccList.setOwner(e.get(appUserOwner.LOGIN_ID));
                    ccList.setState(CcState.valueOf(e.get(ACC.STATE)));
                    ccList.setRevision(e.get(LOG.REVISION_NUM).toString());
                    ccList.setReleaseNum(e.get(LOG.REVISION_NUM).toString());
                    ccList.setDeprecated(e.get(ACC.IS_DEPRECATED) == 1);
                    ccList.setLastUpdateUser(e.get(appUserUpdater.LOGIN_ID));
                    ccList.setLastUpdateTimestamp(Date.from(e.getValue("last_update_timestamp", LocalDateTime.class)
                            .atZone(ZoneId.systemDefault()).toInstant()));
                    ccList.setId(e.get(ACC.ACC_ID).toBigInteger());
                    return  ccList;
                });
    }
}
