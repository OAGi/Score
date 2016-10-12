package org.oagi.srt.web.jsf.component.treetable;

import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class BIETreeTable {

    @Autowired
    private DataTypeRepository dataTypeRepository;

    @Autowired
    private DataTypeSupplementaryComponentRepository dtScRepository;

    @Autowired
    private AggregateCoreComponentRepository accRepository;

    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;

    @Autowired
    private TopLevelConceptRepository topLevelConceptRepository;

    @Autowired
    private BasicCoreComponentRepository bccRepository;

    @Autowired
    private AssociationCoreComponentRepository asccRepository;

    @Autowired
    private BasicCoreComponentPropertyRepository bccpRepository;

    @Autowired
    private AggregateBusinessInformationEntityRepository abieRepository;

    @Autowired
    private AssociationBusinessInformationEntityRepository asbieRepository;

    @Autowired
    private AssociationBusinessInformationEntityPropertyRepository asbiepRepository;

    @Autowired
    private BasicBusinessInformationEntityRepository bbieRepository;

    @Autowired
    private BasicBusinessInformationEntityPropertyRepository bbiepRepository;

    @Autowired
    private BasicBusinessInformationEntitySupplementaryComponentRepository bbiescRepository;

    @Autowired
    private BusinessContextRepository businessContextRepository;

    @Autowired
    private BusinessContextValueRepository businessContextValueRepository;

    @Autowired
    private BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository;

    @Autowired
    private BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository bdtScPriRestriRepository;

    public Node createNode(AssociationCoreComponentProperty asccp, AggregateBusinessInformationEntity abie) {
        TopLevelNode topLevelNode = new TopLevelNode(asccp, abie);
        appendChildren(abie, topLevelNode);
        return topLevelNode;
    }

    private void appendChildren(AggregateBusinessInformationEntity abie, Node parent) {
        List<BasicBusinessInformationEntity> list_01 = getBBIEListByFromAbie(abie);
        List<AssociationBusinessInformationEntity> list_02 = getASBIEListByFromAbie(abie);

        Map<BusinessInformationEntity, Double> sequence = new HashMap();
        ValueComparator bvc = new ValueComparator(sequence);
        TreeMap<BusinessInformationEntity, Double> ordered_sequence = new TreeMap(bvc);

        for (BasicBusinessInformationEntity bbieVO : list_01) {
            double sk = bbieVO.getSeqKey();
            if (getEntityType(bbieVO.getBasedBccId()) == 0L) {
                appendBBIE(bbieVO, parent);
            } else {
                sequence.put(bbieVO, sk);
            }
        }

        for (AssociationBusinessInformationEntity asbieVO : list_02) {
            double sk = asbieVO.getSeqKey();
            sequence.put(asbieVO, sk);
        }

        ordered_sequence.putAll(sequence);
        Set set = ordered_sequence.entrySet();
        Iterator i = set.iterator();
        while (i.hasNext()) {
            Map.Entry me = (Map.Entry) i.next();
            if (me.getKey() instanceof BasicBusinessInformationEntity) {
                appendBBIE((BasicBusinessInformationEntity) me.getKey(), parent);
            } else {
                appendASBIE((AssociationBusinessInformationEntity) me.getKey(), parent);
            }
        }
    }

    private List<BasicBusinessInformationEntity> getBBIEListByFromAbie(AggregateBusinessInformationEntity abie) {
        return bbieRepository.findByFromAbieId(abie.getAbieId());
    }

    private List<AssociationBusinessInformationEntity> getASBIEListByFromAbie(AggregateBusinessInformationEntity abie) {
        return asbieRepository.findByFromAbieId(abie.getAbieId());
    }

    private class ValueComparator implements Comparator<BusinessInformationEntity> {

        Map<BusinessInformationEntity, Double> base;

        public ValueComparator(Map<BusinessInformationEntity, Double> base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with equals.
        public int compare(BusinessInformationEntity a, BusinessInformationEntity b) {
            if (base.get(a) <= base.get(b)) {
                return -1;
            } else {
                return 1;
            } // returning 0 would merge keys
        }
    }

    private int getEntityType(long bccId) {
        BasicCoreComponent basicCoreComponent = bccRepository.findOne(bccId);
        return basicCoreComponent.getEntityType();
    }

    private void appendBBIE(BasicBusinessInformationEntity bbie, Node parent) {
        BasicBusinessInformationEntityProperty bbiep = bbiepRepository.findOne(bbie.getToBbiepId());
        BasicCoreComponentProperty bccp = bccpRepository.findOne(bbiep.getBasedBccpId());

        BBIENode bbieNode = new BBIENode(bbie, bbiep, bccp);
        parent.addChild(bbieNode);

        appendBBIESC(bbie, bbieNode);
    }

    private void appendASBIE(AssociationBusinessInformationEntity asbie, Node parent) {
        AssociationBusinessInformationEntityProperty asbiep = asbiepRepository.findOne(asbie.getToAsbiepId());
        AssociationCoreComponentProperty asccp = asccpRepository.findOne(asbiep.getBasedAsccpId());
        AssociationCoreComponent ascc = asccRepository.findOne(asbie.getBasedAsccId());
        AggregateBusinessInformationEntity abie = abieRepository.findOne(asbiep.getRoleOfAbieId());
        AggregateCoreComponent acc = accRepository.findOne(abie.getBasedAccId());

        ASBIENode asbieNode = new ASBIENode(asbie, asbiep, asccp, abie);
        parent.addChild(asbieNode);

        appendChildren(abie, asbieNode);
    }

    private void appendBBIESC(BasicBusinessInformationEntity bbie, Node parent) {
        List<BasicBusinessInformationEntitySupplementaryComponent> list_01 =
                bbiescRepository.findByBbieId(bbie.getBbieId());
        for (BasicBusinessInformationEntitySupplementaryComponent bbiesc : list_01) {
            DataTypeSupplementaryComponent dtsc = dtScRepository.findOne(bbiesc.getDtScId());

            BBIESCNode bbiescNode = new BBIESCNode(bbiesc, dtsc);
            parent.addChild(bbiescNode);
        }
    }

}
