package org.oagi.srt.repository.entity;

import java.io.Serializable;

public class BusinessContextValue implements Serializable {

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

    @Override
    public String toString() {
        return "BusinessContextValue{" +
                "bizCtxValueId=" + bizCtxValueId +
                ", bizCtxId=" + bizCtxId +
                ", ctxSchemeValueId=" + ctxSchemeValueId +
                '}';
    }
}
