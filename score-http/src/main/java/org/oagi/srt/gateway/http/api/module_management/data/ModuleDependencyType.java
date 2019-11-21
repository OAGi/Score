package org.oagi.srt.gateway.http.api.module_management.data;

public enum ModuleDependencyType {

    Include(0),
    Import(1);

    private final int value;

    ModuleDependencyType(int value) {
        this.value = value;
    }

    public static ModuleDependencyType valueOf(int value) {
        for (ModuleDependencyType state : ModuleDependencyType.values()) {
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
