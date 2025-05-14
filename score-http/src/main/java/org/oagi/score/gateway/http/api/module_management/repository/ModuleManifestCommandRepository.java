package org.oagi.score.gateway.http.api.module_management.repository;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;

import java.util.Collection;
import java.util.function.Function;

public interface ModuleManifestCommandRepository {

    void deleteModuleManifests(Collection<ModuleSetReleaseId> moduleSetReleaseIdList);

    void copyAccModuleManifest(ModuleSetReleaseId moduleSetReleaseId,
                               ModuleSetReleaseId baseModuleSetReleaseId,
                               ReleaseId releaseId, Function<String, ModuleId> pathToModuleId);

    void createAccModuleManifest(AccManifestId accManifestId,
                                 ModuleId moduleId, ModuleSetReleaseId moduleSetReleaseId);

    void deleteAccModuleManifest(AccManifestId accManifestId,
                                 ModuleId moduleId, ModuleSetReleaseId moduleSetReleaseId);

    void copyAsccpModuleManifest(ModuleSetReleaseId moduleSetReleaseId,
                                 ModuleSetReleaseId baseModuleSetReleaseId,
                                 ReleaseId releaseId, Function<String, ModuleId> pathToModuleId);

    void createAsccpModuleManifest(AsccpManifestId asccpManifestId,
                                   ModuleId moduleId, ModuleSetReleaseId moduleSetReleaseId);

    void deleteAsccpModuleManifest(AsccpManifestId asccpManifestId,
                                   ModuleId moduleId, ModuleSetReleaseId moduleSetReleaseId);

    void copyBccpModuleManifest(ModuleSetReleaseId moduleSetReleaseId,
                                ModuleSetReleaseId baseModuleSetReleaseId,
                                ReleaseId releaseId, Function<String, ModuleId> pathToModuleId);

    void createBccpModuleManifest(BccpManifestId bccpManifestId,
                                  ModuleId moduleId, ModuleSetReleaseId moduleSetReleaseId);

    void deleteBccpModuleManifest(BccpManifestId bccpManifestId,
                                  ModuleId moduleId, ModuleSetReleaseId moduleSetReleaseId);

    void copyDtModuleManifest(ModuleSetReleaseId moduleSetReleaseId,
                              ModuleSetReleaseId baseModuleSetReleaseId,
                              ReleaseId releaseId, Function<String, ModuleId> pathToModuleId);

    void createDtModuleManifest(DtManifestId dtManifestId,
                                ModuleId moduleId, ModuleSetReleaseId moduleSetReleaseId);

    void deleteDtModuleManifest(DtManifestId dtManifestId,
                                ModuleId moduleId, ModuleSetReleaseId moduleSetReleaseId);

    void copyCodeListModuleManifest(ModuleSetReleaseId moduleSetReleaseId,
                                    ModuleSetReleaseId baseModuleSetReleaseId,
                                    ReleaseId releaseId, Function<String, ModuleId> pathToModuleId);

    void createCodeListModuleManifest(CodeListManifestId codeListManifestId,
                                      ModuleId moduleId, ModuleSetReleaseId moduleSetReleaseId);

    void deleteCodeListModuleManifest(CodeListManifestId codeListManifestId,
                                      ModuleId moduleId, ModuleSetReleaseId moduleSetReleaseId);

    void copyAgencyIdListModuleManifest(ModuleSetReleaseId moduleSetReleaseId,
                                        ModuleSetReleaseId baseModuleSetReleaseId,
                                        ReleaseId releaseId, Function<String, ModuleId> pathToModuleId);

    void createAgencyIdListModuleManifest(AgencyIdListManifestId agencyIdListManifestId,
                                          ModuleId moduleId, ModuleSetReleaseId moduleSetReleaseId);

    void deleteAgencyIdListModuleManifest(AgencyIdListManifestId agencyIdListManifestId,
                                          ModuleId moduleId, ModuleSetReleaseId moduleSetReleaseId);

    void copyXbtModuleManifest(ModuleSetReleaseId moduleSetReleaseId,
                               ModuleSetReleaseId baseModuleSetReleaseId,
                               ReleaseId releaseId, Function<String, ModuleId> pathToModuleId);

    void createXbtModuleManifest(XbtManifestId xbtManifestId,
                                 ModuleId moduleId, ModuleSetReleaseId moduleSetReleaseId);

    void deleteXbtModuleManifest(XbtManifestId xbtManifestId,
                                 ModuleId moduleId, ModuleSetReleaseId moduleSetReleaseId);

    void copyBlobContentModuleManifest(ModuleSetReleaseId moduleSetReleaseId,
                                       ModuleSetReleaseId baseModuleSetReleaseId,
                                       ReleaseId releaseId, Function<String, ModuleId> pathToModuleId);

}
