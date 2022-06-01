package org.oagi.score.gateway.http.api.graph.data;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.jooq.types.ULong;
import org.oagi.score.service.common.data.CcState;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@JsonSerialize(using = NodeSerializer.class)
public class Node {

    public enum NodeType {
        ACC,
        ASCC,
        ASCCP,
        BCC,
        BCCP,
        DT,
        DT_SC,

        CODE_LIST,
        CODE_LIST_VALUE;

        public String toString() {
            return this.name();
        }
    }

    private NodeType type;
    private ULong manifestId;
    private CcState state;

    private ULong basedManifestId;
    private ULong linkedManifestId;
    private ULong prevManifestId;

    private final Map<String, Object> properties = new HashMap();

    public Node(NodeType type, ULong manifestId, CcState state) {
        setType(type);
        setManifestId(manifestId);
        setState(state);
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public ULong getManifestId() {
        return manifestId;
    }

    public void setManifestId(ULong manifestId) {
        this.manifestId = manifestId;
    }

    public CcState getState() {
        return state;
    }

    public void setState(CcState state) {
        this.state = state;
    }

    public ULong getBasedManifestId() {
        return basedManifestId;
    }

    public void setBasedManifestId(ULong basedManifestId) {
        this.basedManifestId = basedManifestId;
    }

    public ULong getLinkedManifestId() {
        return linkedManifestId;
    }

    public void setLinkedManifestId(ULong linkedManifestId) {
        this.linkedManifestId = linkedManifestId;
    }

    public ULong getPrevManifestId() {
        return prevManifestId;
    }

    public void setPrevManifestId(ULong prevManifestId) {
        this.prevManifestId = prevManifestId;
    }

    public String getTypeAsString() {
        return getType().toString();
    }

    public String getKey() {
        return getTypeAsString() + "-" + getManifestId();
    }

    public static String toKey(NodeType type, BigInteger manifestId) {
        return type.toString() + "-" + manifestId;
    }

    public void put(String key, Object value) {
        this.properties.put(key, value);
    }

    public Map<String, Object> getProperties() {
        return this.properties;
    }

    public static Node toNode(NodeType type, ULong manifestId, CcState state) {
        return new Node(type, manifestId, state);
    }

    public boolean hasTerm(String query) {
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            Object value = entry.getValue();
            if (!(value instanceof String)) {
                continue;
            }
            if (((String) value).toLowerCase().contains(query)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAssociation() {
        return getType() == NodeType.ASCC || getType() == NodeType.BCC || getType() == NodeType.DT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return type == node.type &&
                manifestId.equals(node.manifestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, manifestId);
    }

    @Override
    public String toString() {
        return "Node{" +
                "type=" + type +
                ", manifestId=" + manifestId +
                ", basedManifestId=" + basedManifestId +
                ", linkedManifestId=" + linkedManifestId +
                ", prevManifestId=" + prevManifestId +
                ", properties=" + properties +
                '}';
    }
}
