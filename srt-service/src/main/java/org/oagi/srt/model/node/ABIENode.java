package org.oagi.srt.model.node;

import org.oagi.srt.repository.entity.AggregateBusinessInformationEntity;
import org.oagi.srt.repository.entity.AggregateCoreComponent;
import org.oagi.srt.repository.entity.BusinessContext;

public interface ABIENode extends BIENode {

    public AggregateBusinessInformationEntity getAbie();

    public AggregateCoreComponent getAcc();

    public BusinessContext getBusinessContext();
}
