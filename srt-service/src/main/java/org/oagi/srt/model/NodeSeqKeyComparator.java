package org.oagi.srt.model;

import java.util.Comparator;

public class NodeSeqKeyComparator implements Comparator<Node> {

    @Override
    public int compare(Node o1, Node o2) {
        return o1.getSeqKey() - o2.getSeqKey();
    }
}
