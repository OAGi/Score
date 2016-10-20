package org.oagi.srt.model.cc.impl;

import org.oagi.srt.model.AbstractBaseNode;
import org.oagi.srt.model.CCNode;
import org.oagi.srt.model.CCNodeVisitor;
import org.oagi.srt.model.Node;
import org.oagi.srt.model.cc.ACCNode;
import org.oagi.srt.model.cc.ASCCPNode;
import org.oagi.srt.repository.entity.AssociationCoreComponent;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;

import java.util.Arrays;
import java.util.List;

public class BaseASCCPNode extends AbstractBaseNode implements ASCCPNode {

    private final AssociationCoreComponentProperty asccp;
    private AssociationCoreComponent ascc;
    private ACCNode roleOfAcc;

    public BaseASCCPNode(AssociationCoreComponentProperty asccp) {
        super(0, null);

        if (asccp == null) {
            throw new IllegalArgumentException("'asccp' argument must not be null.");
        }
        this.asccp = asccp;
    }

    public BaseASCCPNode(ACCNode parent, AssociationCoreComponent ascc, AssociationCoreComponentProperty asccp) {
        super(ascc.getSeqKey(), parent);

        if (ascc == null) {
            throw new IllegalArgumentException("'ascc' argument must not be null.");
        }
        this.ascc = ascc;

        if (asccp == null) {
            throw new IllegalArgumentException("'asccp' argument must not be null.");
        }
        this.asccp = asccp;
    }

    @Override
    public String getName() {
        return asccp.getPropertyTerm();
    }

    @Override
    public AssociationCoreComponentProperty getAsccp() {
        return asccp;
    }

    @Override
    public AssociationCoreComponent getAscc() {
        return ascc;
    }

    @Override
    public void setRoleOfAcc(ACCNode roleOfAcc) {
        if (roleOfAcc != null) {
            this.roleOfAcc = roleOfAcc;

            if (asccp.getRoleOfAccId() != roleOfAcc.getAcc().getAccId()) {
                throw new IllegalArgumentException("ACC ID doesn't match between parent and itself.");
            }
        }
    }

    @Override
    public ACCNode getRoleOfAcc() {
        return roleOfAcc;
    }

    @Override
    public ACCNode getType() {
        return roleOfAcc;
    }

    @Override
    public List<? extends Node> getChildren() {
        return Arrays.asList(roleOfAcc);
    }

    @Override
    public void accept(CCNodeVisitor visitor) {
        visitor.visitASCCPNode(this);
        for (Node child : getChildren()) {
            ((CCNode) child).accept(visitor);
        }
    }
}
