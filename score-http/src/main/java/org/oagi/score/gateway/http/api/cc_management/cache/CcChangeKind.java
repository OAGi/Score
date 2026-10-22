package org.oagi.score.gateway.http.api.cc_management.cache;

/** What a row of {@code cc_change_log} says has changed. */
public enum CcChangeKind {
    /** The record with this manifest id, and what is listed under it. */
    ACC, ASCC, BCC, ASCCP, BCCP, DT, DT_SC,
    /** A release: all records of it show it. */
    RELEASE,
    /** The dependencies of a release: the releases a document answers for. */
    RELEASE_DEP,
    /** A library: all records of its releases show it. */
    LIBRARY,
    /** A user who owns or updated records. */
    USER,
    /** A tag, shown with the records it is on. */
    TAG
}
