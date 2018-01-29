package org.oagi.srt.repository.entity;

import java.io.Serializable;

public interface CoreComponent extends IEntity, IGuidEntity,
        TimestampAware, CreatorModifierAware, OwnerUserAware, Serializable {

    public String getDen();

    public boolean isDirty();

    public long getReleaseId();

    public CoreComponentState getState();
}
