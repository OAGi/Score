package org.oagi.srt.api.model;

import com.fasterxml.jackson.annotation.JsonRootName;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;
import org.springframework.hateoas.ResourceSupport;

@JsonRootName("ASCCP")
public class ASCCPDetailsResponse extends ResourceSupport {

    private String guid;

    private String propertyTerm;

    private String definition;

    private String den;

    public ASCCPDetailsResponse() {}

    public ASCCPDetailsResponse(AssociationCoreComponentProperty asccp) {
        this.guid = asccp.getGuid();
        this.propertyTerm = asccp.getPropertyTerm();
        this.definition = asccp.getDefinition();
        this.den = asccp.getDen();
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getPropertyTerm() {
        return propertyTerm;
    }

    public void setPropertyTerm(String propertyTerm) {
        this.propertyTerm = propertyTerm;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getDen() {
        return den;
    }

    public void setDen(String den) {
        this.den = den;
    }

    @Override
    public String toString() {
        return "ASCCPDetailsResponse{" +
                "guid='" + guid + '\'' +
                ", propertyTerm='" + propertyTerm + '\'' +
                ", definition='" + definition + '\'' +
                ", den='" + den + '\'' +
                '}';
    }
}
