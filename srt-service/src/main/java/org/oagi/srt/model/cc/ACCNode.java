package org.oagi.srt.model.cc;

import org.oagi.srt.model.CCNode;
import org.oagi.srt.repository.entity.AggregateCoreComponent;

import java.util.List;

public interface ACCNode extends CCNode {

    public AggregateCoreComponent getAcc();
    public void setBasedAcc(ACCNode basedAcc);
    public ACCNode getBasedAcc();

    public void addASCCPNode(ASCCPNode asccpNode);
    public void addBCCNode(BCCPNode bccNode);
    public List<CCNode> getCCs();

}
