package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "code_list_value")
public class CodeListValue implements Serializable {

    public static final String SEQUENCE_NAME = "CODE_LIST_VALUE_ID_SEQ";

    @Id
    @GeneratedValue(generator = SEQUENCE_NAME, strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME)
    private long codeListValueId;

    @Column
    private long codeListId;

    @Column(nullable = false, length = 100)
    private String value;

    @Column(length = 100)
    private String name;

    @Lob
    @Column(length = 10 * 1024)
    private String definition;

    @Column
    private String definitionSource;

    @Column
    private boolean usedIndicator = true;

    @Column
    private boolean lockedIndicator;

    @Column
    private boolean extensionIndicator;

    @Transient
    private String color;

    @Transient
    private boolean disabled;

    public long getCodeListValueId() {
        return codeListValueId;
    }

    public void setCodeListValueId(long codeListValueId) {
        this.codeListValueId = codeListValueId;
    }

    public long getCodeListId() {
        return codeListId;
    }

    public void setCodeListId(long codeListId) {
        this.codeListId = codeListId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getDefinitionSource() {
        return definitionSource;
    }

    public void setDefinitionSource(String definitionSource) {
        this.definitionSource = definitionSource;
    }

    public boolean isUsedIndicator() {
        return usedIndicator;
    }

    public void setUsedIndicator(boolean usedIndicator) {
        this.usedIndicator = usedIndicator;
    }

    public boolean isLockedIndicator() {
        return lockedIndicator;
    }

    public void setLockedIndicator(boolean lockedIndicator) {
        this.lockedIndicator = lockedIndicator;
    }

    public boolean isExtensionIndicator() {
        return extensionIndicator;
    }

    public void setExtensionIndicator(boolean extensionIndicator) {
        this.extensionIndicator = extensionIndicator;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CodeListValue that = (CodeListValue) o;

        if (codeListValueId != that.codeListValueId) return false;
        if (codeListId != that.codeListId) return false;
        if (usedIndicator != that.usedIndicator) return false;
        if (lockedIndicator != that.lockedIndicator) return false;
        if (extensionIndicator != that.extensionIndicator) return false;
        if (disabled != that.disabled) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (definition != null ? !definition.equals(that.definition) : that.definition != null) return false;
        if (definitionSource != null ? !definitionSource.equals(that.definitionSource) : that.definitionSource != null)
            return false;
        return color != null ? color.equals(that.color) : that.color == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (codeListValueId ^ (codeListValueId >>> 32));
        result = 31 * result + (int) (codeListId ^ (codeListId >>> 32));
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (definition != null ? definition.hashCode() : 0);
        result = 31 * result + (definitionSource != null ? definitionSource.hashCode() : 0);
        result = 31 * result + (usedIndicator ? 1 : 0);
        result = 31 * result + (lockedIndicator ? 1 : 0);
        result = 31 * result + (extensionIndicator ? 1 : 0);
        result = 31 * result + (color != null ? color.hashCode() : 0);
        result = 31 * result + (disabled ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CodeListValue{" +
                "codeListValueId=" + codeListValueId +
                ", codeListId=" + codeListId +
                ", value='" + value + '\'' +
                ", name='" + name + '\'' +
                ", definition='" + definition + '\'' +
                ", definitionSource='" + definitionSource + '\'' +
                ", usedIndicator=" + usedIndicator +
                ", lockedIndicator=" + lockedIndicator +
                ", extensionIndicator=" + extensionIndicator +
                ", color='" + color + '\'' +
                ", disabled=" + disabled +
                '}';
    }
}
