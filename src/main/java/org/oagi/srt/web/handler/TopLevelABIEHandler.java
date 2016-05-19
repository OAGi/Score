package org.oagi.srt.web.handler;

import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.web.handler.BusinessContextHandler.BusinessContextValues;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.ItemSelectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
public class TopLevelABIEHandler implements Serializable {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final long serialVersionUID = -2650693005373031742L;

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Value("${spring.datasource.platform}")
    private String platform;

    private AssociationCoreComponentRepository asccRepository;
    private BasicCoreComponentRepository bccRepository;
    private AggregateCoreComponentRepository accRepository;
    private AssociationCoreComponentPropertyRepository asccpRepository;
    private BasicCoreComponentPropertyRepository bccpRepository;

    private AggregateBusinessInformationEntityRepository abieRepository;
    private AssociationBusinessInformationEntityRepository asbieRepository;
    private AssociationBusinessInformationEntityPropertyRepository asbiepRepository;
    private BasicBusinessInformationEntityRepository bbieRepository;
    private BasicBusinessInformationEntityPropertyRepository bbiepRepository;
    private BasicBusinessInformationEntitySupplementaryComponentRepository bbiescRepository;
    private DataTypeRepository dtRepository;
    private DataTypeSupplementaryComponentRepository dtScRepository;

    private BusinessContextRepository businessContextRepository;
    private BusinessContextValueRepository businessContextValueRepository;
    private BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository;
    private CodeListRepository codeListRepository;

    private UserRepository userRepository;

    private int abieCount = 0;
    private int bbiescCount = 0;
    private int asbiepCount = 0;
    private int asbieCount = 0;
    private int bbiepCount = 0;
    private int bbieCount = 0;

    private BarChartModel barModel;

    private String propertyTerm;
    private String den;
    private String definition;
    private List<AssociationCoreComponentProperty> asccpVOs;

    private AssociationCoreComponentProperty selected;
    private BusinessContext bCSelected;

    private int maxABIEId;
    private int maxASBIEPId;
    private int maxBIEPID;
    private int maxBIEID;
    private int maxBBIESCID;

    @PostConstruct
    private void init() {
        asccRepository = repositoryFactory.associationCoreComponentRepository();
        bccRepository = repositoryFactory.basicCoreComponentRepository();
        accRepository = repositoryFactory.aggregateCoreComponentRepository();
        asccpRepository = repositoryFactory.associationCoreComponentPropertyRepository();
        bccpRepository = repositoryFactory.basicCoreComponentPropertyRepository();

        abieRepository = repositoryFactory.aggregateBusinessInformationEntityRepository();
        asbieRepository =
                repositoryFactory.associationBusinessInformationEntityRepository();
        asbiepRepository =
                repositoryFactory.associationBusinessInformationEntityPropertyRepository();
        bbieRepository =
                repositoryFactory.basicBusinessInformationEntityRepository();
        bbiepRepository =
                repositoryFactory.basicBusinessInformationEntityPropertyRepository();
        bbiescRepository =
                repositoryFactory.basicBusinessInformationEntitySupplementaryComponentRepository();
        dtRepository = repositoryFactory.dataTypeRepository();
        dtScRepository = repositoryFactory.dataTypeSupplementaryComponentRepository();

        businessContextRepository = repositoryFactory.businessContextRepository();
        businessContextValueRepository = repositoryFactory.businessContextValueRepository();
        bdtPriRestriRepository =
                repositoryFactory.businessDataTypePrimitiveRestrictionRepository();
        codeListRepository = repositoryFactory.codeListRepository();

        userRepository = repositoryFactory.userRepository();

        maxABIEId = asbieRepository.findGreatestId();
        maxASBIEPId = asbiepRepository.findGreatestId();
        maxBIEPID = bbiepRepository.findGreatestId();
        maxBIEID = bbieRepository.findGreatestId();
        maxBBIESCID = bbiescRepository.findGreatestId();

        asccpVOs = asccpRepository.findAll();
    }

    public BarChartModel getBarModel() {
        return barModel;
    }

    private int barCount = 20;

    private int getMax() {
        int max = 0;
        if (abieCount > max)
            max = abieCount;
        if (bbiescCount > max)
            max = bbiescCount;
        if (asbiepCount > max)
            max = asbiepCount;
        if (asbieCount > max)
            max = asbieCount;
        if (bbiepCount > max)
            max = bbiepCount;
        if (bbieCount > max)
            max = bbieCount;
        return max;
    }

    private void createBarModel() {
        barCount = getMax();

        barModel = initBarModel();

        barModel.setTitle("Number of items created");
        barModel.setLegendPosition("ne");

        Axis xAxis = barModel.getAxis(AxisType.X);
        xAxis.setLabel("Tables");

        Axis yAxis = barModel.getAxis(AxisType.Y);
        yAxis.setLabel("");
        yAxis.setMin(0);
        yAxis.setMax(barCount + barCount / 10);
        yAxis.setTickInterval(String.valueOf(barCount / 10));
    }


    private BarChartModel initBarModel() {
        BarChartModel model = new BarChartModel();

        ChartSeries tabie = new ChartSeries();
        tabie.setLabel("ABIE");
        tabie.set("", abieCount);

        ChartSeries tasbiep = new ChartSeries();
        tasbiep.setLabel("ASBIEP");
        tasbiep.set("", asbiepCount);

        ChartSeries tasbie = new ChartSeries();
        tasbie.setLabel("ASBIE");
        tasbie.set("", asbieCount);

        ChartSeries tbbie = new ChartSeries();
        tbbie.setLabel("BBIE");
        tbbie.set("", bbieCount);

        ChartSeries tbbiep = new ChartSeries();
        tbbiep.setLabel("BBIEP");
        tbbiep.set("", bbiepCount);

        ChartSeries tbbiesc = new ChartSeries();
        tbbiesc.setLabel("BBIE_SC");
        tbbiesc.set("", bbiescCount);

        model.addSeries(tabie);
        model.addSeries(tasbiep);
        model.addSeries(tasbie);

        model.addSeries(tbbie);
        model.addSeries(tbbiep);
        model.addSeries(tbbiesc);

        return model;
    }

