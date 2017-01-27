package org.oagi.srt.repository.entity.listener;

import org.oagi.srt.repository.entity.CreatorModifierAware;
import org.oagi.srt.repository.entity.User;

public class CreatorModifierAwareEventListener implements PersistEventListener, UpdateEventListener {

    private User user;

    public CreatorModifierAwareEventListener(User user) {
        this.user = user;
    }

    @Override
    public void onPrePersist(Object object) {
        if (object instanceof CreatorModifierAware) {
            CreatorModifierAware creatorModifierAware = (CreatorModifierAware) object;
            long ownerId = user.getAppUserId();
            creatorModifierAware.setCreatedBy(ownerId);
            creatorModifierAware.setLastUpdatedBy(ownerId);
        }
    }

    @Override
    public void onPostPersist(Object object) {

    }

    @Override
    public void onPreUpdate(Object object) {
        if (object instanceof CreatorModifierAware) {
            CreatorModifierAware creatorModifierAware = (CreatorModifierAware) object;
            long ownerId = user.getAppUserId();
            creatorModifierAware.setLastUpdatedBy(ownerId);
        }
    }

    @Override
    public void onPostUpdate(Object object) {

    }
}
