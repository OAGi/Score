package org.oagi.srt.repository.entity;

public interface CreatorModifierAware {

    public long getCreatedBy();

    public void setCreatedBy(long createdBy);

    public long getLastUpdatedBy();

    public void setLastUpdatedBy(long lastUpdatedBy);
}
