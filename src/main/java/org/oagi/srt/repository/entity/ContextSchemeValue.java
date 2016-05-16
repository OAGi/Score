package org.oagi.srt.repository.entity;

public class ContextSchemeValue {

    private int ctxSchemeValueId;
    private String guid;
    private String value;
    private String meaning;
    private int ownerCtxSchemeId;

    public int getCtxSchemeValueId() {
        return ctxSchemeValueId;
    }

    public void setCtxSchemeValueId(int ctxSchemeValueId) {
        this.ctxSchemeValueId = ctxSchemeValueId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public int getOwnerCtxSchemeId() {
        return ownerCtxSchemeId;
    }

    public void setOwnerCtxSchemeId(int ownerCtxSchemeId) {
        this.ownerCtxSchemeId = ownerCtxSchemeId;
    }
}
