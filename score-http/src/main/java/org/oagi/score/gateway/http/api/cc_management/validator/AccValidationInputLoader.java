package org.oagi.score.gateway.http.api.cc_management.validator;

import org.oagi.score.gateway.http.api.cc_management.model.CcAssociation;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.EntityType;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.repository.AccQueryRepository;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.validation.PathSegment;
import org.oagi.score.gateway.http.api.validation.ValidationInputLoader;
import org.oagi.score.gateway.http.api.validation.ValidationSubject;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Lays an ACC out as the tree of the screen shows it, and reads nothing but the core components. The addresses it makes
 * are the ones of the tree (cc-flat-tree.ts): an ACC is <code>ACC-id</code>, a based ACC is a child of the ACC, an
 * association is <code>ASCC-id</code> or <code>BCC-id</code> followed by its property, a group is a child of the ASCCP
 * and a user extension group is not shown, its children are the children of the ACC that has it.
 */
@Component
public class AccValidationInputLoader implements ValidationInputLoader<AccValidationInput> {

    private static final String USER_EXTENSION_GROUP = "User Extension Group";

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Override
    public AccValidationInput load(ScoreUser requester, ValidationSubject subject) {
        AccManifestId accManifestId;
        try {
            accManifestId = new AccManifestId(new BigInteger(subject.id()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + subject.id() + "' is not an ACC manifest id.");
        }
        AccQueryRepository accQuery = repositoryFactory.accQueryRepository(requester);
        AccSummaryRecord acc = accQuery.getAccSummary(accManifestId);
        if (acc == null) {
            throw new IllegalArgumentException("The ACC manifest record with ID " + accManifestId + " could not be found.");
        }
        CcDocument ccDocument = repositoryFactory.ccDocumentFactory().create(requester, acc.release().releaseId());
        return new Walk(ccDocument, accQuery, acc).run();
    }

    private static class Walk {

        private final CcDocument ccDocument;
        private final AccQueryRepository accQuery;
        private final AccSummaryRecord root;
        private final List<AccValidationInput.Property> properties = new ArrayList<>();
        private final List<AccValidationInput.BaseCycle> baseCycles = new ArrayList<>();
        private final List<AccValidationInput.OwnAscc> ownAsccs = new ArrayList<>();
        private final List<AccValidationInput.OwnBcc> ownBccs = new ArrayList<>();
        private final List<AccValidationInput.Base> bases = new ArrayList<>();

        Walk(CcDocument ccDocument, AccQueryRepository accQuery, AccSummaryRecord root) {
            this.ccDocument = ccDocument;
            this.accQuery = accQuery;
            this.root = root;
        }

        AccValidationInput run() {
            Set<AccManifestId> onTheWay = new HashSet<>();
            onTheWay.add(root.accManifestId());
            List<PathSegment> path = new ArrayList<>();
            path.add(segment("ACC", root.accManifestId()));
            walk(root, path, false, false, onTheWay);
            AccSummaryRecord previous = root.prevAccManifestId() != null ? accQuery.getAccSummary(root.prevAccManifestId()) : null;
            NamespaceSummaryRecord namespace = root.namespaceId() != null ? ccDocument.getNamespace(root.namespaceId()) : null;
            return new AccValidationInput(root, previous, namespace, properties, baseCycles, ownAsccs, ownBccs, bases);
        }

        private void walk(AccSummaryRecord acc, List<PathSegment> path, boolean inherited, boolean viaGroup,
                          Set<AccManifestId> onTheWay) {
            for (CcAssociation association : ccDocument.getAssociations(acc)) {
                if (acc == root) {
                    recordOwn(association, path);
                }
                if (association instanceof AsccSummaryRecord ascc) {
                    walkAscc(acc, ascc, path, inherited, viaGroup, onTheWay);
                } else if (association instanceof BccSummaryRecord bcc) {
                    walkBcc(acc, bcc, path, inherited, viaGroup);
                }
            }

            if (acc.basedAccManifestId() != null) {
                AccSummaryRecord base = ccDocument.getAcc(acc.basedAccManifestId());
                if (base == null) {
                    return;
                }
                List<PathSegment> basePath = append(path, segment("ACC", base.accManifestId()));
                if (!onTheWay.add(base.accManifestId())) {
                    baseCycles.add(new AccValidationInput.BaseCycle(acc, base, basePath));
                    return;
                }
                bases.add(new AccValidationInput.Base(base, basePath));
                walk(base, basePath, true, viaGroup, onTheWay);
                onTheWay.remove(base.accManifestId());
            }
        }

        private void recordOwn(CcAssociation association, List<PathSegment> path) {
            if (association instanceof AsccSummaryRecord ascc && ascc.state() != CcState.Deleted) {
                AsccpSummaryRecord asccp = ccDocument.getAsccp(ascc.toAsccpManifestId());
                AccSummaryRecord roleOf = (asccp != null && asccp.roleOfAccManifestId() != null)
                        ? ccDocument.getAcc(asccp.roleOfAccManifestId()) : null;
                List<PathSegment> own = (asccp == null)
                        ? append(path, segment("ASCC", ascc.asccManifestId()))
                        : append(path, segment("ASCC", ascc.asccManifestId()), segment("ASCCP", asccp.asccpManifestId()));
                AsccSummaryRecord previous = ascc.prevAsccManifestId() != null ? accQuery.getAsccSummary(ascc.prevAsccManifestId()) : null;
                int references = (asccp != null && !asccp.reusable())
                        ? (int) accQuery.getAsccSummaryList(asccp.asccpManifestId()).stream()
                        .filter(e -> e.state() != CcState.Deleted).count() : 1;
                ownAsccs.add(new AccValidationInput.OwnAscc(ascc, asccp, roleOf, previous, references, own));
            } else if (association instanceof BccSummaryRecord bcc && bcc.state() != CcState.Deleted) {
                BccpSummaryRecord bccp = ccDocument.getBccp(bcc.toBccpManifestId());
                List<PathSegment> own = (bccp == null)
                        ? append(path, segment("BCC", bcc.bccManifestId()))
                        : append(path, segment("BCC", bcc.bccManifestId()), segment("BCCP", bccp.bccpManifestId()));
                BccSummaryRecord previous = bcc.prevBccManifestId() != null ? accQuery.getBccSummary(bcc.prevBccManifestId()) : null;
                boolean hasSupplementaryComponents = bcc.entityType() == EntityType.Attribute && bccp != null
                        && ccDocument.getDtScList(bccp.dtManifestId()).stream()
                        .anyMatch(sc -> sc.cardinality() != null && sc.cardinality().max() != null && sc.cardinality().max() > 0);
                ownBccs.add(new AccValidationInput.OwnBcc(bcc, bccp, previous, hasSupplementaryComponents, own));
            }
        }

        private void walkAscc(AccSummaryRecord acc, AsccSummaryRecord ascc, List<PathSegment> path,
                              boolean inherited, boolean viaGroup, Set<AccManifestId> onTheWay) {
            if (ascc.state() == CcState.Deleted) {
                return;
            }
            AsccpSummaryRecord asccp = ccDocument.getAsccp(ascc.toAsccpManifestId());
            if (asccp == null || asccp.state() == CcState.Deleted) {
                return;
            }
            List<PathSegment> asccpPath = append(path,
                    segment("ASCC", ascc.asccManifestId()), segment("ASCCP", asccp.asccpManifestId()));

            AccSummaryRecord roleOf = (asccp.roleOfAccManifestId() != null) ? ccDocument.getAcc(asccp.roleOfAccManifestId()) : null;
            if (roleOf != null && roleOf.isGroup()) {
                if (!onTheWay.add(roleOf.accManifestId())) {
                    return;
                }
                boolean flattened = asccp.propertyTerm() != null && asccp.propertyTerm().endsWith(USER_EXTENSION_GROUP);
                walk(roleOf,
                        flattened ? path : append(asccpPath, segment("ACC", roleOf.accManifestId())),
                        inherited, true, onTheWay);
                onTheWay.remove(roleOf.accManifestId());
                return;
            }

            if (asccp.propertyTerm() != null) {
                properties.add(new AccValidationInput.Property(asccp.propertyTerm(), AccValidationInput.Kind.ASCC,
                        acc, inherited, viaGroup, asccpPath));
            }
        }

        private void walkBcc(AccSummaryRecord acc, BccSummaryRecord bcc, List<PathSegment> path,
                             boolean inherited, boolean viaGroup) {
            if (bcc.state() == CcState.Deleted) {
                return;
            }
            BccpSummaryRecord bccp = ccDocument.getBccp(bcc.toBccpManifestId());
            if (bccp == null || bccp.state() == CcState.Deleted || bccp.propertyTerm() == null) {
                return;
            }
            properties.add(new AccValidationInput.Property(bccp.propertyTerm(), AccValidationInput.Kind.BCC,
                    acc, inherited, viaGroup,
                    append(path, segment("BCC", bcc.bccManifestId()), segment("BCCP", bccp.bccpManifestId()))));
        }

        private static PathSegment segment(String type, Object manifestId) {
            return new PathSegment(type, String.valueOf(manifestId));
        }

        private static List<PathSegment> append(List<PathSegment> path, PathSegment... more) {
            List<PathSegment> result = new ArrayList<>(path);
            result.addAll(List.of(more));
            return result;
        }
    }
}
