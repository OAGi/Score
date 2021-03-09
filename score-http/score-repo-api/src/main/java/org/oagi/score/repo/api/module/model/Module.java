package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.Auditable;

import java.io.Serializable;
import java.math.BigInteger;

public class Module extends Auditable implements Comparable<Module>, Serializable {

    private BigInteger moduleId;

    private BigInteger moduleDirId;

    private String path;

    private String name;

    private BigInteger namespaceId;

    private String namespaceUri;

    private boolean assigned;

    private String versionNum;

    public BigInteger getModuleId() {
        return moduleId;
    }

    public void setModuleId(BigInteger moduleId) {
        this.moduleId = moduleId;
    }

    public BigInteger getModuleDirId() {
        return moduleDirId;
    }

    public void setModuleDirId(BigInteger moduleDirId) {
        this.moduleDirId = moduleDirId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigInteger getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(BigInteger namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(String versionNum) {
        this.versionNum = versionNum;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getNamespaceUri() {
        return namespaceUri;
    }

    public void setNamespaceUri(String namespaceUri) {
        this.namespaceUri = namespaceUri;
    }

    public boolean isAssigned() {
        return assigned;
    }

    public void setAssigned(boolean assigned) {
        this.assigned = assigned;
    }

    @Override
    public int compareTo(Module o) {
        return this.moduleId.compareTo(o.getModuleId());
    }
}
