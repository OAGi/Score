package org.oagi.score.gateway.http.api.module_management.repository.jooq;

import org.jooq.DSLContext;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.blob_content.BlobContentManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.export.model.ModuleCCID;
import org.oagi.score.gateway.http.api.module_management.model.ModuleId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetReleaseId;
import org.oagi.score.gateway.http.api.module_management.repository.CoreComponentRepositoryForModuleSetRelease;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.*;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

public class JooqCoreComponentRepositoryForModuleSetRelease extends JooqBaseRepository
        implements CoreComponentRepositoryForModuleSetRelease {

    public JooqCoreComponentRepositoryForModuleSetRelease(DSLContext dslContext, ScoreUser requester,
                                                          RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    private Collection<ReleaseId> releaseIdList(ModuleSetReleaseId moduleSetReleaseId) {
        ReleaseId releaseId = new ReleaseId(
                dslContext().select(MODULE_SET_RELEASE.RELEASE_ID)
                        .from(MODULE_SET_RELEASE)
                        .where(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)))
                        .fetchOneInto(BigInteger.class));

        var releaseQuery = repositoryFactory().releaseQueryRepository(requester());
        return releaseQuery.getIncludedReleaseSummaryList(releaseId).stream()
                .map(e -> e.releaseId()).collect(Collectors.toList());
    }

    public List<AgencyIdListRecord> findAllAgencyIdList(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().selectDistinct(AGENCY_ID_LIST.fields())
                .from(AGENCY_ID_LIST)
                .join(AGENCY_ID_LIST_MANIFEST)
                .on(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID))
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(AGENCY_ID_LIST_MANIFEST.RELEASE_ID))
                .where(RELEASE.RELEASE_ID.in(valueOf(releaseIdList(moduleSetReleaseId))))
                .fetchInto(AgencyIdListRecord.class);
    }

    public List<AgencyIdListManifestRecord> findAllAgencyIdListManifest(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().selectDistinct(AGENCY_ID_LIST_MANIFEST.fields())
                .from(AGENCY_ID_LIST_MANIFEST)
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(AGENCY_ID_LIST_MANIFEST.RELEASE_ID))
                .where(RELEASE.RELEASE_ID.in(valueOf(releaseIdList(moduleSetReleaseId))))
                .fetchInto(AgencyIdListManifestRecord.class);
    }

    public List<AgencyIdListValueRecord> findAllAgencyIdListValue(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().selectDistinct(AGENCY_ID_LIST_VALUE.fields())
                .from(AGENCY_ID_LIST_VALUE)
                .join(AGENCY_ID_LIST_VALUE_MANIFEST)
                .on(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID))
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID))
                .where(RELEASE.RELEASE_ID.in(valueOf(releaseIdList(moduleSetReleaseId))))
                .fetchInto(AgencyIdListValueRecord.class);
    }

    public List<AgencyIdListValueManifestRecord> findAllAgencyIdListValueManifest(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().selectDistinct(AGENCY_ID_LIST_VALUE_MANIFEST.fields())
                .from(AGENCY_ID_LIST_VALUE_MANIFEST)
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID))
                .where(RELEASE.RELEASE_ID.in(valueOf(releaseIdList(moduleSetReleaseId))))
                .fetchInto(AgencyIdListValueManifestRecord.class);
    }

    public List<CodeListRecord> findAllCodeList(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().selectDistinct(CODE_LIST.fields())
                .from(CODE_LIST)
                .join(CODE_LIST_MANIFEST)
                .on(CODE_LIST.CODE_LIST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_ID))
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(CODE_LIST_MANIFEST.RELEASE_ID))
                .where(RELEASE.RELEASE_ID.in(valueOf(releaseIdList(moduleSetReleaseId))))
                .fetchInto(CodeListRecord.class);
    }

    public List<CodeListManifestRecord> findAllCodeListManifest(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().selectDistinct(CODE_LIST_MANIFEST.fields())
                .from(CODE_LIST_MANIFEST)
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(CODE_LIST_MANIFEST.RELEASE_ID))
                .where(RELEASE.RELEASE_ID.in(valueOf(releaseIdList(moduleSetReleaseId))))
                .fetchInto(CodeListManifestRecord.class);
    }

    public List<CodeListValueRecord> findAllCodeListValue(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().selectDistinct(CODE_LIST_VALUE.fields())
                .from(CODE_LIST_VALUE)
                .join(CODE_LIST_VALUE_MANIFEST)
                .on(CODE_LIST_VALUE.CODE_LIST_VALUE_ID.eq(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_ID))
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(CODE_LIST_VALUE_MANIFEST.RELEASE_ID))
                .where(RELEASE.RELEASE_ID.in(valueOf(releaseIdList(moduleSetReleaseId))))
                .fetchInto(CodeListValueRecord.class);
    }

    public List<CodeListValueManifestRecord> findAllCodeListValueManifest(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().selectDistinct(CODE_LIST_VALUE_MANIFEST.fields())
                .from(CODE_LIST_VALUE_MANIFEST)
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(CODE_LIST_VALUE_MANIFEST.RELEASE_ID))
                .where(RELEASE.RELEASE_ID.in(valueOf(releaseIdList(moduleSetReleaseId))))
                .fetchInto(CodeListValueManifestRecord.class);
    }

    public List<DtRecord> findAllDt(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().selectDistinct(DT.fields())
                .from(DT)
                .join(DT_MANIFEST)
                .on(DT.DT_ID.eq(DT_MANIFEST.DT_ID))
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(DT_MANIFEST.RELEASE_ID))
                .where(RELEASE.RELEASE_ID.in(valueOf(releaseIdList(moduleSetReleaseId))))
                .fetchInto(DtRecord.class);
    }

    public List<DtManifestRecord> findAllDtManifest(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().selectDistinct(DT_MANIFEST.fields())
                .from(DT_MANIFEST)
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(DT_MANIFEST.RELEASE_ID))
                .where(RELEASE.RELEASE_ID.in(valueOf(releaseIdList(moduleSetReleaseId))))
                .fetchInto(DtManifestRecord.class);
    }

    public List<DtScRecord> findAllDtSc(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().selectDistinct(DT_SC.fields())
                .from(DT_SC)
                .join(DT_SC_MANIFEST)
                .on(DT_SC.DT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID))
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(DT_SC_MANIFEST.RELEASE_ID))
                .where(RELEASE.RELEASE_ID.in(valueOf(releaseIdList(moduleSetReleaseId))))
                .fetchInto(DtScRecord.class);
    }

    public List<DtScManifestRecord> findAllDtScManifest(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().selectDistinct(DT_SC_MANIFEST.fields())
                .from(DT_SC_MANIFEST)
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(DT_SC_MANIFEST.RELEASE_ID))
                .where(RELEASE.RELEASE_ID.in(valueOf(releaseIdList(moduleSetReleaseId))))
                .fetchInto(DtScManifestRecord.class);
    }

    public List<DtAwdPriRecord> findAllDtAwdPri(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().selectDistinct(DT_AWD_PRI.fields())
                .from(DT_AWD_PRI)
                .join(DT_MANIFEST).on(and(
                        DT_AWD_PRI.RELEASE_ID.eq(DT_MANIFEST.RELEASE_ID),
                        DT_AWD_PRI.DT_ID.eq(DT_MANIFEST.DT_ID)
                ))
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(DT_MANIFEST.RELEASE_ID))
                .where(RELEASE.RELEASE_ID.in(valueOf(releaseIdList(moduleSetReleaseId))))
                .fetchInto(DtAwdPriRecord.class);
    }

    public List<DtScAwdPriRecord> findAllDtScAwdPri(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().selectDistinct(DT_SC_AWD_PRI.fields())
                .from(DT_SC_AWD_PRI)
                .join(DT_SC_MANIFEST).on(and(
                        DT_SC_AWD_PRI.RELEASE_ID.eq(DT_SC_MANIFEST.RELEASE_ID),
                        DT_SC_AWD_PRI.DT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID)
                ))
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(DT_SC_MANIFEST.RELEASE_ID))
                .where(RELEASE.RELEASE_ID.in(valueOf(releaseIdList(moduleSetReleaseId))))
                .fetchInto(DtScAwdPriRecord.class);
    }

    public List<CdtPriRecord> findAllCdtPri() {
        return dslContext().selectFrom(CDT_PRI)
                .fetchInto(CdtPriRecord.class);
    }

    public List<XbtRecord> findAllXbt(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().selectDistinct(XBT.fields())
                .from(XBT)
                .join(XBT_MANIFEST)
                .on(XBT.XBT_ID.eq(XBT_MANIFEST.XBT_ID))
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(XBT_MANIFEST.RELEASE_ID))
                .where(RELEASE.RELEASE_ID.in(valueOf(releaseIdList(moduleSetReleaseId))))
                .fetchInto(XbtRecord.class);
    }

    public List<XbtManifestRecord> findAllXbtManifest(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().selectDistinct(XBT_MANIFEST.fields())
                .from(XBT_MANIFEST)
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(XBT_MANIFEST.RELEASE_ID))
                .where(RELEASE.RELEASE_ID.in(valueOf(releaseIdList(moduleSetReleaseId))))
                .fetchInto(XbtManifestRecord.class);
    }

    public List<AccRecord> findAllAcc(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().selectDistinct(ACC.fields())
                .from(ACC)
                .join(ACC_MANIFEST)
                .on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(ACC_MANIFEST.RELEASE_ID))
                .where(RELEASE.RELEASE_ID.in(valueOf(releaseIdList(moduleSetReleaseId))))
                .fetchInto(AccRecord.class);
    }

    public List<AccManifestRecord> findAllAccManifest(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().selectDistinct(ACC_MANIFEST.fields())
                .from(ACC_MANIFEST)
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(ACC_MANIFEST.RELEASE_ID))
                .where(RELEASE.RELEASE_ID.in(valueOf(releaseIdList(moduleSetReleaseId))))
                .fetchInto(AccManifestRecord.class);
    }

    public List<AsccRecord> findAllAscc(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().selectDistinct(ASCC.fields())
                .from(ASCC)
                .join(ASCC_MANIFEST)
                .on(ASCC.ASCC_ID.eq(ASCC_MANIFEST.ASCC_ID))
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(ASCC_MANIFEST.RELEASE_ID))
                .where(RELEASE.RELEASE_ID.in(valueOf(releaseIdList(moduleSetReleaseId))))
                .fetchInto(AsccRecord.class);
    }

    public List<AsccManifestRecord> findAllAsccManifest(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().selectDistinct(ASCC_MANIFEST.fields())
                .from(ASCC_MANIFEST)
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(ASCC_MANIFEST.RELEASE_ID))
                .where(RELEASE.RELEASE_ID.in(valueOf(releaseIdList(moduleSetReleaseId))))
                .fetchInto(AsccManifestRecord.class);
    }

    public List<AsccpRecord> findAllAsccp(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().selectDistinct(ASCCP.fields())
                .from(ASCCP)
                .join(ASCCP_MANIFEST)
                .on(ASCCP.ASCCP_ID.eq(ASCCP_MANIFEST.ASCCP_ID))
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(ASCCP_MANIFEST.RELEASE_ID))
                .where(RELEASE.RELEASE_ID.in(valueOf(releaseIdList(moduleSetReleaseId))))
                .fetchInto(AsccpRecord.class);
    }

    public List<AsccpManifestRecord> findAllAsccpManifest(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().selectDistinct(ASCCP_MANIFEST.fields())
                .from(ASCCP_MANIFEST)
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(ASCCP_MANIFEST.RELEASE_ID))
                .where(RELEASE.RELEASE_ID.in(valueOf(releaseIdList(moduleSetReleaseId))))
                .fetchInto(AsccpManifestRecord.class);
    }

    public List<BccRecord> findAllBcc(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().selectDistinct(BCC.fields())
                .from(BCC)
                .join(BCC_MANIFEST)
                .on(BCC.BCC_ID.eq(BCC_MANIFEST.BCC_ID))
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(BCC_MANIFEST.RELEASE_ID))
                .where(RELEASE.RELEASE_ID.in(valueOf(releaseIdList(moduleSetReleaseId))))
                .fetchInto(BccRecord.class);
    }

    public List<BccManifestRecord> findAllBccManifest(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().selectDistinct(BCC_MANIFEST.fields())
                .from(BCC_MANIFEST)
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(BCC_MANIFEST.RELEASE_ID))
                .where(RELEASE.RELEASE_ID.in(valueOf(releaseIdList(moduleSetReleaseId))))
                .fetchInto(BccManifestRecord.class);
    }

    public List<BccpRecord> findAllBccp(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().selectDistinct(BCCP.fields())
                .from(BCCP)
                .join(BCCP_MANIFEST)
                .on(BCCP.BCCP_ID.eq(BCCP_MANIFEST.BCCP_ID))
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(BCCP_MANIFEST.RELEASE_ID))
                .where(RELEASE.RELEASE_ID.in(valueOf(releaseIdList(moduleSetReleaseId))))
                .fetchInto(BccpRecord.class);
    }

    public List<BccpManifestRecord> findAllBccpManifest(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().selectDistinct(BCCP_MANIFEST.fields())
                .from(BCCP_MANIFEST)
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(BCCP_MANIFEST.RELEASE_ID))
                .where(RELEASE.RELEASE_ID.in(valueOf(releaseIdList(moduleSetReleaseId))))
                .fetchInto(BccpManifestRecord.class);
    }


    public List<ModuleCCID<AgencyIdListManifestId>> findAllModuleAgencyIdListManifest(
            ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().select(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_ID,
                        AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID,
                        AGENCY_ID_LIST.AGENCY_ID_LIST_ID,
                        MODULE.PATH)
                .from(AGENCY_ID_LIST)
                .join(AGENCY_ID_LIST_MANIFEST).on(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID))
                .join(MODULE_AGENCY_ID_LIST_MANIFEST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(MODULE_AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID))
                .join(MODULE).on(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .where(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)))
                .fetch(record -> new ModuleCCID<>(
                        new ModuleId(record.get(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_ID).toBigInteger()),
                        new AgencyIdListManifestId(record.get(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger()),
                        record.get(MODULE.PATH)));
    }

    public List<ModuleCCID<CodeListManifestId>> findAllModuleCodeListManifest(
            ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().select(MODULE_CODE_LIST_MANIFEST.MODULE_ID,
                        CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID,
                        CODE_LIST.CODE_LIST_ID,
                        MODULE.PATH)
                .from(CODE_LIST)
                .join(CODE_LIST_MANIFEST).on(CODE_LIST.CODE_LIST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_ID))
                .join(MODULE_CODE_LIST_MANIFEST).on(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(MODULE_CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID))
                .join(MODULE).on(MODULE_CODE_LIST_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .where(MODULE_CODE_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)))
                .fetch(record -> new ModuleCCID<>(
                        new ModuleId(record.get(MODULE_CODE_LIST_MANIFEST.MODULE_ID).toBigInteger()),
                        new CodeListManifestId(record.get(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID).toBigInteger()),
                        record.get(MODULE.PATH)));
    }

    public List<ModuleCCID<AccManifestId>> findAllModuleAccManifest(
            ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().select(MODULE_ACC_MANIFEST.MODULE_ID,
                        ACC_MANIFEST.ACC_MANIFEST_ID,
                        ACC.ACC_ID,
                        MODULE.PATH)
                .from(ACC)
                .join(ACC_MANIFEST).on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                .join(MODULE_ACC_MANIFEST).on(ACC_MANIFEST.ACC_MANIFEST_ID.eq(MODULE_ACC_MANIFEST.ACC_MANIFEST_ID))
                .join(MODULE).on(MODULE_ACC_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .where(MODULE_ACC_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)))
                .fetch(record -> new ModuleCCID<>(
                        new ModuleId(record.get(MODULE_ACC_MANIFEST.MODULE_ID).toBigInteger()),
                        new AccManifestId(record.get(ACC_MANIFEST.ACC_MANIFEST_ID).toBigInteger()),
                        record.get(MODULE.PATH)));
    }

    public List<ModuleCCID<AsccpManifestId>> findAllModuleAsccpManifest(
            ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().select(MODULE_ASCCP_MANIFEST.MODULE_ID,
                        ASCCP_MANIFEST.ASCCP_MANIFEST_ID,
                        ASCCP.ASCCP_ID,
                        MODULE.PATH)
                .from(ASCCP)
                .join(ASCCP_MANIFEST).on(ASCCP.ASCCP_ID.eq(ASCCP_MANIFEST.ASCCP_ID))
                .join(MODULE_ASCCP_MANIFEST).on(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(MODULE_ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .join(MODULE).on(MODULE_ASCCP_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .where(MODULE_ASCCP_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)))
                .fetch(record -> new ModuleCCID<>(
                        new ModuleId(record.get(MODULE_ASCCP_MANIFEST.MODULE_ID).toBigInteger()),
                        new AsccpManifestId(record.get(ASCCP_MANIFEST.ASCCP_MANIFEST_ID).toBigInteger()),
                        record.get(MODULE.PATH)));
    }

    public List<ModuleCCID<BccpManifestId>> findAllModuleBccpManifest(
            ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().select(MODULE_BCCP_MANIFEST.MODULE_ID,
                        BCCP_MANIFEST.BCCP_MANIFEST_ID,
                        BCCP.BCCP_ID,
                        MODULE.PATH)
                .from(BCCP)
                .join(BCCP_MANIFEST).on(BCCP.BCCP_ID.eq(BCCP_MANIFEST.BCCP_ID))
                .join(MODULE_BCCP_MANIFEST).on(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(MODULE_BCCP_MANIFEST.BCCP_MANIFEST_ID))
                .join(MODULE).on(MODULE_BCCP_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .where(MODULE_BCCP_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)))
                .fetch(record -> new ModuleCCID<>(
                        new ModuleId(record.get(MODULE_BCCP_MANIFEST.MODULE_ID).toBigInteger()),
                        new BccpManifestId(record.get(BCCP_MANIFEST.BCCP_MANIFEST_ID).toBigInteger()),
                        record.get(MODULE.PATH)));
    }

    public List<ModuleCCID<DtManifestId>> findAllModuleDtManifest(
            ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().select(MODULE_DT_MANIFEST.MODULE_ID,
                        DT_MANIFEST.DT_MANIFEST_ID,
                        DT.DT_ID,
                        MODULE.PATH)
                .from(DT)
                .join(DT_MANIFEST).on(DT.DT_ID.eq(DT_MANIFEST.DT_ID))
                .join(MODULE_DT_MANIFEST).on(DT_MANIFEST.DT_MANIFEST_ID.eq(MODULE_DT_MANIFEST.DT_MANIFEST_ID))
                .join(MODULE).on(MODULE_DT_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .where(MODULE_DT_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)))
                .fetch(record -> new ModuleCCID<>(
                        new ModuleId(record.get(MODULE_DT_MANIFEST.MODULE_ID).toBigInteger()),
                        new DtManifestId(record.get(DT_MANIFEST.DT_MANIFEST_ID).toBigInteger()),
                        record.get(MODULE.PATH)));
    }

    public List<ModuleCCID<XbtManifestId>> findAllModuleXbtManifest(
            ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().select(MODULE_XBT_MANIFEST.MODULE_ID,
                        XBT_MANIFEST.XBT_MANIFEST_ID,
                        XBT.XBT_ID,
                        MODULE.PATH)
                .from(XBT)
                .join(XBT_MANIFEST).on(XBT.XBT_ID.eq(XBT_MANIFEST.XBT_ID))
                .join(MODULE_XBT_MANIFEST).on(XBT_MANIFEST.XBT_MANIFEST_ID.eq(MODULE_XBT_MANIFEST.XBT_MANIFEST_ID))
                .join(MODULE).on(MODULE_XBT_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .where(MODULE_XBT_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)))
                .fetch(record -> new ModuleCCID<>(
                        new ModuleId(record.get(MODULE_XBT_MANIFEST.MODULE_ID).toBigInteger()),
                        new XbtManifestId(record.get(XBT_MANIFEST.XBT_MANIFEST_ID).toBigInteger()),
                        record.get(MODULE.PATH)));
    }

    public List<ModuleCCID<BlobContentManifestId>> findAllModuleBlobContentManifest(
            ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().select(MODULE_BLOB_CONTENT_MANIFEST.MODULE_ID,
                        BLOB_CONTENT_MANIFEST.BLOB_CONTENT_MANIFEST_ID,
                        BLOB_CONTENT.BLOB_CONTENT_ID,
                        MODULE.PATH)
                .from(BLOB_CONTENT)
                .join(BLOB_CONTENT_MANIFEST).on(BLOB_CONTENT.BLOB_CONTENT_ID.eq(BLOB_CONTENT_MANIFEST.BLOB_CONTENT_ID))
                .join(MODULE_BLOB_CONTENT_MANIFEST).on(BLOB_CONTENT_MANIFEST.BLOB_CONTENT_MANIFEST_ID.eq(MODULE_BLOB_CONTENT_MANIFEST.BLOB_CONTENT_MANIFEST_ID))
                .join(MODULE).on(MODULE_BLOB_CONTENT_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .where(MODULE_BLOB_CONTENT_MANIFEST.MODULE_SET_RELEASE_ID.eq(valueOf(moduleSetReleaseId)))
                .fetch(record -> new ModuleCCID<>(
                        new ModuleId(record.get(MODULE_BLOB_CONTENT_MANIFEST.MODULE_ID).toBigInteger()),
                        new BlobContentManifestId(record.get(BLOB_CONTENT_MANIFEST.BLOB_CONTENT_MANIFEST_ID).toBigInteger()),
                        record.get(MODULE.PATH)));
    }

    public List<BlobContentManifestRecord> findAllBlobContentManifest(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().select(BLOB_CONTENT_MANIFEST.fields())
                .from(BLOB_CONTENT_MANIFEST)
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(BLOB_CONTENT_MANIFEST.RELEASE_ID))
                .where(RELEASE.RELEASE_ID.in(valueOf(releaseIdList(moduleSetReleaseId))))
                .fetchInto(BlobContentManifestRecord.class);
    }

    public List<BlobContentRecord> findAllBlobContent(ModuleSetReleaseId moduleSetReleaseId) {
        return dslContext().select(BLOB_CONTENT.fields())
                .from(BLOB_CONTENT)
                .join(BLOB_CONTENT_MANIFEST)
                .on(BLOB_CONTENT.BLOB_CONTENT_ID.eq(BLOB_CONTENT_MANIFEST.BLOB_CONTENT_ID))
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(BLOB_CONTENT_MANIFEST.RELEASE_ID))
                .where(RELEASE.RELEASE_ID.in(valueOf(releaseIdList(moduleSetReleaseId))))
                .fetchInto(BlobContentRecord.class);
    }

    public List<SeqKeyRecord> findAllSeqKeyRecord() {
        return dslContext().select(SEQ_KEY.fields())
                .from(SEQ_KEY)
                .fetchInto(SeqKeyRecord.class);
    }

    public List<ReleaseRecord> findAllRelease() {
        return dslContext().select(RELEASE.fields())
                .from(RELEASE)
                .fetchInto(ReleaseRecord.class);
    }

    public List<NamespaceSummaryRecord> findAllNamespace() {
        var namespaceQuery = repositoryFactory().namespaceQueryRepository(requester());
        return namespaceQuery.getNamespaceSummaryList();
    }
}
