package org.oagi.score.repo.api.bie.model;

public enum BiePackageState {

    WIP(1),
    QA(2),
    Production(3);

    private final int level;

    BiePackageState(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

}
