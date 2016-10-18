package org.oagi.srt.model.bod.impl;

import org.oagi.srt.model.Node;
import org.oagi.srt.model.NodeVisitor;
import org.oagi.srt.model.bod.BBIENode;
import org.oagi.srt.model.bod.Fetcher;
import org.oagi.srt.repository.entity.*;

import java.util.List;

public class LazyBBIENode extends AbstractLazyNode implements BBIENode {

    private BBIENode bbieNode;

    public LazyBBIENode(BBIENode bbieNode, Fetcher fetcher, int childrenCount, Node parent) {
        super(bbieNode, fetcher, childrenCount, parent);
        this.bbieNode = bbieNode;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visitBBIENode(this);
        if (isFetched()) {
            for (Node child : getChildren()) {
                child.accept(visitor);
            }
        }
    }

    public BasicBusinessInformationEntity getBbie() {
        return bbieNode.getBbie();
    }

    public void setBbie(BasicBusinessInformationEntity bbie) {
        bbieNode.setBbie(bbie);
    }

    public BasicBusinessInformationEntityProperty getBbiep() {
        return bbieNode.getBbiep();
    }

    public void setBbiep(BasicBusinessInformationEntityProperty bbiep) {
        bbieNode.setBbiep(bbiep);
    }

    public BasicCoreComponentProperty getBccp() {
        return bbieNode.getBccp();
    }

    public void setBccp(BasicCoreComponentProperty bccp) {
        bbieNode.setBccp(bccp);
    }

    public DataType getBdt() {
        return bbieNode.getBdt();
    }

    public void setBdt(DataType bdt) {
        bbieNode.setBdt(bdt);
    }

    public void setBdtPrimitiveRestrictionId(long bdtPrimitiveRestrictionId) {
        bbieNode.setBdtPrimitiveRestrictionId(bdtPrimitiveRestrictionId);
    }

    public long getBdtPrimitiveRestrictionId() {
        return bbieNode.getBdtPrimitiveRestrictionId();
    }

    public List<BusinessDataTypePrimitiveRestriction> getBdtPriRestriList() {
        return bbieNode.getBdtPriRestriList();
    }

    public void setBdtPriRestriList(List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList) {
        bbieNode.setBdtPriRestriList(bdtPriRestriList);
    }
}
