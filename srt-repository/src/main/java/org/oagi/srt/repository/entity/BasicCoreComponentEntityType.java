package org.oagi.srt.repository.entity;

public enum BasicCoreComponentEntityType {

    Attribute(0),
    Element(1);

    private final int value;

    BasicCoreComponentEntityType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static BasicCoreComponentEntityType valueOf(int value) {
        for (BasicCoreComponentEntityType state : BasicCoreComponentEntityType.values()) {
            if (state.getValue() == value) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }
}
