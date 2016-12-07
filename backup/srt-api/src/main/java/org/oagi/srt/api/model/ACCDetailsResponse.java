package org.oagi.srt.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.oagi.srt.repository.entity.AggregateCoreComponent;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;
import org.oagi.srt.repository.entity.BasicCoreComponentProperty;
import org.springframework.hateoas.Link;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("ACC")
public class ACCDetailsResponse extends ResourceSupportResponse {

    private String guid;

    private String objectClassTerm;

    private String den;

    private String definition;

    private String objectClassQualifier;

    @JsonProperty("sequences")
    @JsonInclude
    private List<CCPResponse> sequences = new ArrayList();

    public ACCDetailsResponse(AggregateCoreComponent acc) {
        super("ACC");
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

    public void append(BasicCoreComponentProperty bccp, Link selfLink) {
        BCCPResponse bccpResponse = new BCCPResponse(bccp);
        bccpResponse.add(selfLink);
        sequences.add(bccpResponse);
    }

    public void append(AssociationCoreComponentProperty asccp, Link selfLink) {
        ASCCPResponse asccpResponse = new ASCCPResponse(asccp);
        asccpResponse.add(selfLink);
        sequences.add(asccpResponse);
    }

    @Override
    public String toString() {
        return "ACCDetailsResponse{" +
                "guid='" + guid + '\'' +
                ", objectClassTerm='" + objectClassTerm + '\'' +
                ", den='" + den + '\'' +
                ", definition='" + definition + '\'' +
                ", objectClassQualifier='" + objectClassQualifier + '\'' +
                ", sequences=" + sequences +
                '}';
    }
}
