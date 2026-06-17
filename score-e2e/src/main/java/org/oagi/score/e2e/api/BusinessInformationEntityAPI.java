package org.oagi.score.e2e.api;

import org.oagi.score.e2e.obj.ASCCPObject;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.BusinessContextObject;
import org.oagi.score.e2e.obj.TopLevelASBIEPObject;

import java.math.BigInteger;
import java.util.List;

/**
 * APIs for the business information entity (BIE) management.
 */
public interface BusinessInformationEntityAPI {

    TopLevelASBIEPObject generateRandomTopLevelASBIEP(List<BusinessContextObject> businessContexts,
                                                      ASCCPObject asccp, AppUserObject creator, String state);

    TopLevelASBIEPObject getTopLevelASBIEPByID(BigInteger topLevelAsbiepId);

    TopLevelASBIEPObject getTopLevelASBIEPByDENAndReleaseNum(String den, String branch);

    void updateTopLevelASBIEP(TopLevelASBIEPObject topLevelASBIEP);

    void deleteTopLevelASBIEPByTopLevelASBIEPId(TopLevelASBIEPObject topLevelAsbiep);

    /**
     * Materialize the first-level child BBIEP/BBIE rows of the given top-level BIE from the element
     * BCCs declared on its root ABIE's ACC. {@link #generateRandomTopLevelASBIEP} creates only the
     * root ABIE + ASBIEP (no child nodes), so this is required before {@link #seedAllBbieProfiling}
     * has anything to update and before a single controlled per-element backward-compatibility diff
     * can be seeded (issue #1733). Each created BBIE starts {@code is_used = 1} with the BCC's own
     * cardinality; call {@link #seedAllBbieProfiling} afterwards to set a deterministic profiling.
     */
    void materializeUsedBbieChildren(BigInteger topLevelAsbiepId, BigInteger createdByUserId);

    /**
     * Directly set the used flag, cardinality, and optional max-length facet of every BBIE under
     * the given top-level BIE. This bypasses the editor and is used to seed deterministic backward
     * compatibility diffs between two BIEs built on the same ASCCP (issue #1733). A {@code null}
     * {@code maxLengthFacet} clears the max-length facet. {@code cardinalityMax} of {@code -1}
     * denotes an unbounded maximum.
     */
    void seedAllBbieProfiling(BigInteger topLevelAsbiepId, boolean used,
                              int cardinalityMin, int cardinalityMax, Long maxLengthFacet);

    /**
     * Link an existing top-level BIE to a BIE Package (mirrors the backend "Add BIE" command without
     * driving the UI dialog). Used for deterministic BIE Package test setup.
     */
    void addBieToBiePackage(BigInteger biePackageId, BigInteger topLevelAsbiepId, BigInteger createdByUserId);

    /**
     * Replace a top-level BIE in a BIE Package with another (mirrors the backend "Replace BIE"
     * command: links the new BIE with its prior chained to the old one, so the package's
     * head-of-chain resolves to the new BIE).
     */
    void replaceBieInBiePackage(BigInteger biePackageId, BigInteger prevTopLevelAsbiepId,
                                BigInteger topLevelAsbiepId, BigInteger createdByUserId);

}
