package org.oagi.srt.export.model;

import org.oagi.srt.repository.entity.AggregateCoreComponent;

public class ACCComplexType implements ACC {

    private AggregateCoreComponent acc;
    private ACC basedAcc;

    public ACCComplexType(AggregateCoreComponent acc) {
        this.acc = acc;
    }

    public ACCComplexType(AggregateCoreComponent acc, ACC basedAcc) {
        this.acc = acc;
        this.basedAcc = basedAcc;
    }

    public int getRawId() {
        return acc.getAccId();
    }

    public String getName() {
        return acc.getObjectClassTerm()
                .replaceAll(" ", "")
                .replace("Identifier", "ID");
    }

    public boolean isAbstract() {
        return acc.isAbstract();
    }

    public String getGuid() {
        return acc.getGuid();
    }

    public ACC getBasedACC() {
        return basedAcc;
    }
}
