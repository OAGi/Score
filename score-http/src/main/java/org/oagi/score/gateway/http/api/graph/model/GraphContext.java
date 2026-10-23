package org.oagi.score.gateway.http.api.graph.model;

import java.util.Collection;
import java.util.List;

public interface GraphContext {

    List<Node> findChildren(Node node, boolean excludeUEG);

    /** A hint that the graph rooted at this node is about to be built, so that a context can load all of it at once. */
    default void prefetchGraph(Node root) {
    }

    /** A hint that the children of these nodes are about to be found, so that a context can load them all at once. */
    default void prefetchChildren(Collection<Node> nodes) {
    }

}
