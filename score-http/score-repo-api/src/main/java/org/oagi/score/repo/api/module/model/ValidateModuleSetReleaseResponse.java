package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.Response;

import java.util.HashMap;
import java.util.Map;

public class ValidateModuleSetReleaseResponse extends Response {

    private Map<String, String> results = new HashMap<>();

    private String requestId;

    private long progress;

    private long length;

    private boolean done;

    public void addResult(String moduleName, String message) {
        results.put(moduleName, message);
    }

    public void setResults(Map<String, String> results) {
        this.results = new HashMap<>(results);
    }

    public Map<String, String> getResults() {
        return results;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
