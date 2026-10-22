package org.oagi.score.gateway.http.api.cc_management.model;

/**
 * What reading the graph of one component put into the cache: the manifest ids of the components it reaches, and of the
 * components that stand in for the ones without associations (the ones they were made from). It is what a reader that
 * finds it can read the whole graph back with in a few bulk reads, without looking for the components again.
 */
public record GraphClosure(long[] accs, long[] asccps, long[] bccps, long[] dts,
                           long[] previousAccs, long[] previousDts) {
}
