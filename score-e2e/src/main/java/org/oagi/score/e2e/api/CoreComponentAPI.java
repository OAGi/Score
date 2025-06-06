package org.oagi.score.e2e.api;

import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.DtScRecord;
import org.oagi.score.e2e.obj.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * APIs for the core component management.
 */
public interface CoreComponentAPI {

    /**
     * Return the ACC by the given manifest ID.
     *
     * @param accManifestId ACC manifest ID
     * @return ACC object
     */
    ACCObject getACCByManifestId(BigInteger accManifestId);

    /**
     * Return the ACC by the given DEN and release number.
     *
     * @param den        DEN
     * @param releaseNum release number
     * @return ACC object
     */
    ACCObject getACCByDENAndReleaseNum(LibraryObject library, String den, String releaseNum);

    /**
     * Return the ASCCP by the given manifest ID.
     *
     * @param asccpManifestId ASCCP manifest ID
     * @return ASCCP object
     */
    ASCCPObject getASCCPByManifestId(BigInteger asccpManifestId);

    /**
     * Return the ASCCP by the given DEN and release number.
     *
     * @param den        DEN
     * @param releaseNum release number
     * @return ASCCP object
     */
    ASCCPObject getASCCPByDENAndReleaseNum(LibraryObject library, String den, String releaseNum);

    /**
     * Return the BCCP by the given manifest ID.
     *
     * @param bccpManifestId BCCP manifest ID
     * @return BCCP object
     */
    BCCPObject getBCCPByManifestId(BigInteger bccpManifestId);

    /**
     * Return the BCCP by the given DEN and release number.
     *
     * @param den        DEN
     * @param releaseNum release number
     * @return BCCP object
     */
    BCCPObject getBCCPByDENAndReleaseNum(LibraryObject library, String den, String releaseNum);

    /**
     * Return the CDT by the given manifest ID.
     *
     * @param dtManifestId DT manifest ID
     * @return CDT object
     */
    DTObject getCDTByManifestId(BigInteger dtManifestId);

    /**
     * Return the CDT by the given DEN and release number.
     *
     * @param den        DEN
     * @param releaseNum release number
     * @return CDT object
     */
    DTObject getCDTByDENAndReleaseNum(LibraryObject library, String den, String releaseNum);

    /**
     * Return the BDT by the given manifest ID.
     *
     * @param dtManifestId DT manifest ID
     * @return BDT object
     */
    DTObject getBDTByManifestId(BigInteger dtManifestId);

    /**
     * @param guid
     * @param releaseNum
     * @return
     */
    DTObject getBDTByGuidAndReleaseNum(LibraryObject library, String guid, String releaseNum);

    /**
     * Return the BDT by the given DEN and release number.
     *
     * @param den        DEN
     * @param releaseNum release number
     * @return a list of BDT objects
     */
    List<DTObject> getBDTByDENAndReleaseNum(LibraryObject library, String den, String releaseNum);

    /**
     * Create a random ACC.
     *
     * @param creator   account who creates this ACC
     * @param release   release
     * @param namespace namespace
     * @param state     ACC state
     * @return created ACC object
     */
    ACCObject createRandomACC(AppUserObject creator, ReleaseObject release,
                              NamespaceObject namespace, String state);


    /**
     * Create a random ACC.
     *
     * @param creator         account who creates this ACC
     * @param release         release
     * @param namespace       namespace
     * @param state           ACC state
     * @param type            ACC type
     * @param objectClassTerm object class term
     * @return created ACC object
     */
    ACCObject createRandomACC(AppUserObject creator, ReleaseObject release,
                              NamespaceObject namespace, String state,
                              ComponentType type, String objectClassTerm);

    /**
     * Create a random ASCCP.
     *
     * @param roleOfAcc The ACC from which this ASCCP is created.
     * @param creator   account who creates this ASCCP
     * @param namespace namespace
     * @param state     ASCCP state
     * @return created ASCCP object
     */
    ASCCPObject createRandomASCCP(ACCObject roleOfAcc, AppUserObject creator,
                                  NamespaceObject namespace, String state);

    /**
     * Create a random BCCP.
     *
     * @param branch    The branch of the BCCP
     * @param dataType  The data type of the BCCP
     * @param creator   account who creates this BCCP
     * @param namespace namespace
     * @param state     BCCP state
     * @return created BCCP object
     */
    BCCPObject createRandomBCCP(ReleaseObject branch,
                                DTObject dataType, AppUserObject creator,
                                NamespaceObject namespace, String state);

    /**
     * Create a random BDT with the CCTS DT v3.1 specification rule.
     *
     * @param baseDataType the base data type
     * @param creator      account who creates this BDT
     * @param namespace    namespace
     * @param state        BDT state
     * @return created BDT object
     */
    DTObject createRandomBDT(ReleaseObject release, DTObject baseDataType, AppUserObject creator,
                             NamespaceObject namespace, String state);

    ACCObject createRevisedACC(ACCObject prevAcc, AppUserObject creator, ReleaseObject release, String state);

    ASCCPObject createRevisedASCCP(ASCCPObject prevAsccp, ACCObject roleOfAcc,
                                   AppUserObject creator, ReleaseObject release, String state);

    BCCPObject createRevisedBCCP(BCCPObject prevBccp, DTObject dataType,
                                 AppUserObject creator, ReleaseObject release, String state);

    void updateACC(ACCObject acc);

    void updateBasedACC(ACCObject acc, ACCObject basedAcc);

    ASCCObject appendASCC(ACCObject fromAcc, ASCCPObject toAsccp, String state);

    void updateASCC(ASCCObject ascc);

    ASCCObject appendExtension(ACCObject fromAcc, AppUserObject creator,
                               NamespaceObject namespace, String state);

    BCCObject appendBCC(ACCObject fromAcc, BCCPObject toBccp, String state);

    void updateBCC(BCCObject bcc);

    void updateASCCP(ASCCPObject asccp);

    void updateBCCP(BCCPObject bccp);

    void updateBasedDT(BCCPObject bccp, DTObject dataType);

    ACCObject createRandomACCSemanticGroupType(AppUserObject creator, ReleaseObject release,
                                               NamespaceObject namespace, String state);

    DTObject getLatestDTCreated(LibraryObject library, String den, String branch);

    BCCPObject getLatestBCCPCreatedByUser(AppUserObject user, String branch);

    List<DTSCObject> getSupplementaryComponentsForDT(BigInteger dtID, String release);

    DTSCObject getNewlyCreatedSCForDT(BigInteger dtId, String releaseNumber);

    boolean SCPropertyTermIsUnique(DTObject dataType, String release, String objectClassTerm, String representationTerm, String propertyTerm);

    List<String> getRepresentationTermsForCDTs(String release);

    List<String> getValueDomainsByCDTRepresentationTerm(String representationTerm);

    String getDefaultValueDomainByCDTRepresentationTerm(String representationTerm);

    DTObject getRevisedDT(DTObject previousDT);
}
