package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Repository
public class CoreComponentRepositoryForRelease {
    
    @Autowired
    private DSLContext dslContext;

    public List<AgencyIdListRecord> findAllAgencyIdList(ULong releaseId) {
        return dslContext.select(AGENCY_ID_LIST.fields())
                .from(AGENCY_ID_LIST)
                .join(AGENCY_ID_LIST_MANIFEST)
                .on(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID))
                .where(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchInto(AgencyIdListRecord.class);
    }

    public List<AgencyIdListManifestRecord> findAllAgencyIdListManifest(ULong releaseId) {
        return dslContext.select(AGENCY_ID_LIST_MANIFEST.fields())
                .from(AGENCY_ID_LIST_MANIFEST)
                .where(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchInto(AgencyIdListManifestRecord.class);
    }

    public List<AgencyIdListValueRecord> findAllAgencyIdListValue(ULong releaseId) {
        return dslContext.select(AGENCY_ID_LIST_VALUE.fields())
                .from(AGENCY_ID_LIST_VALUE)
                .join(AGENCY_ID_LIST_VALUE_MANIFEST)
                .on(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID))
                .where(AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchInto(AgencyIdListValueRecord.class);
    }

    public List<AgencyIdListValueManifestRecord> findAllAgencyIdListValueManifest(ULong releaseId) {
        return dslContext.select(AGENCY_ID_LIST_VALUE_MANIFEST.fields())
                .from(AGENCY_ID_LIST_VALUE_MANIFEST)
                .where(AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchInto(AgencyIdListValueManifestRecord.class);
    }

    public List<CodeListRecord> findAllCodeList(ULong releaseId) {
        return dslContext.select(CODE_LIST.fields())
                .from(CODE_LIST)
                .join(CODE_LIST_MANIFEST)
                .on(CODE_LIST.CODE_LIST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_ID))
                .where(CODE_LIST_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchInto(CodeListRecord.class);
    }

    public List<CodeListManifestRecord> findAllCodeListManifest(ULong releaseId) {
        return dslContext.select(CODE_LIST_MANIFEST.fields())
                .from(CODE_LIST_MANIFEST)
                .where(CODE_LIST_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchInto(CodeListManifestRecord.class);
    }

    public List<CodeListValueRecord> findAllCodeListValue(ULong releaseId) {
        return dslContext.select(CODE_LIST_VALUE.fields())
                .from(CODE_LIST_VALUE)
                .join(CODE_LIST_VALUE_MANIFEST)
                .on(CODE_LIST_VALUE.CODE_LIST_VALUE_ID.eq(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_ID))
                .where(CODE_LIST_VALUE_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchInto(CodeListValueRecord.class);
    }

    public List<CodeListValueManifestRecord> findAllCodeListValueManifest(ULong releaseId) {
        return dslContext.select(CODE_LIST_VALUE_MANIFEST.fields())
                .from(CODE_LIST_VALUE_MANIFEST)
                .where(CODE_LIST_VALUE_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchInto(CodeListValueManifestRecord.class);
    }

    public List<DtRecord> findAllDt(ULong releaseId) {
        return dslContext.select(DT.fields())
                .from(DT)
                .join(DT_MANIFEST)
                .on(DT.DT_ID.eq(DT_MANIFEST.DT_ID))
                .where(DT_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchInto(DtRecord.class);
    }

    public List<DtManifestRecord> findAllDtManifest(ULong releaseId) {
        return dslContext.select(DT_MANIFEST.fields())
                .from(DT_MANIFEST)
                .where(DT_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchInto(DtManifestRecord.class);
    }

    public List<DtScRecord> findAllDtSc(ULong releaseId) {
        return dslContext.select(DT_SC.fields())
                .from(DT_SC)
                .join(DT_SC_MANIFEST)
                .on(DT_SC.DT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID))
                .where(DT_SC_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchInto(DtScRecord.class);
    }

    public List<DtScManifestRecord> findAllDtScManifest(ULong releaseId) {
        return dslContext.select(DT_SC_MANIFEST.fields())
                .from(DT_SC_MANIFEST)
                .where(DT_SC_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchInto(DtScManifestRecord.class);
    }

    public List<BdtPriRestriRecord> findAllBdtPriRestri(ULong releaseId) {
        return dslContext.select(BDT_PRI_RESTRI.fields())
                .from(BDT_PRI_RESTRI)
                .join(DT_MANIFEST).on(BDT_PRI_RESTRI.BDT_MANIFEST_ID.eq(DT_MANIFEST.DT_MANIFEST_ID))
                .where(DT_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchInto(BdtPriRestriRecord.class);
    }

    public List<CdtAwdPriXpsTypeMapRecord> findAllCdtAwdPriXpsTypeMap(ULong releaseId) {
        return dslContext.select(CDT_AWD_PRI_XPS_TYPE_MAP.fields())
                .from(CDT_AWD_PRI_XPS_TYPE_MAP)
                .join(CDT_AWD_PRI).on(CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_ID.eq(CDT_AWD_PRI.CDT_AWD_PRI_ID))
                .join(DT).on(CDT_AWD_PRI.CDT_ID.eq(DT.DT_ID))
                .join(DT_MANIFEST).on(DT.DT_ID.eq(DT_MANIFEST.DT_ID))
                .where(DT_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchInto(CdtAwdPriXpsTypeMapRecord.class);
    }

    public List<BdtScPriRestriRecord> findAllBdtScPriRestri(ULong releaseId) {
        return dslContext.select(BDT_SC_PRI_RESTRI.fields())
                .from(BDT_SC_PRI_RESTRI)
                .join(DT_SC_MANIFEST).on(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID.eq(DT_SC_MANIFEST.DT_SC_MANIFEST_ID))
                .where(DT_SC_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchInto(BdtScPriRestriRecord.class);
    }

    public List<CdtScAwdPriXpsTypeMapRecord> findAllCdtScAwdPriXpsTypeMap(ULong releaseId) {
        return dslContext.select(CDT_SC_AWD_PRI_XPS_TYPE_MAP.fields())
                .from(CDT_SC_AWD_PRI_XPS_TYPE_MAP)
                .join(CDT_SC_AWD_PRI).on(CDT_SC_AWD_PRI_XPS_TYPE_MAP.CDT_SC_AWD_PRI_ID.eq(CDT_SC_AWD_PRI.CDT_SC_AWD_PRI_ID))
                .join(DT_SC).on(CDT_SC_AWD_PRI.CDT_SC_ID.eq(DT_SC.DT_SC_ID))
                .join(DT_SC_MANIFEST).on(DT_SC.DT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID))
                .where(DT_SC_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchInto(CdtScAwdPriXpsTypeMapRecord.class);
    }

    public List<CdtScAwdPriRecord> findAllCdtScAwdPri(ULong releaseId) {
        return dslContext.select(CDT_SC_AWD_PRI.fields())
                .from(CDT_SC_AWD_PRI)
                .join(DT_SC).on(CDT_SC_AWD_PRI.CDT_SC_ID.eq(DT_SC.DT_SC_ID))
                .join(DT_SC_MANIFEST).on(DT_SC.DT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID))
                .where(DT_SC_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchInto(CdtScAwdPriRecord.class);
    }

    public List<CdtAwdPriRecord> findAllCdtAwdPri(ULong releaseId) {
        return dslContext.select(CDT_AWD_PRI.fields())
                .from(CDT_AWD_PRI)
                .join(DT).on(CDT_AWD_PRI.CDT_ID.eq(DT.DT_ID))
                .join(DT_MANIFEST).on(DT.DT_ID.eq(DT_MANIFEST.DT_ID))
                .where(DT_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchInto(CdtAwdPriRecord.class);
    }

    public List<CdtPriRecord> findAllCdtPri() {
        return dslContext.selectFrom(CDT_PRI)
                .fetchInto(CdtPriRecord.class);
    }

    public List<XbtRecord> findAllXbt(ULong releaseId) {
        return dslContext.select(XBT.fields())
                .from(XBT)
                .join(XBT_MANIFEST)
                .on(XBT.XBT_ID.eq(XBT_MANIFEST.XBT_ID))
                .where(XBT_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchInto(XbtRecord.class);
    }

    public List<XbtManifestRecord> findAllXbtManifest(ULong releaseId) {
        return dslContext.select(XBT_MANIFEST.fields())
                .from(XBT_MANIFEST)
                .where(XBT_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchInto(XbtManifestRecord.class);
    }
    
    public List<AccRecord> findAllAcc(ULong releaseId) {
        return dslContext.select(ACC.fields())
                .from(ACC)
                .join(ACC_MANIFEST)
                .on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                .where(ACC_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchInto(AccRecord.class);
    }

    public List<AccManifestRecord> findAllAccManifest(ULong releaseId) {
        return dslContext.select(ACC_MANIFEST.fields())
                .from(ACC_MANIFEST)
                .where(ACC_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchInto(AccManifestRecord.class);
    }

    public List<AsccRecord> findAllAscc(ULong releaseId) {
        return dslContext.select(ASCC.fields())
                .from(ASCC)
                .join(ASCC_MANIFEST)
                .on(ASCC.ASCC_ID.eq(ASCC_MANIFEST.ASCC_ID))
                .where(ASCC_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchInto(AsccRecord.class);
    }

    public List<AsccManifestRecord> findAllAsccManifest(ULong releaseId) {
        return dslContext.select(ASCC_MANIFEST.fields())
                .from(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchInto(AsccManifestRecord.class);
    }

    public List<AsccpRecord> findAllAsccp(ULong releaseId) {
        return dslContext.select(ASCCP.fields())
                .from(ASCCP)
                .join(ASCCP_MANIFEST)
                .on(ASCCP.ASCCP_ID.eq(ASCCP_MANIFEST.ASCCP_ID))
                .where(ASCCP_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchInto(AsccpRecord.class);
    }

    public List<AsccpManifestRecord> findAllAsccpManifest(ULong releaseId) {
        return dslContext.select(ASCCP_MANIFEST.fields())
                .from(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchInto(AsccpManifestRecord.class);
    }

    public List<BccRecord> findAllBcc(ULong releaseId) {
        return dslContext.select(BCC.fields())
                .from(BCC)
                .join(BCC_MANIFEST)
                .on(BCC.BCC_ID.eq(BCC_MANIFEST.BCC_ID))
                .where(BCC_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchInto(BccRecord.class);
    }

    public List<BccManifestRecord> findAllBccManifest(ULong releaseId) {
        return dslContext.select(BCC_MANIFEST.fields())
                .from(BCC_MANIFEST)
                .where(BCC_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchInto(BccManifestRecord.class);
    }

    public List<BccpRecord> findAllBccp(ULong releaseId) {
        return dslContext.select(BCCP.fields())
                .from(BCCP)
                .join(BCCP_MANIFEST)
                .on(BCCP.BCCP_ID.eq(BCCP_MANIFEST.BCCP_ID))
                .where(BCCP_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchInto(BccpRecord.class);
    }

    public List<BccpManifestRecord> findAllBccpManifest(ULong releaseId) {
        return dslContext.select(BCCP_MANIFEST.fields())
                .from(BCCP_MANIFEST)
                .where(BCCP_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchInto(BccpManifestRecord.class);
    }

    public List<BlobContentRecord> findAllBlobContent(ULong releaseId) {
        return dslContext.select(BLOB_CONTENT.fields())
                .from(BLOB_CONTENT)
                .join(BLOB_CONTENT_MANIFEST)
                .on(BLOB_CONTENT.BLOB_CONTENT_ID.eq(BLOB_CONTENT_MANIFEST.BLOB_CONTENT_ID))
                .where(BLOB_CONTENT_MANIFEST.RELEASE_ID.eq(releaseId))
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
