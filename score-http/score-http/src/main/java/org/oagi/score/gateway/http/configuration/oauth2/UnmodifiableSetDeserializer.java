package org.oagi.score.gateway.http.configuration.oauth2;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

final class UnmodifiableSetDeserializer extends JsonDeserializer<Set<?>> {

    @Override
    public Set<?> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        JsonNode setNode = mapper.readTree(parser);
        Set<Object> result = new LinkedHashSet<>();
        if (setNode != null && setNode.isObject()) {
            Iterator<JsonNode> iterator = setNode.iterator();
            while (iterator.hasNext()) {
                JsonNode value = iterator.next();
                result.add(mapper.readValue(value.traverse(mapper), Object.class));
            }
        }
        return Collections.unmodifiableSet(result);
    }
}