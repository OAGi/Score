package org.oagi.score.gateway.http.api.cc_management.data;

public enum CcState {

    Editing(1),
    Candidate(2),
    Published(3);

    private final int value;

    CcState(int value) {
        this.value = value;
    }

    public static CcState valueOf(int value) {
        for (CcState state : CcState.values()) {
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
