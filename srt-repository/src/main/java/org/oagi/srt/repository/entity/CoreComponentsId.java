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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CoreComponentsId that = (CoreComponentsId) o;

        if (id != that.id) return false;
        return type != null ? type.equals(that.type) : that.type == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CoreComponentsId{" +
                "id=" + id +
                ", type='" + type + '\'' +
                '}';
    }
}
