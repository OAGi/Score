package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.Response;
import org.oagi.score.repo.api.businesscontext.model.BusinessContextValue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class CreateModuleResponse extends Response {

    private final Module module;

    public CreateModuleResponse(Module module) {
        this.module = module;
    }

    public Module getModule() {
        return module;
    }
}
