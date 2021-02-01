package org.oagi.score.service.common.data;

public enum BCCEntityType {

    Attribute(0),
    Element(1);

    private final int value;

    BCCEntityType(int value) {
        this.value = value;
    }

    public static BCCEntityType valueOf(int value) {
        for (BCCEntityType state : BCCEntityType.values()) {
            if (state.getValue() == value) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }

    public int getValue() {
        return value;
    }

}
