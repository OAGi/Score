package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "biz_ctx_value")
public class BusinessContextValue implements Serializable {

    @Id
    @GeneratedValue(generator = "BIZ_CTX_VALUE_ID_SEQ", strategy = GenerationType.AUTO)
    @SequenceGenerator(name = "BIZ_CTX_VALUE_ID_SEQ", sequenceName = "BIZ_CTX_VALUE_ID_SEQ", allocationSize = 1)
    private int bizCtxValueId;

    @Column(nullable = false)
    private int bizCtxId;

    @Column(nullable = false)
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
