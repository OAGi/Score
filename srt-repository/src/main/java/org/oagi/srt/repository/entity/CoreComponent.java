package org.oagi.srt.repository.entity;

public interface CoreComponent extends IDefinition, TimestampAware {

    public String getGuid();

    public String getDen();

    public boolean isDirty();
}
