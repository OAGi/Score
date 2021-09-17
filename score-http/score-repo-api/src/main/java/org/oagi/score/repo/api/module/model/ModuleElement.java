package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.Auditable;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

public class ModuleElement extends Auditable implements Comparable<ModuleElement>, Serializable {

    private BigInteger moduleId;

    private String name;

    private BigInteger namespaceId;

    private String namespaceUri;

    private String versionNum;

    private String path;

    private boolean assigned;

    private boolean isDirectory;

    private BigInteger parentModuleId;

    private List<ModuleElement> child;

    public List<ModuleElement> getChild() {
        return child;
    }

    public void setChild(List<ModuleElement> child) {
        this.child = child;
    }

    public BigInteger getModuleId() {
        return moduleId;
    }

    public void setModuleId(BigInteger moduleId) {
        this.moduleId = moduleId;
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

    public String getNamespaceUri() {
        return namespaceUri;
    }

    public void setNamespaceUri(String namespaceUri) {
        this.namespaceUri = namespaceUri;
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

    public boolean isAssigned() {
        return assigned;
    }

    public void setAssigned(boolean assigned) {
        this.assigned = assigned;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public BigInteger getParentModuleId() {
        return parentModuleId;
    }

    public void setParentModuleId(BigInteger parentModuleId) {
        this.parentModuleId = parentModuleId;
    }

    @Override
    public int compareTo(ModuleElement o) {
        return this.isDirectory ? o.isDirectory() ? this.moduleId.compareTo(o.getModuleId()) : 1 : 1;
    }
}
