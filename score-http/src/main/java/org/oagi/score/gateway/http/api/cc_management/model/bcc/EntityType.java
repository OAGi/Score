package org.oagi.score.gateway.http.api.cc_management.model.bcc;

public enum EntityType {

    Attribute(0),
    Element(1);

    private final int value;

    EntityType(int value) {
        this.value = value;
    }

    public static EntityType valueOf(int value) {
        for (EntityType state : EntityType.values()) {
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
