package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.Response;

import java.util.HashMap;
import java.util.Map;

public class ValidateModuleSetReleaseResponse extends Response {

    private Map<String, String> results = new HashMap<>();

    public void addResult(String moduleName, String message) {
        results.put(moduleName, message);
    }

    public Map<String, String> getResults() {
        return results;
    }

}
