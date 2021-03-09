package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.Auditable;

import java.io.Serializable;
import java.math.BigInteger;

public class ModuleDir extends Auditable implements Comparable<ModuleDir>, Serializable {

    private BigInteger moduleDirId;

    private BigInteger parentModuleDirId;

    private String name;

    private String path;

    public BigInteger getModuleDirId() {
        return moduleDirId;
    }

    public void setModuleDirId(BigInteger moduleDirId) {
        this.moduleDirId = moduleDirId;
    }

    public BigInteger getParentModuleDirId() {
        return parentModuleDirId;
    }

    public void setParentModuleDirId(BigInteger parentModuleDirId) {
        this.parentModuleDirId = parentModuleDirId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public int compareTo(ModuleDir o) {
        return this.moduleDirId.compareTo(o.getModuleDirId());
    }
}
