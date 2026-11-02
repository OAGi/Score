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

    /**
     * Return the newest inherited BIE created from the given base for the given owner, or
     * {@code null} while the asynchronous creation command has not produced one yet.
     */
    TopLevelASBIEPObject getLatestInheritedTopLevelASBIEP(BigInteger basedTopLevelAsbiepId,
                                                          BigInteger ownerUserId);

    TopLevelASBIEPObject getTopLevelASBIEPByDENAndReleaseNum(String den, String branch);

    void updateTopLevelASBIEP(TopLevelASBIEPObject topLevelASBIEP);

    void deleteTopLevelASBIEPByTopLevelASBIEPId(TopLevelASBIEPObject topLevelAsbiep);

    /**
     * Create BBIEP/BBIE nodes for the element BCCs used by the root ABIE of the given top-level BIE,
     * including elements inherited through the ACC base chain. {@link #generateRandomTopLevelASBIEP}
     * creates only the root ABIE + ASBIEP (no child nodes), so this is required before
     * {@link #seedAllBbieProfiling} has anything to update and before a single controlled
     * per-element backward-compatibility diff can be seeded (issue #1733). Each created BBIE starts
     * {@code is_used = 1} with the BCC's own cardinality; call {@link #seedAllBbieProfiling}
     * afterwards to set deterministic profiling.
     */
    void createBbieNodesForUsedElements(BigInteger topLevelAsbiepId, BigInteger createdByUserId);

    /**
     * Create one deterministic BBIE_SC under the first BBIE of the given top-level BIE. This is
     * used by uplift regression fixtures that need to exercise BBIE_SC copying.
     */
    void createBbieScForFirstBbie(BigInteger topLevelAsbiepId, BigInteger createdByUserId);

    /**
     * Create one used BBIE_SC under the BBIE and DT_SC identified by their property terms.
     * This is useful when a fixture must seed a specific supplementary component instead of
     * relying on database insertion order.
     */
    void createBbieScForBbieAndDtSc(BigInteger topLevelAsbiepId, BigInteger createdByUserId,
                                    String bbiePropertyTerm, String dtScPropertyTerm,
                                    String dtScRepresentationTerm);

    List<BigInteger> getReusedTopLevelAsbiepIds(BigInteger topLevelAsbiepId);

    int countBbieSc(BigInteger topLevelAsbiepId);

    /**
     * Return the BBIE paths that own BBIE_SC records under the given output BIE.
     * This verifies that inline BBIE_SC records are attached to distinct copied BBIEs.
     */
    List<String> getBbiePathsHavingBbieSc(BigInteger topLevelAsbiepId);

    /**
     * Return the stable XBT ID referenced by the BBIE primitive manifest at the given BIE path.
     * The join through XBT_MANIFEST verifies the persisted manifest points to that XBT.
     */
    BigInteger getBbieXbtIdByPath(BigInteger topLevelAsbiepId, String bbiePath);

    /**
     * Return the stable XBT ID referenced by the BBIE_SC primitive manifest belonging to the BBIE
     * at the given BIE path and supplementary component name.
     *
     * <p>The property and representation terms are separate columns in {@code DT_SC}; callers
     * must provide both rather than the display name produced by concatenating them.</p>
     */
    BigInteger getBbieScXbtIdByBbiePathAndPropertyAndRepresentationTerm(BigInteger topLevelAsbiepId,
                                                                         String bbiePath,
                                                                         String propertyTerm,
                                                                         String representationTerm);

    boolean hasValidBbieOwnership(BigInteger topLevelAsbiepId);

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
     * Override the primitive value domain (the assigned XBT) of every BBIE under the given top-level
     * BIE with the XBT whose XSD built-in type is {@code builtInType} (for example
     * {@code "xsd:normalizedString"} or {@code "xsd:token"}), resolved in the BIE's own release; any
     * code-list / agency-id-list override is cleared. This bypasses the editor and is used to seed a
     * deterministic value-domain (primitive) narrowing between two BIEs built on the same ASCCP
     * (issue #1733). The XBT must be set on <em>both</em> the prior and the current BIE for the
     * backward-compatibility diff to consider the value domain; if either side leaves the BBIE's XBT
     * null, the backend short-circuits and records no value-domain break.
     */
    void seedAllBbieValueDomainByBuiltInType(BigInteger topLevelAsbiepId, String builtInType);

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
