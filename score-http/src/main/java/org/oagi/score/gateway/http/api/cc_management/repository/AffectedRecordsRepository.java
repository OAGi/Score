package org.oagi.score.gateway.http.api.cc_management.repository;

import org.oagi.score.gateway.http.change.RecordType;
import org.oagi.score.gateway.http.change.RecordRef;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

/** The core component records that show what a change names when it is not made to a record of theirs: a release, a library, a user, a tag, an XBT, a code list or an agency identification list. */
public interface AffectedRecordsRepository {

    /**
     * The data types and their supplementary components whose allowed primitives are the XBT, the code lists or the agency
     * identification lists with these manifest ids (the primitives show their names).
     */
    List<RecordRef> recordsShowing(RecordType type, Collection<BigInteger> manifestIds);

    List<RecordRef> recordsOfRelease(BigInteger releaseId);

    List<RecordRef> recordsOfLibrary(BigInteger libraryId);

    /** The records the user owns, created or updated last. */
    List<RecordRef> recordsOfUser(BigInteger userId);

    List<RecordRef> recordsTaggedWith(BigInteger tagId);

    List<BigInteger> allReleaseIds();
}
