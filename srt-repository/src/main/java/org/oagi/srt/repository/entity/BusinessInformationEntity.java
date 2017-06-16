package org.oagi.srt.repository.entity;

import org.oagi.srt.repository.entity.listener.PersistEventListener;
import org.oagi.srt.repository.entity.listener.UpdateEventListener;

/*
 * Marker interface
 */
public interface BusinessInformationEntity extends IDefinition, IGuidEntity {

    public long getOwnerTopLevelAbieId();

    public void setOwnerTopLevelAbieId(long ownerTopLevelAbieId);

    public void addPersistEventListener(PersistEventListener persistEventListener);

    public void addUpdateEventListener(UpdateEventListener updateEventListener);

    public BusinessInformationEntity clone();

}
