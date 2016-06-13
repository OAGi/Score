package org.oagi.srt.repository.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "code_list_value")
public class CodeListValue implements Serializable {

    @Id
    @GeneratedValue(generator = "CODE_LIST_VALUE_ID_SEQ", strategy = GenerationType.AUTO)
    @SequenceGenerator(name = "CODE_LIST_VALUE_ID_SEQ", sequenceName = "CODE_LIST_VALUE_ID_SEQ", allocationSize = 1)
    private int codeListValueId;

    @Column
    private int codeListId;

    @Column(nullable = false)
    private String value;

    @Column
    private String name;

    @Column
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

    public int getCodeListValueId() {
        return codeListValueId;
    }

    public void setCodeListValueId(int codeListValueId) {
        this.codeListValueId = codeListValueId;
    }

    public int getCodeListId() {
        return codeListId;
    }

    public void setCodeListId(int codeListId) {
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
