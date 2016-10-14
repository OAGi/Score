package org.oagi.srt.repository.entity.listener;

public interface PersistEventListener {

    public void onPrePersist(Object object);

    public void onPostPersist(Object object);

}
