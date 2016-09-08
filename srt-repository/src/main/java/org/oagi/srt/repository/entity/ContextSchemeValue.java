package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ctx_scheme_value")
public class ContextSchemeValue implements Serializable {

    public static final String SEQUENCE_NAME = "CTX_SCHEME_VALUE_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME)
    private long ctxSchemeValueId;

    @Column(nullable = false, length = 41)
    private String guid;

    @Column(nullable = false, length = 45)
    private String value;

    @Lob
    @Column(length = 10 * 1024)
    private String meaning;

    @Column
    private Long ownerCtxSchemeId;

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

    public long getOwnerCtxSchemeId() {
        return (ownerCtxSchemeId == null) ? 0L : ownerCtxSchemeId;
    }

    public void setOwnerCtxSchemeId(long ownerCtxSchemeId) {
        this.ownerCtxSchemeId = ownerCtxSchemeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContextSchemeValue that = (ContextSchemeValue) o;

        if (ctxSchemeValueId != that.ctxSchemeValueId) return false;
        if (guid != null ? !guid.equals(that.guid) : that.guid != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        if (meaning != null ? !meaning.equals(that.meaning) : that.meaning != null) return false;
        return ownerCtxSchemeId != null ? ownerCtxSchemeId.equals(that.ownerCtxSchemeId) : that.ownerCtxSchemeId == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (ctxSchemeValueId ^ (ctxSchemeValueId >>> 32));
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (meaning != null ? meaning.hashCode() : 0);
        result = 31 * result + (ownerCtxSchemeId != null ? ownerCtxSchemeId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ContextSchemeValue{" +
                "ctxSchemeValueId=" + ctxSchemeValueId +
                ", guid='" + guid + '\'' +
                ", value='" + value + '\'' +
                ", meaning='" + meaning + '\'' +
                ", ownerCtxSchemeId=" + ownerCtxSchemeId +
                '}';
    }
}
