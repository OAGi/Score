package org.oagi.srt.repository.entity;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class CoreComponentsId implements Serializable {

    private long id;

    private String type;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
