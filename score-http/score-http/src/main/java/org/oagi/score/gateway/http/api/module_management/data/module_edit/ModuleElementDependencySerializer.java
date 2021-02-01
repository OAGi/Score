package org.oagi.score.gateway.http.api.module_management.data.module_edit;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class ModuleElementDependencySerializer extends JsonSerializer<ModuleElementDependency> {

    @Override
    public void serialize(ModuleElementDependency value,
                          JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("type", value.getDependencyType());
        gen.writeNumberField("dependingModuleId", value.getDependingElement().getModuleId());
        gen.writeNumberField("dependedModuleId", value.getDependedElement().getModuleId());
        gen.writeEndObject();
    }
}
