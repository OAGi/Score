package org.oagi.score.gateway.http.api.module_management.repository;

import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.module_management.model.*;
import org.oagi.score.gateway.http.api.module_management.repository.criteria.ModuleSetListFilterCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.ResultAndCount;

import java.util.List;

public interface ModuleSetQueryRepository {

    List<ModuleSetSummaryRecord> getModuleSetSummaryList(LibraryId libraryId);

    ModuleSetDetailsRecord getModuleSetDetails(ModuleSetId moduleSetId);

    ResultAndCount<ModuleSetListEntryRecord> getModuleSetList(
            ModuleSetListFilterCriteria filterCriteria, PageRequest pageRequest);

    ModuleSetMetadataRecord getModuleSetMetadata(ModuleSetId moduleSetId);

}
