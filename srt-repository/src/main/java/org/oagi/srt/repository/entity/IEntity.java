package org.oagi.srt.repository.entity;

import java.io.Serializable;

public interface IEntity extends Serializable {

    public long getId();

    public void setId(long id);

    public String tableName();

}
