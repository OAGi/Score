package org.oagi.srt.model.cc;

import org.oagi.srt.model.CCNode;
import org.oagi.srt.repository.entity.AggregateCoreComponent;

import java.util.List;

public interface ACCNode extends CCNode {

    public AggregateCoreComponent getACC();
    public void setBasedACC(ACCNode basedAcc);
    public ACCNode getBasedACC();

    public void addASCCPNode(ASCCPNode asccpNode);
    public void addBCCNode(BCCPNode bccNode);
    public List<CCNode> getCCs();

}
