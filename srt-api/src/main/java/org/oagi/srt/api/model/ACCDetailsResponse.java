package org.oagi.srt.api.model;

import com.fasterxml.jackson.annotation.JsonRootName;
import org.oagi.srt.repository.entity.AggregateCoreComponent;
import org.springframework.hateoas.ResourceSupport;

@JsonRootName("ACC")
public class ACCDetailsResponse extends ResourceSupport {

    private String guid;

    private String objectClassTerm;

    private String den;

    private String definition;

    private String objectClassQualifier;

    public ACCDetailsResponse() {}

    public ACCDetailsResponse(AggregateCoreComponent acc) {
        this.guid = acc.getGuid();
        this.objectClassTerm = acc.getObjectClassTerm();
        this.den = acc.getDen();
        this.definition = acc.getDefinition();
        this.objectClassQualifier = acc.getObjectClassQualifier();
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getObjectClassTerm() {
        return objectClassTerm;
    }

    public void setObjectClassTerm(String objectClassTerm) {
        this.objectClassTerm = objectClassTerm;
    }

    public String getDen() {
        return den;
    }

    public void setDen(String den) {
        this.den = den;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getObjectClassQualifier() {
        return objectClassQualifier;
    }

    public void setObjectClassQualifier(String objectClassQualifier) {
        this.objectClassQualifier = objectClassQualifier;
    }

    @Override
    public String toString() {
        return "ACCDetailsResponse{" +
                "guid='" + guid + '\'' +
                ", objectClassTerm='" + objectClassTerm + '\'' +
                ", den='" + den + '\'' +
                ", definition='" + definition + '\'' +
                ", objectClassQualifier='" + objectClassQualifier + '\'' +
                '}';
    }
}
