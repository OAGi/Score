package org.oagi.score.gateway.http.api.module_management.repository;

import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.module_management.model.*;
import org.oagi.score.gateway.http.api.module_management.repository.criteria.ModuleSetReleaseListFilterCriteria;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;

import java.util.List;

/**
 * Repository interface for querying module set releases.
 * Provides methods to check whether a module set release
 * exists for a given release ID.
 */
public interface ModuleSetReleaseQueryRepository {

    List<ModuleSetReleaseSummaryRecord> getModuleSetReleaseSummaryList(LibraryId libraryId);

    List<ModuleSetReleaseSummaryRecord> getModuleSetReleaseSummaryList(ReleaseId releaseId);

    ModuleSetReleaseDetailsRecord getModuleSetReleaseDetails(
            ModuleSetReleaseId moduleSetReleaseId);

    ResultAndCount<ModuleSetReleaseListEntryRecord> getModuleSetReleaseList(
            ModuleSetReleaseListFilterCriteria filterCriteria, PageRequest pageRequest);

    ModuleSetReleaseSummaryRecord getDefaultModuleSetReleaseSummary(ReleaseId releaseId);

    /**
     * Determines whether a module set release is associated
     * with the specified release ID.
     *
     * @param releaseId the unique identifier of the release.
     * @return {@code true} if a module set release exists, otherwise {@code false}.
     */
    boolean exists(ReleaseId releaseId);

    boolean exists(ModuleSetId moduleSetId);

    /**
     * Determines whether another module set release of the release has the same name.
     *
     * @param releaseId            the release of the module set release.
     * @param moduleSetReleaseId   the module set release to leave out (the one being updated), or {@code null}.
     * @param moduleSetReleaseName the name to look for, compared after trimming.
     * @return {@code true} if there is such a module set release, otherwise {@code false}.
     */
    boolean hasSameNameModuleSetRelease(ReleaseId releaseId, ModuleSetReleaseId moduleSetReleaseId,
                                        String moduleSetReleaseName);

    String getDefaultName(ModuleSetId moduleSetId);

    List<ModuleAccRecord> getModuleAccList(ModuleSetReleaseId moduleSetReleaseId);

    List<ModuleAsccpRecord> getModuleAsccpList(ModuleSetReleaseId moduleSetReleaseId);

    List<ModuleBccpRecord> getModuleBccpList(ModuleSetReleaseId moduleSetReleaseId);

    List<ModuleDtRecord> getModuleDtList(ModuleSetReleaseId moduleSetReleaseId);

    List<ModuleCodeListRecord> getModuleCodeListList(ModuleSetReleaseId moduleSetReleaseId);

    List<ModuleAgencyIdListRecord> getModuleAgencyIdListList(ModuleSetReleaseId moduleSetReleaseId);

    List<ModuleXbtRecord> getModuleXbtList(ModuleSetReleaseId moduleSetReleaseId);

    List<ModuleBlobContentRecord> getModuleBlobContentList(ModuleSetReleaseId moduleSetReleaseId);

}
