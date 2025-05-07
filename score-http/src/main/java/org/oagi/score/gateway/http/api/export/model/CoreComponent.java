package org.oagi.score.gateway.http.api.export.model;

import java.io.Serializable;

public interface CoreComponent extends Serializable {

    public String getDen();

    public boolean isDirty();
}
