package org.oagi.score.gateway.http.api.graph.model;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.util.Map;

public class NodeSerializer extends StdSerializer<Node> {

    public NodeSerializer() {
        super(Node.class);
    }

    @Override
    public void serialize(Node node, JsonGenerator gen, SerializationContext provider) {
        gen.writeStartObject();
        gen.writeStringProperty("type", node.getTypeAsString());
        gen.writeNumberProperty("manifestId", node.getManifestId().value().longValue());
        if (node.getBasedManifestId() != null) {
            gen.writeNumberProperty("basedManifestId", node.getBasedManifestId().value().longValue());
        }
        if (node.getLinkedManifestId() != null) {
            gen.writeNumberProperty("linkedManifestId", node.getLinkedManifestId().value().longValue());
        }
        if (node.getPrevManifestId() != null) {
            gen.writeNumberProperty("prevManifestId", node.getPrevManifestId().value().longValue());
        }
        if (!node.getTagList().isEmpty()) {
            gen.writePOJOProperty("tagList", node.getTagList());
        }
        for (Map.Entry<String, Object> entry : node.getProperties().entrySet()) {
            gen.writePOJOProperty(entry.getKey(), entry.getValue());
        }
        gen.writeEndObject();
    }
}
