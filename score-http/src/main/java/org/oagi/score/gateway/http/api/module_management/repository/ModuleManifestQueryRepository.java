package org.oagi.score.gateway.http.api.module_management.repository;

import org.oagi.score.gateway.http.api.module_management.model.AssignNodeRecord;
import org.oagi.score.gateway.http.api.module_management.model.ModuleId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

import java.util.List;

public interface ModuleManifestQueryRepository {

    List<AssignNodeRecord> getAssignableACCByModuleSetReleaseId(
            ModuleSetReleaseId moduleSetReleaseId, ReleaseId releaseId);

    List<AssignNodeRecord> getAssignedACCByModuleSetReleaseId(
            ModuleSetReleaseId moduleSetReleaseId, ModuleId moduleId);

    List<AssignNodeRecord> getAssignableASCCPByModuleSetReleaseId(
            ModuleSetReleaseId moduleSetReleaseId, ReleaseId releaseId);

    List<AssignNodeRecord> getAssignedASCCPByModuleSetReleaseId(
            ModuleSetReleaseId moduleSetReleaseId, ModuleId moduleId);

    List<AssignNodeRecord> getAssignableBCCPByModuleSetReleaseId(
            ModuleSetReleaseId moduleSetReleaseId, ReleaseId releaseId);

    List<AssignNodeRecord> getAssignedBCCPByModuleSetReleaseId(
            ModuleSetReleaseId moduleSetReleaseId, ModuleId moduleId);

    List<AssignNodeRecord> getAssignableDTByModuleSetReleaseId(
            ModuleSetReleaseId moduleSetReleaseId, ReleaseId releaseId);

    List<AssignNodeRecord> getAssignedDTByModuleSetReleaseId(
            ModuleSetReleaseId moduleSetReleaseId, ModuleId moduleId);

    List<AssignNodeRecord> getAssignableCodeListByModuleSetReleaseId(
            ModuleSetReleaseId moduleSetReleaseId, ReleaseId releaseId);

    List<AssignNodeRecord> getAssignedCodeListByModuleSetReleaseId(
            ModuleSetReleaseId moduleSetReleaseId, ModuleId moduleId);

    List<AssignNodeRecord> getAssignableAgencyIdListByModuleSetReleaseId(
            ModuleSetReleaseId moduleSetReleaseId, ReleaseId releaseId);

    List<AssignNodeRecord> getAssignedAgencyIdListByModuleSetReleaseId(
            ModuleSetReleaseId moduleSetReleaseId, ModuleId moduleId);

    List<AssignNodeRecord> getAssignableXBTByModuleSetReleaseId(
            ModuleSetReleaseId moduleSetReleaseId, ReleaseId releaseId);

    List<AssignNodeRecord> getAssignedXBTByModuleSetReleaseId(
            ModuleSetReleaseId moduleSetReleaseId, ModuleId moduleId);

}
