package org.oagi.srt.model.node;

public interface BIENodeVisitor {

    public void visit(ABIENode abieNode);

    public void visit(ASBIEPNode asbiepNode);

    public void visit(BBIEPNode bbiepNode);

    public void visit(BBIESCNode bbieScNode);
}
