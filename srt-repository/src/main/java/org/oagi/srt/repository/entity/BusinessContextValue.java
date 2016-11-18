package org.oagi.srt.repository.entity;

import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "biz_ctx_value")
@org.hibernate.annotations.Cache(region = "", usage = CacheConcurrencyStrategy.READ_WRITE)
public class BusinessContextValue implements Serializable {

    public static final String SEQUENCE_NAME = "BIZ_CTX_VALUE_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    private long bizCtxValueId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "biz_ctx_id", nullable = false)
    private BusinessContext businessContext;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ctx_scheme_value_id", nullable = false)
    private ContextSchemeValue contextSchemeValue;

    public long getBizCtxValueId() {
        return bizCtxValueId;
    }

    public void setBizCtxValueId(long bizCtxValueId) {
        this.bizCtxValueId = bizCtxValueId;
    }

    public BusinessContext getBusinessContext() {
        return businessContext;
    }

    public void setBusinessContext(BusinessContext businessContext) {
        this.businessContext = businessContext;
    }

    public ContextSchemeValue getContextSchemeValue() {
        return contextSchemeValue;
    }

    public void setContextSchemeValue(ContextSchemeValue contextSchemeValue) {
        this.contextSchemeValue = contextSchemeValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BusinessContextValue that = (BusinessContextValue) o;

        if (bizCtxValueId != 0L && bizCtxValueId == that.bizCtxValueId) return true;
        return false;
    }

    @Override
    public int hashCode() {
        int result = (int) (bizCtxValueId ^ (bizCtxValueId >>> 32));
        result = 31 * result + (businessContext != null ? businessContext.hashCode() : 0);
        result = 31 * result + (contextSchemeValue != null ? contextSchemeValue.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BusinessContextValue{" +
                "bizCtxValueId=" + bizCtxValueId +
                ", businessContext=" + businessContext +
                ", contextSchemeValue=" + contextSchemeValue +
                '}';
    }
}
