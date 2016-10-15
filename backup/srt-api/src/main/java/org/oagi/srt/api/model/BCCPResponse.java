package org.oagi.srt.api.model;

import com.fasterxml.jackson.annotation.JsonRootName;
import org.oagi.srt.repository.entity.BasicCoreComponentProperty;

@JsonRootName("BCCP")
public class BCCPResponse extends ResourceSupportResponse implements CCPResponse {

    private String guid;

    private String propertyTerm;

    private String representationTerm;

    private String den;

    public BCCPResponse(BasicCoreComponentProperty bccp) {
        super("BCCP");
        this.guid = bccp.getGuid();
        this.propertyTerm = bccp.getPropertyTerm();
        this.representationTerm = bccp.getRepresentationTerm();
        this.den = bccp.getDen();
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

    public String getRepresentationTerm() {
        return representationTerm;
    }

    public void setRepresentationTerm(String representationTerm) {
        this.representationTerm = representationTerm;
    }

    public String getDen() {
        return den;
    }

    public void setDen(String den) {
        this.den = den;
    }

    @Override
    public String toString() {
        return "BCCPResponse{" +
                "guid='" + guid + '\'' +
                ", propertyTerm='" + propertyTerm + '\'' +
                ", representationTerm='" + representationTerm + '\'' +
                ", den='" + den + '\'' +
                '}';
    }
}
