package org.oagi.score.e2e.api;

import org.oagi.score.e2e.obj.ModuleObject;

import java.math.BigInteger;
import java.util.List;

public interface ModuleAPI {
    List<ModuleObject> getModulesByModuleSet(BigInteger moduleSetId);

    List<ModuleObject> getSubmodules(BigInteger moduleId);
}
