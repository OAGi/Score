package org.oagi.srt.repository.entity;

public enum DataTypeType {

    CoreDataType(0),
    BusinessDataType(1);

    private int value;

    DataTypeType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DataTypeType valueOf(int value) {
        for (DataTypeType state : DataTypeType.values()) {
            if (state.getValue() == value) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }


}
