package org.oagi.score.e2e.api;

import org.oagi.score.e2e.obj.AgencyIDListObject;
import org.oagi.score.e2e.obj.AgencyIDListValueObject;
import org.oagi.score.e2e.obj.AppUserObject;

import java.math.BigInteger;
import java.util.ArrayList;

/**
 * APIs for the agency ID list value management.
 */
public interface AgencyIDListValueAPI {

    AgencyIDListValueObject createRandomAgencyIDListValue(AppUserObject creator, AgencyIDListObject agencyIDList);

    AgencyIDListValueObject getAgencyIDListValueByManifestId(BigInteger agencyIDListValueManifestId);

    ArrayList<AgencyIDListValueObject> getAgencyIDListValueByAgencyListID(AgencyIDListObject agencyIDList);

}
