package org.oagi.score.gateway.http.api.graph.model;

import lombok.Data;

@Data
public class FindUsagesResponse {

    private String rootNodeKey;
    private Graph graph;

}
