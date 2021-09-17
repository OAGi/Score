package org.oagi.score.gateway.http.api.graph.data;

import lombok.Data;

import java.util.List;

@Data
public class FindUsagesResponse {

    private String rootNodeKey;
    private Graph graph;

}
