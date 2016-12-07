package org.oagi.srt.repository.entity.listener;

public interface UpdateEventListener {

    public void onPreUpdate(Object object);

    public void onPostUpdate(Object object);

}
