package org.oagi.srt.export.model;

import org.oagi.srt.repository.entity.AggregateCoreComponent;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;

public class ASCCPGroup extends ASCCP {

    ASCCPGroup(AssociationCoreComponentProperty asccp, AggregateCoreComponent roleOfAcc) {
        super(asccp, roleOfAcc);
    }
}
