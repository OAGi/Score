package org.oagi.score.gateway.http.api.module_management.data.module_edit;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Collection;

public class ModuleElementSerializer extends JsonSerializer<ModuleElement> {

    @Override
    public void serialize(ModuleElement value,
                          JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("moduleId", value.getModuleId());
        gen.writeStringField("name", value.getName());
        gen.writeBooleanField("directory", value.isDirectory());

        Collection<ModuleElement> elements = value.getElements();
        gen.writeFieldName("children");
        gen.writeStartArray();
        for (ModuleElement child : elements) {
            gen.writeObject(child);
        }
        gen.writeEndArray();

        Collection<ModuleElementDependency> dependents = value.getDependents();
        gen.writeFieldName("dependents");
        gen.writeStartArray();
        for (ModuleElementDependency child : dependents) {
            gen.writeObject(child);
        }
        gen.writeEndArray();

        gen.writeEndObject();
    }

}
