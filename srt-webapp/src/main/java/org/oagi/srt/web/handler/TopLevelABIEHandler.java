package org.oagi.srt.web.handler;

import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.provider.CoreComponentProvider;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.CoreComponentService;
import org.primefaces.context.RequestContext;
import org.primefaces.event.*;
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
    private BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository bdtScPriRestriRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessObjectDocumentRepository bodRepository;

    @Autowired
    private CoreComponentService coreComponentService;

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

    private List<ABIEView> bodAbieViewList = new ArrayList<ABIEView>();
    private ABIEView selectedBod;

    public ABIEView getSelectedBod() {
        return selectedBod;
    }

    public void setSelectedBod(ABIEView selectedBod) {
        this.selectedBod = selectedBod;
    }

    public List<ABIEView> getBodList() {
        if (bodAbieViewList.isEmpty()) {
            List<BusinessObjectDocument> bodList = bodRepository.findAll();
            for (BusinessObjectDocument bod : bodList) {
                AggregateBusinessInformationEntity abie =
                        abieRepository.findOne(bod.getTopLevelAbieId());
                if (abie == null) {
                    continue;
                }

                AssociationBusinessInformationEntityProperty asbiep =
                        asbiepRepository.findOneByRoleOfAbieId(abie.getAbieId());
                if (asbiep == null) {
                    continue;
                }

                AssociationCoreComponentProperty asccp =
                        asccpRepository.findOne(asbiep.getBasedAsccpId());
                if (asccp == null) {
                    continue;
                }

                BusinessContext businessContext =
                        businessContextRepository.findOne(bod.getBizCtxId());

                abie.setBizCtxName(businessContext.getName());

                ABIEView aABIEView = applicationContext.getBean(ABIEView.class,
                        asccp.getPropertyTerm(),
                        abie.getAbieId(), "ABIE");
                aABIEView.setBod(bod);
                aABIEView.setAbie(abie);
                bodAbieViewList.add(aABIEView);
            }
        }
        return bodAbieViewList;
    }

    public void setBodList(List<ABIEView> bodList) {
        this.bodAbieViewList = bodList;
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
    public void createBIEs() {
        int userId = userRepository.findAppUserIdByLoginId("oagis");

        AggregateCoreComponent acc =
                accRepository.findOne(selected.getRoleOfAccId());
        BusinessObjectDocument bod = createBOD(bCSelected.getBizCtxId());
        topAbieVO = createABIE(userId, acc, bod);
        updateBOD(bod, topAbieVO);

        int abieId = topAbieVO.getAbieId();

        ABIEView rootABIEView = applicationContext.getBean(ABIEView.class, selected.getPropertyTerm(), abieId, "ABIE");
        rootABIEView.setBod(bod);
        rootABIEView.setAbie(topAbieVO);
        root = new DefaultTreeNode(rootABIEView, null);

        asbiepVO = createASBIEP(userId, selected, abieId, bod);

        aABIEView = applicationContext.getBean(ABIEView.class, selected.getPropertyTerm(), abieId, "ABIE");
        aABIEView.setBod(bod);
        aABIEView.setAbie(topAbieVO);
        aABIEView.setColor("blue");
        aABIEView.setAcc(acc);
        aABIEView.setAsbiep(asbiepVO);

        TreeNode toplevelNode = new DefaultTreeNode(aABIEView, root);

        CreateBIEContext createBIEContext = new CreateBIEContext(userId, bod);
        createBIEs(createBIEContext, selected.getRoleOfAccId(), topAbieVO);
        createBIEContext.save();

        createBIEChildren(abieId, toplevelNode);
    }

    public String onFlowProcess_Copy(FlowEvent event) {

        if (event.getNewStep().equals(SRTConstants.TAB_TOP_LEVEL_ABIE_SELECT_BC)) {

        } else if (event.getNewStep().equals(SRTConstants.TAB_TOP_LEVEL_ABIE_COPY_UC_BIE)) {
            // TODO if go back from the confirmation page? avoid that situation

            AggregateBusinessInformationEntity selectedAbie =
                    selectedBod.getAbie();
            BusinessObjectDocument selectedBod = bodRepository.findByTopLevelAbieId(selectedAbie.getAbieId());
            AggregateCoreComponent aggregateCoreComponent =
                    accRepository.findOne(selectedAbie.getBasedAccId());
            AssociationCoreComponentProperty asccp =
                    asccpRepository.findOneByRoleOfAccId(aggregateCoreComponent.getAccId());

            int userId = userRepository.findAppUserIdByLoginId("oagis");
            BusinessObjectDocument copiedBod = copyBOD(selectedBod);
            topAbieVO = copyABIE(userId, selectedAbie, copiedBod);
            updateBOD(copiedBod, topAbieVO);
            int abieId = topAbieVO.getAbieId();

            ABIEView rootABIEView = applicationContext.getBean(ABIEView.class, asccp.getPropertyTerm(), abieId, "ABIE");
            BusinessObjectDocument bod = bodRepository.findOne(topAbieVO.getBodId());
            rootABIEView.setBod(bod);
            rootABIEView.setAbie(topAbieVO);
            root = new DefaultTreeNode(rootABIEView, null);
            AssociationBusinessInformationEntityProperty asbiep =
                    copyASBIEP(userId, asbiepRepository.
                            findOneByRoleOfAbieId(selectedAbie.getAbieId()), abieId, copiedBod);
            aABIEView = applicationContext.getBean(ABIEView.class, asccp.getPropertyTerm(), abieId, "ABIE");
            aABIEView.setBod(bod);
            aABIEView.setAbie(topAbieVO);
            aABIEView.setColor("blue");
            aABIEView.setAsbiep(asbiep);
            TreeNode toplevelNode = new DefaultTreeNode(aABIEView, root);

            copyBIEs(userId, selectedAbie, topAbieVO, asbiep, -1, toplevelNode, copiedBod);

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
        List<CoreComponent> assoc = coreComponentService.getCoreComponents(aggregateCoreComponent, createBIEContext);
        return getAssocList(createBIEContext, assoc);
    }

    private List<CoreComponent> queryNestedChildAssoc_wo_attribute(CreateBIEContext createBIEContext, AggregateCoreComponent aggregateCoreComponent) {
        List<CoreComponent> assoc = coreComponentService.getCoreComponentsWithoutAttributes(aggregateCoreComponent, createBIEContext);
        return getAssocList(createBIEContext, assoc);
    }

    private List<CoreComponent> queryChildAssoc(CreateBIEContext createBIEContext,
                                                AggregateCoreComponent acc) {
        List<CoreComponent> assoc = coreComponentService.getCoreComponents(acc, createBIEContext);
        return assoc;
    }

    private List<CoreComponent> handleNestedGroup(CreateBIEContext createBIEContext,
                                                  AggregateCoreComponent acc,
                                                  List<CoreComponent> coreComponents, int gPosition) {
        List<CoreComponent> bList;
        try {
            bList = queryChildAssoc(createBIEContext, acc);
        } catch (StackOverflowError e) {
            throw e;
        }
        if (!bList.isEmpty()) {
            coreComponents.addAll(gPosition, bList);
            coreComponents.remove(gPosition + bList.size());
        }

        for (int i = 0; i < coreComponents.size(); i++) {
            CoreComponent coreComponent = coreComponents.get(i);
            if (coreComponent instanceof AssociationCoreComponent &&
                groupcheck(createBIEContext, (AssociationCoreComponent) coreComponent)) {

                AssociationCoreComponent ascc = (AssociationCoreComponent) coreComponent;
                AssociationCoreComponentProperty asccp = createBIEContext.getASCCP(ascc.getToAsccpId());
                coreComponents = handleNestedGroup(
                        createBIEContext, createBIEContext.getACC(asccp.getRoleOfAccId()), coreComponents, i);
            }
        }

        return coreComponents;
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

    private class CreateBIEContext implements CoreComponentProvider {
        private ABIETaskHolder abieTaskHolder;
        private BBIETreeTaskHolder bbieTreeTaskHolder;
        private ASBIETreeTaskHolder asbieTreeTaskHolder;

        private Map<Integer, AggregateCoreComponent> aggregateCoreComponentMap;
        private Map<Integer, AssociationCoreComponentProperty> associationCoreComponentPropertyMap;
        private Map<Integer, BasicCoreComponentProperty> basicCoreComponentPropertyMap;

        private Map<Integer, BusinessDataTypePrimitiveRestriction> bdtPriRestriDefaultMap;
        private Map<Integer, BusinessDataTypePrimitiveRestriction> bdtPriRestriCodeListMap;

        private Map<Integer, BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriDefaultMap;
        private Map<Integer, BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriCodeListMap;

        private int userId;
        private BusinessObjectDocument bod;

        private List<BasicCoreComponent> basicCoreComponents;
        private List<AssociationCoreComponent> associationCoreComponents;
        private List<DataTypeSupplementaryComponent> dataTypeSupplementaryComponents;

        public CreateBIEContext(int userId, BusinessObjectDocument bod) {
            abieTaskHolder = new ABIETaskHolder();
            bbieTreeTaskHolder = new BBIETreeTaskHolder();
            asbieTreeTaskHolder = new ASBIETreeTaskHolder();
            this.userId = userId;
            this.bod = bod;

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

            List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList = bdtPriRestriRepository.findAll();
            bdtPriRestriDefaultMap = bdtPriRestriList.stream()
                    .filter(bdtPriRestri -> bdtPriRestri.isDefault())
                    .collect(Collectors.toMap(bdtPriRestri -> bdtPriRestri.getBdtId(), Function.identity()));
            bdtPriRestriCodeListMap = bdtPriRestriList.stream()
                    .filter(bdtPriRestri -> bdtPriRestri.getCodeListId() > 0)
                    .collect(Collectors.toMap(bdtPriRestri -> bdtPriRestri.getBdtId(), Function.identity()));

            List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriList = bdtScPriRestriRepository.findAll();
            bdtScPriRestriDefaultMap = bdtScPriRestriList.stream()
                    .filter(bdtScPriRestri -> bdtScPriRestri.isDefault())
                    .collect(Collectors.toMap(bdtScPriRestri -> bdtScPriRestri.getBdtScId(), Function.identity()));
            bdtScPriRestriCodeListMap = bdtScPriRestriList.stream()
                    .filter(bdtScPriRestri -> bdtScPriRestri.getCodeListId() > 0)
                    .collect(Collectors.toMap(bdtScPriRestri -> bdtScPriRestri.getBdtScId(), Function.identity()));
        }

        public int getUserId() {
            return userId;
        }

        public BusinessObjectDocument getBod() {
            return bod;
        }

        public AggregateCoreComponent getACC(int accId) {
            return aggregateCoreComponentMap.get(accId);
        }

        public AssociationCoreComponentProperty getASCCP(int asccpId) {
            return associationCoreComponentPropertyMap.get(asccpId);
        }

        public AggregateBusinessInformationEntity createABIE(AggregateCoreComponent acc) {
            return abieTaskHolder.createABIE(userId, acc);
        }

        public void createBBIETree(BasicCoreComponent bcc, AggregateBusinessInformationEntity abie, int seqKey) {
            bbieTreeTaskHolder.createBBIETree(this, bcc, abie, seqKey);
        }

        public void createASBIETree(AssociationCoreComponent asccVO, AggregateBusinessInformationEntity abie, int seqKey) {
            asbieTreeTaskHolder.createASBIETree(this, asccVO, abie, seqKey);
        }

        @Override
        public List<BasicCoreComponent> getBCCs(int fromAccId) {
            return basicCoreComponents.stream()
                    .filter(bcc -> bcc.getFromAccId() == fromAccId)
                    .collect(Collectors.toList());
        }

        @Override
        public List<BasicCoreComponent> getBCCsWithoutAttributes(int accId) {
            return getBCCs(accId).stream()
                    .filter(e -> e.getSeqKey() != 0)
                    .collect(Collectors.toList());
        }

        @Override
        public List<AssociationCoreComponent> getASCCs(int fromAccId) {
            return associationCoreComponents.stream()
                    .filter(acc -> acc.getFromAccId() == fromAccId)
                    .collect(Collectors.toList());
        }

        public BasicCoreComponentProperty getBCCP(int toBccpId) {
            return basicCoreComponentPropertyMap.get(toBccpId);
        }

        public int getDefaultBdtPriRestriId(int bdtId) {
            return bdtPriRestriDefaultMap.get(bdtId).getBdtPriRestriId();
        }

        public int getCodeListIdOfBdtPriRestriId(int bdtId) {
            BusinessDataTypePrimitiveRestriction e = bdtPriRestriCodeListMap.get(bdtId);
            return (e != null) ? e.getCodeListId() : 0;
        }

        public int getDefaultBdtScPriRestriId(int bdtScId) {
            BusinessDataTypeSupplementaryComponentPrimitiveRestriction e = bdtScPriRestriDefaultMap.get(bdtScId);
            return (e != null) ? e.getBdtScPriRestriId() : 0;
        }

        public int getCodeListIdOfBdtScPriRestriId(int bdtScId) {
            BusinessDataTypeSupplementaryComponentPrimitiveRestriction e = bdtScPriRestriCodeListMap.get(bdtScId);
            return (e != null) ? e.getCodeListId() : 0;
        }

        public List<DataTypeSupplementaryComponent> findByOwnerDtId(int ownerDtId) {
            return dataTypeSupplementaryComponents.stream()
                    .filter(dtSc -> dtSc.getOwnerDtId() == ownerDtId)
                    .collect(Collectors.toList());
        }

        public void save() {
            abieTaskHolder.save(bod);
            bbieTreeTaskHolder.save(bod);
            asbieTreeTaskHolder.save(bod);
        }
    }

    private class ABIETaskHolder {

        private List<AggregateBusinessInformationEntity> aggregateBusinessInformationEntitys = new ArrayList();

        public AggregateBusinessInformationEntity createABIE(int userId, AggregateCoreComponent acc) {
            AggregateBusinessInformationEntity abie = new AggregateBusinessInformationEntity();
            String abieGuid = Utility.generateGUID();
            abie.setGuid(abieGuid);
            abie.setBasedAccId(acc.getAccId());
            abie.setDefinition(acc.getDefinition());
            abie.setCreatedBy(userId);
            abie.setLastUpdatedBy(userId);

            aggregateBusinessInformationEntitys.add(abie);

            return abie;
        }

        public void save(BusinessObjectDocument bod) {
            aggregateBusinessInformationEntitys.stream()
                    .forEach(e -> e.setBodId(bod.getBodId()));
            abieRepository.save(aggregateBusinessInformationEntitys);
        }

    }

    public void createBIEs(CreateBIEContext createBIEContext, int accId, AggregateBusinessInformationEntity abie) {
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
            int bdtPrimitiveRestrictionId = createBIEContext.getDefaultBdtPriRestriId(bdtId);
            int codeListId = createBIEContext.getCodeListIdOfBdtPriRestriId(bdtId);

            createBBIEP(createBIEContext.getUserId(), bccp);
            createBBIE(createBIEContext.getUserId(), bdtPrimitiveRestrictionId, codeListId);
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

        private void createBBIE(int userId, int bdtPrimitiveRestrictionId, int codeListId) {
            bbie = new BasicBusinessInformationEntity();
            bbie.setGuid(Utility.generateGUID());
            bbie.setBasedBccId(bcc.getBccId());
            // bbie.setFromAbieId(abie);
            // bbie.setToBbiepId(bbiepId);
            bbie.setNillable(false);
            bbie.setCardinalityMax(bcc.getCardinalityMax());
            bbie.setCardinalityMin(bcc.getCardinalityMin());
            bbie.setBdtPriRestriId(bdtPrimitiveRestrictionId);
//            if (codeListId > 0) {
//                bbie.setCodeListId(codeListId);
//            }
            bbie.setCreatedBy(userId);
            bbie.setLastUpdatedBy(userId);
            bbie.setSeqKey(seqKey);
        }

        private void createBBIESC(CreateBIEContext createBIEContext, int bdtId) {
            bbieScList = createBIEContext.findByOwnerDtId(bdtId)
                    .stream()
                    .filter(dtSc -> dtSc.getMaxCardinality() != 0)
                    .map(dtSc -> {
                        BasicBusinessInformationEntitySupplementaryComponent bbieSc =
                                new BasicBusinessInformationEntitySupplementaryComponent();
                        // bbieSc.setBbieId(bbieId);
                        int bdtScId = dtSc.getDtScId();
                        bbieSc.setDtScId(bdtScId);
                        int bdtScPriRestriId = createBIEContext.getDefaultBdtScPriRestriId(bdtScId);
                        if (bdtScPriRestriId > 0) {
                            bbieSc.setDtScPriRestriId(bdtScPriRestriId);
                        }
                        int codeListId = createBIEContext.getCodeListIdOfBdtScPriRestriId(bdtScId);
//                        if (codeListId > 0) {
//                            bbieSc.setCodeListId(codeListId);
//                        }
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

        public void save(BusinessObjectDocument bod) {
            List<BasicBusinessInformationEntityProperty> bbiepList =
                    createBBIETreeTasks.stream()
                            .map(task -> task.getBbiep())
                            .collect(Collectors.toList());
            bbiepList.stream().forEach(e -> e.setBodId(bod.getBodId()));
            bbiepRepository.save(bbiepList);
            bbiepCount += createBBIETreeTasks.size();

            createBBIETreeTasks.stream()
                    .forEach(task -> {
                        task.getBbie().setFromAbieId(task.getAbie().getAbieId());
                        task.getBbie().setToBbiepId(task.getBbiep().getBbiepId());
                    });

            List<BasicBusinessInformationEntity> bbieList =
                    createBBIETreeTasks.stream()
                            .map(task -> task.getBbie())
                            .collect(Collectors.toList());
            bbieList.stream().forEach(e -> e.setBodId(bod.getBodId()));
            bbieRepository.save(bbieList);
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
            bbieScList.stream().forEach(e -> e.setBodId(bod.getBodId()));
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

            AggregateBusinessInformationEntity newAbie = createBIEContext.createABIE(acc);

            createASBIETreeTasks.add(new CreateASBIETreeTask(createBIEContext.getUserId(), asccp, newAbie, asccVO, abie, seqKey));
            createBIEs(createBIEContext, acc.getAccId(), newAbie);
        }

        public void save(BusinessObjectDocument bod) {
            createASBIETreeTasks.stream()
                    .forEach(task -> {
                        task.getAsbiep().setRoleOfAbieId(task.getRoleOfAbie().getAbieId());
                    });
            List<AssociationBusinessInformationEntityProperty> asbiepList =
                    createASBIETreeTasks.stream()
                            .map(task -> task.getAsbiep())
                            .collect(Collectors.toList());
            asbiepList.stream().forEach(e -> e.setBodId(bod.getBodId()));
            asbiepRepository.save(asbiepList);
            asbiepCount += createASBIETreeTasks.size();

            createASBIETreeTasks.stream()
                    .forEach(task -> {
                        task.getAsbie().setFromAbieId(task.getFromAbie().getAbieId());
                        task.getAsbie().setToAsbiepId(task.getAsbiep().getAsbiepId());
                    });

            List<AssociationBusinessInformationEntity> asbieList =
                    createASBIETreeTasks.stream()
                            .map(task -> task.getAsbie())
                            .collect(Collectors.toList());
            asbieList.stream().forEach(e -> e.setBodId(bod.getBodId()));
            asbieRepository.save(asbieList);
            asbieCount += createASBIETreeTasks.size();
        }
    }

    private void createASBIETree(int userId, CreateBIEContext createBIEContext, AssociationCoreComponent asccVO,
                                 int abieId, int seqKey, int seq_base) {
        logger.debug("enter createASBIETree(" + asccVO.getAsccId() + ", " + abieId + ", " + seqKey + ")");
        long currentTimeMillis = System.currentTimeMillis();

        AssociationCoreComponentProperty asccp =
                asccpRepository.findAsccpIdAndRoleOfAccIdAndDefinitionByAsccpIdAndRevisionNum(asccVO.getToAsccpId(), 0);
        AggregateCoreComponent acc = accRepository.findAccIdAndBasedAccIdAndDefinitionByAccIdAndRevisionNum(asccp.getRoleOfAccId(), 0);

        AggregateBusinessInformationEntity abie = createABIE(userId, acc, createBIEContext.getBod());
        AssociationBusinessInformationEntityProperty asbiep = createASBIEP(userId, asccp, abie.getAbieId(), createBIEContext.getBod());

        createASBIE(userId, asccVO, abieId, asbiep.getAsbiepId(), seqKey, createBIEContext.getBod());
        createBIEs(createBIEContext, acc.getAccId(), abie);

        logger.debug("leave createASBIETree(" + asccVO.getAsccId() + ", " + abieId + ", " + seqKey +
                "), elapsed time(ms): " + (System.currentTimeMillis() - currentTimeMillis));
    }

    private List<BasicCoreComponent> getBCC(int accId) {
        return bccRepository.findByFromAccId(accId);
    }

    private List<BasicCoreComponent> getBCCwoAttribute(CreateBIEContext createBIEContext, int accId) {
        return createBIEContext.getBCCs(accId).stream()
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

    private BusinessObjectDocument createBOD(int bizCtxId) {
        BusinessObjectDocument bod = new BusinessObjectDocument();
        bod.setBizCtxId(bizCtxId);
        bod.setState(SRTConstants.TOP_LEVEL_ABIE_STATE_EDITING);
        return bodRepository.saveAndFlush(bod);
    }

    private BusinessObjectDocument copyBOD(BusinessObjectDocument source) {
        BusinessObjectDocument bod = new BusinessObjectDocument();
        bod.setBizCtxId(source.getBizCtxId());
        bod.setState(SRTConstants.TOP_LEVEL_ABIE_STATE_EDITING);
        return bodRepository.saveAndFlush(bod);
    }

    private void updateBOD(BusinessObjectDocument bod, AggregateBusinessInformationEntity topLevelAbie) {
        bod.setTopLevelAbieId(topLevelAbie.getAbieId());
        bodRepository.save(bod);
    }

    private AggregateBusinessInformationEntity createABIE(int userId, AggregateCoreComponent acc, BusinessObjectDocument bod) {
        AggregateBusinessInformationEntity abie = new AggregateBusinessInformationEntity();
        String abieGuid = Utility.generateGUID();
        abie.setGuid(abieGuid);
        abie.setBasedAccId(acc.getAccId());
        abie.setDefinition(acc.getDefinition());
        abie.setCreatedBy(userId);
        abie.setLastUpdatedBy(userId);
        abie.setBodId(bod.getBodId());

        abieRepository.saveAndFlush(abie);
        abieCount++;

        return abie;
    }

    private AssociationBusinessInformationEntityProperty createASBIEP(int userId,
                                                                      AssociationCoreComponentProperty asccp,
                                                                      int abieId,
                                                                      BusinessObjectDocument bod) {
        AssociationBusinessInformationEntityProperty asbiep =
                new AssociationBusinessInformationEntityProperty();
        asbiep.setGuid(Utility.generateGUID());
        asbiep.setBasedAsccpId(asccp.getAsccpId());
        asbiep.setRoleOfAbieId(abieId);
        asbiep.setCreatedBy(userId);
        asbiep.setLastUpdatedBy(userId);
        asbiep.setDefinition(asccp.getDefinition());
        asbiep.setBodId(bod.getBodId());

        asbiepRepository.saveAndFlush(asbiep);
        asbiepCount++;

        return asbiep;
    }

    private AssociationBusinessInformationEntity createASBIE(int userId, AssociationCoreComponent asccVO,
                                                             int abieId, int asbiep, int seqKey,
                                                             BusinessObjectDocument bod) {
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
        asbie.setBodId(bod.getBodId());

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
            AssociationBusinessInformationEntityProperty nasbiepVO, int groupPosition, TreeNode tNode,
            BusinessObjectDocument bod) {
        List<BasicBusinessInformationEntity> basicBusinessInformationEntities = getBBIEsFromABIE(oabieVO.getAbieId());

        for (BasicBusinessInformationEntity basicBusinessInformationEntity : basicBusinessInformationEntities) {
            BasicBusinessInformationEntityProperty oBBIEPVO =
                    bbiepRepository.findOne(basicBusinessInformationEntity.getToBbiepId());
            BasicBusinessInformationEntityProperty nBBIEPVO = copyBBIEP(userId, oBBIEPVO, bod);
            BasicBusinessInformationEntity nBasicBusinessInformationEntity =
                    copyBBIE(userId, basicBusinessInformationEntity, nabieVO.getAbieId(), nBBIEPVO.getBbiepId(), bod);

            BasicCoreComponentProperty bccpVO = bccpRepository.findOne(nBBIEPVO.getBasedBccpId());

            ABIEView av = applicationContext.getBean(ABIEView.class, bccpVO.getPropertyTerm(),
                    nBasicBusinessInformationEntity.getBbieId(), "BBIE");
            av.setBod(bodRepository.findOne(nBBIEPVO.getBodId()));
            av.setBbiep(nBBIEPVO);
            av.setBasicCoreComponentPropertyAndBasicBusinessInformationEntity(bccpVO, nBasicBusinessInformationEntity);

            DataType dtVO = dataTypeRepository.findOne(bccpVO.getBdtId());
            av.setBdtName(dtVO.getDen());
            av.setColor("green");
            TreeNode tNode2 = new DefaultTreeNode(av, tNode);
            List<BasicBusinessInformationEntitySupplementaryComponent> bbiesc =
                    getBBIESCsFromBBIE(basicBusinessInformationEntity.getBbieId());
            for (BasicBusinessInformationEntitySupplementaryComponent oBBIESCVO : bbiesc) {
                copyBBIESC(oBBIESCVO, nBasicBusinessInformationEntity.getBbieId(), tNode2, bod);
            }
        }

        List<AssociationBusinessInformationEntity> asbie = getASBIEsFromABIE(oabieVO.getAbieId());
        for (AssociationBusinessInformationEntity oASBIEVO : asbie) {
            AssociationBusinessInformationEntityProperty o_next_asbiepVO =
                    asbiepRepository.findOne(oASBIEVO.getToAsbiepId());
            AggregateBusinessInformationEntity o_next_abieVO =
                    abieRepository.findOne(o_next_asbiepVO.getRoleOfAbieId());
            AggregateBusinessInformationEntity n_next_abieVO = copyABIE(userId, o_next_abieVO, bod);
            AssociationBusinessInformationEntityProperty n_next_asbiepVO =
                    copyASBIEP(userId, o_next_asbiepVO, n_next_abieVO.getAbieId(), bod);
            AssociationBusinessInformationEntity nASBIEVO =
                    copyASBIE(userId, oASBIEVO, nabieVO.getAbieId(), n_next_asbiepVO.getAsbiepId(), bod);

            AssociationCoreComponentProperty asccpVO =
                    asccpRepository.findOne(n_next_asbiepVO.getBasedAsccpId());

            ABIEView av = applicationContext.getBean(ABIEView.class, asccpVO.getPropertyTerm(), nabieVO.getAbieId(), "ASBIE");
            av.setColor("blue");
            av.setBod(bodRepository.findOne(n_next_abieVO.getBodId()));
            av.setAbie(n_next_abieVO);
            av.setAsbie(nASBIEVO);
            av.setAsbiep(n_next_asbiepVO);
            TreeNode tNode2 = new DefaultTreeNode(av, tNode);
            copyBIEs(userId, o_next_abieVO, n_next_abieVO, n_next_asbiepVO, -1, tNode2, bod);
        }
    }

    private AggregateBusinessInformationEntity copyABIE(int userId,
                                                        AggregateBusinessInformationEntity sourceAbie,
                                                        BusinessObjectDocument bod) {
        AggregateBusinessInformationEntity cloneAbie = new AggregateBusinessInformationEntity();
        String abieGuid = Utility.generateGUID();
        cloneAbie.setGuid(abieGuid);
        cloneAbie.setBasedAccId(sourceAbie.getBasedAccId());
        cloneAbie.setDefinition(sourceAbie.getDefinition());
        cloneAbie.setClientId(sourceAbie.getClientId());
        cloneAbie.setVersion(sourceAbie.getVersion());
        cloneAbie.setStatus(sourceAbie.getStatus());
        cloneAbie.setRemark(sourceAbie.getRemark());
        cloneAbie.setBizTerm(sourceAbie.getBizTerm());
        cloneAbie.setCreatedBy(userId);
        cloneAbie.setLastUpdatedBy(userId);
        cloneAbie.setBodId(bod.getBodId());

        abieRepository.saveAndFlush(cloneAbie);
        abieCount++;

        return cloneAbie;
    }

    private AssociationBusinessInformationEntityProperty copyASBIEP(int userId,
                                                                    AssociationBusinessInformationEntityProperty sourceAsbiep,
                                                                    int abieId, BusinessObjectDocument bod) {
        AssociationBusinessInformationEntityProperty cloneAsbiep =
                new AssociationBusinessInformationEntityProperty();
        cloneAsbiep.setGuid(Utility.generateGUID());
        cloneAsbiep.setBasedAsccpId(sourceAsbiep.getBasedAsccpId());
        cloneAsbiep.setRoleOfAbieId(abieId);
        cloneAsbiep.setCreatedBy(userId);
        cloneAsbiep.setLastUpdatedBy(userId);
        cloneAsbiep.setDefinition(sourceAsbiep.getDefinition());
        cloneAsbiep.setBodId(bod.getBodId());

        asbiepRepository.saveAndFlush(cloneAsbiep);
        asbiepCount++;

        return cloneAsbiep;
    }

    private AssociationBusinessInformationEntity copyASBIE(int userId, AssociationBusinessInformationEntity oasbieVO,
                                                           int abieid, int asbiepid, BusinessObjectDocument bod) {
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
        asbieVO.setBodId(bod.getBodId());

        asbieRepository.saveAndFlush(asbieVO);
        asbieCount++;

        return asbieVO;
    }

    private BasicBusinessInformationEntityProperty copyBBIEP(int userId,
                                                             BasicBusinessInformationEntityProperty obbiepVO,
                                                             BusinessObjectDocument bod) {
        BasicBusinessInformationEntityProperty nbbiepVO = new BasicBusinessInformationEntityProperty();
        nbbiepVO.setBasedBccpId(obbiepVO.getBasedBccpId());
        nbbiepVO.setDefinition(obbiepVO.getDefinition());
        nbbiepVO.setRemark(obbiepVO.getRemark());
        nbbiepVO.setBizTerm(obbiepVO.getBizTerm());
        nbbiepVO.setGuid(Utility.generateGUID());
        nbbiepVO.setCreatedBy(userId);
        nbbiepVO.setLastUpdatedBy(userId);
        nbbiepVO.setBodId(bod.getBodId());

        bbiepRepository.saveAndFlush(nbbiepVO);
        bbiepCount++;

        return nbbiepVO;
    }

    private BasicBusinessInformationEntity copyBBIE(int userId, BasicBusinessInformationEntity obbieVO,
                                                    int abie, int bbiep, BusinessObjectDocument bod) {
        BasicBusinessInformationEntity nbbieVO = new BasicBusinessInformationEntity();
        nbbieVO.setBasedBccId(obbieVO.getBasedBccId());
        int bdtPriRestriId = obbieVO.getBdtPriRestriId();
        if (bdtPriRestriId > 0) {
            nbbieVO.setBdtPriRestriId(bdtPriRestriId);
        }
        int codeListId = obbieVO.getCodeListId();
        if (codeListId > 0) {
            nbbieVO.setCodeListId(codeListId);
        }
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
        nbbieVO.setBodId(bod.getBodId());

        bbieRepository.saveAndFlush(nbbieVO);
        bbieCount++;

        return nbbieVO;
    }

    private void copyBBIESC(BasicBusinessInformationEntitySupplementaryComponent obbiescvo,
                            int bbie, TreeNode tNode, BusinessObjectDocument bod) {
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
        nbbiescVO.setBodId(bod.getBodId());

        bbiescRepository.saveAndFlush(nbbiescVO);
        bbiescCount++;

        DataTypeSupplementaryComponent dtscvo = dtScRepository.findOne(nbbiescVO.getDtScId());

        ABIEView av = applicationContext.getBean(ABIEView.class, dtscvo.getPropertyTerm(), nbbiescVO.getBbieScId(), "BBIESC");
        av.setBod(bodRepository.findOne(nbbiescVO.getBodId()));
        av.setBbiesc(nbbiescVO);
        String sc_name = "";
        if (dtscvo.getRepresentationTerm().equalsIgnoreCase("Text") ||
            dtscvo.getPropertyTerm().contains(dtscvo.getRepresentationTerm())) {
            sc_name = Utility.spaceSeparator(dtscvo.getPropertyTerm());
        } else {
            sc_name = Utility.spaceSeparator(dtscvo.getPropertyTerm()).concat(dtscvo.getRepresentationTerm());
        }
        av.setName(sc_name);

        av.setColor("orange");
        TreeNode tNode1 = new DefaultTreeNode(av, tNode);
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
        BusinessObjectDocument bod = bodRepository.findOne(selectedBod.getAbie().getBodId());

        ABIEView rootABIEView = applicationContext.getBean(ABIEView.class, selectedBod.getName(), selectedBod.getId(), "ROOT");
        rootABIEView.setBod(bod);
        rootABIEView.setAbie(selectedBod.getAbie());
        root = new DefaultTreeNode(rootABIEView, null);

        aABIEView = applicationContext.getBean(ABIEView.class, selectedBod.getName(), selectedBod.getId(), "ABIE");
        aABIEView.setBod(bod);
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
        av.setBod(bodRepository.findOne(bbiepVO.getBodId()));
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
        av.setBod(bodRepository.findOne(abieVO.getBodId()));
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
            av_01.setBod(bodRepository.findOne(bbiescVO.getBodId()));
            av_01.setBbiesc(bbiescVO);

            String sc_name = "";
            if (dtscVO.getRepresentationTerm().equalsIgnoreCase("Text") ||
                dtscVO.getPropertyTerm().contains(dtscVO.getRepresentationTerm())) {
                sc_name = Utility.spaceSeparator(dtscVO.getPropertyTerm());
            } else {
                sc_name = Utility.spaceSeparator(dtscVO.getPropertyTerm().concat(dtscVO.getRepresentationTerm()));
            }
            av_01.setName(sc_name);

            av_01.setColor("orange");
            TreeNode tNode1 = new DefaultTreeNode(av_01, parent);
        }
    }

    private HashSet<Integer> openedNodes = new HashSet<Integer>();

    public void expand(NodeSelectEvent event) {
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

    public void collapse(NodeCollapseEvent event) {
        TreeNode targetTreeNode = event.getTreeNode();
        if ("root".equals(targetTreeNode.getParent().getRowKey())) {
            return;
        }

        collapseNode(targetTreeNode);
        targetTreeNode.getChildren().clear();
    }

    private void collapseNode(TreeNode treeNode) {
        ABIEView abieView = (ABIEView) treeNode.getData();
        openedNodes.remove(abieView.getId());
        for (TreeNode child : treeNode.getChildren()) {
            collapseNode(child);
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

        for (BusinessContextHandler.BusinessContextValues bcv : bcH.getBcValues()) {
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
            bbieVO.setCodeListId(null);
        } else if (aABIEView.getRestrictionType().equalsIgnoreCase("Code")) {
            if (codeList != null) {
                bbieVO.setCodeListId(codeList.getCodeListId());
                bbieVO.setBdtPriRestriId(null);
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