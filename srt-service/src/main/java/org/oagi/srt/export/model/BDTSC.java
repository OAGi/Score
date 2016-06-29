package org.oagi.srt.export.model;

public class BDTSC {

    private int dtScId;

    private String guid;

    private String name;

    private int minCardinality;

    private int maxCardinality;

    private boolean hasBasedBDTSC;

    public BDTSC(int dtScId, String guid, String name,
                 int minCardinality, int maxCardinality, boolean hasBasedBDTSC) {
        this.dtScId = dtScId;
        this.guid = guid;
        this.name = name;
        this.minCardinality = minCardinality;
        this.maxCardinality = maxCardinality;
        this.hasBasedBDTSC = hasBasedBDTSC;
    }

    public int getDtScId() {
        return dtScId;
    }

    public String getGuid() {
        return guid;
    }

    public String getName() {
        return name;
    }

    public int getMinCardinality() {
        return minCardinality;
    }

    public int getMaxCardinality() {
        return maxCardinality;
    }

    public boolean hasBasedBDTSC() {
        return hasBasedBDTSC;
    }
}
