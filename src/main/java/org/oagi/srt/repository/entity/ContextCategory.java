package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ctx_category")
public class ContextCategory implements Serializable {

    @Id
    @GeneratedValue(generator = "CTX_CATEGORY_ID_SEQ", strategy = GenerationType.AUTO)
    @SequenceGenerator(name = "CTX_CATEGORY_ID_SEQ", sequenceName = "CTX_CATEGORY_ID_SEQ", allocationSize = 1)
    private int ctxCategoryId;

    @Column(nullable = false)
    private String guid;

    @Column
    private String name;

    @Column
    private String description;

    public int getCtxCategoryId() {
        return ctxCategoryId;
    }

    public void setCtxCategoryId(int ctxCategoryId) {
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
    public String toString() {
        return "ContextCategory{" +
                "ctxCategoryId=" + ctxCategoryId +
                ", guid='" + guid + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
