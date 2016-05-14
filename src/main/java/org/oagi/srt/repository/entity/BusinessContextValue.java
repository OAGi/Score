package org.oagi.srt.repository.entity;

public class BusinessContextValue {
    private int bizCtxValueId;
    private int bizCtxId;
    private int ctxSchemeValueId;

    public int getBizCtxValueId() {
        return bizCtxValueId;
    }

    public void setBizCtxValueId(int bizCtxValueId) {
        this.bizCtxValueId = bizCtxValueId;
    }

    public int getBizCtxId() {
        return bizCtxId;
    }

    public void setBizCtxId(int bizCtxId) {
        this.bizCtxId = bizCtxId;
    }

    public int getCtxSchemeValueId() {
        return ctxSchemeValueId;
    }

    public void setCtxSchemeValueId(int ctxSchemeValueId) {
        this.ctxSchemeValueId = ctxSchemeValueId;
    }
}
