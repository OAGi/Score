package org.oagi.srt.model.cc.impl;

import org.oagi.srt.model.*;
import org.oagi.srt.model.bie.Fetcher;
import org.oagi.srt.model.cc.ACCNode;
import org.oagi.srt.model.cc.ASCCPNode;
import org.oagi.srt.repository.entity.AssociationCoreComponent;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;

public class LazyASCCPNode extends AbstractLazyNode implements ASCCPNode, LazyCCNode {

    private ASCCPNode asccpNode;

    public LazyASCCPNode(ASCCPNode asccpNode, Fetcher fetcher, int childrenCount) {
        this(asccpNode, fetcher, childrenCount, null);
    }

    public LazyASCCPNode(ASCCPNode asccpNode, Fetcher fetcher, int childrenCount, Node parent) {
        super(asccpNode, fetcher, childrenCount, parent);
        this.asccpNode = asccpNode;
    }

    @Override
    public void accept(CCNodeVisitor visitor) {
        visitor.visitASCCPNode(this);
        if (isFetched()) {
            for (Node child : getChildren()) {
                ((CCNode) child).accept(visitor);
            }
        }
    }

    @Override
    public AssociationCoreComponentProperty getAsccp() {
        return asccpNode.getAsccp();
    }

    @Override
    public AssociationCoreComponent getAscc() {
        return asccpNode.getAscc();
    }

    @Override
    public void setRoleOfAcc(ACCNode roleOfAcc) {
        asccpNode.setRoleOfAcc(roleOfAcc);
    }

    @Override
    public ACCNode getRoleOfAcc() {
        return asccpNode.getRoleOfAcc();
    }
}
