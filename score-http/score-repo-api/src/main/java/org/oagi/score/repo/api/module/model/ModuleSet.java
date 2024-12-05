package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.Auditable;

import java.io.Serializable;
import java.math.BigInteger;

public class ModuleSet extends Auditable implements Comparable<ModuleSet>, Serializable {

    private BigInteger moduleSetId;

    private BigInteger libraryId;

    private String guid;

    private String name;

    private String description;

    public BigInteger getModuleSetId() {
        return moduleSetId;
    }

    public void setModuleSetId(BigInteger moduleSetId) {
        this.moduleSetId = moduleSetId;
    }

    public BigInteger getLibraryId() {
        return libraryId;
    }

    public void setLibraryId(BigInteger libraryId) {
        this.libraryId = libraryId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int compareTo(ModuleSet o) {
        return this.moduleSetId.compareTo(o.getModuleSetId());
    }
}
