package org.oagi.srt.repository.entity;

public enum CoreComponentState {

    Editing(1),
    Candidate(2),
    Published(3);

    private final int value;

    CoreComponentState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static CoreComponentState valueOf(int value) {
        for (CoreComponentState state : CoreComponentState.values()) {
            if (state.getValue() == value) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }
    
}
