package org.oagi.srt.repository.entity;

import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ctx_scheme_value")
@org.hibernate.annotations.Cache(region = "", usage = CacheConcurrencyStrategy.READ_WRITE)
public class ContextSchemeValue implements Serializable {

    public static final String SEQUENCE_NAME = "CTX_SCHEME_VALUE_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    private long ctxSchemeValueId;

    @Column(nullable = false, length = 41)
    private String guid;

    @Column(nullable = false, length = 45)
    private String value;

    @Lob
    @Column(length = 10 * 1024)
    private String meaning;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_ctx_scheme_id", nullable = false)
    private ContextScheme contextScheme;

    public long getCtxSchemeValueId() {
        return ctxSchemeValueId;
    }

    public void setCtxSchemeValueId(long ctxSchemeValueId) {
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

    public ContextScheme getContextScheme() {
        return contextScheme;
    }

    public void setContextScheme(ContextScheme contextScheme) {
        this.contextScheme = contextScheme;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContextSchemeValue that = (ContextSchemeValue) o;

        if (ctxSchemeValueId != 0L && ctxSchemeValueId == that.ctxSchemeValueId) return true;
        if (guid != null) {
            if (guid.equals(that.guid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = (int) (ctxSchemeValueId ^ (ctxSchemeValueId >>> 32));
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (meaning != null ? meaning.hashCode() : 0);
        result = 31 * result + (contextScheme != null ? contextScheme.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ContextSchemeValue{" +
                "ctxSchemeValueId=" + ctxSchemeValueId +
                ", guid='" + guid + '\'' +
                ", value='" + value + '\'' +
                ", meaning='" + meaning + '\'' +
                ", contextScheme=" + contextScheme +
                '}';
    }
}
