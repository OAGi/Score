package org.oagi.score.repo.component.graph;

import org.oagi.score.gateway.http.api.graph.data.Node;

import java.util.List;

public interface GraphContext {

    List<Node> findChildren(Node node, boolean excludeUEG);

}
