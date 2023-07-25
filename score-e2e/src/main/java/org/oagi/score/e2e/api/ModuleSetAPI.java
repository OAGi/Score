package org.oagi.score.e2e.api;

import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.ModuleSetObject;

import java.util.List;

public interface ModuleSetAPI {

    ModuleSetObject getTheLatestModuleSetCreatedBy(AppUserObject user);

    ModuleSetObject getModuleSetByName(String moduleSetName);

    List<ModuleSetObject> getAllModuleSets();

}
