package org.oagi.score.e2e.api;

import org.oagi.score.e2e.obj.AgencyIDListObject;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.obj.ReleaseObject;

import java.math.BigInteger;
import java.util.List;

/**
 * APIs for the agency ID list management.
 */
public interface AgencyIDListAPI {

    AgencyIDListObject createRandomAgencyIDList(AppUserObject creator, NamespaceObject namespace,
                                                ReleaseObject release, String state);

    AgencyIDListObject getAgencyIDListByManifestId(BigInteger agencyIdListManifestId);

    /**
     * Return the list of agency ID list objects.
     *
     * @param release the release object
     * @return the list of agency ID list objects
     */
    List<AgencyIDListObject> getAgencyIDListsByRelease(ReleaseObject release);

    /**
     * Return the agency ID list object by given name and branch
     *
     * @param name   the name of the agency ID list.
     * @param branch the branch
     * @return the agency ID list object
     */
    AgencyIDListObject getAgencyIDListByNameAndBranch(String name, String branch);

    AgencyIDListObject getNewlyCreatedAgencyIDList(AppUserObject user, String release);

    void updateAgencyIDList(AgencyIDListObject agencyIDList);

}
