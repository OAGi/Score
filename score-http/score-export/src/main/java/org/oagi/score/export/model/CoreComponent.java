package org.oagi.score.export.model;

import java.io.Serializable;

public interface CoreComponent extends Serializable {

    public String getDen();

    public boolean isDirty();
}
