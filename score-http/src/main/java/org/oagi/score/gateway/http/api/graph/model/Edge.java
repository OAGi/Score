package org.oagi.score.gateway.http.api.graph.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Edge {

    public static final transient Edge EMPTY_EDGE = new Edge();

    private List<String> targets = new ArrayList();

    public void addTarget(String target) {
        if (!targets.contains(target)) {
            targets.add(target);
        }
    }

    public void addTarget(int index, String target) {
        if (!targets.contains(target)) {
            targets.add(index, target);
        }
    }
}
