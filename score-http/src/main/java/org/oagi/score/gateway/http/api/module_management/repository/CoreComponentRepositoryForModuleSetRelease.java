package org.oagi.score.gateway.http.api.module_management.repository;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.blob_content.BlobContentManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.export.model.ModuleCCID;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetReleaseId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.*;

import java.util.List;

public interface CoreComponentRepositoryForModuleSetRelease {

    List<AgencyIdListRecord> findAllAgencyIdList(ModuleSetReleaseId moduleSetReleaseId);

    List<AgencyIdListManifestRecord> findAllAgencyIdListManifest(ModuleSetReleaseId moduleSetReleaseId);

    List<AgencyIdListValueRecord> findAllAgencyIdListValue(ModuleSetReleaseId moduleSetReleaseId);

    List<AgencyIdListValueManifestRecord> findAllAgencyIdListValueManifest(ModuleSetReleaseId moduleSetReleaseId);

    List<CodeListRecord> findAllCodeList(ModuleSetReleaseId moduleSetReleaseId);

    List<CodeListManifestRecord> findAllCodeListManifest(ModuleSetReleaseId moduleSetReleaseId);

    List<CodeListValueRecord> findAllCodeListValue(ModuleSetReleaseId moduleSetReleaseId);

    List<CodeListValueManifestRecord> findAllCodeListValueManifest(ModuleSetReleaseId moduleSetReleaseId);

    List<DtRecord> findAllDt(ModuleSetReleaseId moduleSetReleaseId);

    List<DtManifestRecord> findAllDtManifest(ModuleSetReleaseId moduleSetReleaseId);

    List<DtScRecord> findAllDtSc(ModuleSetReleaseId moduleSetReleaseId);

    List<DtScManifestRecord> findAllDtScManifest(ModuleSetReleaseId moduleSetReleaseId);

    List<DtAwdPriRecord> findAllDtAwdPri(ModuleSetReleaseId moduleSetReleaseId);

    List<DtScAwdPriRecord> findAllDtScAwdPri(ModuleSetReleaseId moduleSetReleaseId);

    List<CdtPriRecord> findAllCdtPri();

    List<XbtRecord> findAllXbt(ModuleSetReleaseId moduleSetReleaseId);

    List<XbtManifestRecord> findAllXbtManifest(ModuleSetReleaseId moduleSetReleaseId);

    List<AccRecord> findAllAcc(ModuleSetReleaseId moduleSetReleaseId);

    List<AccManifestRecord> findAllAccManifest(ModuleSetReleaseId moduleSetReleaseId);

    List<AsccRecord> findAllAscc(ModuleSetReleaseId moduleSetReleaseId);

    List<AsccManifestRecord> findAllAsccManifest(ModuleSetReleaseId moduleSetReleaseId);

    List<AsccpRecord> findAllAsccp(ModuleSetReleaseId moduleSetReleaseId);

    List<AsccpManifestRecord> findAllAsccpManifest(ModuleSetReleaseId moduleSetReleaseId);

    List<BccRecord> findAllBcc(ModuleSetReleaseId moduleSetReleaseId);

    List<BccManifestRecord> findAllBccManifest(ModuleSetReleaseId moduleSetReleaseId);

    List<BccpRecord> findAllBccp(ModuleSetReleaseId moduleSetReleaseId);

    List<BccpManifestRecord> findAllBccpManifest(ModuleSetReleaseId moduleSetReleaseId);

    List<ModuleCCID<AgencyIdListManifestId>> findAllModuleAgencyIdListManifest(
            ModuleSetReleaseId moduleSetReleaseId);

    List<ModuleCCID<CodeListManifestId>> findAllModuleCodeListManifest(
            ModuleSetReleaseId moduleSetReleaseId);

    List<ModuleCCID<AccManifestId>> findAllModuleAccManifest(
            ModuleSetReleaseId moduleSetReleaseId);

    List<ModuleCCID<AsccpManifestId>> findAllModuleAsccpManifest(
            ModuleSetReleaseId moduleSetReleaseId);

    List<ModuleCCID<BccpManifestId>> findAllModuleBccpManifest(
            ModuleSetReleaseId moduleSetReleaseId);

    List<ModuleCCID<DtManifestId>> findAllModuleDtManifest(
            ModuleSetReleaseId moduleSetReleaseId);

    List<ModuleCCID<XbtManifestId>> findAllModuleXbtManifest(
            ModuleSetReleaseId moduleSetReleaseId);

    List<ModuleCCID<BlobContentManifestId>> findAllModuleBlobContentManifest(
            ModuleSetReleaseId moduleSetReleaseId);

    List<BlobContentManifestRecord> findAllBlobContentManifest(ModuleSetReleaseId moduleSetReleaseId);

    List<BlobContentRecord> findAllBlobContent(ModuleSetReleaseId moduleSetReleaseId);

    List<SeqKeyRecord> findAllSeqKeyRecord();

    List<ReleaseRecord> findAllRelease();

    List<NamespaceSummaryRecord> findAllNamespace();

}
