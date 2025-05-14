package org.oagi.score.gateway.http.api.bie_management.model;

public enum BieState {

    Initiating(0),
    WIP(1),
    QA(2),
    Production(3);

    private final int level;

    BieState(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

}
