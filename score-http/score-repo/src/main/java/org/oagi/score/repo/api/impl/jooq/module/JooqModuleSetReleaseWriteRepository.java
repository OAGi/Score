package org.oagi.score.repo.api.impl.jooq.module;

import org.jooq.DSLContext;
import org.jooq.InsertSetMoreStep;
import org.jooq.UpdateSetStep;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.corecomponent.model.CcType;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.ModuleSetReleaseRecord;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.module.ModuleSetReleaseWriteRepository;
import org.oagi.score.repo.api.module.model.*;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.inline;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;
import static org.oagi.score.repo.api.user.model.ScoreRole.END_USER;

public class JooqModuleSetReleaseWriteRepository
        extends JooqScoreRepository
        implements ModuleSetReleaseWriteRepository {

    public JooqModuleSetReleaseWriteRepository(DSLContext dslContext) {
        super(dslContext);
    }

    private String getDefaultModuleSetReleaseName(BigInteger moduleSetId) {
        String moduleSetReleaseName = dslContext().select(MODULE_SET.NAME)
                .from(MODULE_SET)
                .where(MODULE_SET.MODULE_SET_ID.eq(ULong.valueOf(moduleSetId)))
                .fetchOneInto(String.class);
        // Issue #1276
        return concatWithDuplicateElimination(moduleSetReleaseName, "Module Set Release");
    }

    private String concatWithDuplicateElimination(String str1, String str2) {
        String[] str2Tokens = str2.split(" ");
        String partialStr2 = "";
        for (int i = 0, len = str2Tokens.length; i < len; ++i) {
            partialStr2 += ((i == 0) ? "" : " ") + str2Tokens[i];
            if (str1.endsWith(partialStr2)) {
                return str1 + " " + String.join(" ", Arrays.copyOfRange(str2Tokens, i + 1, len));
            }
        }
        return str1 + " " + str2;
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public CreateModuleSetReleaseResponse createModuleSetRelease(CreateModuleSetReleaseRequest request) throws ScoreDataAccessException {
        ScoreUser requester = request.getRequester();
        ULong requesterUserId = ULong.valueOf(requester.getUserId());
        LocalDateTime timestamp = LocalDateTime.now();

        if (request.isDefault()) {
            dslContext().update(MODULE_SET_RELEASE)
                    .set(MODULE_SET_RELEASE.IS_DEFAULT, (byte) 0)
                    .where(MODULE_SET_RELEASE.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId())))
                    .execute();
        }

        String moduleSetReleaseName = request.getModuleSetReleaseName();
        if (!StringUtils.hasLength(moduleSetReleaseName)) {
            moduleSetReleaseName = getDefaultModuleSetReleaseName(request.getModuleSetId());
        }

        InsertSetMoreStep<ModuleSetReleaseRecord> insertSetMoreStep =
                dslContext().insertInto(MODULE_SET_RELEASE)
                        .set(MODULE_SET_RELEASE.RELEASE_ID, ULong.valueOf(request.getReleaseId()))
                        .set(MODULE_SET_RELEASE.MODULE_SET_ID, ULong.valueOf(request.getModuleSetId()))
                        .set(MODULE_SET_RELEASE.NAME, moduleSetReleaseName);

        String moduleSetReleaseDescription = request.getModuleSetReleaseDescription();
        if (StringUtils.hasLength(moduleSetReleaseDescription)) {
            insertSetMoreStep = insertSetMoreStep.set(MODULE_SET_RELEASE.DESCRIPTION, moduleSetReleaseDescription);
        }

        ModuleSetReleaseRecord moduleSetReleaseRecord = insertSetMoreStep
                .set(MODULE_SET_RELEASE.IS_DEFAULT, request.isDefault() ? (byte) 1 : 0)
                .set(MODULE_SET_RELEASE.CREATED_BY, requesterUserId)
                .set(MODULE_SET_RELEASE.LAST_UPDATED_BY, requesterUserId)
                .set(MODULE_SET_RELEASE.CREATION_TIMESTAMP, timestamp)
                .set(MODULE_SET_RELEASE.LAST_UPDATE_TIMESTAMP, timestamp)
                .returning()
                .fetchOne();

        ModuleSetRelease moduleSetRelease = new ModuleSetRelease();
        moduleSetRelease.setModuleSetReleaseId(moduleSetReleaseRecord.getModuleSetReleaseId().toBigInteger());
        moduleSetRelease.setReleaseId(moduleSetReleaseRecord.getReleaseId().toBigInteger());
        moduleSetRelease.setModuleSetId(moduleSetReleaseRecord.getModuleSetId().toBigInteger());
        moduleSetRelease.setModuleSetReleaseName(moduleSetReleaseRecord.getName());
        moduleSetRelease.setDefault(request.isDefault());
        moduleSetRelease.setCreatedBy(requester);
        moduleSetRelease.setCreationTimestamp(
                Date.from(moduleSetReleaseRecord.getCreationTimestamp().atZone(ZoneId.systemDefault()).toInstant()));
        moduleSetRelease.setLastUpdatedBy(requester);
        moduleSetRelease.setLastUpdateTimestamp(
                Date.from(moduleSetReleaseRecord.getLastUpdateTimestamp().atZone(ZoneId.systemDefault()).toInstant()));

        if (request.getBaseModuleSetReleaseId() != null) {
            copyModuleCcManifest(requesterUserId, timestamp, moduleSetReleaseRecord, ULong.valueOf(request.getBaseModuleSetReleaseId()));
        }

        return new CreateModuleSetReleaseResponse(moduleSetRelease);
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public UpdateModuleSetReleaseResponse updateModuleSetRelease(UpdateModuleSetReleaseRequest request) throws ScoreDataAccessException {
        ScoreUser requester = request.getRequester();
        ULong requesterUserId = ULong.valueOf(requester.getUserId());
        LocalDateTime timestamp = LocalDateTime.now();

        if (request.isDefault()) {
            dslContext().update(MODULE_SET_RELEASE)
                    .set(MODULE_SET_RELEASE.IS_DEFAULT, (byte) 0)
                    .where(MODULE_SET_RELEASE.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId())))
                    .execute();
        }

        String moduleSetReleaseName = request.getModuleSetReleaseName();
        if (!StringUtils.hasLength(moduleSetReleaseName)) {
            moduleSetReleaseName = getDefaultModuleSetReleaseName(request.getModuleSetId());
        }
        String moduleSetReleaseDescription = request.getModuleSetReleaseDescription();

        UpdateSetStep updateSetStep = dslContext().update(MODULE_SET_RELEASE)
                .set(MODULE_SET_RELEASE.RELEASE_ID, ULong.valueOf(request.getReleaseId()))
                .set(MODULE_SET_RELEASE.MODULE_SET_ID, ULong.valueOf(request.getModuleSetId()))
                .set(MODULE_SET_RELEASE.NAME, moduleSetReleaseName);

        if (StringUtils.hasLength(moduleSetReleaseDescription)) {
            updateSetStep = updateSetStep.set(MODULE_SET_RELEASE.DESCRIPTION, moduleSetReleaseDescription);
        } else {
            updateSetStep = updateSetStep.setNull(MODULE_SET_RELEASE.DESCRIPTION);
        }

        updateSetStep.set(MODULE_SET_RELEASE.IS_DEFAULT, request.isDefault() ? (byte) 1 : 0)
                .set(MODULE_SET_RELEASE.LAST_UPDATED_BY, requesterUserId)
                .set(MODULE_SET_RELEASE.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId())))
                .execute();

        ModuleSetReleaseRecord moduleSetReleaseRecord = dslContext().selectFrom(MODULE_SET_RELEASE)
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId())))
                .fetchOne();

        ModuleSetRelease moduleSetRelease = new ModuleSetRelease();
        moduleSetRelease.setModuleSetReleaseId(moduleSetReleaseRecord.getModuleSetReleaseId().toBigInteger());
        moduleSetRelease.setReleaseId(moduleSetReleaseRecord.getReleaseId().toBigInteger());
        moduleSetRelease.setModuleSetId(moduleSetReleaseRecord.getModuleSetId().toBigInteger());
        moduleSetRelease.setModuleSetReleaseName(moduleSetReleaseRecord.getName());
        moduleSetRelease.setModuleSetReleaseDescription(moduleSetReleaseRecord.getDescription());
        moduleSetRelease.setDefault(request.isDefault());

        moduleSetRelease.setCreationTimestamp(
                Date.from(moduleSetReleaseRecord.getCreationTimestamp().atZone(ZoneId.systemDefault()).toInstant()));
        moduleSetRelease.setLastUpdatedBy(requester);
        moduleSetRelease.setLastUpdateTimestamp(
                Date.from(moduleSetReleaseRecord.getLastUpdateTimestamp().atZone(ZoneId.systemDefault()).toInstant()));

        return new UpdateModuleSetReleaseResponse(moduleSetRelease);
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public DeleteModuleSetReleaseResponse deleteModuleSetRelease(DeleteModuleSetReleaseRequest request) throws ScoreDataAccessException {
        dslContext().deleteFrom(MODULE_ACC_MANIFEST)
                .where(MODULE_ACC_MANIFEST.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId())))
                .execute();
        dslContext().deleteFrom(MODULE_ASCCP_MANIFEST)
                .where(MODULE_ASCCP_MANIFEST.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId())))
                .execute();
        dslContext().deleteFrom(MODULE_BCCP_MANIFEST)
                .where(MODULE_BCCP_MANIFEST.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId())))
                .execute();
        dslContext().deleteFrom(MODULE_DT_MANIFEST)
                .where(MODULE_DT_MANIFEST.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId())))
                .execute();
        dslContext().deleteFrom(MODULE_CODE_LIST_MANIFEST)
                .where(MODULE_CODE_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId())))
                .execute();
        dslContext().deleteFrom(MODULE_AGENCY_ID_LIST_MANIFEST)
                .where(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId())))
                .execute();
        dslContext().deleteFrom(MODULE_XBT_MANIFEST)
                .where(MODULE_XBT_MANIFEST.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId())))
                .execute();
        dslContext().deleteFrom(MODULE_BLOB_CONTENT_MANIFEST)
                .where(MODULE_BLOB_CONTENT_MANIFEST.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId())))
                .execute();
        dslContext().deleteFrom(MODULE_SET_RELEASE)
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId())))
                .execute();

        return new DeleteModuleSetReleaseResponse();
    }

    private void copyModuleCcManifest(ULong requesterUserId, LocalDateTime timestamp,
                                     ModuleSetReleaseRecord moduleSetReleaseRecord, ULong baseModuleSetReleaseId) {

        ULong releaseId = moduleSetReleaseRecord.getReleaseId();

        Map<String, ULong> targetModulePathIdMap = dslContext().select(MODULE.MODULE_ID, MODULE.PATH)
                .from(MODULE)
                .where(MODULE.MODULE_SET_ID.eq(moduleSetReleaseRecord.getModuleSetId()))
                .fetchStream().collect(Collectors.toMap(e -> e.get(MODULE.PATH), e -> e.get(MODULE.MODULE_ID)));

        // copy MODULE_ACC_MANIFEST
        dslContext().batch(
                dslContext().select(ACC_MANIFEST.as("acc_manifest_target").ACC_MANIFEST_ID,
                                MODULE.PATH)
                        .from(MODULE_ACC_MANIFEST)
                        .join(ACC_MANIFEST).on(MODULE_ACC_MANIFEST.ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                        .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                        .join(ACC.as("acc_target")).on(ACC.GUID.eq(ACC.as("acc_target").GUID))
                        .join(ACC_MANIFEST.as("acc_manifest_target")).on(and(
                                ACC.as("acc_target").ACC_ID.eq(ACC_MANIFEST.as("acc_manifest_target").ACC_ID),
                                ACC_MANIFEST.as("acc_manifest_target").RELEASE_ID.eq(releaseId)))
                        .join(MODULE).on(MODULE_ACC_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                        .where(MODULE_ACC_MANIFEST.MODULE_SET_RELEASE_ID.eq(baseModuleSetReleaseId))
                        .fetchStream().map(record -> dslContext().insertInto(MODULE_ACC_MANIFEST)
                                .set(MODULE_ACC_MANIFEST.MODULE_SET_RELEASE_ID, moduleSetReleaseRecord.getModuleSetReleaseId())
                                .set(MODULE_ACC_MANIFEST.ACC_MANIFEST_ID, record.get(ACC_MANIFEST.as("acc_manifest_target").ACC_MANIFEST_ID))
                                .set(MODULE_ACC_MANIFEST.MODULE_ID, targetModulePathIdMap.get(record.get(MODULE.PATH)))
                                .set(MODULE_ACC_MANIFEST.CREATED_BY, requesterUserId)
                                .set(MODULE_ACC_MANIFEST.CREATION_TIMESTAMP, timestamp)
                                .set(MODULE_ACC_MANIFEST.LAST_UPDATED_BY, requesterUserId)
                                .set(MODULE_ACC_MANIFEST.LAST_UPDATE_TIMESTAMP, timestamp)).collect(Collectors.toList())
        ).execute();

        // copy MODULE_ASCCP_MANIFEST
        dslContext().batch(
                dslContext().select(ASCCP_MANIFEST.as("asccp_manifest_target").ASCCP_MANIFEST_ID,
                                MODULE.PATH)
                        .from(MODULE_ASCCP_MANIFEST)
                        .join(ASCCP_MANIFEST).on(MODULE_ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                        .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                        .join(ASCCP.as("asccp_target")).on(ASCCP.GUID.eq(ASCCP.as("asccp_target").GUID))
                        .join(ASCCP_MANIFEST.as("asccp_manifest_target")).on(and(
                                ASCCP.as("asccp_target").ASCCP_ID.eq(ASCCP_MANIFEST.as("asccp_manifest_target").ASCCP_ID),
                                ASCCP_MANIFEST.as("asccp_manifest_target").RELEASE_ID.eq(releaseId)))
                        .join(MODULE).on(MODULE_ASCCP_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                        .where(MODULE_ASCCP_MANIFEST.MODULE_SET_RELEASE_ID.eq(baseModuleSetReleaseId))
                        .fetchStream().map(record -> dslContext().insertInto(MODULE_ASCCP_MANIFEST)
                                .set(MODULE_ASCCP_MANIFEST.MODULE_SET_RELEASE_ID, moduleSetReleaseRecord.getModuleSetReleaseId())
                                .set(MODULE_ASCCP_MANIFEST.ASCCP_MANIFEST_ID, record.get(ASCCP_MANIFEST.as("asccp_manifest_target").ASCCP_MANIFEST_ID))
                                .set(MODULE_ASCCP_MANIFEST.MODULE_ID, targetModulePathIdMap.get(record.get(MODULE.PATH)))
                                .set(MODULE_ASCCP_MANIFEST.CREATED_BY, requesterUserId)
                                .set(MODULE_ASCCP_MANIFEST.CREATION_TIMESTAMP, timestamp)
                                .set(MODULE_ASCCP_MANIFEST.LAST_UPDATED_BY, requesterUserId)
                                .set(MODULE_ASCCP_MANIFEST.LAST_UPDATE_TIMESTAMP, timestamp)).collect(Collectors.toList())
        ).execute();

        // copy MODULE_BCCP_MANIFEST
        dslContext().batch(
                dslContext().select(BCCP_MANIFEST.as("bccp_manifest_target").BCCP_MANIFEST_ID,
                                MODULE.PATH)
                        .from(MODULE_BCCP_MANIFEST)
                        .join(BCCP_MANIFEST).on(MODULE_BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.BCCP_MANIFEST_ID))
                        .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                        .join(BCCP.as("bccp_target")).on(BCCP.GUID.eq(BCCP.as("bccp_target").GUID))
                        .join(BCCP_MANIFEST.as("bccp_manifest_target")).on(and(
                                BCCP.as("bccp_target").BCCP_ID.eq(BCCP_MANIFEST.as("bccp_manifest_target").BCCP_ID),
                                BCCP_MANIFEST.as("bccp_manifest_target").RELEASE_ID.eq(releaseId)))
                        .join(MODULE).on(MODULE_BCCP_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                        .where(MODULE_BCCP_MANIFEST.MODULE_SET_RELEASE_ID.eq(baseModuleSetReleaseId))
                        .fetchStream().map(record -> dslContext().insertInto(MODULE_BCCP_MANIFEST)
                                .set(MODULE_BCCP_MANIFEST.MODULE_SET_RELEASE_ID, moduleSetReleaseRecord.getModuleSetReleaseId())
                                .set(MODULE_BCCP_MANIFEST.BCCP_MANIFEST_ID, record.get(BCCP_MANIFEST.as("bccp_manifest_target").BCCP_MANIFEST_ID))
                                .set(MODULE_BCCP_MANIFEST.MODULE_ID, targetModulePathIdMap.get(record.get(MODULE.PATH)))
                                .set(MODULE_BCCP_MANIFEST.CREATED_BY, requesterUserId)
                                .set(MODULE_BCCP_MANIFEST.CREATION_TIMESTAMP, timestamp)
                                .set(MODULE_BCCP_MANIFEST.LAST_UPDATED_BY, requesterUserId)
                                .set(MODULE_BCCP_MANIFEST.LAST_UPDATE_TIMESTAMP, timestamp)).collect(Collectors.toList())
        ).execute();

        // copy MODULE_CODE_LIST_MANIFEST
        dslContext().batch(
                dslContext().select(CODE_LIST_MANIFEST.as("code_list_manifest_target").CODE_LIST_MANIFEST_ID,
                                MODULE.PATH)
                        .from(MODULE_CODE_LIST_MANIFEST)
                        .join(CODE_LIST_MANIFEST).on(MODULE_CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID))
                        .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                        .join(CODE_LIST.as("code_list_target")).on(CODE_LIST.GUID.eq(CODE_LIST.as("code_list_target").GUID))
                        .join(CODE_LIST_MANIFEST.as("code_list_manifest_target")).on(and(
                                CODE_LIST.as("code_list_target").CODE_LIST_ID.eq(CODE_LIST_MANIFEST.as("code_list_manifest_target").CODE_LIST_ID),
                                CODE_LIST_MANIFEST.as("code_list_manifest_target").RELEASE_ID.eq(releaseId)))
                        .join(MODULE).on(MODULE_CODE_LIST_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                        .where(MODULE_CODE_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(baseModuleSetReleaseId))
                        .fetchStream().map(record -> dslContext().insertInto(MODULE_CODE_LIST_MANIFEST)
                                .set(MODULE_CODE_LIST_MANIFEST.MODULE_SET_RELEASE_ID, moduleSetReleaseRecord.getModuleSetReleaseId())
                                .set(MODULE_CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID, record.get(CODE_LIST_MANIFEST.as("code_list_manifest_target").CODE_LIST_MANIFEST_ID))
                                .set(MODULE_CODE_LIST_MANIFEST.MODULE_ID, targetModulePathIdMap.get(record.get(MODULE.PATH)))
                                .set(MODULE_CODE_LIST_MANIFEST.CREATED_BY, requesterUserId)
                                .set(MODULE_CODE_LIST_MANIFEST.CREATION_TIMESTAMP, timestamp)
                                .set(MODULE_CODE_LIST_MANIFEST.LAST_UPDATED_BY, requesterUserId)
                                .set(MODULE_CODE_LIST_MANIFEST.LAST_UPDATE_TIMESTAMP, timestamp)).collect(Collectors.toList())
        ).execute();

        // copy MODULE_AGENCY_ID_LIST_MANIFEST
        dslContext().batch(
                dslContext().select(AGENCY_ID_LIST_MANIFEST.as("agency_id_list_manifest_target").AGENCY_ID_LIST_MANIFEST_ID,
                                MODULE.PATH)
                        .from(MODULE_AGENCY_ID_LIST_MANIFEST)
                        .join(AGENCY_ID_LIST_MANIFEST).on(MODULE_AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID))
                        .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                        .join(AGENCY_ID_LIST.as("agency_id_list_target")).on(AGENCY_ID_LIST.GUID.eq(AGENCY_ID_LIST.as("agency_id_list_target").GUID))
                        .join(AGENCY_ID_LIST_MANIFEST.as("agency_id_list_manifest_target")).on(and(
                                AGENCY_ID_LIST.as("agency_id_list_target").AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST_MANIFEST.as("agency_id_list_manifest_target").AGENCY_ID_LIST_ID),
                                AGENCY_ID_LIST_MANIFEST.as("agency_id_list_manifest_target").RELEASE_ID.eq(releaseId)))
                        .join(MODULE).on(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                        .where(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(baseModuleSetReleaseId))
                        .fetchStream().map(record -> dslContext().insertInto(MODULE_AGENCY_ID_LIST_MANIFEST)
                                .set(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_SET_RELEASE_ID, moduleSetReleaseRecord.getModuleSetReleaseId())
                                .set(MODULE_AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID, record.get(AGENCY_ID_LIST_MANIFEST.as("agency_id_list_manifest_target").AGENCY_ID_LIST_MANIFEST_ID))
                                .set(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_ID, targetModulePathIdMap.get(record.get(MODULE.PATH)))
                                .set(MODULE_AGENCY_ID_LIST_MANIFEST.CREATED_BY, requesterUserId)
                                .set(MODULE_AGENCY_ID_LIST_MANIFEST.CREATION_TIMESTAMP, timestamp)
                                .set(MODULE_AGENCY_ID_LIST_MANIFEST.LAST_UPDATED_BY, requesterUserId)
                                .set(MODULE_AGENCY_ID_LIST_MANIFEST.LAST_UPDATE_TIMESTAMP, timestamp)).collect(Collectors.toList())
        ).execute();

        // copy MODULE_BLOB_CONTENT_MANIFEST
        dslContext().insertInto(MODULE_BLOB_CONTENT_MANIFEST,
                MODULE_BLOB_CONTENT_MANIFEST.MODULE_SET_RELEASE_ID,
                MODULE_BLOB_CONTENT_MANIFEST.BLOB_CONTENT_MANIFEST_ID,
                MODULE_BLOB_CONTENT_MANIFEST.MODULE_ID,
                MODULE_BLOB_CONTENT_MANIFEST.CREATED_BY,
                MODULE_BLOB_CONTENT_MANIFEST.CREATION_TIMESTAMP,
                MODULE_BLOB_CONTENT_MANIFEST.LAST_UPDATED_BY,
                MODULE_BLOB_CONTENT_MANIFEST.LAST_UPDATE_TIMESTAMP)
                .select(dslContext().select(
                        inline(moduleSetReleaseRecord.getModuleSetReleaseId()),
                        MODULE_BLOB_CONTENT_MANIFEST.BLOB_CONTENT_MANIFEST_ID,
                        MODULE_BLOB_CONTENT_MANIFEST.MODULE_ID,
                        inline(requesterUserId),
                        inline(timestamp),
                        inline(requesterUserId),
                        inline(timestamp))
                        .from(MODULE_BLOB_CONTENT_MANIFEST)
                        .join(BLOB_CONTENT_MANIFEST).on(MODULE_BLOB_CONTENT_MANIFEST.BLOB_CONTENT_MANIFEST_ID.eq(BLOB_CONTENT_MANIFEST.BLOB_CONTENT_MANIFEST_ID))
                        .where(MODULE_BLOB_CONTENT_MANIFEST.MODULE_SET_RELEASE_ID.eq(baseModuleSetReleaseId)))
                .execute();

        // copy MODULE_XBT_MANIFEST
        dslContext().batch(
                dslContext().select(XBT_MANIFEST.as("xbt_manifest_target").XBT_MANIFEST_ID,
                                MODULE.PATH)
                        .from(MODULE_XBT_MANIFEST)
                        .join(XBT_MANIFEST).on(MODULE_XBT_MANIFEST.XBT_MANIFEST_ID.eq(XBT_MANIFEST.XBT_MANIFEST_ID))
                        .join(XBT).on(XBT_MANIFEST.XBT_ID.eq(XBT.XBT_ID))
                        .join(XBT.as("xbt_target")).on(XBT.GUID.eq(XBT.as("xbt_target").GUID))
                        .join(XBT_MANIFEST.as("xbt_manifest_target")).on(and(
                                XBT.as("xbt_target").XBT_ID.eq(XBT_MANIFEST.as("xbt_manifest_target").XBT_ID),
                                XBT_MANIFEST.as("xbt_manifest_target").RELEASE_ID.eq(releaseId)))
                        .join(MODULE).on(MODULE_XBT_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                        .where(MODULE_XBT_MANIFEST.MODULE_SET_RELEASE_ID.eq(baseModuleSetReleaseId))
                        .fetchStream().map(record -> dslContext().insertInto(MODULE_XBT_MANIFEST)
                                .set(MODULE_XBT_MANIFEST.MODULE_SET_RELEASE_ID, moduleSetReleaseRecord.getModuleSetReleaseId())
                                .set(MODULE_XBT_MANIFEST.XBT_MANIFEST_ID, record.get(XBT_MANIFEST.as("xbt_manifest_target").XBT_MANIFEST_ID))
                                .set(MODULE_XBT_MANIFEST.MODULE_ID, targetModulePathIdMap.get(record.get(MODULE.PATH)))
                                .set(MODULE_XBT_MANIFEST.CREATED_BY, requesterUserId)
                                .set(MODULE_XBT_MANIFEST.CREATION_TIMESTAMP, timestamp)
                                .set(MODULE_XBT_MANIFEST.LAST_UPDATED_BY, requesterUserId)
                                .set(MODULE_XBT_MANIFEST.LAST_UPDATE_TIMESTAMP, timestamp)).collect(Collectors.toList())
        ).execute();

        // copy MODULE_DT_MANIFEST
        dslContext().batch(
                dslContext().select(DT_MANIFEST.as("dt_manifest_target").DT_MANIFEST_ID,
                                MODULE.PATH)
                        .from(MODULE_DT_MANIFEST)
                        .join(DT_MANIFEST).on(MODULE_DT_MANIFEST.DT_MANIFEST_ID.eq(DT_MANIFEST.DT_MANIFEST_ID))
                        .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                        .join(DT.as("dt_target")).on(DT.GUID.eq(DT.as("dt_target").GUID))
                        .join(DT_MANIFEST.as("dt_manifest_target")).on(and(
                                DT.as("dt_target").DT_ID.eq(DT_MANIFEST.as("dt_manifest_target").DT_ID),
                                DT_MANIFEST.as("dt_manifest_target").RELEASE_ID.eq(releaseId)))
                        .join(MODULE).on(MODULE_DT_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                        .where(MODULE_DT_MANIFEST.MODULE_SET_RELEASE_ID.eq(baseModuleSetReleaseId))
                        .fetchStream().map(record -> dslContext().insertInto(MODULE_DT_MANIFEST)
                                .set(MODULE_DT_MANIFEST.MODULE_SET_RELEASE_ID, moduleSetReleaseRecord.getModuleSetReleaseId())
                                .set(MODULE_DT_MANIFEST.DT_MANIFEST_ID, record.get(DT_MANIFEST.as("dt_manifest_target").DT_MANIFEST_ID))
                                .set(MODULE_DT_MANIFEST.MODULE_ID, targetModulePathIdMap.get(record.get(MODULE.PATH)))
                                .set(MODULE_DT_MANIFEST.CREATED_BY, requesterUserId)
                                .set(MODULE_DT_MANIFEST.CREATION_TIMESTAMP, timestamp)
                                .set(MODULE_DT_MANIFEST.LAST_UPDATED_BY, requesterUserId)
                                .set(MODULE_DT_MANIFEST.LAST_UPDATE_TIMESTAMP, timestamp)).collect(Collectors.toList())
        ).execute();
    }

    @Override
    public void createModuleManifest(CreateModuleManifestRequest request) throws ScoreDataAccessException {
        if (request.getType().equals(CcType.ACC)) {
            dslContext().insertInto(MODULE_ACC_MANIFEST)
                    .set(MODULE_ACC_MANIFEST.MODULE_SET_RELEASE_ID, ULong.valueOf(request.getModuleSetReleaseId()))
                    .set(MODULE_ACC_MANIFEST.ACC_MANIFEST_ID, ULong.valueOf(request.getManifestId()))
                    .set(MODULE_ACC_MANIFEST.MODULE_ID, ULong.valueOf(request.getModuleId()))
                    .set(MODULE_ACC_MANIFEST.CREATED_BY, ULong.valueOf(request.getRequester().getUserId()))
                    .set(MODULE_ACC_MANIFEST.CREATION_TIMESTAMP, request.getTimestamp())
                    .set(MODULE_ACC_MANIFEST.LAST_UPDATED_BY, ULong.valueOf(request.getRequester().getUserId()))
                    .set(MODULE_ACC_MANIFEST.LAST_UPDATE_TIMESTAMP, request.getTimestamp())
                    .execute();
        } else if (request.getType().equals(CcType.ASCCP)) {
            dslContext().insertInto(MODULE_ASCCP_MANIFEST)
                    .set(MODULE_ASCCP_MANIFEST.MODULE_SET_RELEASE_ID, ULong.valueOf(request.getModuleSetReleaseId()))
                    .set(MODULE_ASCCP_MANIFEST.ASCCP_MANIFEST_ID, ULong.valueOf(request.getManifestId()))
                    .set(MODULE_ASCCP_MANIFEST.MODULE_ID, ULong.valueOf(request.getModuleId()))
                    .set(MODULE_ASCCP_MANIFEST.CREATED_BY, ULong.valueOf(request.getRequester().getUserId()))
                    .set(MODULE_ASCCP_MANIFEST.CREATION_TIMESTAMP, request.getTimestamp())
                    .set(MODULE_ASCCP_MANIFEST.LAST_UPDATED_BY, ULong.valueOf(request.getRequester().getUserId()))
                    .set(MODULE_ASCCP_MANIFEST.LAST_UPDATE_TIMESTAMP, request.getTimestamp())
                    .execute();
        } else if (request.getType().equals(CcType.BCCP)) {
            dslContext().insertInto(MODULE_BCCP_MANIFEST)
                    .set(MODULE_BCCP_MANIFEST.MODULE_SET_RELEASE_ID, ULong.valueOf(request.getModuleSetReleaseId()))
                    .set(MODULE_BCCP_MANIFEST.BCCP_MANIFEST_ID, ULong.valueOf(request.getManifestId()))
                    .set(MODULE_BCCP_MANIFEST.MODULE_ID, ULong.valueOf(request.getModuleId()))
                    .set(MODULE_BCCP_MANIFEST.CREATED_BY, ULong.valueOf(request.getRequester().getUserId()))
                    .set(MODULE_BCCP_MANIFEST.CREATION_TIMESTAMP, request.getTimestamp())
                    .set(MODULE_BCCP_MANIFEST.LAST_UPDATED_BY, ULong.valueOf(request.getRequester().getUserId()))
                    .set(MODULE_BCCP_MANIFEST.LAST_UPDATE_TIMESTAMP, request.getTimestamp())
                    .execute();
        } else if (request.getType().equals(CcType.DT)) {
            dslContext().insertInto(MODULE_DT_MANIFEST)
                    .set(MODULE_DT_MANIFEST.MODULE_SET_RELEASE_ID, ULong.valueOf(request.getModuleSetReleaseId()))
                    .set(MODULE_DT_MANIFEST.DT_MANIFEST_ID, ULong.valueOf(request.getManifestId()))
                    .set(MODULE_DT_MANIFEST.MODULE_ID, ULong.valueOf(request.getModuleId()))
                    .set(MODULE_DT_MANIFEST.CREATED_BY, ULong.valueOf(request.getRequester().getUserId()))
                    .set(MODULE_DT_MANIFEST.CREATION_TIMESTAMP, request.getTimestamp())
                    .set(MODULE_DT_MANIFEST.LAST_UPDATED_BY, ULong.valueOf(request.getRequester().getUserId()))
                    .set(MODULE_DT_MANIFEST.LAST_UPDATE_TIMESTAMP, request.getTimestamp())
                    .execute();
        } else if (request.getType().equals(CcType.CODE_LIST)) {
            dslContext().insertInto(MODULE_CODE_LIST_MANIFEST)
                    .set(MODULE_CODE_LIST_MANIFEST.MODULE_SET_RELEASE_ID, ULong.valueOf(request.getModuleSetReleaseId()))
                    .set(MODULE_CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID, ULong.valueOf(request.getManifestId()))
                    .set(MODULE_CODE_LIST_MANIFEST.MODULE_ID, ULong.valueOf(request.getModuleId()))
                    .set(MODULE_CODE_LIST_MANIFEST.CREATED_BY, ULong.valueOf(request.getRequester().getUserId()))
                    .set(MODULE_CODE_LIST_MANIFEST.CREATION_TIMESTAMP, request.getTimestamp())
                    .set(MODULE_CODE_LIST_MANIFEST.LAST_UPDATED_BY, ULong.valueOf(request.getRequester().getUserId()))
                    .set(MODULE_CODE_LIST_MANIFEST.LAST_UPDATE_TIMESTAMP, request.getTimestamp())
                    .execute();
        } else if (request.getType().equals(CcType.AGENCY_ID_LIST)) {
            dslContext().insertInto(MODULE_AGENCY_ID_LIST_MANIFEST)
                    .set(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_SET_RELEASE_ID, ULong.valueOf(request.getModuleSetReleaseId()))
                    .set(MODULE_AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID, ULong.valueOf(request.getManifestId()))
                    .set(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_ID, ULong.valueOf(request.getModuleId()))
                    .set(MODULE_AGENCY_ID_LIST_MANIFEST.CREATED_BY, ULong.valueOf(request.getRequester().getUserId()))
                    .set(MODULE_AGENCY_ID_LIST_MANIFEST.CREATION_TIMESTAMP, request.getTimestamp())
                    .set(MODULE_AGENCY_ID_LIST_MANIFEST.LAST_UPDATED_BY, ULong.valueOf(request.getRequester().getUserId()))
                    .set(MODULE_AGENCY_ID_LIST_MANIFEST.LAST_UPDATE_TIMESTAMP, request.getTimestamp())
                    .execute();
        } else if (request.getType().equals(CcType.XBT)) {
            dslContext().insertInto(MODULE_XBT_MANIFEST)
                    .set(MODULE_XBT_MANIFEST.MODULE_SET_RELEASE_ID, ULong.valueOf(request.getModuleSetReleaseId()))
                    .set(MODULE_XBT_MANIFEST.XBT_MANIFEST_ID, ULong.valueOf(request.getManifestId()))
                    .set(MODULE_XBT_MANIFEST.MODULE_ID, ULong.valueOf(request.getModuleId()))
                    .set(MODULE_XBT_MANIFEST.CREATED_BY, ULong.valueOf(request.getRequester().getUserId()))
                    .set(MODULE_XBT_MANIFEST.CREATION_TIMESTAMP, request.getTimestamp())
                    .set(MODULE_XBT_MANIFEST.LAST_UPDATED_BY, ULong.valueOf(request.getRequester().getUserId()))
                    .set(MODULE_XBT_MANIFEST.LAST_UPDATE_TIMESTAMP, request.getTimestamp())
                    .execute();
        }
    }

    @Override
    public void deleteModuleManifest(DeleteModuleManifestRequest request) throws ScoreDataAccessException {
        if (request.getType().equals(CcType.ACC)) {
            dslContext().deleteFrom(MODULE_ACC_MANIFEST)
                    .where(and(MODULE_ACC_MANIFEST.ACC_MANIFEST_ID.eq(ULong.valueOf(request.getManifestId())),
                            MODULE_ACC_MANIFEST.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId())),
                            MODULE_ACC_MANIFEST.MODULE_ID.eq(ULong.valueOf(request.getModuleId()))))
                    .execute();
        } else if (request.getType().equals(CcType.ASCCP)) {
            dslContext().deleteFrom(MODULE_ASCCP_MANIFEST)
                    .where(and(MODULE_ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ULong.valueOf(request.getManifestId())),
                            MODULE_ASCCP_MANIFEST.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId())),
                            MODULE_ASCCP_MANIFEST.MODULE_ID.eq(ULong.valueOf(request.getModuleId()))))
                    .execute();
        } else if (request.getType().equals(CcType.BCCP)) {
            dslContext().deleteFrom(MODULE_BCCP_MANIFEST)
                    .where(and(MODULE_BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(ULong.valueOf(request.getManifestId())),
                            MODULE_BCCP_MANIFEST.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId())),
                            MODULE_BCCP_MANIFEST.MODULE_ID.eq(ULong.valueOf(request.getModuleId()))))
                    .execute();
        } else if (request.getType().equals(CcType.DT)) {
            dslContext().deleteFrom(MODULE_DT_MANIFEST)
                    .where(and(MODULE_DT_MANIFEST.DT_MANIFEST_ID.eq(ULong.valueOf(request.getManifestId())),
                            MODULE_DT_MANIFEST.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId())),
                            MODULE_DT_MANIFEST.MODULE_ID.eq(ULong.valueOf(request.getModuleId()))))
                    .execute();
        } else if (request.getType().equals(CcType.CODE_LIST)) {
            dslContext().deleteFrom(MODULE_CODE_LIST_MANIFEST)
                    .where(and(MODULE_CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(ULong.valueOf(request.getManifestId())),
                            MODULE_CODE_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId())),
                            MODULE_CODE_LIST_MANIFEST.MODULE_ID.eq(ULong.valueOf(request.getModuleId()))))
                    .execute();
        } else if (request.getType().equals(CcType.AGENCY_ID_LIST)) {
            dslContext().deleteFrom(MODULE_AGENCY_ID_LIST_MANIFEST)
                    .where(and(MODULE_AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(ULong.valueOf(request.getManifestId())),
                            MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId())),
                            MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_ID.eq(ULong.valueOf(request.getModuleId()))))
                    .execute();
        } else if (request.getType().equals(CcType.XBT)) {
            dslContext().deleteFrom(MODULE_XBT_MANIFEST)
                    .where(and(MODULE_XBT_MANIFEST.XBT_MANIFEST_ID.eq(ULong.valueOf(request.getManifestId())),
                            MODULE_XBT_MANIFEST.MODULE_SET_RELEASE_ID.eq(ULong.valueOf(request.getModuleSetReleaseId())),
                            MODULE_XBT_MANIFEST.MODULE_ID.eq(ULong.valueOf(request.getModuleId()))))
                    .execute();
        }
    }
}