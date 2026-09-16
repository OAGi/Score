package org.oagi.score.gateway.http.api.bie_management.model;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.abie.Abie;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.Asbie;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.Asbiep;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.Bbie;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieSc;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScId;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.Bbiep;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepId;
import org.oagi.score.gateway.http.api.bie_management.service.BieDocument;
import org.oagi.score.gateway.http.api.bie_management.service.BieAssociationPaths;
import org.oagi.score.gateway.http.api.bie_management.service.BieAssociationPaths.Association;
import org.oagi.score.gateway.http.api.bie_management.service.BieVisitContext;
import org.oagi.score.gateway.http.api.bie_management.service.BieVisitor;
import org.oagi.score.gateway.http.api.cc_management.model.CcAssociation;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.oagi.score.gateway.http.api.bie_management.service.BieVisitResult.SKIP_SUBTREE;

@Data
public class BieDocumentImpl implements BieDocument {

    private TopLevelAsbiepSummaryRecord topLevelAsbiep;
    private Map<AbieId, Abie> abieMap;
    private Map<AbieId, List<Asbie>> asbieMap;
    private Map<AbieId, List<Bbie>> bbieMap;
    private Map<BbieId, Bbie> bbieByIdMap;
    private Map<AsbiepId, Asbiep> asbiepMap;
    private Map<BbiepId, Bbiep> bbiepMap;
    private Map<BbieId, List<BbieSc>> bbieScMap;
    private Map<BbieScId, BbieSc> bbieScByIdMap;

    private CcDocument ccDocument;

    BieDocumentImpl(BieSet bieSet) {
        this.topLevelAsbiep = bieSet.getTopLevelAsbiep();
        this.abieMap = bieSet.getAbieList().stream()
                .collect(Collectors.toMap(Abie::getAbieId, Function.identity()));
        this.asbieMap = bieSet.getAsbieList().stream()
                .collect(Collectors.groupingBy(Asbie::getFromAbieId));
        this.bbieMap = bieSet.getBbieList().stream()
                .collect(Collectors.groupingBy(Bbie::getFromAbieId));
        this.bbieByIdMap = bieSet.getBbieList().stream()
                .collect(Collectors.toMap(Bbie::getBbieId, Function.identity()));
        this.asbiepMap = bieSet.getAsbiepList().stream()
                .collect(Collectors.toMap(Asbiep::getAsbiepId, Function.identity()));
        this.bbiepMap = bieSet.getBbiepList().stream()
                .collect(Collectors.toMap(Bbiep::getBbiepId, Function.identity()));
        this.bbieScMap = bieSet.getBbieScList().stream()
                .collect(Collectors.groupingBy(BbieSc::getBbieId));
        this.bbieScByIdMap = bieSet.getBbieScList().stream()
                .collect(Collectors.toMap(BbieSc::getBbieScId, Function.identity()));
    }

    void with(CcDocument ccDocument) {
        this.ccDocument = ccDocument;
    }

    @Override
    public Asbiep getRootAsbiep() {
        return asbiepMap.get(topLevelAsbiep.asbiepId());
    }

    @Override
    public Abie getAbie(Asbiep asbiep) {
        if (asbiep == null) {
            return null;
        }
        return abieMap.get(asbiep.getRoleOfAbieId());
    }

