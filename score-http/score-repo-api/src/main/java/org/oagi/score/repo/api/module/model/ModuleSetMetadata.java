package org.oagi.score.repo.api.module.model;

import java.io.Serializable;
import java.math.BigInteger;

public class ModuleSetMetadata implements Serializable {

    private BigInteger moduleSetId;

    private int numberOfDirectories;

    private int numberOfFiles;

    public BigInteger getModuleSetId() {
        return moduleSetId;
    }

    public void setModuleSetId(BigInteger moduleSetId) {
        this.moduleSetId = moduleSetId;
    }

    public int getNumberOfDirectories() {
        return numberOfDirectories;
    }

    public void setNumberOfDirectories(int numberOfDirectories) {
        this.numberOfDirectories = numberOfDirectories;
    }

    public int getNumberOfFiles() {
        return numberOfFiles;
    }

    public void setNumberOfFiles(int numberOfFiles) {
        this.numberOfFiles = numberOfFiles;
    }
}
