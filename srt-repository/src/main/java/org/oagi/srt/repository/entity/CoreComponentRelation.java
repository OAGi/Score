package org.oagi.srt.repository.entity;

public interface CoreComponentRelation extends CoreComponent {

    public int getCardinalityMin();

    public int getCardinalityMax();

    public int getSeqKey();

}
