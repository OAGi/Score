package org.oagi.score.gateway.http.api.export.model;

import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SchemaCodeList {

    private CodeListManifestId codeListManifestId;

    private String guid;

    private String name;

    private String enumTypeGuid;

    private List<String> values = new ArrayList();

    private SchemaCodeList baseCodeList;

    private NamespaceId namespaceId;

    public SchemaCodeList(CodeListManifestId codeListManifestId, NamespaceId namespaceId) {
        this.codeListManifestId = codeListManifestId;
        this.namespaceId = namespaceId;
    }

    public CodeListManifestId getCodeListManifestId() {
        return codeListManifestId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getName() {
        return name.replaceAll(" ", "").replace("Identifier", "ID");
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnumTypeGuid() {
        return enumTypeGuid;
    }

    public void setEnumTypeGuid(String enumTypeGuid) {
        this.enumTypeGuid = enumTypeGuid;
    }

    public void addValue(String value) {
        this.values.add(value);
    }

    public Collection<String> getValues() {
        return this.values;
    }

    public void setBaseCodeList(SchemaCodeList baseCodeList) {
        this.baseCodeList = baseCodeList;
    }

    public SchemaCodeList getBaseCodeList() {
        return baseCodeList;
    }

    public NamespaceId getNamespaceId() {
        return this.namespaceId;
    }

}
