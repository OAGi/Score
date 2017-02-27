package org.oagi.srt.model.node;

import org.oagi.srt.repository.entity.AggregateCoreComponent;

public interface ACCNode extends CCNode {

    public AggregateCoreComponent getAcc();

    public ACCNode getBase();

}
