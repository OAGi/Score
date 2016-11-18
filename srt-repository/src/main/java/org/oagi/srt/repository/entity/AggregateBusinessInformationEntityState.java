package org.oagi.srt.repository.entity;

public enum AggregateBusinessInformationEntityState {

    Editing(2),
    Candidate(3),
    Published(4);

    private final int value;

    AggregateBusinessInformationEntityState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AggregateBusinessInformationEntityState valueOf(int value) {
        for (AggregateBusinessInformationEntityState state : AggregateBusinessInformationEntityState.values()) {
            if (state.getValue() == value) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }

}
