package org.oagi.srt.model.cc.impl;

import org.oagi.srt.model.*;
import org.oagi.srt.model.bie.Fetcher;
import org.oagi.srt.model.cc.ACCNode;
import org.oagi.srt.model.cc.ASCCPNode;
import org.oagi.srt.model.cc.BCCPNode;
import org.oagi.srt.repository.entity.*;

import java.util.List;

public class LazyBCCPNode extends AbstractLazyNode implements BCCPNode, LazyCCNode {

    private BCCPNode bccpNode;

    public LazyBCCPNode(BCCPNode bccpNode, Fetcher fetcher, int childrenCount, Node parent) {
        super(bccpNode, fetcher, childrenCount, parent);
        this.bccpNode = bccpNode;
    }

    @Override
    public void accept(CCNodeVisitor visitor) {
        visitor.visitBCCPNode(this);
        if (isFetched()) {
            for (Node child : getChildren()) {
                ((CCNode) child).accept(visitor);
            }
        }
    }

    @Override
    public BasicCoreComponent getBcc() {
        return bccpNode.getBcc();
    }

    @Override
    public BasicCoreComponentProperty getBccp() {
        return bccpNode.getBccp();
    }

    @Override
    public DataType getBdt() {
        return bccpNode.getBdt();
    }

    @Override
    public ACCNode getFromAcc() {
        return bccpNode.getFromAcc();
    }
}
