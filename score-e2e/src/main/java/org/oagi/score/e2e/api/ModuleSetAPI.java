package org.oagi.score.e2e.api;

import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.ModuleSetObject;

public interface ModuleSetAPI {
    ModuleSetObject getTheLatestModuleSetCreatedBy(AppUserObject user);
}
