package org.oagi.score.gateway.http.api.module_management.repository;

import org.oagi.score.gateway.http.api.export.model.ScoreModule;
import org.oagi.score.gateway.http.api.module_management.model.ModuleId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSetReleaseId;
import org.oagi.score.gateway.http.api.module_management.model.ModuleSummaryRecord;

import java.util.List;

public interface ModuleQueryRepository {

    ModuleSummaryRecord getRootModule(ModuleSetId moduleSetId);

    List<ModuleSummaryRecord> getModuleSummaryList(ModuleSetId moduleSetId);

    List<ModuleSummaryRecord> getTopLevelModules(ModuleSetId moduleSetId);

    ModuleSummaryRecord getModule(ModuleId moduleId);

    List<ModuleSummaryRecord> getChildren(ModuleId moduleId);

    ModuleSummaryRecord getDuplicateModule(ModuleId moduleId, String name);

    boolean hasDuplicateName(ModuleId parentModuleId, String name);

    List<ScoreModule> getScoreModules(ModuleSetReleaseId moduleSetReleaseId);

}
