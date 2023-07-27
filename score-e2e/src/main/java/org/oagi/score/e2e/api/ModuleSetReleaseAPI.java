package org.oagi.score.e2e.api;

import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.ModuleSetReleaseObject;

import java.math.BigInteger;

public interface ModuleSetReleaseAPI {

    ModuleSetReleaseObject getModuleSetReleaseById(BigInteger moduleSetReleaseId);

    ModuleSetReleaseObject getModuleSetReleaseByName(String moduleSetReleaseName);

    ModuleSetReleaseObject getTheLatestModuleSetReleaseCreatedBy(AppUserObject user);

    void updateModuleSetRelease(ModuleSetReleaseObject moduleSetRelease);

}
