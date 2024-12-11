package org.oagi.score.e2e.api;

import org.oagi.score.e2e.obj.*;

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

    /**
     * Return the agency ID list object by given name and branch
     *
     * @param name   the name of the agency ID list.
     * @param branch the branch
     * @param state  the state of the agency ID list.
     * @return the agency ID list object
     */
    AgencyIDListObject getAgencyIDListByNameAndBranchAndState(String name, String branch, String state);

    AgencyIDListObject getNewlyCreatedAgencyIDList(AppUserObject user, String release);

    void updateAgencyIDList(AgencyIDListObject agencyIDList);

}
