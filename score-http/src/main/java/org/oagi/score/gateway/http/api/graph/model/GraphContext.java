package org.oagi.score.gateway.http.api.graph.model;

import java.util.List;

public interface GraphContext {

    List<Node> findChildren(Node node, boolean excludeUEG);

}
