package org.oagi.srt.repository.entity.listener;

import org.oagi.srt.repository.entity.TimestampAware;

import java.sql.Timestamp;
import java.util.Date;

public class TimestampAwareEventListener implements PersistEventListener, UpdateEventListener {

    @Override
    public void onPrePersist(Object object) {
        if (object instanceof TimestampAware) {
            TimestampAware timestampAware = (TimestampAware) object;
            Date date = new Timestamp(System.currentTimeMillis());
            timestampAware.setCreationTimestamp(date);
            timestampAware.setLastUpdateTimestamp(date);
        }
    }

    @Override
    public void onPostPersist(Object object) {

    }

    @Override
    public void onPreUpdate(Object object) {
        if (object instanceof TimestampAware) {
            TimestampAware timestampAware = (TimestampAware) object;
            Date date = new Timestamp(System.currentTimeMillis());
            timestampAware.setLastUpdateTimestamp(date);
        }
    }

    @Override
    public void onPostUpdate(Object object) {

    }
}
