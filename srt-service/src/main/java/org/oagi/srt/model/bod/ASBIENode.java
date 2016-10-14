package org.oagi.srt.model.bod;

import org.oagi.srt.model.Node;
import org.oagi.srt.model.NodeVisitor;
import org.oagi.srt.repository.entity.AggregateBusinessInformationEntity;
import org.oagi.srt.repository.entity.AssociationBusinessInformationEntity;
import org.oagi.srt.repository.entity.AssociationBusinessInformationEntityProperty;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;

import java.util.ArrayList;
import java.util.List;

public class ASBIENode extends AbstractNode {

    private AssociationBusinessInformationEntity asbie;
    private AssociationBusinessInformationEntityProperty asbiep;
    private AssociationCoreComponentProperty asccp;
    private AggregateBusinessInformationEntity abie;
    private List<Node> children = new ArrayList();

    public ASBIENode(int seqKey, Node parent,
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
    public void accept(NodeVisitor visitor) {
        visitor.visitASBIENode(this);
        for (Node node : getChildren()) {
            node.accept(visitor);
        }
    }
}
