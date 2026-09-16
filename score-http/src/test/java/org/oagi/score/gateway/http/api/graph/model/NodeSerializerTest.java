package org.oagi.score.gateway.http.api.graph.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

class NodeSerializerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesStableComponentIdentityAndManifestLinks() throws Exception {
        Node node = Node.toNode(Node.NodeType.ACC, AccManifestId.from("11"), null);
        node.setBasedManifestId(AccManifestId.from("10"));
        node.setLinkedManifestId(AccManifestId.from("12"));
        node.setPrevManifestId(AccManifestId.from("9"));
        node.put("componentId", BigInteger.valueOf(101));
        node.put("guid", "acc-guid");

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(node));

        assertThat(json.path("manifestId").asInt()).isEqualTo(11);
        assertThat(json.path("basedManifestId").asInt()).isEqualTo(10);
        assertThat(json.path("linkedManifestId").asInt()).isEqualTo(12);
        assertThat(json.path("prevManifestId").asInt()).isEqualTo(9);
        assertThat(json.path("componentId").asInt()).isEqualTo(101);
        assertThat(json.path("guid").asText()).isEqualTo("acc-guid");
    }

    @Test
    void omitsManifestLinksWhenTheyAreNotPresent() throws Exception {
        Node node = Node.toNode(Node.NodeType.DT_SC, AccManifestId.from("21"), null);

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(node));

        assertThat(json.has("basedManifestId")).isFalse();
        assertThat(json.has("linkedManifestId")).isFalse();
        assertThat(json.has("prevManifestId")).isFalse();
    }
}
