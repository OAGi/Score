package org.oagi.srt.model.bie.visitor;

import org.oagi.srt.model.BIENode;
import org.oagi.srt.model.BIENodeVisitor;
import org.oagi.srt.model.Node;
import org.oagi.srt.model.bie.ASBIENode;
import org.oagi.srt.model.bie.BBIENode;
import org.oagi.srt.model.bie.BBIESCNode;
import org.oagi.srt.model.bie.TopLevelNode;

import java.util.Collections;

public class BIENodeSortVisitor implements BIENodeVisitor {

    @Override
    public void startNode(TopLevelNode topLevelNode) {
        sortChildren(topLevelNode);
    }

    @Override
    public void visitASBIENode(ASBIENode asbieNode) {
        sortChildren(asbieNode);
    }

    @Override
    public void visitBBIENode(BBIENode bbieNode) {
        sortChildren(bbieNode);
    }

    @Override
    public void visitBBIESCNode(BBIESCNode bbiescNode) {
    }

    @Override
    public void endNode() {
    }

    private void sortChildren(Node node) {
        Collections.sort(node.getChildren(), (a, b) -> {
            int result = a.getSeqKey() - b.getSeqKey();
            if (result == 0) {
                return (int) (getCreationTimestamp((BIENode) a) - getCreationTimestamp((BIENode) b));
            } else {
                return result;
            }
        });
    }

    private long getCreationTimestamp(BIENode node) {
        if (node instanceof ASBIENode) {
            return ((ASBIENode) node).getAsccp().getCreationTimestamp().getTime();
        }
        if (node instanceof BBIENode) {
            return ((BBIENode) node).getBccp().getCreationTimestamp().getTime();
        }
        return 0L;
    }
}
