package org.oagi.score.gateway.http.api.cc_management.validator;

import org.oagi.score.gateway.http.api.cc_management.model.Cardinality;
import org.oagi.score.gateway.http.api.cc_management.model.CcAssociation;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.EntityType;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;

import java.math.BigInteger;
import java.util.*;

/**
 * A change of the associations of some ACCs, described by what the associations of those ACCs would be afterwards, and
 * the ACCs that a change of them reaches. It mirrors what the operation does (refactor to a base ACC); nothing is written.
 *
 * @param seeds         the ACCs that change
 * @param afterwards    what the associations of each ACC whose associations change would be
 * @param accs          the ACC records that change (an ACC that gets another base ACC)
 */
public record AccStructureChange(Set<AccManifestId> seeds,
                                 Map<AccManifestId, List<CcAssociation>> afterwards,
                                 Map<AccManifestId, AccSummaryRecord> accs) {

    public AccStructureChange(Set<AccManifestId> seeds, Map<AccManifestId, List<CcAssociation>> afterwards) {
        this(seeds, afterwards, Map.of());
    }

    /**
     * The ACCs whose content a change of the seeds changes: the ACCs themselves, the ACCs that have one of them as
     * their base ACC (and so on down), and, for an ACC that is a group, the ACCs that contain it (a group's content is
     * the content of the ACC that has it). Every ACC is only once, and an ACC that is deleted is left out.
     */
    public static Set<AccManifestId> affectedBy(CcDocument ccDocument, Collection<AccManifestId> seeds) {
        Set<AccManifestId> reached = new LinkedHashSet<>();
        Deque<AccManifestId> queue = new ArrayDeque<>(seeds);
        while (!queue.isEmpty()) {
            AccManifestId current = queue.poll();
            AccSummaryRecord acc = ccDocument.getAcc(current);
            if (acc == null || acc.state() == CcState.Deleted || !reached.add(current)) {
                continue;
            }
            for (AccSummaryRecord derived : ccDocument.getAccListByBasedAccManifestId(current)) {
                queue.add(derived.accManifestId());
            }
            if (acc.isGroup()) {
                for (AsccpSummaryRecord asccp : ccDocument.getAsccpListByRoleOfAccManifestId(current)) {
                    for (AccSummaryRecord container : ccDocument.getAccListByToAsccpManifestId(asccp.asccpManifestId())) {
                        if (container != null) {
                            queue.add(container.accManifestId());
                        }
                    }
                }
            }
        }
        return reached;
    }

    /**
     * Refactor an association to a base ACC: the association is made in the base ACC, and the associations to the same
     * property in the base ACC and in every ACC that has it as its base are taken away (they are the base ACC's now).
     *
     * @param moved   the association that is moved
     * @param removed what the operation takes away, as the operation finds it
     */
    public static AccStructureChange refactor(CcDocument ccDocument, CcAssociation moved, AccManifestId destination,
                                              Collection<? extends CcAssociation> removed) {
        Map<AccManifestId, List<CcAssociation>> afterwards = new LinkedHashMap<>();
        Set<AccManifestId> seeds = new LinkedHashSet<>();
        seeds.add(destination);
        for (CcAssociation e : removed) {
            AccManifestId from = fromAcc(e);
            seeds.add(from);
            afterwards.computeIfAbsent(from, k -> new ArrayList<>(ownAssociations(ccDocument, k)))
                    .removeIf(x -> sameAssociation(x, e));
        }
        afterwards.computeIfAbsent(destination, k -> new ArrayList<>(ownAssociations(ccDocument, k)))
                .add(copyTo(moved, destination, -1));
        return new AccStructureChange(seeds, afterwards);
    }

    /** Append an association to a property (an ASCCP): the ACC has it besides what it has. */
    public static AccStructureChange append(CcDocument ccDocument, AccManifestId accManifestId, AsccpSummaryRecord asccp) {
        AccSummaryRecord acc = ccDocument.getAcc(accManifestId);
        return appended(ccDocument, accManifestId, new AsccSummaryRecord(acc.library(), acc.release(),
                new AsccManifestId(BigInteger.valueOf(-1)), null, null, accManifestId, asccp.asccpManifestId(), null, null, null,
                asccp.den(), new Cardinality(0, 1), false, CcState.WIP, null, 1, acc.owner(), null, null));
    }

    /** Append an association to a property (a BCCP): the ACC has it besides what it has. */
    public static AccStructureChange append(CcDocument ccDocument, AccManifestId accManifestId, BccpSummaryRecord bccp) {
        AccSummaryRecord acc = ccDocument.getAcc(accManifestId);
        return appended(ccDocument, accManifestId, new BccSummaryRecord(acc.library(), acc.release(),
                new BccManifestId(BigInteger.valueOf(-1)), null, null, accManifestId, bccp.bccpManifestId(), null, null, null,
                EntityType.Element, bccp.den(), new Cardinality(0, 1), false, false, CcState.WIP, null, null, 1, acc.owner(), null, null));
    }

    private static AccStructureChange appended(CcDocument ccDocument, AccManifestId accManifestId, CcAssociation association) {
        List<CcAssociation> content = new ArrayList<>(ownAssociations(ccDocument, accManifestId));
        content.add(association);
        return new AccStructureChange(Set.of(accManifestId), Map.of(accManifestId, content));
    }

    /** Set the base ACC: the ACC, and so every ACC that has it as its base, has the content of the new base ACC too. */
    public static AccStructureChange setBase(CcDocument ccDocument, AccManifestId accManifestId, AccManifestId basedAccManifestId) {
        AccSummaryRecord acc = ccDocument.getAcc(accManifestId);
        AccSummaryRecord withBase = new AccSummaryRecord(acc.library(), acc.release(), acc.accManifestId(), acc.accId(), acc.guid(),
                acc.type(), basedAccManifestId, acc.den(), acc.objectClassTerm(), acc.objectClassQualifier(), acc.componentType(),
                acc.isAbstract(), acc.deprecated(), acc.state(), acc.namespaceId(), acc.definition(), acc.revisionNum(),
                acc.owner(), acc.prevAccManifestId(), acc.nextAccManifestId());
        return new AccStructureChange(Set.of(accManifestId), Map.of(), Map.of(accManifestId, withBase));
    }

    private static List<CcAssociation> ownAssociations(CcDocument ccDocument, AccManifestId accManifestId) {
        AccSummaryRecord acc = ccDocument.getAcc(accManifestId);
        return acc == null ? List.of() : ccDocument.getAssociations(acc).stream().filter(e -> !deleted(e)).toList();
    }

    private static boolean deleted(CcAssociation association) {
        return association instanceof AsccSummaryRecord a ? a.state() == CcState.Deleted
                : association instanceof BccSummaryRecord b && b.state() == CcState.Deleted;
    }

    private static AccManifestId fromAcc(CcAssociation association) {
        return association instanceof AsccSummaryRecord a ? a.fromAccManifestId() : ((BccSummaryRecord) association).fromAccManifestId();
    }

    private static boolean sameAssociation(CcAssociation a, CcAssociation b) {
        if (a instanceof AsccSummaryRecord x && b instanceof AsccSummaryRecord y) {
            return x.asccManifestId().equals(y.asccManifestId());
        }
        return a instanceof BccSummaryRecord x && b instanceof BccSummaryRecord y && x.bccManifestId().equals(y.bccManifestId());
    }

    /** What the operation creates: a copy that has the property and the cardinality of the original in another ACC. The id is made up. */
    private static CcAssociation copyTo(CcAssociation association, AccManifestId accManifestId, long madeUpId) {
        if (association instanceof AsccSummaryRecord a) {
            return new AsccSummaryRecord(a.library(), a.release(), new AsccManifestId(BigInteger.valueOf(madeUpId)), null, null,
                    accManifestId, a.toAsccpManifestId(), null, null, null, a.den(), a.cardinality(), false, CcState.WIP,
                    null, 1, a.owner(), null, null);
        }
        BccSummaryRecord b = (BccSummaryRecord) association;
        return new BccSummaryRecord(b.library(), b.release(), new BccManifestId(BigInteger.valueOf(madeUpId)), null, null,
                accManifestId, b.toBccpManifestId(), null, null, null, b.entityType(), b.den(), b.cardinality(), false,
                b.nillable(), CcState.WIP, b.valueConstraint(), null, 1, b.owner(), null, null);
    }
}
