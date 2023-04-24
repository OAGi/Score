package org.oagi.score.e2e.api;

import org.oagi.score.e2e.obj.AgencyIDListObject;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.ReleaseObject;

/**
 * APIs for the agency ID list management.
 */
public interface AgencyIDListAPI {
    AgencyIDListObject getNewlyCreatedAgencyIDList(AppUserObject user, String release);
}
