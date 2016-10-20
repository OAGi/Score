package org.oagi.srt.model.cc.impl;

import org.oagi.srt.model.*;
import org.oagi.srt.model.bie.Fetcher;
import org.oagi.srt.model.cc.ACCNode;
import org.oagi.srt.model.cc.ASCCPNode;
import org.oagi.srt.model.cc.BCCPNode;
import org.oagi.srt.repository.entity.AggregateCoreComponent;

import java.util.List;

public class LazyACCNode extends AbstractLazyNode implements ACCNode, LazyCCNode {

    private ACCNode accNode;

    public LazyACCNode(ACCNode accNode, Fetcher fetcher, int childrenCount, Node parent) {
        super(accNode, fetcher, childrenCount, parent);
        this.accNode = accNode;
    }

    @Override
    public void accept(CCNodeVisitor visitor) {
        visitor.visitACCNode(this);
        if (isFetched()) {
            for (Node child : getChildren()) {
                ((CCNode) child).accept(visitor);
            }
        }
    }

    @Override
    public AggregateCoreComponent getAcc() {
        return accNode.getAcc();
    }

    @Override
    public void setBasedAcc(ACCNode basedAcc) {
        accNode.setBasedAcc(basedAcc);
    }

    @Override
    public ACCNode getBasedAcc() {
        return accNode.getBasedAcc();
    }

    @Override
    public void addASCCPNode(ASCCPNode asccpNode) {
        accNode.addASCCPNode(asccpNode);
    }

    @Override
    public void addBCCNode(BCCPNode bccNode) {
        accNode.addBCCNode(bccNode);
    }

    @Override
    public List<CCNode> getCCs() {
        return accNode.getCCs();
    }
}
