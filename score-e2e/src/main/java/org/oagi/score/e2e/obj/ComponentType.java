package org.oagi.score.e2e.obj;

public enum ComponentType {

    Base(0),
    Semantics(1),
    Extension(2),
    SemanticGroup(3),
    UserExtensionGroup(4),
    Embedded(5),
    OAGIS10Nouns(6),
    OAGIS10BODs(7),
    BOD(8),
    Verb(9),
    Noun(10);

    private final int value;

    ComponentType(int value) {
        this.value = value;
    }

    public static ComponentType valueOf(int value) {
        for (ComponentType state : ComponentType.values()) {
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
