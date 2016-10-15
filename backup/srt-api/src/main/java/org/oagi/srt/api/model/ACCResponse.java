package org.oagi.srt.api.model;

import com.fasterxml.jackson.annotation.JsonRootName;
import org.oagi.srt.repository.entity.AggregateCoreComponent;

@JsonRootName("ACC")
public class ACCResponse extends ResourceSupportResponse {

    private String guid;

    private String objectClassTerm;

    private String den;

    public ACCResponse(AggregateCoreComponent acc) {
        super("ACC");
        this.guid = acc.getGuid();
        this.objectClassTerm = acc.getObjectClassTerm();
        this.den = acc.getDen();
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

    @Override
    public String toString() {
        return "ACCResponse{" +
                "guid='" + guid + '\'' +
                ", objectClassTerm='" + objectClassTerm + '\'' +
                ", den='" + den + '\'' +
                '}';
    }
}
