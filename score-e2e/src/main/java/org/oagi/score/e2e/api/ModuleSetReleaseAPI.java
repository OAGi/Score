package org.oagi.score.e2e.api;

import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.ModuleSetReleaseObject;

public interface ModuleSetReleaseAPI {
    ModuleSetReleaseObject getTheLatestModuleSetReleaseCreatedBy(AppUserObject user);
}