    public void itemSelect(ItemSelectEvent event) {
        String str = "";
        switch (event.getSeriesIndex()) {
            case 0:
                str = "ABIE: " + abieCount;
                break;
            case 1:
                str = "ASBIEP: " + asbiepCount;
                break;
            case 2:
                str = "ASBIE: " + asbieCount;
                break;
            case 3:
                str = "BBIE: " + bbieCount;
                break;
            case 4:
                str = "BBIEP: " + bbiepCount;
                break;
            case 5:
                str = "BBIE_SC: " + bbiescCount;
                break;
            default:
                str = "";
                break;
        }

        FacesMessage msg = new FacesMessage(str,
                "Item Index: " + event.getItemIndex() + ", Series Index:" + event.getSeriesIndex());

        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    private boolean skip;

    private List<ABIEView> bodList = new ArrayList<ABIEView>();
    private ABIEView selectedBod;

    public ABIEView getSelectedBod() {
        return selectedBod;
    }

    public void setSelectedBod(ABIEView selectedBod) {
        this.selectedBod = selectedBod;
    }

    public List<ABIEView> getBodList() {
        if (bodList.isEmpty()) {

            List<AggregateBusinessInformationEntity> aggregateBusinessInformationEntities =
                    abieRepository.findByTopLevel(true);
            for (AggregateBusinessInformationEntity aggregateBusinessInformationEntity : aggregateBusinessInformationEntities) {
                AssociationBusinessInformationEntityProperty associationBusinessInformationEntityProperty =
                        asbiepRepository.findOneByRoleOfAbieId(aggregateBusinessInformationEntity.getAbieId());

                AssociationCoreComponentProperty associationCoreComponentProperty =
                        asccpRepository.findOneByAsccpId(associationBusinessInformationEntityProperty.getBasedAsccpId());

                BusinessContext businessContext =
                        businessContextRepository.findOneByBusinessContextId(aggregateBusinessInformationEntity.getBizCtxId());

                aggregateBusinessInformationEntity.setBizCtxName(businessContext.getName());
                ABIEView aABIEView = new ABIEView(
                        repositoryFactory,
                        associationCoreComponentProperty.getPropertyTerm(),
                        aggregateBusinessInformationEntity.getAbieId(), "ABIE");
                aABIEView.setAbie(aggregateBusinessInformationEntity);
                bodList.add(aABIEView);
            }
        }
        return bodList;
    }

    public void setBodList(List<ABIEView> bodList) {
        this.bodList = bodList;
    }

    public void save() {
        FacesMessage msg = new FacesMessage("Successful", "Welcome :" + "");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public void search() {
        asccpVOs = asccpRepository.findByPropertyTermContaining(getPropertyTerm());
    }

    public void addMessage(String summary) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary, null);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    private AggregateBusinessInformationEntity topAbieVO;
    private AssociationBusinessInformationEntityProperty asbiepVO;
    private BasicBusinessInformationEntity bieVO;

    public String onFlowProcess(FlowEvent event) {

        if (event.getNewStep().equals(SRTConstants.TAB_TOP_LEVEL_ABIE_SELECT_BC)) {

        } else if (event.getNewStep().equals(SRTConstants.TAB_TOP_LEVEL_ABIE_CREATE_UC_BIE)) {
            // TODO if go back from the confirmation page? avoid that situation

            createBIEs();
            createBarModel();
        }

        return event.getNewStep();
    }

    @Transactional(rollbackFor = Throwable.class)
    private void createBIEs() {
        AggregateCoreComponent aggregateCoreComponent =
                accRepository.findOneByAccId(selected.getRoleOfAccId());
        topAbieVO = createABIE(aggregateCoreComponent, bCSelected.getBizCtxId(), true);
        int abieId = topAbieVO.getAbieId();

        ABIEView rootABIEView = new ABIEView(repositoryFactory, selected.getPropertyTerm(), abieId, "ABIE");
        rootABIEView.setAbie(topAbieVO);
        root = new DefaultTreeNode(rootABIEView, null);

        asbiepVO = createASBIEP(selected, abieId);

        aABIEView = new ABIEView(repositoryFactory, selected.getPropertyTerm(), abieId, "ABIE");
        aABIEView.setAbie(topAbieVO);
        aABIEView.setColor("blue");
        aABIEView.setAcc(aggregateCoreComponent);
        aABIEView.setAsbiep(asbiepVO);

        TreeNode toplevelNode = new DefaultTreeNode(aABIEView, root);
        createBIEs(selected.getRoleOfAccId(), abieId);
        createBIEChildren(abieId, toplevelNode);
    }

    public String onFlowProcess_Copy(FlowEvent event) {

        if (event.getNewStep().equals(SRTConstants.TAB_TOP_LEVEL_ABIE_SELECT_BC)) {

        } else if (event.getNewStep().equals(SRTConstants.TAB_TOP_LEVEL_ABIE_COPY_UC_BIE)) {
            // TODO if go back from the confirmation page? avoid that situation

            AggregateBusinessInformationEntity selectedAggregateBusinessInformationEntity =
                    selectedBod.getAbie();
            AggregateCoreComponent aggregateCoreComponent =
                    accRepository.findOneByAccId(selectedAggregateBusinessInformationEntity.getBasedAccId());

            AssociationCoreComponentProperty associationCoreComponentProperty =
                    asccpRepository.findOneByRoleOfAccId(aggregateCoreComponent.getAccId());

            topAbieVO = copyABIE(selectedAggregateBusinessInformationEntity, bCSelected.getBizCtxId(), true);
            int abieId = topAbieVO.getAbieId();

            ABIEView rootABIEView = new ABIEView(repositoryFactory, associationCoreComponentProperty.getPropertyTerm(), abieId, "ABIE");
            rootABIEView.setAbie(topAbieVO);
            root = new DefaultTreeNode(rootABIEView, null);
            AssociationBusinessInformationEntityProperty associationBusinessInformationEntityProperty =
                    copyASBIEP(asbiepRepository.
                            findOneByRoleOfAbieId(selectedAggregateBusinessInformationEntity.getAbieId()), abieId);
            aABIEView = new ABIEView(repositoryFactory, associationCoreComponentProperty.getPropertyTerm(), abieId, "ABIE");
            aABIEView.setAbie(topAbieVO);
            aABIEView.setColor("blue");
            aABIEView.setAsbiep(associationBusinessInformationEntityProperty);
            TreeNode toplevelNode = new DefaultTreeNode(aABIEView, root);

            copyBIEs(selectedAggregateBusinessInformationEntity, topAbieVO, associationBusinessInformationEntityProperty, -1, toplevelNode);

            createBarModel();
        }

        return event.getNewStep();
    }

    public void onComplete() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Progress Completed"));
    }

    private Integer progress;

