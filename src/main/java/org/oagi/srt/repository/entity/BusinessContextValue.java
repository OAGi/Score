package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "biz_ctx_value")
public class BusinessContextValue implements Serializable {

    public static final String SEQUENCE_NAME = "BIZ_CTX_VALUE_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @GenericGenerator(
            name = SEQUENCE_NAME,
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = SEQUENCE_NAME),
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled-lo"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
            }
    )
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
        int result = bizCtxValueId;
        result = 31 * result + bizCtxId;
        result = 31 * result + ctxSchemeValueId;
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
