package org.oagi.srt.repository.entity;

import java.util.Date;

public interface TimestampAware {

    public Date getCreationTimestamp();

    public void setCreationTimestamp(Date creationTimestamp);

    public Date getLastUpdateTimestamp();

    public void setLastUpdateTimestamp(Date lastUpdateTimestamp);
}