    public Integer getProgress() {
        if (progress == null) {
            progress = 0;
        } else {
            progress = progress + (int) (Math.random() * 35);

            if (progress > 100)
                progress = 100;
        }

        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    private List<CoreComponent> getAssocList(List<CoreComponent> list) {
        for (int i = 0; i < list.size(); i++) {
            CoreComponent srt = list.get(i);
            if (srt instanceof AssociationCoreComponent && groupcheck((AssociationCoreComponent) srt)) {
                AssociationCoreComponent associationCoreComponent = (AssociationCoreComponent) srt;
                AssociationCoreComponentProperty associationCoreComponentProperty =
                        asccpRepository.findOneByAsccpId(associationCoreComponent.getToAsccpId());
                AggregateCoreComponent aggregateCoreComponent =
                        accRepository.findOneByAccId(associationCoreComponentProperty.getRoleOfAccId());
                list = handleNestedGroup(aggregateCoreComponent, list, i);
            }
        }
        return list;
    }

    private List<CoreComponent> queryNestedChildAssoc(AggregateCoreComponent aggregateCoreComponent) {
        logger.debug("enter queryNestedChildAssoc(" + aggregateCoreComponent.getAccId() + ")");
        long currentTimeMillis = System.currentTimeMillis();

        List<BasicCoreComponent> bcc_tmp_assoc = getBCCwoAttribute(aggregateCoreComponent.getAccId());
        List<AssociationCoreComponent> ascc_tmp_assoc = getASCC(aggregateCoreComponent.getAccId());

        int size = bcc_tmp_assoc.size() + ascc_tmp_assoc.size();
        List<CoreComponent> tmp_assoc = new ArrayList(size);
        tmp_assoc.addAll(bcc_tmp_assoc);
        tmp_assoc.addAll(ascc_tmp_assoc);

        ArrayList<CoreComponent> assoc = new ArrayList(size);
        CoreComponent a = new CoreComponent();
        for (int i = 0; i < size; i++)
            assoc.add(a);

        if ("oracle".equalsIgnoreCase(platform)) {
            tmp_assoc = getAssocList(tmp_assoc);
        }

        int attribute_cnt = 0;
        for (CoreComponent coreComponent : tmp_assoc) {
            if (coreComponent instanceof BasicCoreComponent) {
                BasicCoreComponent basicCoreComponent = (BasicCoreComponent) coreComponent;
                if (basicCoreComponent.getSeqKey() == 0) {
                    assoc.set(attribute_cnt, basicCoreComponent);
                    attribute_cnt++;
                } else if (basicCoreComponent.getSeqKey() > 0) {
                    assoc.set(basicCoreComponent.getSeqKey() - 1 + attribute_cnt, basicCoreComponent);
                }
            } else {
                AssociationCoreComponent associationCoreComponent = (AssociationCoreComponent) coreComponent;
                assoc.set(associationCoreComponent.getSeqKey() - 1 + attribute_cnt, associationCoreComponent);
            }
        }

        assoc.trimToSize();

        try {
            if ("mysql".equalsIgnoreCase(platform)) {
                return getAssocList(assoc);
            } else {
                return assoc;
            }
        } finally {
            logger.debug("leave queryNestedChildAssoc(" + aggregateCoreComponent.getAccId() + "), elapsed time(ms): " +
                    (System.currentTimeMillis() - currentTimeMillis));
        }
    }

    private List<CoreComponent> queryNestedChildAssoc_wo_attribute(AggregateCoreComponent aggregateCoreComponent) {
        logger.debug("enter queryNestedChildAssoc_wo_attribute(" + aggregateCoreComponent.getAccId() + ")");
        long currentTimeMillis = System.currentTimeMillis();

        List<BasicCoreComponent> bcc_tmp_assoc = getBCCwoAttribute(aggregateCoreComponent.getAccId());
        List<AssociationCoreComponent> ascc_tmp_assoc = getASCC(aggregateCoreComponent.getAccId());
        int size = bcc_tmp_assoc.size() + ascc_tmp_assoc.size();
        List<CoreComponent> tmp_assoc = new ArrayList(size);
        tmp_assoc.addAll(bcc_tmp_assoc);
        tmp_assoc.addAll(ascc_tmp_assoc);

        ArrayList<CoreComponent> assoc = new ArrayList(size);
        CoreComponent a = new CoreComponent();
        for (int i = 0; i < size; i++)
            assoc.add(a);

        for (CoreComponent coreComponent : tmp_assoc) {
            if (coreComponent instanceof BasicCoreComponent) {
                BasicCoreComponent basicCoreComponent = (BasicCoreComponent) coreComponent;
                if (basicCoreComponent.getSeqKey() > 0)
                    logger.debug("BCC: " + basicCoreComponent.getFromAccId() + " ~ " + basicCoreComponent.getToBccpId() + " (" + basicCoreComponent.getSeqKey() + ")");
                assoc.set(basicCoreComponent.getSeqKey() - 1, basicCoreComponent);
            } else {
                AssociationCoreComponent associationCoreComponent = (AssociationCoreComponent) coreComponent;
                logger.debug("ASCC: " + associationCoreComponent.getFromAccId() + " ~ " + associationCoreComponent.getToAsccpId() + " (" + associationCoreComponent.getSeqKey() + ")");
                assoc.set(associationCoreComponent.getSeqKey() - 1, associationCoreComponent);
            }
        }

        assoc.trimToSize();

        try {
            if ("mysql".equalsIgnoreCase(platform)) {
                return getAssocList(assoc);
            } else {
                return assoc;
            }
        } finally {
            logger.debug("leave queryNestedChildAssoc_wo_attribute(" + aggregateCoreComponent.getAccId() + "), elapsed time(ms): " + (System.currentTimeMillis() - currentTimeMillis));
        }
    }

    private List<CoreComponent> queryChildAssoc(AggregateCoreComponent aggregateCoreComponent) {
        List<BasicCoreComponent> bcc_tmp_assoc = getBCC(aggregateCoreComponent.getAccId());
        List<AssociationCoreComponent> ascc_tmp_assoc = getASCC(aggregateCoreComponent.getAccId());
        int size = bcc_tmp_assoc.size() + ascc_tmp_assoc.size();
        List<CoreComponent> tmp_assoc = new ArrayList(size);
        tmp_assoc.addAll(bcc_tmp_assoc);
        tmp_assoc.addAll(ascc_tmp_assoc);

        ArrayList<CoreComponent> assoc = new ArrayList(size);
        CoreComponent a = new CoreComponent();
        for (int i = 0; i < size; i++)
            assoc.add(a);

        int attribute_cnt = 0;
        for (BasicCoreComponent basicCoreComponent : bcc_tmp_assoc) {
            if (basicCoreComponent.getSeqKey() == 0) {
                assoc.set(attribute_cnt, basicCoreComponent);
                attribute_cnt++;
            } else if (basicCoreComponent.getSeqKey() > 0) {
                assoc.set(basicCoreComponent.getSeqKey() - 1 + attribute_cnt, basicCoreComponent);
            }
        }
        for (AssociationCoreComponent associationCoreComponent : ascc_tmp_assoc) {
            assoc.set(associationCoreComponent.getSeqKey() - 1 + attribute_cnt, associationCoreComponent);
        }

        assoc.trimToSize();

        return assoc;
    }

    private List<CoreComponent> handleNestedGroup(
            AggregateCoreComponent aggregateCoreComponent, List<CoreComponent> coreComponents, int gPosition) {
        List<CoreComponent> list = new ArrayList();
        list.addAll(coreComponents);

        List<CoreComponent> bList = queryChildAssoc(aggregateCoreComponent);
        list.addAll(gPosition, bList);
        list.remove(gPosition + bList.size());

        for (int i = 0; i < list.size(); i++) {
            CoreComponent coreComponent = list.get(i);
            if (coreComponent instanceof AssociationCoreComponent && groupcheck((AssociationCoreComponent) coreComponent)) {
                AssociationCoreComponent associationCoreComponent = (AssociationCoreComponent) coreComponent;
                AssociationCoreComponentProperty associationCoreComponentProperty =
                        asccpRepository.findOneByAsccpId(associationCoreComponent.getToAsccpId());
                list = handleNestedGroup(accRepository.findOneByAccId(
                        associationCoreComponentProperty.getRoleOfAccId()), list, i);
            }
        }

        return list;
    }

    private boolean groupcheck(AssociationCoreComponent associationCoreComponent) {
        boolean check = false;
        AssociationCoreComponentProperty asccp =
                asccpRepository.findOneByAsccpId(associationCoreComponent.getToAsccpId());
        AggregateCoreComponent acc = accRepository.findOneByAccId(asccp.getRoleOfAccId());
        if (acc.getOagisComponentType() == 3) {
            check = true;
        }
        return check;
    }

    private void createBIEs(int accId, int abieId) {
        logger.debug("enter createBIEs(" + accId + ", " + abieId + ")");
        long currentTimeMillis = System.currentTimeMillis();

        LinkedList<AggregateCoreComponent> accList = new LinkedList();
        AggregateCoreComponent aggregateCoreComponent = getAggregateCoreComponent(accId);
        accList.add(aggregateCoreComponent);
        while (aggregateCoreComponent.getBasedAccId() > 0) {
            aggregateCoreComponent = getAggregateCoreComponent(aggregateCoreComponent.getBasedAccId());
            accList.add(aggregateCoreComponent);
        }

        while (!accList.isEmpty()) {
            aggregateCoreComponent = accList.pollFirst();
            int skb = 0;
            for (AggregateCoreComponent cnt_acc : accList) {
                skb += queryNestedChildAssoc_wo_attribute(cnt_acc).size(); //here
            }

            List<CoreComponent> childAssoc = queryNestedChildAssoc(aggregateCoreComponent);
            int attr_cnt = childAssoc.size() - queryNestedChildAssoc_wo_attribute(aggregateCoreComponent).size();
            for (int i = 0; i < childAssoc.size(); i++) {
                CoreComponent assoc = childAssoc.get(i);
                if (assoc instanceof BasicCoreComponent) {
                    BasicCoreComponent bcc = (BasicCoreComponent) assoc;
                    if (bcc.getSeqKey() == 0)
                        createBBIETree(bcc, abieId, skb);
                }
            }

            for (int i = 0; i < childAssoc.size(); i++) {
                CoreComponent assoc = childAssoc.get(i);
                if (assoc instanceof BasicCoreComponent) {
                    BasicCoreComponent bcc = (BasicCoreComponent) assoc;
                    if (bcc.getSeqKey() > 0)
                        createBBIETree(bcc, abieId, skb + i - attr_cnt);
                } else if (assoc instanceof AssociationCoreComponent) {
                    AssociationCoreComponent ascc = (AssociationCoreComponent) assoc;
                    createASBIETree(ascc, abieId, skb + i - attr_cnt, skb);
                }
            }
        }

        logger.debug("leave createBIEs(" + accId + ", " + abieId + "), elapsed time(ms): " + (System.currentTimeMillis() - currentTimeMillis));
    }

    class ValueComparator implements Comparator<BusinessInformationEntity> {

        Map<BusinessInformationEntity, Integer> base;

        public ValueComparator(Map<BusinessInformationEntity, Integer> base) {
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

    private void createBBIETree(BasicCoreComponent bccVO, int abie, int seqKey) {
        logger.debug("enter createBBIETree(" + bccVO.getBccId() + ", " + abie + ", " + seqKey + ")");
        long currentTimeMillis = System.currentTimeMillis();

        BasicCoreComponentProperty bccpVO =
                bccpRepository.findOneByBccpIdAndRevisionNum(bccVO.getToBccpId(), 0);

        BasicBusinessInformationEntityProperty bbiepVO = createBBIEP(bccpVO);

        BusinessDataTypePrimitiveRestriction aBDTPrimitiveRestrictionVO =
                bdtPriRestriRepository.findOneByBdtIdAndDefault(bccpVO.getBdtId(), true);

        int bdtPrimitiveRestrictionId = aBDTPrimitiveRestrictionVO.getBdtPriRestriId();

        BasicBusinessInformationEntity bbieVO = createBBIE(bccVO, abie, bbiepVO.getBbiepId(), seqKey, bdtPrimitiveRestrictionId);
        DataType dtVO = dtRepository.findOneByDtId(bccpVO.getBdtId());

        int bbieID = bbieVO.getBbieId();
        createBBIESC(bbieID, bccpVO.getBdtId());

        logger.debug("leave createBBIETree(" + bccVO.getBccId() + ", " + abie + ", " + seqKey +
                "), elapsed time(ms): " + (System.currentTimeMillis() - currentTimeMillis));
    }

    private void createASBIETree(AssociationCoreComponent asccVO, int abie, int seqKey, int seq_base) {
        logger.debug("enter createASBIETree(" + asccVO.getAsccId() + ", " + abie + ", " + seqKey + ")");
        long currentTimeMillis = System.currentTimeMillis();

        AssociationCoreComponentProperty asccpVO = getASCCP(asccVO.getToAsccpId());
        AggregateCoreComponent accVOFromASCCP = getAggregateCoreComponent(asccpVO.getRoleOfAccId());

        AggregateBusinessInformationEntity abieVO = createABIE(accVOFromASCCP, bCSelected.getBizCtxId(), false);
        AssociationBusinessInformationEntityProperty asbiepVO = createASBIEP(asccpVO, abieVO.getAbieId());

        AssociationBusinessInformationEntity asbieVO = createASBIE(asccVO, abie, asbiepVO.getAsbiepId(), seqKey);

        createBIEs(accVOFromASCCP.getAccId(), abieVO.getAbieId());

        logger.debug("leave createASBIETree(" + asccVO.getAsccId() + ", " + abie + ", " + seqKey +
                "), elapsed time(ms): " + (System.currentTimeMillis() - currentTimeMillis));
    }

    private List<BasicCoreComponent> getBCC(int accId) {
        return bccRepository.findByFromAccId(accId);
    }

    private List<BasicCoreComponent> getBCCwoAttribute(int accId) {
        return getBCC(accId).stream()
                .filter(basicCoreComponent -> basicCoreComponent.getSeqKey() > 0)
                .collect(Collectors.toList());
    }

    private List<AssociationCoreComponent> getASCC(int accId) {
        return asccRepository.findByFromAccId(accId);
    }

    private AssociationCoreComponentProperty getASCCP(int asccpId) {
        return asccpRepository.findOneByAsccpIdAndRevisionNum(asccpId, 0);
    }

    private AggregateCoreComponent getAggregateCoreComponent(int accId) {
        return accRepository.findOneByAccIdAndRevisionNum(accId, 0);
    }

    private List<AssociationBusinessInformationEntity> getASBIEsFromABIE(int abieId) {
        return asbieRepository.findByFromAbieId(abieId);
    }

    private List<BasicBusinessInformationEntity> getBBIEsFromABIE(int abieId) {
        return bbieRepository.findByFromAbieId(abieId);
    }

    private List<BasicBusinessInformationEntitySupplementaryComponent> getBBIESCsFromBBIE(int bbieid) {
        return bbiescRepository.findByBbieId(bbieid);
    }


    private AggregateBusinessInformationEntity createABIE(AggregateCoreComponent aggregateCoreComponent, int bizCtxId, boolean topLevel) {
        AggregateBusinessInformationEntity abie = new AggregateBusinessInformationEntity();
        String abieGuid = Utility.generateGUID();
        abie.setGuid(abieGuid);
        abie.setBasedAccId(aggregateCoreComponent.getAccId());
        abie.setDefinition(aggregateCoreComponent.getDefinition());
        abie.setTopLevel(topLevel);
        abie.setBizCtxId(bizCtxId);
        int userId = getUserId();
        abie.setCreatedBy(userId);
        abie.setLastUpdatedBy(userId);
        if (topLevel)
            abie.setState(SRTConstants.TOP_LEVEL_ABIE_STATE_EDITING);

        abieRepository.save(abie);
        abieCount++;

        return abie;
    }

    private AssociationBusinessInformationEntityProperty createASBIEP(
            AssociationCoreComponentProperty associationCoreComponentProperty, int abieId) {
        AssociationBusinessInformationEntityProperty asbiep =
                new AssociationBusinessInformationEntityProperty();
        asbiep.setGuid(Utility.generateGUID());
        asbiep.setBasedAsccpId(associationCoreComponentProperty.getAsccpId());
        asbiep.setRoleOfAbieId(abieId);
        int userId = getUserId();
        asbiep.setCreatedBy(userId);
        asbiep.setLastUpdatedBy(userId);
        asbiep.setDefinition(associationCoreComponentProperty.getDefinition());

        asbiepRepository.save(asbiep);
        asbiepCount++;

        return asbiep;
    }

    private AssociationBusinessInformationEntity createASBIE(AssociationCoreComponent asccVO, int abie, int asbiep, int seqKey) {
        AssociationBusinessInformationEntity asbie = new AssociationBusinessInformationEntity();
        asbie.setGuid(Utility.generateGUID());
        asbie.setFromAbieId(abie);
        asbie.setToAsbiepId(asbiep);
        asbie.setBasedAscc(asccVO.getAsccId());
        asbie.setCardinalityMax(asccVO.getCardinalityMax());
        asbie.setCardinalityMin(asccVO.getCardinalityMin());
        asbie.setDefinition(asccVO.getDefinition());
        int userId = getUserId();
        asbie.setCreatedBy(userId);
        asbie.setLastUpdatedBy(userId);
        asbie.setSeqKey(seqKey);

        asbieRepository.save(asbie);
        asbieCount++;

        return asbie;
    }

    private BasicBusinessInformationEntityProperty createBBIEP(BasicCoreComponentProperty bccpVO) {
        BasicBusinessInformationEntityProperty bbiep = new BasicBusinessInformationEntityProperty();
        bbiep.setGuid(Utility.generateGUID());
        bbiep.setBasedBccpId(bccpVO.getBccpId());
        int userId = getUserId();
        bbiep.setCreatedBy(userId);
        bbiep.setLastUpdatedBy(userId);
        bbiep.setDefinition(bccpVO.getDefinition());

        bbiepRepository.save(bbiep);
        bbiepCount++;

        return bbiep;
    }

    private BasicBusinessInformationEntity createBBIE(
            BasicCoreComponent bccVO, int abie, int bbiep, int seqKey, int bdtPrimitiveRestrictionId) {
        BasicBusinessInformationEntity bbie = new BasicBusinessInformationEntity();
        bbie.setGuid(Utility.generateGUID());
        bbie.setBasedBccId(bccVO.getBccId());
        bbie.setFromAbieId(abie);
        bbie.setToBbiepId(bbiep);
        bbie.setNillable(false);
        bbie.setCardinalityMax(bccVO.getCardinalityMax());
        bbie.setCardinalityMin(bccVO.getCardinalityMin());
        bbie.setBdtPriRestriId(bdtPrimitiveRestrictionId);
        int userId = getUserId();
        bbie.setCreatedBy(userId);
        bbie.setLastUpdatedBy(userId);
        bbie.setSeqKey(seqKey);

        bbieRepository.save(bbie);
        bbieCount++;

        return bbie;
    }

    private void createBBIESC(int bbie, int bdt) {
        List<DataTypeSupplementaryComponent> list =
                dtScRepository.findByOwnerDtId(bdt);

        HashMap<String, String> hm = new HashMap<String, String>();
        for (DataTypeSupplementaryComponent dtsc : list) {
            if (dtsc.getMaxCardinality() == 0)
                continue;

            BasicBusinessInformationEntitySupplementaryComponent bbiescVO = new BasicBusinessInformationEntitySupplementaryComponent();
            bbiescVO.setBbieId(bbie);
            bbiescVO.setDtScId(dtsc.getDtScId());
            bbiescVO.setMaxCardinality(dtsc.getMaxCardinality());
            bbiescVO.setMinCardinality(dtsc.getMinCardinality());
            bbiescVO.setDefinition(dtsc.getDefinition());

            bbiescRepository.save(bbiescVO);
            bbiescCount++;

            String sc_name = "";
            if (dtsc.getRepresentationTerm().equalsIgnoreCase("Text"))
                sc_name = Utility.toLowerCamelCase(dtsc.getPropertyTerm());
            else if (dtsc.getRepresentationTerm().equalsIgnoreCase("Identifier"))
                sc_name = Utility.toLowerCamelCase(dtsc.getPropertyTerm()).concat("ID");
            else
                sc_name = Utility.toLowerCamelCase(dtsc.getPropertyTerm()).concat(Utility.toCamelCase(dtsc.getRepresentationTerm()));

            hm.put(dtsc.getPropertyTerm(), dtsc.getGuid());
        }
    }

    private void copyBIEs(
            AggregateBusinessInformationEntity oabieVO,
            AggregateBusinessInformationEntity nabieVO,
            AssociationBusinessInformationEntityProperty nasbiepVO, int groupPosition, TreeNode tNode) {
        List<BasicBusinessInformationEntity> basicBusinessInformationEntities = getBBIEsFromABIE(oabieVO.getAbieId());

        for (BasicBusinessInformationEntity basicBusinessInformationEntity : basicBusinessInformationEntities) {
            BasicBusinessInformationEntityProperty oBBIEPVO =
                    bbiepRepository.findOneByBbiepId(basicBusinessInformationEntity.getToBbiepId());
            BasicBusinessInformationEntityProperty nBBIEPVO = copyBBIEP(oBBIEPVO);
            BasicBusinessInformationEntity nBasicBusinessInformationEntity =
                    copyBBIE(basicBusinessInformationEntity, nabieVO.getAbieId(), nBBIEPVO.getBbiepId());

            BasicCoreComponentProperty bccpVO =
                    bccpRepository.findOneByBccpId(nBBIEPVO.getBasedBccpId());

            ABIEView av = new ABIEView(repositoryFactory, bccpVO.getPropertyTerm(), nBasicBusinessInformationEntity.getBbieId(), "BBIE");
            av.setBbiep(nBBIEPVO);
            av.setBasicCoreComponentPropertyAndBasicBusinessInformationEntity(bccpVO, nBasicBusinessInformationEntity);

            DataType dtVO = dtRepository.findOneByDtId(bccpVO.getBdtId());
            av.setBdtName(dtVO.getDen());
            av.setColor("green");
            TreeNode tNode2 = new DefaultTreeNode(av, tNode);
            List<BasicBusinessInformationEntitySupplementaryComponent> bbiesc =
                    getBBIESCsFromBBIE(basicBusinessInformationEntity.getBbieId());
            for (BasicBusinessInformationEntitySupplementaryComponent oBBIESCVO : bbiesc) {
                copyBBIESC(oBBIESCVO, nBasicBusinessInformationEntity.getBbieId(), tNode2);
            }
        }

        List<AssociationBusinessInformationEntity> asbie = getASBIEsFromABIE(oabieVO.getAbieId());
        for (AssociationBusinessInformationEntity oASBIEVO : asbie) {
            AssociationBusinessInformationEntityProperty o_next_asbiepVO =
                    asbiepRepository.findOneByAsbiepId(oASBIEVO.getToAsbiepId());
            AggregateBusinessInformationEntity o_next_abieVO =
                    abieRepository.findOneByAbieId(o_next_asbiepVO.getRoleOfAbieId());
            AggregateBusinessInformationEntity n_next_abieVO = copyABIE(o_next_abieVO, bCSelected.getBizCtxId(), false);
            AssociationBusinessInformationEntityProperty n_next_asbiepVO = copyASBIEP(o_next_asbiepVO, n_next_abieVO.getAbieId());
            AssociationBusinessInformationEntity nASBIEVO = copyASBIE(oASBIEVO, nabieVO.getAbieId(), n_next_asbiepVO.getAsbiepId());

            AssociationCoreComponentProperty asccpVO =
                    asccpRepository.findOneByAsccpId(n_next_asbiepVO.getBasedAsccpId());

            ABIEView av = new ABIEView(repositoryFactory, asccpVO.getPropertyTerm(), nabieVO.getAbieId(), "ASBIE");
            av.setColor("blue");
            av.setAbie(n_next_abieVO);
            av.setAsbie(nASBIEVO);
            av.setAsbiep(n_next_asbiepVO);
            TreeNode tNode2 = new DefaultTreeNode(av, tNode);
            copyBIEs(o_next_abieVO, n_next_abieVO, n_next_asbiepVO, -1, tNode2);
        }
    }

    private AggregateBusinessInformationEntity copyABIE(
            AggregateBusinessInformationEntity sourceAggregateBusinessInformationEntity, int bizCtxId, boolean topLevel) {
        AggregateBusinessInformationEntity cloneAggregateBusinessInformationEntity = new AggregateBusinessInformationEntity();
        String abieGuid = Utility.generateGUID();
        cloneAggregateBusinessInformationEntity.setGuid(abieGuid);
        cloneAggregateBusinessInformationEntity.setBasedAccId(sourceAggregateBusinessInformationEntity.getBasedAccId());
        cloneAggregateBusinessInformationEntity.setDefinition(sourceAggregateBusinessInformationEntity.getDefinition());
        cloneAggregateBusinessInformationEntity.setTopLevel(topLevel);
        cloneAggregateBusinessInformationEntity.setBizCtxId(bizCtxId);
        cloneAggregateBusinessInformationEntity.setClientId(sourceAggregateBusinessInformationEntity.getClientId());
        cloneAggregateBusinessInformationEntity.setVersion(sourceAggregateBusinessInformationEntity.getVersion());
        cloneAggregateBusinessInformationEntity.setStatus(sourceAggregateBusinessInformationEntity.getStatus());
        cloneAggregateBusinessInformationEntity.setRemark(sourceAggregateBusinessInformationEntity.getRemark());
        cloneAggregateBusinessInformationEntity.setBizTerm(sourceAggregateBusinessInformationEntity.getBizTerm());
        int userId = getUserId();
        cloneAggregateBusinessInformationEntity.setCreatedBy(userId);
        cloneAggregateBusinessInformationEntity.setLastUpdatedBy(userId);
        if (topLevel)
            cloneAggregateBusinessInformationEntity.setState(SRTConstants.TOP_LEVEL_ABIE_STATE_EDITING);

        abieRepository.save(cloneAggregateBusinessInformationEntity);
        abieCount++;

        return cloneAggregateBusinessInformationEntity;
    }

    private AssociationBusinessInformationEntityProperty copyASBIEP(
            AssociationBusinessInformationEntityProperty sourceAssociationBusinessInformationEntityProperty, int abieId) {
        AssociationBusinessInformationEntityProperty cloneAssociationBusinessInformationEntityProperty =
                new AssociationBusinessInformationEntityProperty();
        cloneAssociationBusinessInformationEntityProperty.setGuid(Utility.generateGUID());
        cloneAssociationBusinessInformationEntityProperty.setBasedAsccpId(sourceAssociationBusinessInformationEntityProperty.getBasedAsccpId());
        cloneAssociationBusinessInformationEntityProperty.setRoleOfAbieId(abieId);
        int userId = getUserId();
        cloneAssociationBusinessInformationEntityProperty.setCreatedBy(userId);
        cloneAssociationBusinessInformationEntityProperty.setLastUpdatedBy(userId);
        cloneAssociationBusinessInformationEntityProperty.setDefinition(sourceAssociationBusinessInformationEntityProperty.getDefinition());

        asbiepRepository.save(cloneAssociationBusinessInformationEntityProperty);
        asbiepCount++;

        return cloneAssociationBusinessInformationEntityProperty;
    }

    private AssociationBusinessInformationEntity copyASBIE(AssociationBusinessInformationEntity oasbieVO, int abieid, int asbiepid) {
        AssociationBusinessInformationEntity asbieVO = new AssociationBusinessInformationEntity();
        asbieVO.setGuid(Utility.generateGUID());
        asbieVO.setFromAbieId(abieid);
        asbieVO.setToAsbiepId(asbiepid);
        asbieVO.setBasedAscc(oasbieVO.getBasedAscc());
        asbieVO.setCardinalityMax(oasbieVO.getCardinalityMax());
        asbieVO.setCardinalityMin(oasbieVO.getCardinalityMin());
        asbieVO.setDefinition(oasbieVO.getDefinition());
        int userId = getUserId();
        asbieVO.setCreatedBy(userId);
        asbieVO.setLastUpdatedBy(userId);
        asbieVO.setSeqKey(oasbieVO.getSeqKey());

        asbieRepository.save(asbieVO);
        asbieCount++;

        return asbieVO;
    }

    private BasicBusinessInformationEntityProperty copyBBIEP(BasicBusinessInformationEntityProperty obbiepVO) {
        BasicBusinessInformationEntityProperty nbbiepVO = new BasicBusinessInformationEntityProperty();
        nbbiepVO.setBasedBccpId(obbiepVO.getBasedBccpId());
        nbbiepVO.setDefinition(obbiepVO.getDefinition());
        nbbiepVO.setRemark(obbiepVO.getRemark());
        nbbiepVO.setBizTerm(obbiepVO.getBizTerm());
        nbbiepVO.setGuid(Utility.generateGUID());
        int userId = getUserId();
        nbbiepVO.setCreatedBy(userId);
        nbbiepVO.setLastUpdatedBy(userId);

        bbiepRepository.save(nbbiepVO);
        bbiepCount++;

        return nbbiepVO;
    }

    private BasicBusinessInformationEntity copyBBIE(BasicBusinessInformationEntity obbieVO, int abie, int bbiep) {
        BasicBusinessInformationEntity nbbieVO = new BasicBusinessInformationEntity();
        nbbieVO.setBasedBccId(obbieVO.getBasedBccId());
        nbbieVO.setBdtPriRestriId(obbieVO.getBdtPriRestriId());
        nbbieVO.setCodeListId(obbieVO.getCodeListId());
        nbbieVO.setCardinalityMax(obbieVO.getCardinalityMax());
        nbbieVO.setCardinalityMin(obbieVO.getCardinalityMin());
        nbbieVO.setDefaultValue(obbieVO.getDefaultValue());
        nbbieVO.setNillable(obbieVO.isNillable());
        nbbieVO.setFixedValue(obbieVO.getFixedValue());
        nbbieVO.setNill(obbieVO.isNill());
        nbbieVO.setDefinition(obbieVO.getDefinition());
        nbbieVO.setRemark(obbieVO.getRemark());

        nbbieVO.setGuid(Utility.generateGUID());
        nbbieVO.setFromAbieId(abie);
        nbbieVO.setToBbiepId(bbiep);//come back
        int userId = getUserId();
        nbbieVO.setCreatedBy(userId);
        nbbieVO.setLastUpdatedBy(userId);

        bbieRepository.save(nbbieVO);
        bbieCount++;

        return nbbieVO;
    }

    private void copyBBIESC(BasicBusinessInformationEntitySupplementaryComponent obbiescvo, int bbie, TreeNode tNode) {
        BasicBusinessInformationEntitySupplementaryComponent nbbiescVO = new BasicBusinessInformationEntitySupplementaryComponent();
        nbbiescVO.setDtScId(obbiescvo.getDtScId());
        nbbiescVO.setDtScPriRestriId(obbiescvo.getDtScPriRestriId());
        nbbiescVO.setCodeListId(obbiescvo.getCodeListId());
        nbbiescVO.setAgencyIdListId(obbiescvo.getAgencyIdListId());
        nbbiescVO.setMaxCardinality(obbiescvo.getMaxCardinality());
        nbbiescVO.setMinCardinality(obbiescvo.getMinCardinality());
        nbbiescVO.setDefaultValue(obbiescvo.getDefaultValue());
        nbbiescVO.setFixedValue(obbiescvo.getFixedValue());
        nbbiescVO.setDefinition(obbiescvo.getDefinition());
        nbbiescVO.setRemark(obbiescvo.getRemark());
        nbbiescVO.setBizTerm(obbiescvo.getBizTerm());

        nbbiescVO.setBbieId(bbie);
        bbiescRepository.save(nbbiescVO);
        bbiescCount++;

        DataTypeSupplementaryComponent dtscvo = dtScRepository.findOneByDtScId(nbbiescVO.getDtScId());

        ABIEView av = new ABIEView(repositoryFactory, dtscvo.getPropertyTerm(), nbbiescVO.getBbieScId(), "BBIESC");

        av.setBbiesc(nbbiescVO);
        String sc_name = "";
        if (dtscvo.getRepresentationTerm().equalsIgnoreCase("Text"))
            sc_name = Utility.toLowerCamelCase(dtscvo.getPropertyTerm());
        else if (dtscvo.getRepresentationTerm().equalsIgnoreCase("Identifier"))
            sc_name = Utility.toLowerCamelCase(dtscvo.getPropertyTerm()).concat("ID");
        else
            sc_name = Utility.toLowerCamelCase(dtscvo.getPropertyTerm()).concat(Utility.toCamelCase(dtscvo.getRepresentationTerm()));
        av.setName(sc_name);

        av.setColor("orange");
        TreeNode tNode1 = new DefaultTreeNode(av, tNode);
    }

    private void extendABIE() {
        int state;
        int userId = 0;
        //if(Extension is selected thru ui)
        AggregateBusinessInformationEntity abieVO = new AggregateBusinessInformationEntity();
        if (abieVO.getState() == 1) { // Assume that 1 is editing

            AggregateCoreComponent ueACC = new AggregateCoreComponent(); //get ueACC
            if (ueACC == null) {
                //Create a New user Extension Group
                if (ueACC.getOagisComponentType() == 4) {
                    AggregateCoreComponent nueACC = new AggregateCoreComponent();
                    nueACC.setGuid(Utility.generateGUID());
                    String objectClassTerm = "";
                    if (ueACC.getObjectClassTerm().equalsIgnoreCase("Application Area Extension"))
                        objectClassTerm = "Application Area User Extension Group";
                    else if (ueACC.getObjectClassTerm().equalsIgnoreCase("All Extension"))
                        objectClassTerm = "All User Extension Group";
                    else if (ueACC.getObjectClassTerm().equalsIgnoreCase("Attached Item Extension"))
                        objectClassTerm = "Attached  Item User Extension Group";
                    nueACC.setObjectClassTerm(objectClassTerm);
                    nueACC.setDen(objectClassTerm + ". Details");
                    nueACC.setDefinition("A system created component containing user extension to the " + ueACC.getObjectClassTerm());
                    nueACC.setObjectClassQualifier("");
                    nueACC.setOagisComponentType(4);
                    nueACC.setState(1);
                    nueACC.setRevisionNum(0);
                    nueACC.setRevisionTrackingNum(0);
                    accRepository.save(nueACC);

                    AssociationCoreComponentProperty nASCCP = new AssociationCoreComponentProperty();
                    nASCCP.setGuid(Utility.generateGUID());
                    nASCCP.setPropertyTerm(objectClassTerm);
                    nASCCP.setRoleOfAccId(ueACC.getAccId());
                    nASCCP.setDen(nASCCP.getPropertyTerm() + "." + Utility.first(ueACC.getDen()));
                    nASCCP.setState(4);
                    nASCCP.setReusableIndicator(false);
                    nASCCP.setRevisionNum(0);
                    nASCCP.setRevisionTrackingNum(0);
                    asccpRepository.save(nASCCP);

                    AssociationCoreComponent nASCC = new AssociationCoreComponent();
                    nASCC.setGuid(Utility.generateGUID());
                    nASCC.setCardinalityMin(1);
                    nASCC.setCardinalityMax(1);
                    nASCC.setFromAccId(ueACC.getAccId());
                    nASCC.setToAsccpId(nASCCP.getAsccpId());
                    nASCC.setDen(ueACC.getObjectClassTerm() + ". " + nASCCP.getDen());
                    nASCC.setDefinition("System created association to the system created user extension group component");
                    nASCC.setState(4);
                    nASCC.setRevisionNum(0);
                    nASCC.setRevisionTrackingNum(0);
                    asccRepository.save(nASCC);
                    //here : history record population

                    AssociationCoreComponentProperty hASCCP = new AssociationCoreComponentProperty(); // populate ASCCP history record
                    hASCCP.setRevisionNum(1);
                    hASCCP.setRevisionTrackingNum(1);
                    asccpRepository.save(hASCCP);

                    AssociationCoreComponent hASCC = new AssociationCoreComponent(); //populate ASCC history record
                    hASCC.setRevisionNum(1);
                    hASCC.setRevisionTrackingNum(1);
                    asccRepository.save(hASCC);
                }
            } else {
                if (ueACC.getState() == 1 && ueACC.getOwnerUserId() == userId) {
                    //Go to Editing user Extension Group
                } else if (ueACC.getState() == 1 && ueACC.getOwnerUserId() != userId) {
                    //Indicate to the user that the eACC is being extended by another user.
                    //The user can acknowledge and then return to top-level BIE editing page.
                } else if (ueACC.getState() == 2 && ueACC.getOwnerUserId() == userId) {
                    //Indicate to the user that the eACC is in Candidate state and
                    //ask whether the user wants to switch the state back to Editing thru UI.
                    boolean state_back = false;
                    if (state_back == true) {
                        ueACC.setState(1);
                        //find its association and set to Editing
                    } else {
                        abieVO.setState(1);
                    }
                } else if (ueACC.getState() == 2 && ueACC.getOwnerUserId() != userId) {
                    //Indicate to the user that the eACC is being extended by another user.
                    //The user can acknowledge and then return to top-level BIE editing page.
                    boolean review_extension = false;
                    if (review_extension == true) {
                        //Go to Review user extension group ACC
                    } else {
                        abieVO.setState(1);
                    }
                } else if (ueACC.getState() == 4) {
                    //Indicate to the user that the eACC was last extended by who.
                    boolean create_new_revision = false;
                    //Ask the user to whether he would like to create a new revision.

                    if (create_new_revision)
                        ;//Go to create a New User Extension Group ACC Revision
                    //, the State of the ueACC current record and all its associations (BCC and ASCC) must be updated to Editing.
                    //Then, the corresponding history records must be created with an incremental revision_num from the latest revision.

                }
            }
        }
    }

    private void editUserExtensionGroupACC() {
        //UI implementation is needed.
        //Delete an existing association
        //Make edit to details of existing associations
        //The user should be able to make a new revision or modification
        //Add an association, Create a new ACC, create a new ASCCP, create a new BCCP


    }

    public List<AssociationCoreComponentProperty> getAsccpVOs() {
        return asccpVOs;
    }

    public List<String> completeInput(String query) {
        return asccpRepository.findAll().stream()
                .filter(associationCoreComponentProperty -> associationCoreComponentProperty.getPropertyTerm().contains(query))
                .map(associationCoreComponentProperty -> associationCoreComponentProperty.getPropertyTerm())
                .collect(Collectors.toList());
    }

    public AssociationCoreComponentProperty getSelected() {
        return selected;
    }

    public void setSelected(AssociationCoreComponentProperty selected) {
        this.selected = selected;
    }

    public BusinessContext getbCSelected() {
        return bCSelected;
    }

    public void setbCSelected(BusinessContext bCSelected) {
        this.bCSelected = bCSelected;
    }

    public String getPropertyTerm() {
        return propertyTerm;
    }

    public String getDEN() {
        return den;
    }

    public String getDefinition() {
        return definition;
    }

    public void setPropertyTerm(String propertyTerm) {
        this.propertyTerm = propertyTerm;
    }

    public void setDEN(String den) {
        this.den = den;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public void onRowSelect(SelectEvent event) {
        AssociationCoreComponentProperty asccp = (AssociationCoreComponentProperty) event.getObject();
        FacesMessage msg = new FacesMessage(asccp.getPropertyTerm(), String.valueOf(asccp.getAsccpId()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
        selected = asccp;
    }

    public void onBODRowSelect(SelectEvent event) {
        FacesMessage msg = new FacesMessage(((ABIEView) event.getObject()).getName(), String.valueOf(((ABIEView) event.getObject()).getName()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
        selectedBod = (ABIEView) event.getObject();
        //root = new DefaultTreeNode(selectedBod, null);

        logger.debug("#### " + selectedBod.getName());
        ABIEView rootABIEView = new ABIEView(repositoryFactory, selectedBod.getName(), selectedBod.getId(), "ROOT");
        rootABIEView.setAbie(selectedBod.getAbie());
        root = new DefaultTreeNode(rootABIEView, null);

        aABIEView = new ABIEView(repositoryFactory, selectedBod.getName(), selectedBod.getId(), "ABIE");
        aABIEView.setAbie(selectedBod.getAbie());
        aABIEView.setColor("blue");
        TreeNode toplevelNode = new DefaultTreeNode(aABIEView, root);

        createBIEChildren(selectedBod.getId(), toplevelNode);
    }

    private void createBIEChildren(int abieId, TreeNode tNode) {
        List<BasicBusinessInformationEntity> list_01 = bbieRepository.findByFromAbieId(abieId);
        List<AssociationBusinessInformationEntity> list_02 = asbieRepository.findByFromAbieId(abieId);

        Map<BusinessInformationEntity, Integer> sequence = new HashMap();
        ValueComparator bvc = new ValueComparator(sequence);
        TreeMap<BusinessInformationEntity, Integer> ordered_sequence = new TreeMap(bvc);

        for (BasicBusinessInformationEntity bbieVO : list_01) {
            int sk = bbieVO.getSeqKey();
            if (getEntityType(bbieVO.getBasedBccId()) == 0)
                showBBIETree(bbieVO, tNode);
            else
                sequence.put(bbieVO, sk);
        }

        for (AssociationBusinessInformationEntity asbieVO : list_02) {
            int sk = asbieVO.getSeqKey();
            sequence.put(asbieVO, sk);
        }

        ordered_sequence.putAll(sequence);
        Set set = ordered_sequence.entrySet();
        Iterator i = set.iterator();
        while (i.hasNext()) {
            Map.Entry me = (Map.Entry) i.next();
            if (me.getKey() instanceof BasicBusinessInformationEntity)
                showBBIETree((BasicBusinessInformationEntity) me.getKey(), tNode);
            else
                showASBIETree((AssociationBusinessInformationEntity) me.getKey(), tNode);
        }
    }

    private void showBBIETree(BasicBusinessInformationEntity bbieVO, TreeNode tNode) {
        BasicBusinessInformationEntityProperty bbiepVO = bbiepRepository.findOneByBbiepId(bbieVO.getToBbiepId());
        BasicCoreComponentProperty bccpVO = bccpRepository.findOneByBccpId(bbiepVO.getBasedBccpId());
        BasicCoreComponent bccVO = bccRepository.findOneByBccId(bbieVO.getBasedBccId());

        ABIEView av = new ABIEView(repositoryFactory, bccpVO.getPropertyTerm(), bbieVO.getBbieId(), "BBIE");
        av.setBcc(bccVO);
        av.setBbiep(bbiepVO);
        av.setBasicCoreComponentPropertyAndBasicBusinessInformationEntity(bccpVO, bbieVO);

        DataType dtVO = dtRepository.findOneByDtId(bccpVO.getBdtId());
        av.setBdtName(dtVO.getDen());
        av.setColor("green");
        TreeNode tNode2 = new DefaultTreeNode(av, tNode);
    }

    private void showASBIETree(AssociationBusinessInformationEntity asbieVO, TreeNode tNode) {
        AssociationBusinessInformationEntityProperty asbiepVO = asbiepRepository.findOneByAsbiepId(asbieVO.getToAsbiepId());
        AssociationCoreComponentProperty asccpVO = asccpRepository.findOneByAsccpId(asbiepVO.getBasedAsccpId());
        AssociationCoreComponent asccVO = asccRepository.findOneByAsccId(asbieVO.getBasedAscc());
        AggregateBusinessInformationEntity abieVO = abieRepository.findOneByAbieId(asbiepVO.getRoleOfAbieId());
        AggregateCoreComponent accVO = accRepository.findOneByAccId(abieVO.getBasedAccId());

        ABIEView av = new ABIEView(repositoryFactory, asccpVO.getPropertyTerm(), abieVO.getAbieId(), "ASBIE");
        av.setColor("blue");
        av.setAscc(asccVO);
        av.setAsccp(asccpVO);
        av.setAcc(accVO);
        av.setAbie(abieVO);
        av.setAsbiep(asbiepVO);
        av.setAsbie(asbieVO);
        TreeNode tNode2 = new DefaultTreeNode(av, tNode);
    }

    private void createBBIESCChild(BasicBusinessInformationEntity bbieVO, TreeNode parent) {
        List<BasicBusinessInformationEntitySupplementaryComponent> list_01 =
                bbiescRepository.findByBbieId(bbieVO.getBbieId());
        for (BasicBusinessInformationEntitySupplementaryComponent bbiescVO : list_01) {
            DataTypeSupplementaryComponent dtscVO = dtScRepository.findOneByDtScId(bbiescVO.getDtScId());

            ABIEView av_01 = new ABIEView(repositoryFactory, dtscVO.getPropertyTerm(), bbiescVO.getBbieScId(), "BBIESC");
            av_01.setDtsc(dtscVO);
            av_01.setBbiesc(bbiescVO);

            String sc_name = "";
            if (dtscVO.getRepresentationTerm().equalsIgnoreCase("Text"))
                sc_name = Utility.toLowerCamelCase(dtscVO.getPropertyTerm());
            else if (dtscVO.getRepresentationTerm().equalsIgnoreCase("Identifier"))
                sc_name = Utility.toLowerCamelCase(dtscVO.getPropertyTerm()).concat("ID");
            else
                sc_name = Utility.toLowerCamelCase(dtscVO.getPropertyTerm());
            av_01.setName(sc_name);

            av_01.setColor("orange");
            TreeNode tNode1 = new DefaultTreeNode(av_01, parent);
        }
    }

    private HashSet<Integer> openedNodes = new HashSet<Integer>();

    public void expand() {
        ABIEView abieView = (ABIEView) selectedTreeNode.getData();
        if (!openedNodes.contains(abieView.getId())) {
            openedNodes.add(abieView.getId());
            if ("ASBIE".equalsIgnoreCase(abieView.getType()))
                createBIEChildren(abieView.getAbie().getAbieId(), selectedTreeNode);
            else if ("BBIE".equalsIgnoreCase(abieView.getType()))
                createBBIESCChild(abieView.getBbie(), selectedTreeNode);
        }

        aABIEView = abieView;
        codeList = null;

        if ("BBIE".equalsIgnoreCase(aABIEView.getType())) {
            aABIEView.getBdtPrimitiveRestrictions();

            BusinessDataTypePrimitiveRestriction aBDTPrimitiveRestrictionVO =
                    bdtPriRestriRepository.findOneByBdtPriRestriId(aABIEView.getBdtPrimitiveRestrictionId());
            CodeList codeList = codeListRepository.findOneByCodeListId(aBDTPrimitiveRestrictionVO.getCodeListId());
            codeLists = (codeList != null) ? Arrays.asList(codeList) : Collections.emptyList();
        }
    }

    public void onEdit() {
        FacesContext context = FacesContext.getCurrentInstance();
        String objectId = context.getExternalContext().getRequestParameterMap().get("objectId");
        logger.debug("###objectId " + objectId);
//
//	    ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
//		try {
//			logger.debug("### " + selectedBod.getName());
//
//			ABIEView av = new ABIEView("AAA", 1, "ABIE");
//			root = new DefaultTreeNode(av, null);
//			ABIEView av1 = new ABIEView("AAA", 1, "ABIE");
//			selectedBod
//			DefaultTreeNode node = new DefaultTreeNode(av1, root);
//			context.setRequest(root);
//			context.redirect(context.getRequestContextPath() + "/top_level_abie_edit.xhtml");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
    }

    public void onBCRowSelect(SelectEvent event) {
        logger.debug(((BusinessContext) event.getObject()).getBizCtxId() + "Business context Selected");
    }


    public void onBCSelect(BusinessContext businessContext) {
        bCSelected = businessContext;
        FacesMessage msg = new FacesMessage(bCSelected.getName(), bCSelected.getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
        logger.debug(bCSelected.getBizCtxId() + " is selected");
    }

    @Transactional(rollbackFor = Throwable.class)
    public void onBCSelect(BusinessContextHandler bcH) {
        BusinessContext bcVO = new BusinessContext();
        bcVO.setName(bcH.getName());
        String guid = Utility.generateGUID();
        bcVO.setGuid(guid);
        businessContextRepository.save(bcVO);

        for (BusinessContextValues bcv : bcH.getBcValues()) {
            for (ContextSchemeValue cVO : bcv.getCsList()) {
                BusinessContextValue bcvVO = new BusinessContextValue();
                bcvVO.setBizCtxId(bcVO.getBizCtxId());
                bcvVO.setCtxSchemeValueId(cVO.getCtxSchemeValueId());
                businessContextValueRepository.save(bcvVO);
            }
        }

        bCSelected = bcVO;
        FacesMessage msg = new FacesMessage(bCSelected.getName(), bCSelected.getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void onRowUnselect(UnselectEvent event) {
        FacesMessage msg = new FacesMessage("Item Unselected", String.valueOf(((AssociationCoreComponentProperty) event.getObject()).getAsccpId()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void onBODRowUnselect(UnselectEvent event) {
        FacesMessage msg = new FacesMessage("Item Unselected", String.valueOf(((ABIEView) event.getObject()).getName()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    private ABIEView selectedDocument;

    private TreeNode root;

    public TreeNode getRoot() {
        if (root == null)
            root = new DefaultTreeNode();
        return root;
    }

    public ABIEView getSelectedDocument() {
        return selectedDocument;
    }

    public void setSelectedDocument(ABIEView selectedDocument) {
        this.selectedDocument = selectedDocument;
    }

    private int getUserId() {
        User user = userRepository.findOneByLoginId("oagis");
        return user.getAppUserId();
    }

    private int min;

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    private TreeNode selectedTreeNode;

    private ABIEView aABIEView;

    public ABIEView getaABIEView() {
        if (aABIEView == null)
            aABIEView = new ABIEView(repositoryFactory);
        return aABIEView;
    }

    public void setaABIEView(ABIEView aABIEView) {
        this.aABIEView = aABIEView;
    }

    public TreeNode getSelectedTreeNode() {
        if (selectedTreeNode == null)
            selectedTreeNode = root;

        aABIEView = (ABIEView) selectedTreeNode.getData();
        return selectedTreeNode;
    }

    public void setSelectedTreeNode(TreeNode selectedTreeNode) {
        this.selectedTreeNode = selectedTreeNode;
    }

    public void updateTree() {
        ABIEView aABIEView = (ABIEView) selectedTreeNode.getData();
        logger.debug("### " + aABIEView.getName());
    }

    public void showDetails() {
        aABIEView = (ABIEView) selectedTreeNode.getData();
        codeList = null;

        if ("BBIE".equalsIgnoreCase(aABIEView.getType())) {
            aABIEView.getBdtPrimitiveRestrictions();

            BusinessDataTypePrimitiveRestriction aBDTPrimitiveRestrictionVO =
                    bdtPriRestriRepository.findOneByBdtPriRestriId(aABIEView.getBdtPrimitiveRestrictionId());
            CodeList codeList = codeListRepository.findOneByCodeListId(aBDTPrimitiveRestrictionVO.getCodeListId());
            codeLists = (codeList != null) ? Arrays.asList(codeList) : Collections.emptyList();
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void saveChanges() {
        aABIEView = (ABIEView) selectedTreeNode.getData();
        if (aABIEView.getType().equals("ASBIE")) {
            saveASBIEChanges(aABIEView);
        } else if (aABIEView.getType().equals("BBIE")) {
            saveBBIEChanges(aABIEView);
        } else if (aABIEView.getType().equals("BBIESC")) {
            saveBBIESCChanges(aABIEView);
        } else if (aABIEView.getType().equals("ABIE")) {
            saveABIEChanges(aABIEView);
        }

        FacesMessage msg = new FacesMessage("Changes on '" + aABIEView.getName() + "' are just saved!", "Changes on '" + aABIEView.getName() + "' are just saved!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    private void saveBBIEChanges(ABIEView aABIEView) {
        BasicBusinessInformationEntity bbieVO = aABIEView.getBbie();
        BasicBusinessInformationEntityProperty bbiepVO = aABIEView.getBbiep();

        if (aABIEView.getRestrictionType().equalsIgnoreCase("Primitive")) {
            bbieVO.setBdtPriRestriId(aABIEView.getBdtPrimitiveRestrictionId());
            bbieVO.setCodeListId(0);
        } else if (aABIEView.getRestrictionType().equalsIgnoreCase("Code")) {
            if (codeList != null) {
                bbieVO.setCodeListId(codeList.getCodeListId());
                bbieVO.setBdtPriRestriId(0);
            }
        }

        bbieRepository.update(bbieVO);
        bbiepRepository.update(bbiepVO);
    }

    public void chooseCodeForTLBIE() {
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("modal", true);
        options.put("draggable", true);
        options.put("resizable", true);
        options.put("contentHeight", 800);
        RequestContext.getCurrentInstance().openDialog("top_level_abie_create_code_select", options, null);
    }

    private List<CodeList> codeLists = new ArrayList();

    private List<CodeList> codeLists2 = new ArrayList();

    public List<CodeList> getCodeLists() {
        return codeLists;
    }

    public void setCodeLists(List<CodeList> codeLists) {
        this.codeLists = codeLists;
    }

    public List<CodeList> getCodeLists2() {
        return codeLists2;
    }

    public void setCodeLists2(List<CodeList> codeLists2) {
        this.codeLists2 = codeLists2;
    }

    public void chooseDerivedCodeForTLBIE(int bdtPrimitiveRestrictionId) {
        Map<String, Object> options = new HashMap();
        options.put("modal", true);
        options.put("draggable", true);
        options.put("resizable", true);
        options.put("contentHeight", 800);

        BusinessDataTypePrimitiveRestriction aBDTPrimitiveRestrictionVO =
                bdtPriRestriRepository.findOneByBdtPriRestriId(bdtPrimitiveRestrictionId);
        CodeList codeList = codeListRepository.findOneByCodeListId(aBDTPrimitiveRestrictionVO.getCodeListId());
        codeLists = (codeList != null) ? Arrays.asList(codeList) : Collections.emptyList();

        logger.debug("##### " + codeLists);

        RequestContext context = RequestContext.getCurrentInstance();
        context.openDialog("top_level_abie_create_derived_code_select", options, null);
    }

    CodeList codeList;
    CodeList selectedCodeList;

    public CodeList getSelectedCodeList() {
        return selectedCodeList;
    }

    public void setSelectedCodeList(CodeList selectedCodeList) {
        this.selectedCodeList = selectedCodeList;
    }

    public void onCodeListRowSelect(SelectEvent event) {
        codeList = (CodeList) event.getObject();
        FacesMessage msg = new FacesMessage(codeList.getName(), String.valueOf(codeList.getCodeListId()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
        logger.debug("######## " + codeList.getName());
    }

    public void onCodeListRowUnselect(UnselectEvent event) {
        CodeList codeList = (CodeList) event.getObject();
        FacesMessage msg = new FacesMessage("Item Unselected", String.valueOf(codeList.getCodeListId()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public CodeList getCodeList() {
        return codeList;
    }

    public void setCodeList(CodeList codeList) {
        this.codeList = this.codeList;
    }

    public void onCodeListChosen(SelectEvent event) {
        CodeListHandler ch = (CodeListHandler) event.getObject();
        codeList = ch.getSelected();
        logger.debug(codeList.getName());
    }

    public void onDerivedCodeListChosen(SelectEvent event) {
        TopLevelABIEHandler ch = (TopLevelABIEHandler) event.getObject();
        codeList = ch.getSelectedCodeList();
    }

    String codeListName;

    public String getCodeListName() {
        return codeListName;
    }

    public void setCodeListName(String codeListName) {
        this.codeListName = codeListName;
    }

    public int getEntityType(int bccId) {
        BasicCoreComponent basicCoreComponent = bccRepository.findOneByBccId(bccId);
        return basicCoreComponent.getEntityType();
    }

    public void searchCodeList() {
        codeLists2 = codeListRepository.findByNameContainingAndStateIsPublishedAndExtensibleIndicatorIsTrue(getCodeListName());
        if (codeLists2.isEmpty()) {
            FacesMessage msg = new FacesMessage("[" + getCodeListName() + "] No such Code List exists or not yet published or not extensible", "[" + getCodeListName() + "] No such Code List exists or not yet published or not extensible");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    public List<String> completeCodeListInput(String query) {
        return codeListRepository.findByNameContaining(query).stream()
                .map(codeList -> codeList.getName())
                .collect(Collectors.toList());
    }

    private void saveBBIESCChanges(ABIEView aABIEView) {
        BasicBusinessInformationEntitySupplementaryComponent bbiescVO = aABIEView.getBbiesc();
        bbiescRepository.update(bbiescVO);
    }

    public void closeDialog() {
        RequestContext.getCurrentInstance().closeDialog(this);
    }

    private void saveASBIEChanges(ABIEView aABIEView) {
        AssociationBusinessInformationEntity asbieVO = aABIEView.getAsbie();
        AggregateBusinessInformationEntity abieVO = aABIEView.getAbie();
        AssociationBusinessInformationEntityProperty asbiepVO = aABIEView.getAsbiep();

        asbieRepository.update(asbieVO);
        asbiepRepository.update(asbiepVO);
        abieRepository.update(abieVO);
    }

    private void saveABIEChanges(ABIEView aABIEView) {
        AggregateBusinessInformationEntity abieVO = aABIEView.getAbie();
        abieRepository.update(abieVO);
    }

}