package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ctx_scheme_value")
public class ContextSchemeValue implements Serializable {

    public static final String SEQUENCE_NAME = "CTX_SCHEME_VALUE_ID_SEQ";

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
    private int ctxSchemeValueId;

    @Column(nullable = false)
    private String guid;

    @Column(nullable = false)
    private String value;

    @Column
    private String meaning;

    @Column
    private Integer ownerCtxSchemeId;

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
        return (ownerCtxSchemeId == null) ? 0 : ownerCtxSchemeId;
    }

    public void setOwnerCtxSchemeId(int ownerCtxSchemeId) {
        this.ownerCtxSchemeId = ownerCtxSchemeId;
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
