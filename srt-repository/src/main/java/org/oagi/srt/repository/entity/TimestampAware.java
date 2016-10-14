package org.oagi.srt.repository.entity;

import java.util.Date;

public interface TimestampAware {

    public void setCreationTimestamp(Date creationTimestamp);

    public void setLastUpdateTimestamp(Date lastUpdateTimestamp);
}
