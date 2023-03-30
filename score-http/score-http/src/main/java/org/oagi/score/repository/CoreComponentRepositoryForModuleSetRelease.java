package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.export.model.ModuleCCID;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Repository
public class CoreComponentRepositoryForModuleSetRelease {

    @Autowired
    private DSLContext dslContext;

    public List<AgencyIdListRecord> findAllAgencyIdList(ULong moduleSetReleaseId) {
        return dslContext.select(AGENCY_ID_LIST.fields())
                .from(AGENCY_ID_LIST)
                .join(AGENCY_ID_LIST_MANIFEST)
                .on(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID))
                .join(MODULE_SET_RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(AGENCY_ID_LIST_MANIFEST.RELEASE_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(AgencyIdListRecord.class);
    }

    public List<AgencyIdListManifestRecord> findAllAgencyIdListManifest(ULong moduleSetReleaseId) {
        return dslContext.select(AGENCY_ID_LIST_MANIFEST.fields())
                .from(AGENCY_ID_LIST_MANIFEST)
                .join(MODULE_SET_RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(AGENCY_ID_LIST_MANIFEST.RELEASE_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(AgencyIdListManifestRecord.class);
    }

    public List<AgencyIdListValueRecord> findAllAgencyIdListValue(ULong moduleSetReleaseId) {
        return dslContext.select(AGENCY_ID_LIST_VALUE.fields())
                .from(AGENCY_ID_LIST_VALUE)
                .join(AGENCY_ID_LIST_VALUE_MANIFEST)
                .on(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID))
                .join(MODULE_SET_RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(AgencyIdListValueRecord.class);
    }

    public List<AgencyIdListValueManifestRecord> findAllAgencyIdListValueManifest(ULong moduleSetReleaseId) {
        return dslContext.select(AGENCY_ID_LIST_VALUE_MANIFEST.fields())
                .from(AGENCY_ID_LIST_VALUE_MANIFEST)
                .join(MODULE_SET_RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(AgencyIdListValueManifestRecord.class);
    }

    public List<CodeListRecord> findAllCodeList(ULong moduleSetReleaseId) {
        return dslContext.select(CODE_LIST.fields())
                .from(CODE_LIST)
                .join(CODE_LIST_MANIFEST)
                .on(CODE_LIST.CODE_LIST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_ID))
                .join(MODULE_SET_RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(CODE_LIST_MANIFEST.RELEASE_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(CodeListRecord.class);
    }

    public List<CodeListManifestRecord> findAllCodeListManifest(ULong moduleSetReleaseId) {
        return dslContext.select(CODE_LIST_MANIFEST.fields())
                .from(CODE_LIST_MANIFEST)
                .join(MODULE_SET_RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(CODE_LIST_MANIFEST.RELEASE_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(CodeListManifestRecord.class);
    }

    public List<CodeListValueRecord> findAllCodeListValue(ULong moduleSetReleaseId) {
        return dslContext.select(CODE_LIST_VALUE.fields())
                .from(CODE_LIST_VALUE)
                .join(CODE_LIST_VALUE_MANIFEST)
                .on(CODE_LIST_VALUE.CODE_LIST_VALUE_ID.eq(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_ID))
                .join(MODULE_SET_RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(CODE_LIST_VALUE_MANIFEST.RELEASE_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(CodeListValueRecord.class);
    }

    public List<CodeListValueManifestRecord> findAllCodeListValueManifest(ULong moduleSetReleaseId) {
        return dslContext.select(CODE_LIST_VALUE_MANIFEST.fields())
                .from(CODE_LIST_VALUE_MANIFEST)
                .join(MODULE_SET_RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(CODE_LIST_VALUE_MANIFEST.RELEASE_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(CodeListValueManifestRecord.class);
    }

    public List<DtRecord> findAllDt(ULong moduleSetReleaseId) {
        return dslContext.select(DT.fields())
                .from(DT)
                .join(DT_MANIFEST)
                .on(DT.DT_ID.eq(DT_MANIFEST.DT_ID))
                .join(MODULE_SET_RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(DT_MANIFEST.RELEASE_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(DtRecord.class);
    }

    public List<DtManifestRecord> findAllDtManifest(ULong moduleSetReleaseId) {
        return dslContext.select(DT_MANIFEST.fields())
                .from(DT_MANIFEST)
                .join(MODULE_SET_RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(DT_MANIFEST.RELEASE_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(DtManifestRecord.class);
    }

    public List<DtScRecord> findAllDtSc(ULong moduleSetReleaseId) {
        return dslContext.select(DT_SC.fields())
                .from(DT_SC)
                .join(DT_SC_MANIFEST)
                .on(DT_SC.DT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID))
                .join(MODULE_SET_RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(DT_SC_MANIFEST.RELEASE_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(DtScRecord.class);
    }

    public List<DtScManifestRecord> findAllDtScManifest(ULong moduleSetReleaseId) {
        return dslContext.select(DT_SC_MANIFEST.fields())
                .from(DT_SC_MANIFEST)
                .join(MODULE_SET_RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(DT_SC_MANIFEST.RELEASE_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(DtScManifestRecord.class);
    }

    public List<BdtPriRestriRecord> findAllBdtPriRestri(ULong moduleSetReleaseId) {
        return dslContext.select(BDT_PRI_RESTRI.fields())
                .from(BDT_PRI_RESTRI)
                .join(DT_MANIFEST).on(BDT_PRI_RESTRI.BDT_MANIFEST_ID.eq(DT_MANIFEST.DT_MANIFEST_ID))
                .join(MODULE_SET_RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(DT_MANIFEST.RELEASE_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(BdtPriRestriRecord.class);
    }

    public List<CdtAwdPriXpsTypeMapRecord> findAllCdtAwdPriXpsTypeMap(ULong moduleSetReleaseId) {
        return dslContext.select(CDT_AWD_PRI_XPS_TYPE_MAP.fields())
                .from(CDT_AWD_PRI_XPS_TYPE_MAP)
                .join(CDT_AWD_PRI).on(CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_ID.eq(CDT_AWD_PRI.CDT_AWD_PRI_ID))
                .join(DT).on(CDT_AWD_PRI.CDT_ID.eq(DT.DT_ID))
                .join(DT_MANIFEST).on(DT.DT_ID.eq(DT_MANIFEST.DT_ID))
                .join(MODULE_SET_RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(DT_MANIFEST.RELEASE_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(CdtAwdPriXpsTypeMapRecord.class);
    }

    public List<BdtScPriRestriRecord> findAllBdtScPriRestri(ULong moduleSetReleaseId) {
        return dslContext.select(BDT_SC_PRI_RESTRI.fields())
                .from(BDT_SC_PRI_RESTRI)
                .join(DT_SC_MANIFEST).on(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID.eq(DT_SC_MANIFEST.DT_SC_MANIFEST_ID))
                .join(MODULE_SET_RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(DT_SC_MANIFEST.RELEASE_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(BdtScPriRestriRecord.class);
    }

    public List<CdtScAwdPriXpsTypeMapRecord> findAllCdtScAwdPriXpsTypeMap(ULong moduleSetReleaseId) {
        return dslContext.select(CDT_SC_AWD_PRI_XPS_TYPE_MAP.fields())
                .from(CDT_SC_AWD_PRI_XPS_TYPE_MAP)
                .join(CDT_SC_AWD_PRI).on(CDT_SC_AWD_PRI_XPS_TYPE_MAP.CDT_SC_AWD_PRI_ID.eq(CDT_SC_AWD_PRI.CDT_SC_AWD_PRI_ID))
                .join(DT_SC).on(CDT_SC_AWD_PRI.CDT_SC_ID.eq(DT_SC.DT_SC_ID))
                .join(DT_SC_MANIFEST).on(DT_SC.DT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID))
                .join(MODULE_SET_RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(DT_SC_MANIFEST.RELEASE_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(CdtScAwdPriXpsTypeMapRecord.class);
    }

    public List<CdtScAwdPriRecord> findAllCdtScAwdPri(ULong moduleSetReleaseId) {
        return dslContext.select(CDT_SC_AWD_PRI.fields())
                .from(CDT_SC_AWD_PRI)
                .join(DT_SC).on(CDT_SC_AWD_PRI.CDT_SC_ID.eq(DT_SC.DT_SC_ID))
                .join(DT_SC_MANIFEST).on(DT_SC.DT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID))
                .join(MODULE_SET_RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(DT_SC_MANIFEST.RELEASE_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(CdtScAwdPriRecord.class);
    }

    public List<CdtAwdPriRecord> findAllCdtAwdPri(ULong moduleSetReleaseId) {
        return dslContext.select(CDT_AWD_PRI.fields())
                .from(CDT_AWD_PRI)
                .join(DT).on(CDT_AWD_PRI.CDT_ID.eq(DT.DT_ID))
                .join(DT_MANIFEST).on(DT.DT_ID.eq(DT_MANIFEST.DT_ID))
                .join(MODULE_SET_RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(DT_MANIFEST.RELEASE_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(CdtAwdPriRecord.class);
    }

    public List<CdtPriRecord> findAllCdtPri() {
        return dslContext.selectFrom(CDT_PRI)
                .fetchInto(CdtPriRecord.class);
    }

    public List<XbtRecord> findAllXbt(ULong moduleSetReleaseId) {
        return dslContext.select(XBT.fields())
                .from(XBT)
                .join(XBT_MANIFEST)
                .on(XBT.XBT_ID.eq(XBT_MANIFEST.XBT_ID))
                .join(MODULE_SET_RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(XBT_MANIFEST.RELEASE_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(XbtRecord.class);
    }

    public List<XbtManifestRecord> findAllXbtManifest(ULong moduleSetReleaseId) {
        return dslContext.select(XBT_MANIFEST.fields())
                .from(XBT_MANIFEST)
                .join(MODULE_SET_RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(XBT_MANIFEST.RELEASE_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(XbtManifestRecord.class);
    }
    
    public List<AccRecord> findAllAcc(ULong moduleSetReleaseId) {
        return dslContext.select(ACC.fields())
                .from(ACC)
                .join(ACC_MANIFEST)
                .on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                .join(MODULE_SET_RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(ACC_MANIFEST.RELEASE_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(AccRecord.class);
    }

    public List<AccManifestRecord> findAllAccManifest(ULong moduleSetReleaseId) {
        return dslContext.select(ACC_MANIFEST.fields())
                .from(ACC_MANIFEST)
                .join(MODULE_SET_RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(ACC_MANIFEST.RELEASE_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(AccManifestRecord.class);
    }

    public List<AsccRecord> findAllAscc(ULong moduleSetReleaseId) {
        return dslContext.select(ASCC.fields())
                .from(ASCC)
                .join(ASCC_MANIFEST)
                .on(ASCC.ASCC_ID.eq(ASCC_MANIFEST.ASCC_ID))
                .join(MODULE_SET_RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(ASCC_MANIFEST.RELEASE_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(AsccRecord.class);
    }

    public List<AsccManifestRecord> findAllAsccManifest(ULong moduleSetReleaseId) {
        return dslContext.select(ASCC_MANIFEST.fields())
                .from(ASCC_MANIFEST)
                .join(MODULE_SET_RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(ASCC_MANIFEST.RELEASE_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(AsccManifestRecord.class);
    }

    public List<AsccpRecord> findAllAsccp(ULong moduleSetReleaseId) {
        return dslContext.select(ASCCP.fields())
                .from(ASCCP)
                .join(ASCCP_MANIFEST)
                .on(ASCCP.ASCCP_ID.eq(ASCCP_MANIFEST.ASCCP_ID))
                .join(MODULE_SET_RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(ASCCP_MANIFEST.RELEASE_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(AsccpRecord.class);
    }

    public List<AsccpManifestRecord> findAllAsccpManifest(ULong moduleSetReleaseId) {
        return dslContext.select(ASCCP_MANIFEST.fields())
                .from(ASCCP_MANIFEST)
                .join(MODULE_SET_RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(ASCCP_MANIFEST.RELEASE_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(AsccpManifestRecord.class);
    }

    public List<BccRecord> findAllBcc(ULong moduleSetReleaseId) {
        return dslContext.select(BCC.fields())
                .from(BCC)
                .join(BCC_MANIFEST)
                .on(BCC.BCC_ID.eq(BCC_MANIFEST.BCC_ID))
                .join(MODULE_SET_RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(BCC_MANIFEST.RELEASE_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(BccRecord.class);
    }

    public List<BccManifestRecord> findAllBccManifest(ULong moduleSetReleaseId) {
        return dslContext.select(BCC_MANIFEST.fields())
                .from(BCC_MANIFEST)
                .join(MODULE_SET_RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(BCC_MANIFEST.RELEASE_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(BccManifestRecord.class);
    }

    public List<BccpRecord> findAllBccp(ULong moduleSetReleaseId) {
        return dslContext.select(BCCP.fields())
                .from(BCCP)
                .join(BCCP_MANIFEST)
                .on(BCCP.BCCP_ID.eq(BCCP_MANIFEST.BCCP_ID))
                .join(MODULE_SET_RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(BCCP_MANIFEST.RELEASE_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(BccpRecord.class);
    }

    public List<BccpManifestRecord> findAllBccpManifest(ULong moduleSetReleaseId) {
        return dslContext.select(BCCP_MANIFEST.fields())
                .from(BCCP_MANIFEST)
                .join(MODULE_SET_RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(BCCP_MANIFEST.RELEASE_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(BccpManifestRecord.class);
    }


    public List<ModuleCCID> findAllModuleAgencyIdListManifest(ULong moduleSetReleaseId) {
        return dslContext.select(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_ID,
                AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.as("manifestId"),
                AGENCY_ID_LIST.AGENCY_ID_LIST_ID.as("ccId"),
                MODULE.PATH.as("path"))
                .from(AGENCY_ID_LIST)
                .join(AGENCY_ID_LIST_MANIFEST).on(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID))
                .join(MODULE_AGENCY_ID_LIST_MANIFEST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(MODULE_AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID))
                .join(MODULE).on(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .where(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(ModuleCCID.class);
    }

    public List<ModuleCCID> findAllModuleCodeListManifest(ULong moduleSetReleaseId) {
        return dslContext.select(MODULE_CODE_LIST_MANIFEST.MODULE_ID,
                CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.as("manifestId"),
                CODE_LIST.CODE_LIST_ID.as("ccId"),
                MODULE.PATH.as("path"))
                .from(CODE_LIST)
                .join(CODE_LIST_MANIFEST).on(CODE_LIST.CODE_LIST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_ID))
                .join(MODULE_CODE_LIST_MANIFEST).on(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(MODULE_CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID))
                .join(MODULE).on(MODULE_CODE_LIST_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .where(MODULE_CODE_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(ModuleCCID.class);
    }

    public List<ModuleCCID> findAllModuleAccManifest(ULong moduleSetReleaseId) {
        return dslContext.select(MODULE_ACC_MANIFEST.MODULE_ID,
                ACC_MANIFEST.ACC_MANIFEST_ID.as("manifestId"),
                ACC.ACC_ID.as("ccId"),
                MODULE.PATH.as("path"))
                .from(ACC)
                .join(ACC_MANIFEST).on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                .join(MODULE_ACC_MANIFEST).on(ACC_MANIFEST.ACC_MANIFEST_ID.eq(MODULE_ACC_MANIFEST.ACC_MANIFEST_ID))
                .join(MODULE).on(MODULE_ACC_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .where(MODULE_ACC_MANIFEST.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(ModuleCCID.class);
    }

    public List<ModuleCCID> findAllModuleAsccpManifest(ULong moduleSetReleaseId) {
        return dslContext.select(MODULE_ASCCP_MANIFEST.MODULE_ID,
                ASCCP_MANIFEST.ASCCP_MANIFEST_ID.as("manifestId"),
                ASCCP.ASCCP_ID.as("ccId"),
                MODULE.PATH.as("path"))
                .from(ASCCP)
                .join(ASCCP_MANIFEST).on(ASCCP.ASCCP_ID.eq(ASCCP_MANIFEST.ASCCP_ID))
                .join(MODULE_ASCCP_MANIFEST).on(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(MODULE_ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .join(MODULE).on(MODULE_ASCCP_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .where(MODULE_ASCCP_MANIFEST.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(ModuleCCID.class);
    }

    public List<ModuleCCID> findAllModuleBccpManifest(ULong moduleSetReleaseId) {
        return dslContext.select(MODULE_BCCP_MANIFEST.MODULE_ID,
                BCCP_MANIFEST.BCCP_MANIFEST_ID.as("manifestId"),
                BCCP.BCCP_ID.as("ccId"),
                MODULE.PATH.as("path"))
                .from(BCCP)
                .join(BCCP_MANIFEST).on(BCCP.BCCP_ID.eq(BCCP_MANIFEST.BCCP_ID))
                .join(MODULE_BCCP_MANIFEST).on(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(MODULE_BCCP_MANIFEST.BCCP_MANIFEST_ID))
                .join(MODULE).on(MODULE_BCCP_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .where(MODULE_BCCP_MANIFEST.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(ModuleCCID.class);
    }

    public List<ModuleCCID> findAllModuleDtManifest(ULong moduleSetReleaseId) {
        return dslContext.select(MODULE_DT_MANIFEST.MODULE_ID,
                DT_MANIFEST.DT_MANIFEST_ID.as("manifestId"),
                DT.DT_ID.as("ccId"),
                MODULE.PATH)
                .from(DT)
                .join(DT_MANIFEST).on(DT.DT_ID.eq(DT_MANIFEST.DT_ID))
                .join(MODULE_DT_MANIFEST).on(DT_MANIFEST.DT_MANIFEST_ID.eq(MODULE_DT_MANIFEST.DT_MANIFEST_ID))
                .join(MODULE).on(MODULE_DT_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .where(MODULE_DT_MANIFEST.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(ModuleCCID.class);
    }

    public List<ModuleCCID> findAllModuleXbtManifest(ULong moduleSetReleaseId) {
        return dslContext.select(MODULE_XBT_MANIFEST.MODULE_ID,
                XBT_MANIFEST.XBT_MANIFEST_ID.as("manifestId"),
                XBT.XBT_ID.as("ccId"),
                MODULE.PATH)
                .from(XBT)
                .join(XBT_MANIFEST).on(XBT.XBT_ID.eq(XBT_MANIFEST.XBT_ID))
                .join(MODULE_XBT_MANIFEST).on(XBT_MANIFEST.XBT_MANIFEST_ID.eq(MODULE_XBT_MANIFEST.XBT_MANIFEST_ID))
                .join(MODULE).on(MODULE_XBT_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .where(MODULE_XBT_MANIFEST.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(ModuleCCID.class);
    }

    public List<ModuleCCID> findAllModuleBlobContentManifest(ULong moduleSetReleaseId) {
        return dslContext.select(MODULE_BLOB_CONTENT_MANIFEST.MODULE_ID,
                BLOB_CONTENT_MANIFEST.BLOB_CONTENT_MANIFEST_ID.as("manifestId"),
                BLOB_CONTENT.BLOB_CONTENT_ID.as("ccId"),
                MODULE.PATH)
                .from(BLOB_CONTENT)
                .join(BLOB_CONTENT_MANIFEST).on(BLOB_CONTENT.BLOB_CONTENT_ID.eq(BLOB_CONTENT_MANIFEST.BLOB_CONTENT_ID))
                .join(MODULE_BLOB_CONTENT_MANIFEST).on(BLOB_CONTENT_MANIFEST.BLOB_CONTENT_MANIFEST_ID.eq(MODULE_BLOB_CONTENT_MANIFEST.BLOB_CONTENT_MANIFEST_ID))
                .join(MODULE).on(MODULE_BLOB_CONTENT_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .where(MODULE_BLOB_CONTENT_MANIFEST.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(ModuleCCID.class);
    }

    public List<BlobContentRecord> findAllBlobContent(ULong moduleSetReleaseId) {
        return dslContext.select(BLOB_CONTENT.fields())
                .from(BLOB_CONTENT)
                .join(BLOB_CONTENT_MANIFEST)
                .on(BLOB_CONTENT.BLOB_CONTENT_ID.eq(BLOB_CONTENT_MANIFEST.BLOB_CONTENT_ID))
                .join(MODULE_SET_RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(BLOB_CONTENT_MANIFEST.RELEASE_ID))
                .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId))
                .fetchInto(BlobContentRecord.class);
    }

    public List<SeqKeyRecord> findAllSeqKeyRecord() {
        return dslContext.select(SEQ_KEY.fields())
                .from(SEQ_KEY)
                .fetchInto(SeqKeyRecord.class);
    }

    public List<ReleaseRecord> findAllRelease() {
        return dslContext.select(RELEASE.fields())
                .from(RELEASE)
                .fetchInto(ReleaseRecord.class);
    }

    public List<NamespaceRecord> findAllNamespace() {
        return dslContext.select(NAMESPACE.fields())
                .from(NAMESPACE)
                .fetchInto(NamespaceRecord.class);
    }
}
