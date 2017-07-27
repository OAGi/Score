package org.oagi.srt.repository.entity;

public interface CoreComponent extends IEntity, IGuidEntity, TimestampAware {

    public String getDen();

    public boolean isDirty();
}
