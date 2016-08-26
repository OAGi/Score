package org.oagi.srt.repository.entity;

/*
 * Marker interface
 */
public interface CoreComponent {

    public String getGuid();

    public String getDen();

    public int getCardinalityMin();

    public int getCardinalityMax();
}
