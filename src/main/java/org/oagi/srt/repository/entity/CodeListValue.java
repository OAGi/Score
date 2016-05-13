package org.oagi.srt.repository.entity;

public class CodeListValue {

    private int codeListValueId;
    private int codeListId;
    private String value;
    private String name;
    private String definition;
    private String definitionSource;
    private boolean usedIndicator;
    private boolean lockedIndicator;
    private boolean extensionIndicator;

    private String color;
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
}
