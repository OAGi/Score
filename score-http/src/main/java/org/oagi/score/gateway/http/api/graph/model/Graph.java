package org.oagi.score.gateway.http.api.graph.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.xml.bind.annotation.XmlTransient;
import lombok.Data;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.common.util.StringUtils;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class Graph {

    @XmlTransient
    @JsonIgnore
    private transient Map<Node.NodeType, List<ManifestId>> nodeManifestIds = new HashMap();

    private Map<String, Node> nodes = new LinkedHashMap();
    private Map<String, Edge> edges = new LinkedHashMap();

    public Graph() {
        Arrays.asList(Node.NodeType.values()).stream().forEach(e -> {
            nodeManifestIds.put(e, new ArrayList());
        });
    }

    public boolean addNode(Node node) {
        String key = node.getKey();
        if (nodes.containsKey(key)) {
            return false;
        }

        nodes.put(key, node);
        nodeManifestIds.get(node.getType()).add(node.getManifestId());

        return true;
    }

    public Node getNode(Node.NodeType type, BigInteger manifestId) {
        String key = Node.toKey(type, manifestId);
        return nodes.get(key);
    }

    public Edge addEdges(Node source, List<Node> children) {
        String sourceKey = source.getKey();
        Edge edge;
        if (edges.containsKey(sourceKey)) {
            edge = edges.get(sourceKey);
        } else {
            edge = new Edge();
        }
        children.stream().forEach(e -> {
            edge.addTarget(e.getKey());
        });
        edges.put(sourceKey, edge);
        return edge;
    }

    public List<Node> getChildren(Node node) {
        Edge edge = edges.get(node.getKey());
        if (edge == null) {
            return Collections.emptyList();
        }
        return edge.getTargets().stream().map(e -> getNodes().get(e))
                .collect(Collectors.toList());
    }

    public Collection<List<String>> findPaths(String from, String query) {
        if (!StringUtils.hasLength(query)) {
            return Collections.emptyList();
        }

        Node root = nodes.get(from);
        if (root == null) {
            return Collections.emptyList();
        }

        String qlc = query.toLowerCase().trim();
        LinkedList<List<Node>> linkedList = new LinkedList();
        linkedList.add(Arrays.asList(root));

        Set<List<String>> paths = new LinkedHashSet();

        while (!linkedList.isEmpty()) {
            List<Node> cur = linkedList.poll();
            Node lastNode = cur.get(cur.size() - 1);
            if (lastNode.hasTerm(qlc) && !lastNode.isAssociation()) {
                paths.add(cur.stream().map(e -> e.getKey()).collect(Collectors.toList()));
            }

            List<String> targets = edges.getOrDefault(lastNode.getKey(), Edge.EMPTY_EDGE).getTargets();
            if (!targets.isEmpty()) {
                linkedList.addAll(linkedList.indexOf(cur) + 1, targets.stream().map(e -> {
                    List<Node> n = new ArrayList(cur);
                    n.add(nodes.get(e));
                    return n;
                }).collect(Collectors.toList()));
            }
        }

        return paths;
    }

    public void merge(Graph other) {
        this.nodes.putAll(other.nodes);
        this.edges.putAll(other.edges);
    }

    public Graph copy() {
        Graph copied = new Graph();
        copied.edges = new HashMap(this.edges);
        copied.nodes = new HashMap(this.nodes);
        return copied;
    }

}
