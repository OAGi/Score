package org.oagi.srt.model.bie.impl;

import org.oagi.srt.model.AbstractBaseNode;
import org.oagi.srt.model.BIENode;
import org.oagi.srt.model.BIENodeVisitor;
import org.oagi.srt.model.Node;
import org.oagi.srt.model.bie.ASBIENode;
import org.oagi.srt.model.bie.BBIENode;
import org.oagi.srt.repository.entity.AggregateBusinessInformationEntity;
import org.oagi.srt.repository.entity.AssociationBusinessInformationEntity;
import org.oagi.srt.repository.entity.AssociationBusinessInformationEntityProperty;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;

import java.util.ArrayList;
import java.util.List;

public class BaseASBIENode extends AbstractBaseNode implements ASBIENode {

    private AssociationBusinessInformationEntity asbie;
    private AssociationBusinessInformationEntityProperty asbiep;
    private AssociationCoreComponentProperty asccp;
    private AggregateBusinessInformationEntity abie;
    private List<Node> children = new ArrayList();

    public BaseASBIENode(int seqKey, Node parent,
                         AssociationBusinessInformationEntity asbie,
                         AssociationBusinessInformationEntityProperty asbiep,
                         AssociationCoreComponentProperty asccp,
                         AggregateBusinessInformationEntity abie) {
        super(seqKey, parent);
        this.asbie = asbie;
        this.asbiep = asbiep;
        this.asccp = asccp;
        this.abie = abie;
    }

    @Override
    public boolean isUsed() {
        return getAsbie().isUsed();
    }

    @Override
    public void setUsed(boolean used) {
        getAsbie().setUsed(used);

        if (used) {
            BIENode parent = (BIENode) getParent();
            if (parent != null && !parent.isUsed()) {
                parent.setUsed(used);
            }
        } else {
            for (Node node : getChildren()) {
                ((BIENode) node).setUsed(used);
            }
        }
    }

    public AssociationBusinessInformationEntity getAsbie() {
        return asbie;
    }

    public void setAsbie(AssociationBusinessInformationEntity asbie) {
        this.asbie = asbie;
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

    @Override
    public String getName() {
        return asccp.getPropertyTerm();
    }

    @Override
    public String getType() {
        return "ASBIE";
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
        visitor.visitASBIENode(this);
        for (Node child : getChildren()) {
            ((BIENode) child).accept(visitor);
        }
        if (getParent() == null) {
            visitor.endNode();
        }
    }
}
