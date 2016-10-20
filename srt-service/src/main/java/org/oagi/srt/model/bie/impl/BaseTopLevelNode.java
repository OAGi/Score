package org.oagi.srt.model.bie.impl;

import org.oagi.srt.model.AbstractBaseNode;
import org.oagi.srt.model.BIENode;
import org.oagi.srt.model.BIENodeVisitor;
import org.oagi.srt.model.Node;
import org.oagi.srt.model.bie.ASBIENode;
import org.oagi.srt.model.bie.BBIENode;
import org.oagi.srt.model.bie.TopLevelNode;
import org.oagi.srt.repository.entity.AggregateBusinessInformationEntity;
import org.oagi.srt.repository.entity.AssociationBusinessInformationEntityProperty;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;
import org.oagi.srt.repository.entity.BusinessContext;

import java.util.ArrayList;
import java.util.List;

public class BaseTopLevelNode extends AbstractBaseNode implements TopLevelNode {

    private AssociationBusinessInformationEntityProperty asbiep;
    private AssociationCoreComponentProperty asccp;
    private AggregateBusinessInformationEntity abie;
    private BusinessContext bizCtx;
    private List<Node> children = new ArrayList();

    public BaseTopLevelNode(AssociationBusinessInformationEntityProperty asbiep,
                            AssociationCoreComponentProperty asccp,
                            AggregateBusinessInformationEntity abie,
                            BusinessContext bizCtx) {
        super(0);
        this.asbiep = asbiep;
        this.asccp = asccp;
        this.abie = abie;
        this.bizCtx = bizCtx;
    }

    public AssociationBusinessInformationEntityProperty getAsbiep() {
        return asbiep;
    }

    public void setAsbiep(AssociationBusinessInformationEntityProperty asbiep) {
        this.asbiep = asbiep;
    }

    public AssociationCoreComponentProperty getAsccp() {
        return asccp;
    }

    public void setAsccp(AssociationCoreComponentProperty asccp) {
        this.asccp = asccp;
    }

    public AggregateBusinessInformationEntity getAbie() {
        return abie;
    }

    public void setAbie(AggregateBusinessInformationEntity abie) {
        this.abie = abie;
    }

    public BusinessContext getBizCtx() {
        return bizCtx;
    }

    public void setBizCtx(BusinessContext bizCtx) {
        this.bizCtx = bizCtx;
    }

    @Override
    public String getName() {
        return asccp.getPropertyTerm();
    }

    @Override
    public <T extends Node> void addChild(T child) {
        if (child instanceof BBIENode || child instanceof ASBIENode) {
            children.add(child);
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public List<? extends Node> getChildren() {
        return children;
    }

    @Override
    public void clearChildren() {
        children.clear();
    }

    @Override
    public void accept(BIENodeVisitor visitor) {
        visitor.startNode(this);
        for (Node child : getChildren()) {
            ((BIENode) child).accept(visitor);
        }
        visitor.endNode();
    }
}
