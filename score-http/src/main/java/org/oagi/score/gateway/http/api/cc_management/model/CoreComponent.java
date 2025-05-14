package org.oagi.score.gateway.http.api.cc_management.model;

import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.Id;

public interface CoreComponent<ID extends Id> {

    ID getId();

    Guid guid();

}
