package org.oagi.score.e2e.api;

import org.oagi.score.e2e.obj.*;

import java.math.BigInteger;

/**
 * APIs for the agency ID list management.
 */
public interface AgencyIDListAPI {

    AgencyIDListObject createRandomAgencyIDList(AppUserObject creator, NamespaceObject namespace,
                                                ReleaseObject release, String state);

    AgencyIDListObject getAgencyIDListByManifestId(BigInteger agencyIdListManifestId);

    /**
     * Return the agency ID list object by given name and branch
     *
     * @param name the name of the agency ID list.
     * @param branch the branch
     * @return the agency ID list object
     */
    AgencyIDListObject getAgencyIDListByNameAndBranch(String name, String branch);

    AgencyIDListObject getNewlyCreatedAgencyIDList(AppUserObject user, String release);

    void updateAgencyIDList(AgencyIDListObject agencyIDList);

}
