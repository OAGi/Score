package org.oagi.score.service.bie.model;

import lombok.Data;
import org.oagi.score.repo.api.bie.model.*;
import org.oagi.score.repo.api.corecomponent.model.*;
import org.oagi.score.service.bie.BieDocument;
import org.oagi.score.service.bie.BieVisitContext;
import org.oagi.score.service.bie.BieVisitor;
import org.oagi.score.service.corecomponent.CcDocument;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class BieDocumentImpl implements BieDocument {

    private TopLevelAsbiep topLevelAsbiep;
    private Map<BigInteger, Abie> abieMap;
    private Map<BigInteger, List<Asbie>> asbieMap;
    private Map<BigInteger, List<Bbie>> bbieMap;
    private Map<BigInteger, Bbie> bbieByIdMap;
    private Map<BigInteger, Asbiep> asbiepMap;
    private Map<BigInteger, Bbiep> bbiepMap;
    private Map<BigInteger, List<BbieSc>> bbieScMap;
    private Map<BigInteger, BbieSc> bbieScByIdMap;

    private CcDocument ccDocument;

    BieDocumentImpl(BiePackage biePackage) {
        this.topLevelAsbiep = biePackage.getTopLevelAsbiep();
        this.abieMap = biePackage.getAbieList().stream()
                .collect(Collectors.toMap(Abie::getAbieId, Function.identity()));
        this.asbieMap = biePackage.getAsbieList().stream()
                .collect(Collectors.groupingBy(Asbie::getFromAbieId));
        this.bbieMap = biePackage.getBbieList().stream()
                .collect(Collectors.groupingBy(Bbie::getFromAbieId));
        this.bbieByIdMap = biePackage.getBbieList().stream()
                .collect(Collectors.toMap(Bbie::getBbieId, Function.identity()));
        this.asbiepMap = biePackage.getAsbiepList().stream()
                .collect(Collectors.toMap(Asbiep::getAsbiepId, Function.identity()));
        this.bbiepMap = biePackage.getBbiepList().stream()
                .collect(Collectors.toMap(Bbiep::getBbiepId, Function.identity()));
        this.bbieScMap = biePackage.getBbieScList().stream()
                .collect(Collectors.groupingBy(BbieSc::getBbieId));
        this.bbieScByIdMap = biePackage.getBbieScList().stream()
                .collect(Collectors.toMap(BbieSc::getBbieScId, Function.identity()));
    }

    void with(CcDocument ccDocument) {
        this.ccDocument = ccDocument;
    }

    @Override
    public Asbiep getRootAsbiep() {
        return asbiepMap.get(topLevelAsbiep.getAsbiepId());
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
        BigInteger basedAccManifestId = abie.getBasedAccManifestId();
        AccManifest accManifest = ccDocument.getAccManifest(basedAccManifestId);
        Stack<AccManifest> accManifestStack = new Stack();
        while (accManifest != null) {
            accManifestStack.push(accManifest);
            accManifest = ccDocument.getBasedAccManifest(accManifest);
        }

        while (!accManifestStack.isEmpty()) {
            accManifest = accManifestStack.pop();
            getAssociations(accManifest).forEach(ccAssociation -> {
                if (ccAssociation.isAscc()) {
                    BieAssociation asbie = bieAssociations.get("ASCC-" + ((AsccManifest) ccAssociation).getAsccManifestId());
                    if (asbie != null) {
                        associations.add(asbie);
                    }
                } else if (ccAssociation.isBcc()) {
                    BieAssociation bbie = bieAssociations.get("BCC-" + ((BccManifest) ccAssociation).getBccManifestId());
                    if (bbie != null) {
                        associations.add(bbie);
                    }
                }
            });
        }

        return associations;
    }

    private Collection<CcAssociation> getAssociations(AccManifest accManifest) {
        List<CcAssociation> associations = ccDocument.getAssociations(accManifest);
        List<CcAssociation> nextAssociations = new ArrayList();
        for (int i = 0, len = associations.size(); i < len; ++i) {
            CcAssociation association = associations.get(i);
            if (association.isAscc()) {
                AsccManifest asccManifest = (AsccManifest) association;
                AsccpManifest asccpManifest =
                        ccDocument.getAsccpManifest(asccManifest.getToAsccpManifestId());
                AccManifest roleOfAccManifest =
                        ccDocument.getRoleOfAccManifest(asccpManifest);
                Acc acc = ccDocument.getAcc(roleOfAccManifest);
                if (acc.isGroup()) {
                    nextAssociations.addAll(ccDocument.getAssociations(roleOfAccManifest));
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
    public Bbie getBbie(BigInteger bbieId) {
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
    public BbieSc getBbieSc(BigInteger bbieScId) {
        return this.bbieScByIdMap.get(bbieScId);
    }

    @Override
    public List<BbieSc> getBbieScList(Bbie bbie) {
        if (bbie == null) {
            return Collections.emptyList();
        }

        List<BbieSc> bbieScList = this.bbieScMap.getOrDefault(bbie.getBbieId(), Collections.emptyList());
        Collections.sort(bbieScList, Comparator.comparing(BbieSc::getBbieScId));
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
