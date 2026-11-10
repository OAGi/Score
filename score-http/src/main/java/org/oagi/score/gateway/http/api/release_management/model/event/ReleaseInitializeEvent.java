package org.oagi.score.gateway.http.api.release_management.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.event.Event;

/** A draft has been asked to go back to 'Initialized'. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseInitializeEvent implements Event {

    private UserId userId;
    private ReleaseId releaseId;

}
