package org.oagi.score.gateway.http.api.module_management.model;

import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;

import java.util.List;

public class ModuleElementRecord {

    private ModuleId moduleId;
    private String name;
    private NamespaceId namespaceId;
    private String namespaceUri;
    private String versionNum;
    private boolean directory;
    private ModuleId parentModuleId;
    private List<ModuleElementRecord> children;

    public ModuleId getModuleId() {
        return moduleId;
    }

    public void setModuleId(ModuleId moduleId) {
        this.moduleId = moduleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NamespaceId getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(NamespaceId namespaceId) {
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

    public boolean isDirectory() {
        return directory;
    }

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    public ModuleId getParentModuleId() {
        return parentModuleId;
    }

    public void setParentModuleId(ModuleId parentModuleId) {
        this.parentModuleId = parentModuleId;
    }

    public List<ModuleElementRecord> getChildren() {
        return children;
    }

    public void setChildren(List<ModuleElementRecord> children) {
        this.children = children;
    }
}
