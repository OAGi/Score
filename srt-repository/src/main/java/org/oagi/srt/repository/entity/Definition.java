package org.oagi.srt.repository.entity;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "definition")
public class Definition implements Serializable {

    public static final String SEQUENCE_NAME = "DEFINITION_ID_SEQ";

    public static final String DEFAULT_LANGUAGE_CODE = "en";

    @Id
    @GenericGenerator(
            name = SEQUENCE_NAME,
            strategy = "org.oagi.srt.repository.support.jpa.ByDialectIdentifierGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = SEQUENCE_NAME),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1")
            }
    )
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.AUTO)
    private Long definitionId;

    @Lob
    @Column(length = 10 * 1024)
    private String definition;

    @Column(length = 200)
    private String definitionSource;

    @Column(nullable = false)
    private Long refId;

    @Column(nullable = false, length = 50)
    private String refTableName;

    @Column(nullable = false, length = 20)
    private String languageCode = DEFAULT_LANGUAGE_CODE;

    public Long getDefinitionId() {
        return definitionId;
    }

    public void setDefinitionId(Long definitionId) {
        this.definitionId = definitionId;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        if (definition != null) {
            definition = definition.trim();
        }
        if (StringUtils.isEmpty(definition)) {
            definition = null;
        }
        this.definition = definition;
    }

    public String getDefinitionSource() {
        return definitionSource;
    }

    public void setDefinitionSource(String definitionSource) {
        this.definitionSource = definitionSource;
    }

    public Long getRefId() {
        return refId;
    }

    public void setRefId(Long refId) {
        this.refId = refId;
    }

    public String getRefTableName() {
        return refTableName;
    }

    public void setRefTableName(String refTableName) {
        this.refTableName = refTableName;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Definition that = (Definition) o;

        if (definitionId != null ? !definitionId.equals(that.definitionId) : that.definitionId != null) return false;
        if (definition != null ? !definition.equals(that.definition) : that.definition != null) return false;
        if (definitionSource != null ? !definitionSource.equals(that.definitionSource) : that.definitionSource != null)
            return false;
        if (refId != null ? !refId.equals(that.refId) : that.refId != null) return false;
        if (refTableName != null ? !refTableName.equals(that.refTableName) : that.refTableName != null) return false;
        return languageCode != null ? languageCode.equals(that.languageCode) : that.languageCode == null;
    }

    @Override
    public int hashCode() {
        int result = definitionId != null ? definitionId.hashCode() : 0;
        result = 31 * result + (definitionId != null ? definitionId.hashCode() : 0);
        result = 31 * result + (definitionSource != null ? definitionSource.hashCode() : 0);
        result = 31 * result + (refId != null ? refId.hashCode() : 0);
        result = 31 * result + (refTableName != null ? refTableName.hashCode() : 0);
        result = 31 * result + (languageCode != null ? languageCode.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Definition{" +
                "definitionId=" + definitionId +
                ", definition='" + definition + '\'' +
                ", definitionSource='" + definitionSource + '\'' +
                ", refId=" + refId +
                ", refTableName='" + refTableName + '\'' +
                ", languageCode='" + languageCode + '\'' +
                '}';
    }

    @Override
    public Definition clone() {
        Definition clone = new Definition();
        clone.definition = this.definition;
        clone.definitionSource = this.definitionSource;
        clone.refId = refId;
        clone.refTableName = refTableName;
        clone.languageCode = languageCode;
        return clone;
    }

    @Transient
    private int hashCodeAfterLoaded;

    @PostLoad
    public void afterLoaded() {
        hashCodeAfterLoaded = hashCode();
    }

    public boolean isDirty() {
        return hashCodeAfterLoaded != hashCode();
    }
}
