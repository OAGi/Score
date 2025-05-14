package org.oagi.score.gateway.http.api.bie_management.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.BiePackageId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.event.Event;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BiePackageUpliftRequestEvent implements Event {

    private UserId requestUserId;

    private ReleaseId targetReleaseId;

    private BiePackageId upliftedBiePackageId;

    private List<TopLevelAsbiepId> sourceTopLevelAsbiepIdList;

}
