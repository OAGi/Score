package org.oagi.srt.export.model;

import org.oagi.srt.repository.entity.AggregateCoreComponent;

public class ACCGroup implements ACC {

    private AggregateCoreComponent acc;

    public ACCGroup(AggregateCoreComponent acc) {
        this.acc = acc;
    }

    public String getName() {
        return acc.getObjectClassTerm()
                .replaceAll(" ", "")
                .replace("Identifier", "ID");
    }
}
