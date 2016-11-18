package org.oagi.srt.model.bie.impl;

import org.oagi.srt.model.*;
import org.oagi.srt.model.bie.ASBIENode;
import org.oagi.srt.model.bie.Fetcher;
import org.oagi.srt.repository.entity.AggregateBusinessInformationEntity;
import org.oagi.srt.repository.entity.AssociationBusinessInformationEntity;
import org.oagi.srt.repository.entity.AssociationBusinessInformationEntityProperty;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;

public class LazyASBIENode extends AbstractLazyNode implements ASBIENode, LazyBIENode {

    private ASBIENode asbieNode;

    public LazyASBIENode(ASBIENode asbieNode, Fetcher fetcher, int childrenCount, Node parent) {
        super(asbieNode, fetcher, childrenCount, parent);
        this.asbieNode = asbieNode;
    }

    @Override
    public void accept(BIENodeVisitor visitor) {
        visitor.visitASBIENode(this);
        if (isFetched()) {
            for (Node child : getChildren()) {
                ((BIENode) child).accept(visitor);
            }
        }
        if (getParent() == null) {
            visitor.endNode();
        }
    }

    public AssociationBusinessInformationEntity getAsbie() {
        return asbieNode.getAsbie();
    }

    public void setAsbie(AssociationBusinessInformationEntity asbie) {
        asbieNode.setAsbie(asbie);
    }

    public AssociationBusinessInformationEntityProperty getAsbiep() {
        return asbieNode.getAsbiep();
    }

    public void setAsbiep(AssociationBusinessInformationEntityProperty asbiep) {
        asbieNode.setAsbiep(asbiep);
    }

    public AssociationCoreComponentProperty getAsccp() {
        return asbieNode.getAsccp();
    }

    public void setAsccp(AssociationCoreComponentProperty asccp) {
        asbieNode.setAsccp(asccp);
    }

    public AggregateBusinessInformationEntity getAbie() {
        return asbieNode.getAbie();
    }

    public void setAbie(AggregateBusinessInformationEntity abie) {
        asbieNode.setAbie(abie);
    }
}
