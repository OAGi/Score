package org.oagi.score.data;

public enum OagisComponentType {

    Base(0),
    Semantics(1),
    Extension(2),
    SemanticGroup(3),
    UserExtensionGroup(4),
    Embedded(5),
    OAGIS10Nouns(6),
    OAGIS10BODs(7);

    private final int value;

    OagisComponentType(int value) {
        this.value = value;
    }

    public static OagisComponentType valueOf(int value) {
        for (OagisComponentType state : OagisComponentType.values()) {
            if (state.getValue() == value) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }

    public int getValue() {
        return value;
    }

    public boolean isGroup() {
        return this == SemanticGroup || this == UserExtensionGroup;
    }
}
