package org.oagi.srt.model.treenode;

import org.oagi.srt.repository.entity.*;

public interface AggregateBusinessInformationEntityTreeNode extends BusinessInformationEntityTreeNode {

    public AggregateBusinessInformationEntity getAggregateBusinessInformationEntity();

    public AggregateCoreComponent getAggregateCoreComponent();

    public BusinessContext getBusinessContext();
}
