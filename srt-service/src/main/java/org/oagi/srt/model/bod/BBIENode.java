package org.oagi.srt.model.bod;

import org.oagi.srt.model.Node;
import org.oagi.srt.model.NodeVisitor;
import org.oagi.srt.repository.entity.*;

import java.util.ArrayList;
import java.util.List;

public class BBIENode extends AbstractNode {

    private BasicBusinessInformationEntity bbie;
    private BasicBusinessInformationEntityProperty bbiep;
    private BasicCoreComponentProperty bccp;
    private DataType bdt;
    private List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList;
    private List<Node> children = new ArrayList();

    public BBIENode(int seqKey, Node parent,
                    BasicBusinessInformationEntity bbie,
                    BasicBusinessInformationEntityProperty bbiep,
                    BasicCoreComponentProperty bccp,
                    DataType bdt,
                    List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList) {
        super(seqKey, parent);
        this.bbie = bbie;
        this.bbiep = bbiep;
        this.bccp = bccp;
        this.bdt = bdt;
        this.bdtPriRestriList = bdtPriRestriList;
    }

    public BasicBusinessInformationEntity getBbie() {
        return bbie;
    }

    public void setBbie(BasicBusinessInformationEntity bbie) {
        this.bbie = bbie;
    }

    public BasicBusinessInformationEntityProperty getBbiep() {
        return bbiep;
    }

    public void setBbiep(BasicBusinessInformationEntityProperty bbiep) {
        this.bbiep = bbiep;
    }

    public BasicCoreComponentProperty getBccp() {
        return bccp;
    }

    public void setBccp(BasicCoreComponentProperty bccp) {
        this.bccp = bccp;
    }

    public DataType getBdt() {
        return bdt;
    }

    public void setBdt(DataType bdt) {
        this.bdt = bdt;
    }

    public void setBdtPrimitiveRestrictionId(long bdtPrimitiveRestrictionId) {
        bbie.setBdtPriRestriId(bdtPrimitiveRestrictionId);
    }

    public long getBdtPrimitiveRestrictionId() {
        return bbie.getBdtPriRestriId();
    }

    public List<BusinessDataTypePrimitiveRestriction> getBdtPriRestriList() {
        return bdtPriRestriList;
    }

    public void setBdtPriRestriList(List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList) {
        this.bdtPriRestriList = bdtPriRestriList;
    }

    @Override
    public String getName() {
        return bccp.getPropertyTerm();
    }

    @Override
    public <T extends Node> void addChild(T child) {
        if (child instanceof BBIESCNode) {
            children.add(child);
        } else {
            throw new IllegalStateException();
        }
    }

    public List<? extends Node> getChildren() {
        return children;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visitBBIENode(this);
        for (Node child : getChildren()) {
            child.accept(visitor);
        }
    }
}
