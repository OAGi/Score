package org.oagi.srt.repository.entity;

public interface CoreComponent extends TimestampAware {

    public String getGuid();

    public String getDen();

    public boolean isDirty();
}
