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
import org.springframework.context.ApplicationContext;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
public class TopLevelABIEHandler implements Serializable {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final long serialVersionUID = -2650693005373031742L;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CodeListRepository codeListRepository;

    @Autowired
    private DataTypeRepository dataTypeRepository;

    @Autowired
    private DataTypeSupplementaryComponentRepository dtScRepository;

    @Value("${spring.datasource.platform}")
    private String platform;

    @Autowired
    private AggregateCoreComponentRepository accRepository;

    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;

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

    private long maxABIEId;
    private long maxASBIEPId;
    private long maxBIEPID;
    private long maxBIEID;
    private long maxBBIESCID;

    @PostConstruct
    private void init() {
        maxABIEId = asbieRepository.count();
        maxASBIEPId = asbiepRepository.count();
        maxBIEPID = bbiepRepository.count();
        maxBIEID = bbieRepository.count();
        maxBBIESCID = bbiescRepository.count();

        asccpVOs = asccpRepository.findAllOrderByPropertyTermAsc();
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
                        asccpRepository.findOne(associationBusinessInformationEntityProperty.getBasedAsccpId());

                BusinessContext businessContext =
                        businessContextRepository.findOne(aggregateBusinessInformationEntity.getBizCtxId());

                aggregateBusinessInformationEntity.setBizCtxName(businessContext.getName());
                ABIEView aABIEView = applicationContext.getBean(ABIEView.class,
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
        int userId = userRepository.findAppUserIdByLoginId("oagis");

        AggregateCoreComponent acc =
                accRepository.findOne(selected.getRoleOfAccId());
        topAbieVO = createABIE(userId, acc, bCSelected.getBizCtxId(), true);
        int abieId = topAbieVO.getAbieId();

        ABIEView rootABIEView = applicationContext.getBean(ABIEView.class, selected.getPropertyTerm(), abieId, "ABIE");
        rootABIEView.setAbie(topAbieVO);
        root = new DefaultTreeNode(rootABIEView, null);

        asbiepVO = createASBIEP(userId, selected, abieId);

        aABIEView = applicationContext.getBean(ABIEView.class, selected.getPropertyTerm(), abieId, "ABIE");
        aABIEView.setAbie(topAbieVO);
        aABIEView.setColor("blue");
        aABIEView.setAcc(acc);
        aABIEView.setAsbiep(asbiepVO);

        TreeNode toplevelNode = new DefaultTreeNode(aABIEView, root);

        CreateBIEContext createBIEContext = new CreateBIEContext(userId);
        createBIEs(createBIEContext, selected.getRoleOfAccId(), topAbieVO);
        createBIEContext.save();

        createBIEChildren(abieId, toplevelNode);
    }

    public String onFlowProcess_Copy(FlowEvent event) {

        if (event.getNewStep().equals(SRTConstants.TAB_TOP_LEVEL_ABIE_SELECT_BC)) {

        } else if (event.getNewStep().equals(SRTConstants.TAB_TOP_LEVEL_ABIE_COPY_UC_BIE)) {
            // TODO if go back from the confirmation page? avoid that situation

            AggregateBusinessInformationEntity selectedAggregateBusinessInformationEntity =
                    selectedBod.getAbie();
            AggregateCoreComponent aggregateCoreComponent =
                    accRepository.findOne(selectedAggregateBusinessInformationEntity.getBasedAccId());

            AssociationCoreComponentProperty associationCoreComponentProperty =
                    asccpRepository.findOneByRoleOfAccId(aggregateCoreComponent.getAccId());

            int userId = userRepository.findAppUserIdByLoginId("oagis");
            topAbieVO = copyABIE(userId, selectedAggregateBusinessInformationEntity, bCSelected.getBizCtxId(), true);
            int abieId = topAbieVO.getAbieId();

            ABIEView rootABIEView = applicationContext.getBean(ABIEView.class, associationCoreComponentProperty.getPropertyTerm(), abieId, "ABIE");
            rootABIEView.setAbie(topAbieVO);
            root = new DefaultTreeNode(rootABIEView, null);
            AssociationBusinessInformationEntityProperty associationBusinessInformationEntityProperty =
                    copyASBIEP(userId, asbiepRepository.
                            findOneByRoleOfAbieId(selectedAggregateBusinessInformationEntity.getAbieId()), abieId);
            aABIEView = applicationContext.getBean(ABIEView.class, associationCoreComponentProperty.getPropertyTerm(), abieId, "ABIE");
            aABIEView.setAbie(topAbieVO);
            aABIEView.setColor("blue");
            aABIEView.setAsbiep(associationBusinessInformationEntityProperty);
            TreeNode toplevelNode = new DefaultTreeNode(aABIEView, root);

            copyBIEs(userId, selectedAggregateBusinessInformationEntity, topAbieVO, associationBusinessInformationEntityProperty, -1, toplevelNode);

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

    private List<CoreComponent> getAssocList(CreateBIEContext createBIEContext, List<CoreComponent> list) {
        for (int i = 0; i < list.size(); i++) {
            CoreComponent srt = list.get(i);
            if (srt instanceof AssociationCoreComponent && groupcheck(createBIEContext, (AssociationCoreComponent) srt)) {
                AssociationCoreComponent associationCoreComponent = (AssociationCoreComponent) srt;
                AssociationCoreComponentProperty associationCoreComponentProperty = createBIEContext.getASCCP(associationCoreComponent.getToAsccpId());
                AggregateCoreComponent aggregateCoreComponent = createBIEContext.getACC(associationCoreComponentProperty.getRoleOfAccId());
                list = handleNestedGroup(createBIEContext, aggregateCoreComponent, list, i);
            }
        }
        return list;
    }

    private List<CoreComponent> queryNestedChildAssoc(CreateBIEContext createBIEContext, AggregateCoreComponent aggregateCoreComponent) {
        List<BasicCoreComponent> bcc_tmp_assoc = getBCCwoAttribute(createBIEContext, aggregateCoreComponent.getAccId());
        List<AssociationCoreComponent> ascc_tmp_assoc = createBIEContext.getASCC(aggregateCoreComponent.getAccId());

        int size = bcc_tmp_assoc.size() + ascc_tmp_assoc.size();
        List<CoreComponent> tmp_assoc = new ArrayList(size);
        tmp_assoc.addAll(bcc_tmp_assoc);
        tmp_assoc.addAll(ascc_tmp_assoc);

        ArrayList<CoreComponent> assoc = new ArrayList(size);
        CoreComponent a = new CoreComponent();
        for (int i = 0; i < size; i++)
            assoc.add(a);

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

        return getAssocList(createBIEContext, assoc);
    }

    private List<CoreComponent> queryNestedChildAssoc_wo_attribute(CreateBIEContext createBIEContext, AggregateCoreComponent aggregateCoreComponent) {
        List<BasicCoreComponent> bcc_tmp_assoc = getBCCwoAttribute(createBIEContext, aggregateCoreComponent.getAccId());
        List<AssociationCoreComponent> ascc_tmp_assoc = createBIEContext.getASCC(aggregateCoreComponent.getAccId());
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
                assoc.set(basicCoreComponent.getSeqKey() - 1, basicCoreComponent);
            } else {
                AssociationCoreComponent associationCoreComponent = (AssociationCoreComponent) coreComponent;
                assoc.set(associationCoreComponent.getSeqKey() - 1, associationCoreComponent);
            }
        }

        assoc.trimToSize();

        return getAssocList(createBIEContext, assoc);
    }

    private List<CoreComponent> queryChildAssoc(CreateBIEContext createBIEContext,
                                                AggregateCoreComponent aggregateCoreComponent) {
        List<BasicCoreComponent> bcc_tmp_assoc = createBIEContext.getBCC(aggregateCoreComponent.getAccId());
        List<AssociationCoreComponent> ascc_tmp_assoc = createBIEContext.getASCC(aggregateCoreComponent.getAccId());
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

    private List<CoreComponent> handleNestedGroup(CreateBIEContext createBIEContext,
                                                  AggregateCoreComponent aggregateCoreComponent,
                                                  List<CoreComponent> coreComponents, int gPosition) {
        List<CoreComponent> list = new ArrayList();
        list.addAll(coreComponents);

        List<CoreComponent> bList = queryChildAssoc(createBIEContext, aggregateCoreComponent);
        list.addAll(gPosition, bList);
        list.remove(gPosition + bList.size());

        for (int i = 0; i < list.size(); i++) {
            CoreComponent coreComponent = list.get(i);
            if (coreComponent instanceof AssociationCoreComponent && groupcheck(createBIEContext, (AssociationCoreComponent) coreComponent)) {
                AssociationCoreComponent associationCoreComponent = (AssociationCoreComponent) coreComponent;
                AssociationCoreComponentProperty associationCoreComponentProperty = createBIEContext.getASCCP(associationCoreComponent.getToAsccpId());
                list = handleNestedGroup(
                        createBIEContext, createBIEContext.getACC(associationCoreComponentProperty.getRoleOfAccId()), list, i);
            }
        }

        return list;
    }

    private boolean groupcheck(CreateBIEContext createBIEContext, AssociationCoreComponent associationCoreComponent) {
        boolean check = false;
        AssociationCoreComponentProperty asccp = createBIEContext.getASCCP(associationCoreComponent.getToAsccpId());
        AggregateCoreComponent acc = createBIEContext.getACC(asccp.getRoleOfAccId());
        if (acc.getOagisComponentType() == 3) {
            check = true;
        }
        return check;
    }

    private class CreateBIEContext {
        private ABIETaskHolder abieTaskHolder;
        private BBIETreeTaskHolder bbieTreeTaskHolder;
        private ASBIETreeTaskHolder asbieTreeTaskHolder;

        private Map<Integer, AggregateCoreComponent> aggregateCoreComponentMap;
        private Map<Integer, AssociationCoreComponentProperty> associationCoreComponentPropertyMap;
        private Map<Integer, BasicCoreComponentProperty> basicCoreComponentPropertyMap;
        private Map<Integer, BusinessDataTypePrimitiveRestriction> businessDataTypePrimitiveRestrictionMap;

        private int userId;

        private List<BasicCoreComponent> basicCoreComponents;
        private List<AssociationCoreComponent> associationCoreComponents;
        private List<DataTypeSupplementaryComponent> dataTypeSupplementaryComponents;

        public CreateBIEContext(int userId) {
            abieTaskHolder = new ABIETaskHolder();
            bbieTreeTaskHolder = new BBIETreeTaskHolder();
            asbieTreeTaskHolder = new ASBIETreeTaskHolder();
            this.userId = userId;

            aggregateCoreComponentMap =
                    accRepository.findAll().stream()
                            .filter(acc -> acc.getRevisionNum() == 0)
                            .collect(Collectors.toMap(acc -> acc.getAccId(), Function.identity()));
            associationCoreComponentPropertyMap =
                    asccpRepository.findAll().stream()
                            .filter(asccp -> asccp.getRevisionNum() == 0)
                            .collect(Collectors.toMap(asccp -> asccp.getAsccpId(), Function.identity()));
            basicCoreComponents = bccRepository.findAll();
            associationCoreComponents = asccRepository.findAll();
            dataTypeSupplementaryComponents = dtScRepository.findAll();

            basicCoreComponentPropertyMap = bccpRepository.findAll().stream()
                    .filter(bccp -> bccp.getRevisionNum() == 0)
                    .collect(Collectors.toMap(bccp -> bccp.getBccpId(), Function.identity()));
            businessDataTypePrimitiveRestrictionMap = bdtPriRestriRepository.findAll().stream()
                    .filter(bdtPriRestri -> bdtPriRestri.isDefault())
                    .collect(Collectors.toMap(bdtPriRestri -> bdtPriRestri.getBdtId(), Function.identity()));
        }

        public int getUserId() {
            return userId;
        }

        public AggregateCoreComponent getACC(int accId) {
            return aggregateCoreComponentMap.get(accId);
        }

        public AssociationCoreComponentProperty getASCCP(int asccpId) {
            return associationCoreComponentPropertyMap.get(asccpId);
        }

        public AggregateBusinessInformationEntity createABIE(AggregateCoreComponent acc, int bizCtxId, boolean topLevel) {
            return abieTaskHolder.createABIE(userId, acc, bizCtxId, topLevel);
        }

        public void createBBIETree(BasicCoreComponent bcc, AggregateBusinessInformationEntity abie, int seqKey) {
            bbieTreeTaskHolder.createBBIETree(this, bcc, abie, seqKey);
        }

        public void createASBIETree(AssociationCoreComponent asccVO, AggregateBusinessInformationEntity abie, int seqKey) {
            asbieTreeTaskHolder.createASBIETree(this, asccVO, abie, seqKey);
        }

        public List<BasicCoreComponent> getBCC(int fromAccId) {
            return basicCoreComponents.stream()
                    .filter(bcc -> bcc.getFromAccId() == fromAccId)
                    .collect(Collectors.toList());
        }

        private List<AssociationCoreComponent> getASCC(int fromAccId) {
            return associationCoreComponents.stream()
                    .filter(acc -> acc.getFromAccId() == fromAccId)
                    .collect(Collectors.toList());
        }

        public BasicCoreComponentProperty getBCCP(int toBccpId) {
            return basicCoreComponentPropertyMap.get(toBccpId);
        }

        public int getBdtPrimitiveRestrictionId(int bdtId) {
            return businessDataTypePrimitiveRestrictionMap.get(bdtId).getBdtPriRestriId();
        }

        public List<DataTypeSupplementaryComponent> findByOwnerDtId(int ownerDtId) {
            return dataTypeSupplementaryComponents.stream()
                    .filter(dtSc -> dtSc.getOwnerDtId() == ownerDtId)
                    .collect(Collectors.toList());
        }

        public void save() {
            abieTaskHolder.save();
            bbieTreeTaskHolder.save();
            asbieTreeTaskHolder.save();
        }
    }

    private class ABIETaskHolder {

        private List<AggregateBusinessInformationEntity> aggregateBusinessInformationEntitys = new ArrayList();

        public AggregateBusinessInformationEntity createABIE(int userId, AggregateCoreComponent acc, int bizCtxId, boolean topLevel) {
            AggregateBusinessInformationEntity abie = new AggregateBusinessInformationEntity();
            String abieGuid = Utility.generateGUID();
            abie.setGuid(abieGuid);
            abie.setBasedAccId(acc.getAccId());
            abie.setDefinition(acc.getDefinition());
            abie.setTopLevel(topLevel);
            abie.setBizCtxId(bizCtxId);
            abie.setCreatedBy(userId);
            abie.setLastUpdatedBy(userId);
            if (topLevel)
                abie.setState(SRTConstants.TOP_LEVEL_ABIE_STATE_EDITING);

            aggregateBusinessInformationEntitys.add(abie);

            return abie;
        }

        public void save() {
            abieRepository.save(aggregateBusinessInformationEntitys);
            abieRepository.flush();
        }

    }

    public void createBIEs(CreateBIEContext createBIEContext, int accId, AggregateBusinessInformationEntity abie) {
        logger.debug("enter createBIEs(" + accId + ", " + abie.getGuid() + ")");
        long currentTimeMillis = System.currentTimeMillis();

        LinkedList<AggregateCoreComponent> accList = new LinkedList();
        AggregateCoreComponent aggregateCoreComponent = createBIEContext.getACC(accId);
        accList.add(aggregateCoreComponent);
        while (aggregateCoreComponent.getBasedAccId() > 0) {
            aggregateCoreComponent = createBIEContext.getACC(aggregateCoreComponent.getBasedAccId());
            accList.add(aggregateCoreComponent);
        }

        while (!accList.isEmpty()) {
            aggregateCoreComponent = accList.pollFirst();
            int skb = 0;
            for (AggregateCoreComponent cnt_acc : accList) {
                skb += queryNestedChildAssoc_wo_attribute(createBIEContext, cnt_acc).size(); //here
            }

            List<CoreComponent> childAssoc = queryNestedChildAssoc(createBIEContext, aggregateCoreComponent);
            int attr_cnt = childAssoc.size() - queryNestedChildAssoc_wo_attribute(createBIEContext, aggregateCoreComponent).size();
            for (int i = 0; i < childAssoc.size(); i++) {
                CoreComponent assoc = childAssoc.get(i);
                if (assoc instanceof BasicCoreComponent) {
                    BasicCoreComponent bcc = (BasicCoreComponent) assoc;
                    if (bcc.getSeqKey() == 0) {
                        createBIEContext.createBBIETree(bcc, abie, skb);
                    }
                }
            }

            for (int i = 0; i < childAssoc.size(); i++) {
                CoreComponent assoc = childAssoc.get(i);
                if (assoc instanceof BasicCoreComponent) {
                    BasicCoreComponent bcc = (BasicCoreComponent) assoc;
                    if (bcc.getSeqKey() > 0) {
                        createBIEContext.createBBIETree(bcc, abie, skb + i - attr_cnt);
                    }
                } else if (assoc instanceof AssociationCoreComponent) {
                    AssociationCoreComponent ascc = (AssociationCoreComponent) assoc;
                    createBIEContext.createASBIETree(ascc, abie, skb + i - attr_cnt);
                }
            }
        }

        logger.debug("leave createBIEs(" + accId + ", " + abie.getGuid() + "), elapsed time(ms): " + (System.currentTimeMillis() - currentTimeMillis));
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


    private class CreateBBIETreeTask {

        private BasicCoreComponent bcc;
        private AggregateBusinessInformationEntity abie;
        private int seqKey;

        private BasicBusinessInformationEntityProperty bbiep;
        private BasicBusinessInformationEntity bbie;
        private List<BasicBusinessInformationEntitySupplementaryComponent> bbieScList;

        public CreateBBIETreeTask(CreateBIEContext createBIEContext, BasicCoreComponent bcc, AggregateBusinessInformationEntity abie, int seqKey) {
            this.bcc = bcc;
            this.abie = abie;
            this.seqKey = seqKey;

            BasicCoreComponentProperty bccp = createBIEContext.getBCCP(bcc.getToBccpId());
            int bdtId = bccp.getBdtId();
            int bdtPrimitiveRestrictionId = createBIEContext.getBdtPrimitiveRestrictionId(bdtId);

            createBBIEP(createBIEContext.getUserId(), bccp);
            createBBIE(createBIEContext.getUserId(), bdtPrimitiveRestrictionId);
            createBBIESC(createBIEContext, bdtId);
        }

        private void createBBIEP(int userId, BasicCoreComponentProperty bccp) {
            bbiep = new BasicBusinessInformationEntityProperty();
            bbiep.setGuid(Utility.generateGUID());
            bbiep.setBasedBccpId(bccp.getBccpId());
            bbiep.setCreatedBy(userId);
            bbiep.setLastUpdatedBy(userId);
            bbiep.setDefinition(bccp.getDefinition());
        }

        private void createBBIE(int userId, int bdtPrimitiveRestrictionId) {
            bbie = new BasicBusinessInformationEntity();
            bbie.setGuid(Utility.generateGUID());
            bbie.setBasedBccId(bcc.getBccId());
            // bbie.setFromAbieId(abie);
            // bbie.setToBbiepId(bbiepId);
            bbie.setNillable(false);
            bbie.setCardinalityMax(bcc.getCardinalityMax());
            bbie.setCardinalityMin(bcc.getCardinalityMin());
            bbie.setBdtPriRestriId(bdtPrimitiveRestrictionId);
            bbie.setCreatedBy(userId);
            bbie.setLastUpdatedBy(userId);
            bbie.setSeqKey(seqKey);
        }

        private void createBBIESC(CreateBIEContext createBIEContext, int bdtId) {
            bbieScList = createBIEContext.findByOwnerDtId(bdtId)
                    .stream()
                    .filter(dtSc -> dtSc.getMaxCardinality() == 0)
                    .map(dtSc -> {
                        BasicBusinessInformationEntitySupplementaryComponent bbieSc =
                                new BasicBusinessInformationEntitySupplementaryComponent();
                        // bbieSc.setBbieId(bbieId);
                        bbieSc.setDtScId(dtSc.getDtScId());
                        bbieSc.setMaxCardinality(dtSc.getMaxCardinality());
                        bbieSc.setMinCardinality(dtSc.getMinCardinality());
                        bbieSc.setDefinition(dtSc.getDefinition());
                        return bbieSc;
                    })
                    .collect(Collectors.toList());
        }

        public AggregateBusinessInformationEntity getAbie() {
            return abie;
        }

        public BasicBusinessInformationEntityProperty getBbiep() {
            return bbiep;
        }

        public BasicBusinessInformationEntity getBbie() {
            return bbie;
        }

        public List<BasicBusinessInformationEntitySupplementaryComponent> getBbieScList() {
            return bbieScList;
        }
    }

    private class BBIETreeTaskHolder {
        private List<CreateBBIETreeTask> createBBIETreeTasks = new ArrayList();

        public void createBBIETree(CreateBIEContext createBIEContext, BasicCoreComponent bcc, AggregateBusinessInformationEntity abie, int seqKey) {
            createBBIETreeTasks.add(new CreateBBIETreeTask(createBIEContext, bcc, abie, seqKey));
        }

        public void save() {
            bbiepRepository.save(
                    createBBIETreeTasks.stream()
                            .map(task -> task.getBbiep())
                            .collect(Collectors.toList()));
            bbiepRepository.flush();
            bbiepCount += createBBIETreeTasks.size();

            createBBIETreeTasks.stream()
                    .forEach(task -> {
                        task.getBbie().setFromAbieId(task.getAbie().getAbieId());
                        task.getBbie().setToBbiepId(task.getBbiep().getBbiepId());
                    });

            bbieRepository.save(
                    createBBIETreeTasks.stream()
                            .map(task -> task.getBbie())
                            .collect(Collectors.toList()));
            bbieRepository.flush();
            bbieCount += createBBIETreeTasks.size();

            createBBIETreeTasks.stream()
                    .forEach(task -> {
                        task.getBbieScList().forEach(bbieSc -> {
                            bbieSc.setBbieId(task.getBbie().getBbieId());
                        });
                    });

            List<BasicBusinessInformationEntitySupplementaryComponent> bbieScList = new ArrayList();
            createBBIETreeTasks.stream()
                    .forEach(task -> {
                        bbieScList.addAll(task.getBbieScList());
                    });
            bbiescRepository.save(bbieScList);
            bbiescCount += bbieScList.size();
        }
    }

    private void createBBIETree(BasicCoreComponent bcc, int abie, int seqKey) {
        logger.debug("enter createBBIETree(" + bcc.getBccId() + ", " + abie + ", " + seqKey + ")");
        long currentTimeMillis = System.currentTimeMillis();

//        BasicCoreComponentProperty bccp =
//                bccpRepository.findBccpIdAndBdtIdAndDefinitionByBccpIdAndRevisionNum(bcc.getToBccpId(), 0);
//        int bdtId = bccp.getBdtId();
//        int bdtPrimitiveRestrictionId = bdtPriRestriRepository
//                .findBdtPriRestriIdByBdtIdAndDefault(bdtId, true)
//                .getBdtPriRestriId();
//
//        BasicBusinessInformationEntityProperty bbiep = createBBIEP(bccp);
//        BasicBusinessInformationEntity bbie =
//                createBBIE(bcc, abie, bbiep.getBbiepId(), seqKey, bdtPrimitiveRestrictionId);
//        int bbieId = bbie.getBbieId();
//
//        createBBIESC(bbieId, bdtId);

        logger.debug("leave createBBIETree(" + bcc.getBccId() + ", " + abie + ", " + seqKey +
                "), elapsed time(ms): " + (System.currentTimeMillis() - currentTimeMillis));
    }

    private class CreateASBIETreeTask {

        private AssociationCoreComponentProperty asccp;
        private AggregateBusinessInformationEntity roleOfAbie;
        private AssociationCoreComponent ascc;
        private AggregateBusinessInformationEntity fromAbie;
        private int seqKey;

        private AssociationBusinessInformationEntityProperty asbiep;
        private AssociationBusinessInformationEntity asbie;

        public CreateASBIETreeTask(int userId, AssociationCoreComponentProperty asccp, AggregateBusinessInformationEntity roleOfAbie,
                                   AssociationCoreComponent ascc, AggregateBusinessInformationEntity fromAbie, int seqKey) {
            this.asccp = asccp;
            this.roleOfAbie = roleOfAbie;
            this.ascc = ascc;
            this.fromAbie = fromAbie;
            this.seqKey = seqKey;

            createASBIEP(userId);
            createASBIE(userId);
        }

        public void createASBIEP(int userId) {
            asbiep = new AssociationBusinessInformationEntityProperty();
            asbiep.setGuid(Utility.generateGUID());
            asbiep.setBasedAsccpId(asccp.getAsccpId());
            // asbiep.setRoleOfAbieId(roleOfAbieId);
            asbiep.setCreatedBy(userId);
            asbiep.setLastUpdatedBy(userId);
            asbiep.setDefinition(asccp.getDefinition());
        }

        public void createASBIE(int userId) {
            asbie = new AssociationBusinessInformationEntity();
            asbie.setGuid(Utility.generateGUID());
            // asbie.setFromAbieId(fromAbieId);
            // asbie.setToAsbiepId(asbiep);
            asbie.setBasedAscc(ascc.getAsccId());
            asbie.setCardinalityMax(ascc.getCardinalityMax());
            asbie.setCardinalityMin(ascc.getCardinalityMin());
            asbie.setDefinition(ascc.getDefinition());
            asbie.setCreatedBy(userId);
            asbie.setLastUpdatedBy(userId);
            asbie.setSeqKey(seqKey);
        }

        public AggregateBusinessInformationEntity getRoleOfAbie() {
            return roleOfAbie;
        }

        public AggregateBusinessInformationEntity getFromAbie() {
            return fromAbie;
        }

        public AssociationBusinessInformationEntityProperty getAsbiep() {
            return asbiep;
        }

        public AssociationBusinessInformationEntity getAsbie() {
            return asbie;
        }
    }

    private class ASBIETreeTaskHolder {
        private List<CreateASBIETreeTask> createASBIETreeTasks = new ArrayList();

        public void createASBIETree(CreateBIEContext createBIEContext, AssociationCoreComponent asccVO, AggregateBusinessInformationEntity abie, int seqKey) {
            AssociationCoreComponentProperty asccp = createBIEContext.getASCCP(asccVO.getToAsccpId());
            AggregateCoreComponent acc = createBIEContext.getACC(asccp.getRoleOfAccId());

            AggregateBusinessInformationEntity newAbie = createBIEContext.createABIE(acc, bCSelected.getBizCtxId(), false);

            createASBIETreeTasks.add(new CreateASBIETreeTask(createBIEContext.getUserId(), asccp, newAbie, asccVO, abie, seqKey));
            createBIEs(createBIEContext, acc.getAccId(), newAbie);
        }

        public void save() {
            createASBIETreeTasks.stream()
                    .forEach(task -> {
                        task.getAsbiep().setRoleOfAbieId(task.getRoleOfAbie().getAbieId());
                    });
            asbiepRepository.save(
                    createASBIETreeTasks.stream()
                            .map(task -> task.getAsbiep())
                            .collect(Collectors.toList()));
            asbiepRepository.flush();
            asbiepCount += createASBIETreeTasks.size();

            createASBIETreeTasks.stream()
                    .forEach(task -> {
                        task.getAsbie().setFromAbieId(task.getFromAbie().getAbieId());
                        task.getAsbie().setToAsbiepId(task.getAsbiep().getAsbiepId());
                    });

            asbieRepository.save(
                    createASBIETreeTasks.stream()
                            .map(task -> task.getAsbie())
                            .collect(Collectors.toList()));
            asbieCount += createASBIETreeTasks.size();
        }
    }

    private void createASBIETree(int userId, CreateBIEContext createBIEContext, AssociationCoreComponent asccVO, int abieId, int seqKey, int seq_base) {
        logger.debug("enter createASBIETree(" + asccVO.getAsccId() + ", " + abieId + ", " + seqKey + ")");
        long currentTimeMillis = System.currentTimeMillis();

        AssociationCoreComponentProperty asccp =
                asccpRepository.findAsccpIdAndRoleOfAccIdAndDefinitionByAsccpIdAndRevisionNum(asccVO.getToAsccpId(), 0);
        AggregateCoreComponent acc = accRepository.findAccIdAndBasedAccIdAndDefinitionByAccIdAndRevisionNum(asccp.getRoleOfAccId(), 0);

        AggregateBusinessInformationEntity abie = createABIE(userId, acc, bCSelected.getBizCtxId(), false);
        AssociationBusinessInformationEntityProperty asbiep = createASBIEP(userId, asccp, abie.getAbieId());

        createASBIE(userId, asccVO, abieId, asbiep.getAsbiepId(), seqKey);
        createBIEs(createBIEContext, acc.getAccId(), abie);

        logger.debug("leave createASBIETree(" + asccVO.getAsccId() + ", " + abieId + ", " + seqKey +
                "), elapsed time(ms): " + (System.currentTimeMillis() - currentTimeMillis));
    }

    private List<BasicCoreComponent> getBCC(int accId) {
        return bccRepository.findByFromAccId(accId);
    }

    private List<BasicCoreComponent> getBCCwoAttribute(CreateBIEContext createBIEContext, int accId) {
        return createBIEContext.getBCC(accId).stream()
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


    private AggregateBusinessInformationEntity createABIE(int userId, AggregateCoreComponent acc, int bizCtxId, boolean topLevel) {
        AggregateBusinessInformationEntity abie = new AggregateBusinessInformationEntity();
        String abieGuid = Utility.generateGUID();
        abie.setGuid(abieGuid);
        abie.setBasedAccId(acc.getAccId());
        abie.setDefinition(acc.getDefinition());
        abie.setTopLevel(topLevel);
        abie.setBizCtxId(bizCtxId);
        abie.setCreatedBy(userId);
        abie.setLastUpdatedBy(userId);
        if (topLevel)
            abie.setState(SRTConstants.TOP_LEVEL_ABIE_STATE_EDITING);

        abieRepository.saveAndFlush(abie);
        abieCount++;

        return abie;
    }

    private AssociationBusinessInformationEntityProperty createASBIEP(int userId, AssociationCoreComponentProperty asccp, int abieId) {
        AssociationBusinessInformationEntityProperty asbiep =
                new AssociationBusinessInformationEntityProperty();
        asbiep.setGuid(Utility.generateGUID());
        asbiep.setBasedAsccpId(asccp.getAsccpId());
        asbiep.setRoleOfAbieId(abieId);
        asbiep.setCreatedBy(userId);
        asbiep.setLastUpdatedBy(userId);
        asbiep.setDefinition(asccp.getDefinition());

        asbiepRepository.saveAndFlush(asbiep);
        asbiepCount++;

        return asbiep;
    }

    private AssociationBusinessInformationEntity createASBIE(int userId, AssociationCoreComponent asccVO, int abieId, int asbiep, int seqKey) {
        AssociationBusinessInformationEntity asbie = new AssociationBusinessInformationEntity();
        asbie.setGuid(Utility.generateGUID());
        asbie.setFromAbieId(abieId);
        asbie.setToAsbiepId(asbiep);
        asbie.setBasedAscc(asccVO.getAsccId());
        asbie.setCardinalityMax(asccVO.getCardinalityMax());
        asbie.setCardinalityMin(asccVO.getCardinalityMin());
        asbie.setDefinition(asccVO.getDefinition());
        asbie.setCreatedBy(userId);
        asbie.setLastUpdatedBy(userId);
        asbie.setSeqKey(seqKey);

        asbieRepository.saveAndFlush(asbie);
        asbieCount++;

        return asbie;
    }

    private BasicBusinessInformationEntityProperty createBBIEP(int userId, BasicCoreComponentProperty bbcp) {
        BasicBusinessInformationEntityProperty bbiep = new BasicBusinessInformationEntityProperty();
        bbiep.setGuid(Utility.generateGUID());
        bbiep.setBasedBccpId(bbcp.getBccpId());
        bbiep.setCreatedBy(userId);
        bbiep.setLastUpdatedBy(userId);
        bbiep.setDefinition(bbcp.getDefinition());

        bbiepRepository.saveAndFlush(bbiep);
        bbiepCount++;

        return bbiep;
    }

    private BasicBusinessInformationEntity createBBIE(
            int userId, BasicCoreComponent bccVO, int abie, int bbiep, int seqKey, int bdtPrimitiveRestrictionId) {
        BasicBusinessInformationEntity bbie = new BasicBusinessInformationEntity();
        bbie.setGuid(Utility.generateGUID());
        bbie.setBasedBccId(bccVO.getBccId());
        bbie.setFromAbieId(abie);
        bbie.setToBbiepId(bbiep);
        bbie.setNillable(false);
        bbie.setCardinalityMax(bccVO.getCardinalityMax());
        bbie.setCardinalityMin(bccVO.getCardinalityMin());
        bbie.setBdtPriRestriId(bdtPrimitiveRestrictionId);
        bbie.setCreatedBy(userId);
        bbie.setLastUpdatedBy(userId);
        bbie.setSeqKey(seqKey);

        bbieRepository.saveAndFlush(bbie);
        bbieCount++;

        return bbie;
    }

    private void createBBIESC(int bbie, int bdt) {
        List<DataTypeSupplementaryComponent> dtScList = dtScRepository.findByOwnerDtId(bdt);
        List<BasicBusinessInformationEntitySupplementaryComponent> bbieScList = new ArrayList();

        for (DataTypeSupplementaryComponent dtSc : dtScList) {
            if (dtSc.getMaxCardinality() == 0)
                continue;

            BasicBusinessInformationEntitySupplementaryComponent bbieSc =
                    new BasicBusinessInformationEntitySupplementaryComponent();
            bbieSc.setBbieId(bbie);
            bbieSc.setDtScId(dtSc.getDtScId());
            bbieSc.setMaxCardinality(dtSc.getMaxCardinality());
            bbieSc.setMinCardinality(dtSc.getMinCardinality());
            bbieSc.setDefinition(dtSc.getDefinition());

            bbieScList.add(bbieSc);
        }

        bbiescRepository.save(bbieScList);
        bbiescCount += bbieScList.size();
    }

    private void copyBIEs(
            int userId,
            AggregateBusinessInformationEntity oabieVO,
            AggregateBusinessInformationEntity nabieVO,
            AssociationBusinessInformationEntityProperty nasbiepVO, int groupPosition, TreeNode tNode) {
        List<BasicBusinessInformationEntity> basicBusinessInformationEntities = getBBIEsFromABIE(oabieVO.getAbieId());

        for (BasicBusinessInformationEntity basicBusinessInformationEntity : basicBusinessInformationEntities) {
            BasicBusinessInformationEntityProperty oBBIEPVO =
                    bbiepRepository.findOne(basicBusinessInformationEntity.getToBbiepId());
            BasicBusinessInformationEntityProperty nBBIEPVO = copyBBIEP(userId, oBBIEPVO);
            BasicBusinessInformationEntity nBasicBusinessInformationEntity =
                    copyBBIE(userId, basicBusinessInformationEntity, nabieVO.getAbieId(), nBBIEPVO.getBbiepId());

            BasicCoreComponentProperty bccpVO = bccpRepository.findOne(nBBIEPVO.getBasedBccpId());

            ABIEView av = applicationContext.getBean(ABIEView.class, bccpVO.getPropertyTerm(), nBasicBusinessInformationEntity.getBbieId(), "BBIE");
            av.setBbiep(nBBIEPVO);
            av.setBasicCoreComponentPropertyAndBasicBusinessInformationEntity(bccpVO, nBasicBusinessInformationEntity);

            DataType dtVO = dataTypeRepository.findOne(bccpVO.getBdtId());
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
                    asbiepRepository.findOne(oASBIEVO.getToAsbiepId());
            AggregateBusinessInformationEntity o_next_abieVO =
                    abieRepository.findOne(o_next_asbiepVO.getRoleOfAbieId());
            AggregateBusinessInformationEntity n_next_abieVO = copyABIE(userId, o_next_abieVO, bCSelected.getBizCtxId(), false);
            AssociationBusinessInformationEntityProperty n_next_asbiepVO = copyASBIEP(userId, o_next_asbiepVO, n_next_abieVO.getAbieId());
            AssociationBusinessInformationEntity nASBIEVO = copyASBIE(userId, oASBIEVO, nabieVO.getAbieId(), n_next_asbiepVO.getAsbiepId());

            AssociationCoreComponentProperty asccpVO =
                    asccpRepository.findOne(n_next_asbiepVO.getBasedAsccpId());

            ABIEView av = applicationContext.getBean(ABIEView.class, asccpVO.getPropertyTerm(), nabieVO.getAbieId(), "ASBIE");
            av.setColor("blue");
            av.setAbie(n_next_abieVO);
            av.setAsbie(nASBIEVO);
            av.setAsbiep(n_next_asbiepVO);
            TreeNode tNode2 = new DefaultTreeNode(av, tNode);
            copyBIEs(userId, o_next_abieVO, n_next_abieVO, n_next_asbiepVO, -1, tNode2);
        }
    }

    private AggregateBusinessInformationEntity copyABIE(int userId,
                                                        AggregateBusinessInformationEntity sourceAggregateBusinessInformationEntity,
                                                        int bizCtxId, boolean topLevel) {
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
        cloneAggregateBusinessInformationEntity.setCreatedBy(userId);
        cloneAggregateBusinessInformationEntity.setLastUpdatedBy(userId);
        if (topLevel)
            cloneAggregateBusinessInformationEntity.setState(SRTConstants.TOP_LEVEL_ABIE_STATE_EDITING);

        abieRepository.saveAndFlush(cloneAggregateBusinessInformationEntity);
        abieCount++;

        return cloneAggregateBusinessInformationEntity;
    }

    private AssociationBusinessInformationEntityProperty copyASBIEP(int userId,
                                                                    AssociationBusinessInformationEntityProperty sourceAssociationBusinessInformationEntityProperty,
                                                                    int abieId) {
        AssociationBusinessInformationEntityProperty cloneAssociationBusinessInformationEntityProperty =
                new AssociationBusinessInformationEntityProperty();
        cloneAssociationBusinessInformationEntityProperty.setGuid(Utility.generateGUID());
        cloneAssociationBusinessInformationEntityProperty.setBasedAsccpId(sourceAssociationBusinessInformationEntityProperty.getBasedAsccpId());
        cloneAssociationBusinessInformationEntityProperty.setRoleOfAbieId(abieId);
        cloneAssociationBusinessInformationEntityProperty.setCreatedBy(userId);
        cloneAssociationBusinessInformationEntityProperty.setLastUpdatedBy(userId);
        cloneAssociationBusinessInformationEntityProperty.setDefinition(sourceAssociationBusinessInformationEntityProperty.getDefinition());

        asbiepRepository.saveAndFlush(cloneAssociationBusinessInformationEntityProperty);
        asbiepCount++;

        return cloneAssociationBusinessInformationEntityProperty;
    }

    private AssociationBusinessInformationEntity copyASBIE(int userId, AssociationBusinessInformationEntity oasbieVO, int abieid, int asbiepid) {
        AssociationBusinessInformationEntity asbieVO = new AssociationBusinessInformationEntity();
        asbieVO.setGuid(Utility.generateGUID());
        asbieVO.setFromAbieId(abieid);
        asbieVO.setToAsbiepId(asbiepid);
        asbieVO.setBasedAscc(oasbieVO.getBasedAscc());
        asbieVO.setCardinalityMax(oasbieVO.getCardinalityMax());
        asbieVO.setCardinalityMin(oasbieVO.getCardinalityMin());
        asbieVO.setDefinition(oasbieVO.getDefinition());
        asbieVO.setCreatedBy(userId);
        asbieVO.setLastUpdatedBy(userId);
        asbieVO.setSeqKey(oasbieVO.getSeqKey());

        asbieRepository.saveAndFlush(asbieVO);
        asbieCount++;

        return asbieVO;
    }

    private BasicBusinessInformationEntityProperty copyBBIEP(int userId, BasicBusinessInformationEntityProperty obbiepVO) {
        BasicBusinessInformationEntityProperty nbbiepVO = new BasicBusinessInformationEntityProperty();
        nbbiepVO.setBasedBccpId(obbiepVO.getBasedBccpId());
        nbbiepVO.setDefinition(obbiepVO.getDefinition());
        nbbiepVO.setRemark(obbiepVO.getRemark());
        nbbiepVO.setBizTerm(obbiepVO.getBizTerm());
        nbbiepVO.setGuid(Utility.generateGUID());
        nbbiepVO.setCreatedBy(userId);
        nbbiepVO.setLastUpdatedBy(userId);

        bbiepRepository.saveAndFlush(nbbiepVO);
        bbiepCount++;

        return nbbiepVO;
    }

    private BasicBusinessInformationEntity copyBBIE(int userId, BasicBusinessInformationEntity obbieVO, int abie, int bbiep) {
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
        nbbieVO.setCreatedBy(userId);
        nbbieVO.setLastUpdatedBy(userId);

        bbieRepository.saveAndFlush(nbbieVO);
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
        bbiescRepository.saveAndFlush(nbbiescVO);
        bbiescCount++;

        DataTypeSupplementaryComponent dtscvo = dtScRepository.findOne(nbbiescVO.getDtScId());

        ABIEView av = applicationContext.getBean(ABIEView.class, dtscvo.getPropertyTerm(), nbbiescVO.getBbieScId(), "BBIESC");

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
                    accRepository.saveAndFlush(nueACC);

                    AssociationCoreComponentProperty nASCCP = new AssociationCoreComponentProperty();
                    nASCCP.setGuid(Utility.generateGUID());
                    nASCCP.setPropertyTerm(objectClassTerm);
                    nASCCP.setRoleOfAccId(ueACC.getAccId());
                    nASCCP.setDen(nASCCP.getPropertyTerm() + "." + Utility.first(ueACC.getDen()));
                    nASCCP.setState(4);
                    nASCCP.setReusableIndicator(false);
                    nASCCP.setRevisionNum(0);
                    nASCCP.setRevisionTrackingNum(0);
                    asccpRepository.saveAndFlush(nASCCP);

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
                    asccRepository.saveAndFlush(nASCC);
                    //here : history record population

                    AssociationCoreComponentProperty hASCCP = new AssociationCoreComponentProperty(); // populate ASCCP history record
                    hASCCP.setRevisionNum(1);
                    hASCCP.setRevisionTrackingNum(1);
                    asccpRepository.saveAndFlush(hASCCP);

                    AssociationCoreComponent hASCC = new AssociationCoreComponent(); //populate ASCC history record
                    hASCC.setRevisionNum(1);
                    hASCC.setRevisionTrackingNum(1);
                    asccRepository.saveAndFlush(hASCC);
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
        ABIEView rootABIEView = applicationContext.getBean(ABIEView.class, selectedBod.getName(), selectedBod.getId(), "ROOT");
        rootABIEView.setAbie(selectedBod.getAbie());
        root = new DefaultTreeNode(rootABIEView, null);

        aABIEView = applicationContext.getBean(ABIEView.class, selectedBod.getName(), selectedBod.getId(), "ABIE");
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
        BasicBusinessInformationEntityProperty bbiepVO = bbiepRepository.findOne(bbieVO.getToBbiepId());
        BasicCoreComponentProperty bccpVO = bccpRepository.findOne(bbiepVO.getBasedBccpId());
        BasicCoreComponent bccVO = bccRepository.findOne(bbieVO.getBasedBccId());

        ABIEView av = applicationContext.getBean(ABIEView.class, bccpVO.getPropertyTerm(), bbieVO.getBbieId(), "BBIE");
        av.setBcc(bccVO);
        av.setBbiep(bbiepVO);
        av.setBasicCoreComponentPropertyAndBasicBusinessInformationEntity(bccpVO, bbieVO);

        DataType dtVO = dataTypeRepository.findOne(bccpVO.getBdtId());
        av.setBdtName(dtVO.getDen());
        av.setColor("green");
        TreeNode tNode2 = new DefaultTreeNode(av, tNode);
    }

    private void showASBIETree(AssociationBusinessInformationEntity asbieVO, TreeNode tNode) {
        AssociationBusinessInformationEntityProperty asbiepVO = asbiepRepository.findOne(asbieVO.getToAsbiepId());
        AssociationCoreComponentProperty asccpVO = asccpRepository.findOne(asbiepVO.getBasedAsccpId());
        AssociationCoreComponent asccVO = asccRepository.findOne(asbieVO.getBasedAscc());
        AggregateBusinessInformationEntity abieVO = abieRepository.findOne(asbiepVO.getRoleOfAbieId());
        AggregateCoreComponent accVO = accRepository.findOne(abieVO.getBasedAccId());

        ABIEView av = applicationContext.getBean(ABIEView.class, asccpVO.getPropertyTerm(), abieVO.getAbieId(), "ASBIE");
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
            DataTypeSupplementaryComponent dtscVO = dtScRepository.findOne(bbiescVO.getDtScId());

            ABIEView av_01 = applicationContext.getBean(ABIEView.class, dtscVO.getPropertyTerm(), bbiescVO.getBbieScId(), "BBIESC");
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

            List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList =
                    bdtPriRestriRepository.findByCdtAwdPriXpsTypeMapId(aABIEView.getBdtPrimitiveRestrictionId());
            BusinessDataTypePrimitiveRestriction aBDTPrimitiveRestrictionVO = (bdtPriRestriList.isEmpty()) ? null : bdtPriRestriList.get(0);
            CodeList codeList = (aBDTPrimitiveRestrictionVO != null) ? codeListRepository.findOne(aBDTPrimitiveRestrictionVO.getCodeListId()) : null;
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
        businessContextRepository.saveAndFlush(bcVO);

        for (BusinessContextValues bcv : bcH.getBcValues()) {
            for (ContextSchemeValue cVO : bcv.getCsList()) {
                BusinessContextValue bcvVO = new BusinessContextValue();
                bcvVO.setBizCtxId(bcVO.getBizCtxId());
                bcvVO.setCtxSchemeValueId(cVO.getCtxSchemeValueId());
                businessContextValueRepository.saveAndFlush(bcvVO);
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
            aABIEView = applicationContext.getBean(ABIEView.class);
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

            List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList =
                    bdtPriRestriRepository.findByCdtAwdPriXpsTypeMapId(aABIEView.getBdtPrimitiveRestrictionId());
            BusinessDataTypePrimitiveRestriction aBDTPrimitiveRestrictionVO = (bdtPriRestriList.isEmpty()) ? null : bdtPriRestriList.get(0);
            CodeList codeList = (aBDTPrimitiveRestrictionVO != null) ? codeListRepository.findOne(aBDTPrimitiveRestrictionVO.getCodeListId()) : null;
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

        bbieRepository.saveAndFlush(bbieVO);
        bbiepRepository.saveAndFlush(bbiepVO);
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

        List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList =
                bdtPriRestriRepository.findByCdtAwdPriXpsTypeMapId(bdtPrimitiveRestrictionId);
        BusinessDataTypePrimitiveRestriction aBDTPrimitiveRestrictionVO = (bdtPriRestriList.isEmpty()) ? null : bdtPriRestriList.get(0);
        CodeList codeList = (aBDTPrimitiveRestrictionVO != null) ? codeListRepository.findOne(aBDTPrimitiveRestrictionVO.getCodeListId()) : null;
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
        BasicCoreComponent basicCoreComponent = bccRepository.findOne(bccId);
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
        bbiescRepository.saveAndFlush(bbiescVO);
    }

    public void closeDialog() {
        RequestContext.getCurrentInstance().closeDialog(this);
    }

    private void saveASBIEChanges(ABIEView aABIEView) {
        AssociationBusinessInformationEntity asbieVO = aABIEView.getAsbie();
        AggregateBusinessInformationEntity abieVO = aABIEView.getAbie();
        AssociationBusinessInformationEntityProperty asbiepVO = aABIEView.getAsbiep();

        asbieRepository.saveAndFlush(asbieVO);
        asbiepRepository.saveAndFlush(asbiepVO);
        abieRepository.saveAndFlush(abieVO);
    }

    private void saveABIEChanges(ABIEView aABIEView) {
        AggregateBusinessInformationEntity abieVO = aABIEView.getAbie();
        abieRepository.saveAndFlush(abieVO);
    }

}