package org.oagi.score.gateway.http.api.module_management.repository;

import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetId;

public interface ModuleSetCommandRepository {

    ModuleSetId create(LibraryId libraryId, String name, String description);

    boolean update(ModuleSetId moduleSetId, String name, String description);

    boolean delete(ModuleSetId moduleSetId);

}
