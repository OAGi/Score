package org.oagi.score.gateway.http.api.module_management.data.module_edit;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonSerialize(using = ModuleElementDependencySerializer.class)
public class ModuleElementDependency
        implements Serializable, Comparable<ModuleElementDependency> {
    private String dependencyType;
    private ModuleElement dependingElement;
    private ModuleElement dependedElement;

    public ModuleElementDependency(String dependencyType, ModuleElement dependingElement) {
        this.dependencyType = dependencyType;
        this.dependingElement = dependingElement;
    }

    public String toString() {
        return toString(0);
    }

    public String toString(int indent) {
        return toString(indent, getDependedElement());
    }

    public String toString(int indent, ModuleElement from) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; ++i) {
            sb.append("\t");
        }
        sb.append("[").append(getDependencyType())
                .append(": ")
                .append(getDependingElement().getRelativePath(from))
                .append("]");
        return sb.toString();
    }


    @Override
    public int compareTo(ModuleElementDependency o) {
        return this.toString().compareTo(o.toString());
    }
}
