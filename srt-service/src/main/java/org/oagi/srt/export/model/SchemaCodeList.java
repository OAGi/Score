package org.oagi.srt.export.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SchemaCodeList {

    private String guid;

    private String name;

    private String enumTypeGuid;

    private List<String> values = new ArrayList();

    private SchemaCodeList baseCodeList;

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
}
