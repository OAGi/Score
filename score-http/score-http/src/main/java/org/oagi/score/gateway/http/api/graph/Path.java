package org.oagi.score.gateway.http.api.graph;

import lombok.Data;

import java.util.List;
import java.util.Objects;

@Data
public class Path {

    private List<String> nodeKeys;

    public Path(List<String> nodeKeys) {
        this.nodeKeys = nodeKeys;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Path path = (Path) o;
        return Objects.equals(nodeKeys, path.nodeKeys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeKeys);
    }
}