    @Override
    public Collection<BieAssociation> getAssociations(Abie abie) {
        if (abie == null) {
            return Collections.emptyList();
        }

        Map<String, BieAssociation> bieAssociations = associationIndex(abie);
        return BieAssociationPaths.getAssociationsRegardingBases("", ccDocument,
                        ccDocument.getAcc(abie.getBasedAccManifestId())).stream()
                .map(association -> bieAssociations.get(associationTag(association.getCcAssociation())))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Map<String, BieAssociation> associationIndex(Abie abie) {
        return Stream.concat(
                        asbieMap.getOrDefault(abie.getAbieId(), Collections.emptyList()).stream(),
                        bbieMap.getOrDefault(abie.getAbieId(), Collections.emptyList()).stream())
                .collect(Collectors.toMap(e -> e.isAsbie()
                        ? "ASCC-" + ((Asbie) e).getBasedAsccManifestId()
                        : "BCC-" + ((Bbie) e).getBasedBccManifestId(), Function.identity()));
    }

    @Override
    public Asbiep getAsbiep(Asbie asbie) {
        if (asbie == null) {
            return null;
        }

        return this.asbiepMap.get(asbie.getToAsbiepId());
    }

    @Override
    public Bbie getBbie(BbieId bbieId) {
        return this.bbieByIdMap.get(bbieId);
    }

    @Override
    public Bbiep getBbiep(Bbie bbie) {
        if (bbie == null) {
            return null;
        }

        return this.bbiepMap.get(bbie.getToBbiepId());
    }

    @Override
    public BbieSc getBbieSc(BbieScId bbieScId) {
        return this.bbieScByIdMap.get(bbieScId);
    }

    @Override
    public List<BbieSc> getBbieScList(Bbie bbie) {
        if (bbie == null) {
            return Collections.emptyList();
        }

        List<BbieSc> bbieScList = this.bbieScMap.getOrDefault(bbie.getBbieId(), Collections.emptyList());
        Collections.sort(bbieScList, Comparator.comparing(e -> e.getBbieScId().value()));
        return bbieScList;
    }

    private class BieVisitContextImpl implements BieVisitContext {

        private final BieDocumentImpl bieDocument;
        private final String occurrencePath;
        private final String parentOccurrencePath;

        BieVisitContextImpl(BieDocumentImpl bieDocument, String occurrencePath, String parentOccurrencePath) {
            this.bieDocument = bieDocument;
            this.occurrencePath = occurrencePath;
            this.parentOccurrencePath = parentOccurrencePath;
        }

        @Override
        public BieDocumentImpl getBieDocument() {
            return bieDocument;
        }

        @Override
        public String getOccurrencePath() {
            return occurrencePath;
        }

        @Override
        public String getParentOccurrencePath() {
            return parentOccurrencePath;
        }

    }

    @Override
    public void accept(BieVisitor visitor) {
        visitor.visitStart(topLevelAsbiep, new BieVisitContextImpl(this, null, null));
        accept(visitor, getRootAsbiep(), null);
        visitor.visitEnd(topLevelAsbiep, new BieVisitContextImpl(this, null, null));
    }

    @Override
    public Map<Asbie, Asbiep> getRefAsbieMap() {
        Map<Asbie, Asbiep> refAsbieMap = new LinkedHashMap<>();
        for (List<Asbie> asbieList : asbieMap.values()) {
            for (Asbie asbie : asbieList) {
                Asbiep asbiep = getAsbiep(asbie);
                if (!asbie.getOwnerTopLevelAsbiepId().equals(asbiep.getOwnerTopLevelAsbiepId())) {
                    refAsbieMap.put(asbie, asbiep);
                }
            }
        }
        return refAsbieMap;
    }

    private void accept(BieVisitor visitor, Asbiep asbiep, String parentPath) {
        if (asbiep == null) {
            return;
        }
        String path = appendPath(parentPath, "ASCCP-" + asbiep.getBasedAsccpManifestId());
        BieVisitContextImpl context = new BieVisitContextImpl(this, path, parentPath);
        if (visitor.visitAsbiep(asbiep, context) != SKIP_SUBTREE) {
            accept(visitor, getAbie(asbiep), path);
        }
    }

    private void accept(BieVisitor visitor, Abie abie, String parentPath) {
        if (abie == null) {
            return;
        }
        String path = appendPath(parentPath, "ACC-" + abie.getBasedAccManifestId());
        BieVisitContextImpl context = new BieVisitContextImpl(this, path, parentPath);
        if (visitor.visitAbie(abie, context) == SKIP_SUBTREE) {
            return;
        }
        Map<String, BieAssociation> bieAssociations = associationIndex(abie);
        for (Association source : BieAssociationPaths.getAssociationsRegardingBases(
                parentPath, ccDocument, ccDocument.getAcc(abie.getBasedAccManifestId()))) {
            BieAssociation association = bieAssociations.get(associationTag(source.getCcAssociation()));
            if (association != null) {
                accept(visitor, association, source.getPath(), path);
            }
        }
    }

    private String associationTag(CcAssociation association) {
        return association.isAscc()
                ? "ASCC-" + ((AsccSummaryRecord) association).asccManifestId()
                : "BCC-" + ((BccSummaryRecord) association).bccManifestId();
    }

    private void accept(BieVisitor visitor, BieAssociation association, String path, String parentPath) {
        BieVisitContextImpl context = new BieVisitContextImpl(this, path, parentPath);
        if (association.isAsbie()) {
            Asbie asbie = (Asbie) association;
            if (visitor.visitAsbie(asbie, context) != SKIP_SUBTREE) {
                accept(visitor, getAsbiep(asbie), path);
            }
        } else if (association.isBbie()) {
            Bbie bbie = (Bbie) association;
            if (visitor.visitBbie(bbie, context) == SKIP_SUBTREE) {
                return;
            }
            Bbiep bbiep = getBbiep(bbie);
            // BBIEP enriches the same BBIE occurrence; its immutable key remains the BCC path.
            if (visitor.visitBbiep(bbiep, context) == SKIP_SUBTREE) {
                return;
            }
            var bccp = ccDocument.getBccp(bbiep.getBasedBccpManifestId());
            for (BbieSc sc : getBbieScList(bbie)) {
                var dtSc = ccDocument.getDtSc(sc.getBasedDtScManifestId());
                String scPath = appendPath(path, "BCCP-" + bccp.bccpManifestId()
                        + ">DT-" + dtSc.ownerDtManifestId() + ">DT_SC-" + dtSc.dtScManifestId());
                visitor.visitBbieSc(sc, new BieVisitContextImpl(this, scPath, path));
            }
        }
    }

    private static String appendPath(String parent, String segment) {
        return parent == null ? segment : parent + ">" + segment;
    }
}
