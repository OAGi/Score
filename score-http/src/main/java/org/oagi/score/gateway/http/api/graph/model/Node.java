package org.oagi.score.gateway.http.api.graph.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.api.tag_management.model.TagSummaryRecord;

import java.math.BigInteger;
import java.util.*;

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
    private ManifestId manifestId;
    private CcState state;
    private List<TagSummaryRecord> tagList;

    private ManifestId basedManifestId;
    private ManifestId linkedManifestId;
    private ManifestId prevManifestId;

    private final Map<String, Object> properties = new HashMap();

    public Node(NodeType type, ManifestId manifestId, CcState state) {
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

    public ManifestId getManifestId() {
        return manifestId;
    }

    public void setManifestId(ManifestId manifestId) {
        this.manifestId = manifestId;
    }

    public CcState getState() {
        return state;
    }

    public void setState(CcState state) {
        this.state = state;
    }

    public List<TagSummaryRecord> getTagList() {
        return (tagList != null) ? tagList : Collections.emptyList();
    }

    public void setTagList(List<TagSummaryRecord> tagList) {
        this.tagList = tagList;
    }

    public ManifestId getBasedManifestId() {
        return basedManifestId;
    }

    public void setBasedManifestId(ManifestId basedManifestId) {
        this.basedManifestId = basedManifestId;
    }

    public ManifestId getLinkedManifestId() {
        return linkedManifestId;
    }

    public void setLinkedManifestId(ManifestId linkedManifestId) {
        this.linkedManifestId = linkedManifestId;
    }

    public ManifestId getPrevManifestId() {
        return prevManifestId;
    }

    public void setPrevManifestId(ManifestId prevManifestId) {
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

    public static Node toNode(NodeType type, ManifestId manifestId, CcState state) {
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
                ", tagList=" + tagList +
                ", properties=" + properties +
                '}';
    }
}
