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
import org.oagi.score.gateway.http.api.bie_management.service.BieVisitContext;
import org.oagi.score.gateway.http.api.bie_management.service.BieVisitor;
import org.oagi.score.gateway.http.api.cc_management.model.CcAssociation;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        Map<String, BieAssociation> bieAssociations = Stream.concat(
                this.asbieMap.getOrDefault(abie.getAbieId(), Collections.emptyList()).stream(),
                this.bbieMap.getOrDefault(abie.getAbieId(), Collections.emptyList()).stream())
                .collect(Collectors.toMap(e -> (e.isAsbie() ? "ASCC-" + ((Asbie) e).getBasedAsccManifestId() :
                        "BCC-" + ((Bbie) e).getBasedBccManifestId()), Function.identity()));

        List<BieAssociation> associations = new ArrayList();
        AccManifestId basedAccManifestId = abie.getBasedAccManifestId();
        AccSummaryRecord acc = ccDocument.getAcc(basedAccManifestId);
        Stack<AccSummaryRecord> accStack = new Stack();
        while (acc != null) {
            accStack.push(acc);
            acc = ccDocument.getAcc(acc.basedAccManifestId());
        }

        while (!accStack.isEmpty()) {
            acc = accStack.pop();
            getAssociations(acc).forEach(ccAssociation -> {
                if (ccAssociation.isAscc()) {
                    BieAssociation asbie = bieAssociations.get("ASCC-" + ((AsccSummaryRecord) ccAssociation).asccManifestId());
                    if (asbie != null) {
                        associations.add(asbie);
                    }
                } else if (ccAssociation.isBcc()) {
                    BieAssociation bbie = bieAssociations.get("BCC-" + ((BccSummaryRecord) ccAssociation).bccManifestId());
                    if (bbie != null) {
                        associations.add(bbie);
                    }
                }
            });
        }

        return associations;
    }

    private Collection<CcAssociation> getAssociations(AccSummaryRecord acc) {
        List<CcAssociation> associations = ccDocument.getAssociations(acc);
        List<CcAssociation> nextAssociations = new ArrayList();
        for (int i = 0, len = associations.size(); i < len; ++i) {
            CcAssociation association = associations.get(i);
            if (association.isAscc()) {
                AsccSummaryRecord asccManifest = (AsccSummaryRecord) association;
                AsccpSummaryRecord asccp =
                        ccDocument.getAsccp(asccManifest.toAsccpManifestId());

                AccSummaryRecord roleOfAcc =
                        ccDocument.getAcc(asccp.roleOfAccManifestId());
                if (roleOfAcc.isGroup()) {
                    nextAssociations.addAll(ccDocument.getAssociations(roleOfAcc));
                    continue;
                }
            }
            nextAssociations.add(association);
        }
        return nextAssociations;
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

        private BieDocumentImpl bieDocument;

        BieVisitContextImpl(BieDocumentImpl bieDocument) {
            this.bieDocument = bieDocument;
        }

        @Override
        public BieDocumentImpl getBieDocument() {
            return bieDocument;
        }
    }

    @Override
    public void accept(BieVisitor visitor) {
        BieVisitContext context = new BieVisitContextImpl(this);
        visitor.visitStart(topLevelAsbiep, context);
        accept(visitor, getRootAsbiep(), context);
        visitor.visitEnd(topLevelAsbiep, context);
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

    private void accept(BieVisitor visitor, Asbiep asbiep, BieVisitContext context) {
        visitor.visitAsbiep(asbiep, context);
        accept(visitor, getAbie(asbiep), context);
    }

    private void accept(BieVisitor visitor, Abie abie, BieVisitContext context) {
        visitor.visitAbie(abie, context);
        for (BieAssociation bieAssociation : getAssociations(abie)) {
            accept(visitor, bieAssociation, context);
        }
    }

    private void accept(BieVisitor visitor, BieAssociation bieAssociation, BieVisitContext context) {
        if (bieAssociation.isAsbie()) {
            visitor.visitAsbie((Asbie) bieAssociation, context);
            accept(visitor, getAsbiep((Asbie) bieAssociation), context);
        } else if (bieAssociation.isBbie()) {
            Bbie bbie = (Bbie) bieAssociation;
            visitor.visitBbie(bbie, context);
            visitor.visitBbiep(getBbiep(bbie), context);
            for (BbieSc bbieSc : getBbieScList(bbie)) {
                visitor.visitBbieSc(bbieSc, context);
            }
        }
    }

}
