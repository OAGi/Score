package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "biz_ctx_value")
public class BusinessContextValue implements Serializable {

    public static final String SEQUENCE_NAME = "BIZ_CTX_VALUE_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME)
    private long bizCtxValueId;

    @Column(nullable = false)
    private long bizCtxId;

    @Column(nullable = false)
    private long ctxSchemeValueId;

    public long getBizCtxValueId() {
        return bizCtxValueId;
    }

    public void setBizCtxValueId(long bizCtxValueId) {
        this.bizCtxValueId = bizCtxValueId;
    }

    public long getBizCtxId() {
        return bizCtxId;
    }

    public void setBizCtxId(long bizCtxId) {
        this.bizCtxId = bizCtxId;
    }

    public long getCtxSchemeValueId() {
        return ctxSchemeValueId;
    }

    public void setCtxSchemeValueId(long ctxSchemeValueId) {
        this.ctxSchemeValueId = ctxSchemeValueId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BusinessContextValue that = (BusinessContextValue) o;

        if (bizCtxValueId != that.bizCtxValueId) return false;
        if (bizCtxId != that.bizCtxId) return false;
        return ctxSchemeValueId == that.ctxSchemeValueId;

    }

    @Override
    public int hashCode() {
        int result = (int) (bizCtxValueId ^ (bizCtxValueId >>> 32));
        result = 31 * result + (int) (bizCtxId ^ (bizCtxId >>> 32));
        result = 31 * result + (int) (ctxSchemeValueId ^ (ctxSchemeValueId >>> 32));
        return result;
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
