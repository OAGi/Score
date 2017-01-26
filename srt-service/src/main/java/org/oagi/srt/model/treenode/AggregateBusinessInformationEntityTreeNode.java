package org.oagi.srt.model.treenode;

import org.oagi.srt.repository.entity.*;

public interface AggregateBusinessInformationEntityTreeNode extends BusinessInformationEntityTreeNode {

    public AggregateBusinessInformationEntity getAggregateBusinessInformationEntity();
    public AggregateBusinessInformationEntity getAbie();

    public AggregateCoreComponent getAggregateCoreComponent();
    public AggregateCoreComponent getAcc();

    public BusinessContext getBusinessContext();
}
