package org.oagi.srt.model.treenode;

public interface BusinessInformationEntityTreeNodeVisitor {

    public void visit(AggregateBusinessInformationEntityTreeNode abieNode);

    public void visit(AssociationBusinessInformationEntityPropertyTreeNode asbiepNode);

    public void visit(BasicBusinessInformationEntityPropertyTreeNode bbiepNode);

    public void visit(BasicBusinessInformationEntitySupplementaryComponentTreeNode bbieScNode);
}
