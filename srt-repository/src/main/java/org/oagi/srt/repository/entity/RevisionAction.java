package org.oagi.srt.repository.entity;

public enum RevisionAction {

    Insert(1),
    Update(2),
    Delete(3);

    private final int value;

    RevisionAction(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static RevisionAction valueOf(int value) {
        for (RevisionAction state : RevisionAction.values()) {
            if (state.getValue() == value) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }
}
