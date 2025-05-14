package org.oagi.score.gateway.http.api.export.model;

public interface CoreComponentRelation extends CoreComponent {

    public long getFromAccId();

    public int getCardinalityMin();

    public int getCardinalityMax();

    public int getSeqKey();

    public String getGuid();

}
