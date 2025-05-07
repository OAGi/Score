package org.oagi.score.gateway.http.api.graph.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Map;

public class NodeSerializer extends StdSerializer<Node> {

    public NodeSerializer() {
        super(Node.class);
    }

    @Override
    public void serialize(Node node, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("type", node.getTypeAsString());
        gen.writeNumberField("manifestId", node.getManifestId().value().longValue());
        if (!node.getTagList().isEmpty()) {
            gen.writeObjectField("tagList", node.getTagList());
        }
        for (Map.Entry<String, Object> entry : node.getProperties().entrySet()) {
            gen.writeObjectField(entry.getKey(), entry.getValue());
        }
        gen.writeEndObject();
    }
}
