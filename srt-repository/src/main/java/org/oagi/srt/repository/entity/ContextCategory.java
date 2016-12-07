package org.oagi.srt.repository.entity;

import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ctx_category")
@org.hibernate.annotations.Cache(region = "", usage = CacheConcurrencyStrategy.READ_WRITE)
public class ContextCategory implements Serializable {

    public static final String SEQUENCE_NAME = "CTX_CATEGORY_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    private long ctxCategoryId;

    @Column(nullable = false, length = 41)
    private String guid;

    @Column(length = 45)
    private String name;

    @Lob
    @Column(length = 10 * 1024)
    private String description;

    public long getCtxCategoryId() {
        return ctxCategoryId;
    }

    public void setCtxCategoryId(long ctxCategoryId) {
        this.ctxCategoryId = ctxCategoryId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContextCategory that = (ContextCategory) o;

        if (ctxCategoryId != 0L && ctxCategoryId == that.ctxCategoryId) return true;
        if (guid != null) {
            if (guid.equals(that.guid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = (int) (ctxCategoryId ^ (ctxCategoryId >>> 32));
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ContextCategory{" +
                "ctxCategoryId=" + ctxCategoryId +
                ", guid='" + guid + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
