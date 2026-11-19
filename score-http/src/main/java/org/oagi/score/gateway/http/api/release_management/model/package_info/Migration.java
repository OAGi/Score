package org.oagi.score.gateway.http.api.release_management.model.package_info;

import java.util.Collections;
import java.util.List;

public class Migration {

    private Release release;
    private List<Executable> entrypoints;
    private List<Executable> processes;
    private Metadata metadata;

    public Release getRelease() {
        return release;
    }

    public void setRelease(Release release) {
        this.release = release;
    }

    public List<Executable> getEntrypoints() {
        return (entrypoints != null) ? entrypoints : Collections.emptyList();
    }

    public void setEntrypoints(List<Executable> entrypoints) {
        this.entrypoints = entrypoints;
    }

    public List<Executable> getProcesses() {
        return processes;
    }

    public void setProcesses(List<Executable> processes) {
        this.processes = processes;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }
}

