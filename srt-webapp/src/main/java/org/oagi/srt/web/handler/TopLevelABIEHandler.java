package org.oagi.srt.web.handler;

import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.*;
import org.oagi.srt.web.jsf.beans.codelist.CodeListBean;
import org.oagi.srt.web.jsf.beans.context.business.BusinessContextHandler;
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
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.oagi.srt.repository.entity.AggregateBusinessInformationEntityState.Editing;
import static org.oagi.srt.repository.entity.BasicCoreComponentEntityType.Attribute;

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
    private BusinessInformationEntityService bieService;

    @Autowired
    private ExtensionService extensionService;

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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TopLevelAbieRepository topLevelAbieRepository;

    @Autowired
    private CoreComponentService coreComponentService;

    @Autowired
    private CoreComponentDAO ccDAO;

    @Autowired
    private BusinessInformationEntityDAO bieDAO;

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
    private List<TopLevelConcept> asccpVOs;

    private TopLevelConcept selected;
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

        asccpVOs = topLevelConceptRepository.findAll().stream()
                .sorted((a, b) -> a.getPropertyTerm().compareTo(b.getPropertyTerm()))
                .collect(Collectors.toList());
    }

    public BarChartModel getBarModel() {
        return barModel;
    }

    private int barCount = 20;

    public int getMax() {
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
            List<TopLevelAbie> topLevelAbieList = topLevelAbieRepository.findAll();
            for (TopLevelAbie topLevelAbie : topLevelAbieList) {
                AggregateBusinessInformationEntity abie = topLevelAbie.getAbie();
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
                        businessContextRepository.findOne(abie.getBizCtxId());
                abie.setBizCtxName(businessContext.getName());

                ABIEView aABIEView = applicationContext.getBean(ABIEView.class,
                        asccp.getPropertyTerm(),
                        abie.getAbieId(), "ABIE");
                aABIEView.setTopLevelAbie(topLevelAbie);
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
        asccpVOs = topLevelConceptRepository.findByPropertyTermContaining(getPropertyTerm());
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
            //createNounBIEs(bCSelected);
            //createVerbBIEs(bCSelected);

            createBarModel();
        }

        return event.getNewStep();
    }

    public void createVerbBIEs(BusinessContext businessContext) {
        ArrayList<String> verbList = new ArrayList<>();
        verbList.addAll(Arrays.asList(
                "Acknowledge",
                "Cancel Acknowledge",
                "Cancel",
                "Change Acknowledge",
                "Change",
                "Get",
                "Load",
                "Notify",
                "Post Acknowledge",
                "Post",
                "Process",
                "Show",
                "Sync",
                "Sync Response"
        ));

        BusinessInformationEntityService.CreateBIEsResult createBIEsResult = null;
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
        ExecutorCompletionService<BusinessInformationEntityService.CreateBIEsResult> completionService
                = new ExecutorCompletionService(executorService);
        try {
            for (int i = 0; i < verbList.size(); i++) {
                List<AssociationCoreComponentProperty> asccpList = asccpRepository.findByPropertyTermContaining(verbList.get(i));
                AssociationCoreComponentProperty asccp = asccpList.get(0);
                System.out.print("Start Creating BIEs from Verb " + verbList.get(i));
                completionService.submit(() -> bieService.createBIEs(asccp, businessContext));
            }

            try {
                for (int i = 0; i < verbList.size(); i++) {
                    createBIEsResult = completionService.take().get();
                }
            } catch (InterruptedException e) {
                logger.error("", e);
            } catch (ExecutionException e) {
                logger.error("", e.getCause());
            }
        } finally {
            executorService.shutdown();
        }
        System.out.println("... Done");

        topAbieVO = createBIEsResult.getTopLevelAbie().getAbie();
        long abieId = topAbieVO.getAbieId();

        ABIEView rootABIEView = applicationContext.getBean(ABIEView.class, selected.getPropertyTerm(), abieId, "ABIE");
        rootABIEView.setTopLevelAbie(createBIEsResult.getTopLevelAbie());
        rootABIEView.setAbie(topAbieVO);
        root = new DefaultTreeNode(rootABIEView, null);

        aABIEView = applicationContext.getBean(ABIEView.class, selected.getPropertyTerm(), abieId, "ABIE");
        aABIEView.setTopLevelAbie(createBIEsResult.getTopLevelAbie());
        aABIEView.setAbie(topAbieVO);
        aABIEView.setColor("blue");
        aABIEView.setAcc(createBIEsResult.getAcc());
        aABIEView.setAsbiep(asbiepVO);

        TreeNode toplevelNode = new DefaultTreeNode(aABIEView, root);
        createBIEChildren(abieId, toplevelNode);
        System.out.println("... Done");
    }

    @Transactional(rollbackFor = Throwable.class)
    public void createBIEs() {
        AssociationCoreComponentProperty asccp = asccpRepository.findOne(selected.getAsccpId());
        BusinessInformationEntityService.CreateBIEsResult createBIEsResult = bieService.createBIEs(asccp, bCSelected);

        abieCount = createBIEsResult.getAbieCount();
        bbiescCount = createBIEsResult.getBbiescCount();
        asbiepCount = createBIEsResult.getAsbiepCount();
        asbieCount = createBIEsResult.getAsbieCount();
        bbiepCount = createBIEsResult.getBbiepCount();
        bbieCount = createBIEsResult.getBbieCount();

        topAbieVO = createBIEsResult.getTopLevelAbie().getAbie();
        long abieId = topAbieVO.getAbieId();

        ABIEView rootABIEView = applicationContext.getBean(ABIEView.class, selected.getPropertyTerm(), abieId, "ABIE");
        rootABIEView.setTopLevelAbie(createBIEsResult.getTopLevelAbie());
        rootABIEView.setAbie(topAbieVO);
        root = new DefaultTreeNode(rootABIEView, null);

        aABIEView = applicationContext.getBean(ABIEView.class, selected.getPropertyTerm(), abieId, "ABIE");
        aABIEView.setTopLevelAbie(createBIEsResult.getTopLevelAbie());
        aABIEView.setAbie(topAbieVO);
        aABIEView.setColor("blue");
        aABIEView.setAcc(createBIEsResult.getAcc());
        aABIEView.setAsbiep(asbiepVO);

        TreeNode toplevelNode = new DefaultTreeNode(aABIEView, root);
        createBIEChildren(abieId, toplevelNode);
    }

    public String onFlowProcess_Copy(FlowEvent event) {

        if (event.getNewStep().equals(SRTConstants.TAB_TOP_LEVEL_ABIE_SELECT_BC)) {

        } else if (event.getNewStep().equals(SRTConstants.TAB_TOP_LEVEL_ABIE_COPY_UC_BIE)) {
            // TODO if go back from the confirmation page? avoid that situation

            AggregateBusinessInformationEntity selectedAbie =
                    selectedBod.getAbie();
            TopLevelAbie selectedTopLevelAbie = topLevelAbieRepository.findByAbieId(selectedAbie.getAbieId());
            AggregateCoreComponent aggregateCoreComponent =
                    accRepository.findOne(selectedAbie.getBasedAccId());

            List<AssociationCoreComponentProperty> asccpList = asccpRepository.findByRoleOfAccId(aggregateCoreComponent.getAccId());
            if (asccpList.size() != 1) {
                throw new IllegalStateException();
            }
            AssociationCoreComponentProperty asccp = asccpList.get(0);

            int userId = userRepository.findAppUserIdByLoginId("oagis");
            TopLevelAbie copiedBod = copyTopLevelAbie(selectedTopLevelAbie);
            topAbieVO = copyABIE(userId, selectedAbie, copiedBod);
            updateTopLevelAbie(copiedBod, topAbieVO);
            long abieId = topAbieVO.getAbieId();

            ABIEView rootABIEView = applicationContext.getBean(ABIEView.class, asccp.getPropertyTerm(), abieId, "ABIE");
            TopLevelAbie topLevelAbie = topLevelAbieRepository.findOne(topAbieVO.getOwnerTopLevelAbieId());
            rootABIEView.setTopLevelAbie(topLevelAbie);
            rootABIEView.setAbie(topAbieVO);
            root = new DefaultTreeNode(rootABIEView, null);
            AssociationBusinessInformationEntityProperty asbiep =
                    copyASBIEP(userId, asbiepRepository.
                            findOneByRoleOfAbieId(selectedAbie.getAbieId()), abieId, copiedBod);
            aABIEView = applicationContext.getBean(ABIEView.class, asccp.getPropertyTerm(), abieId, "ABIE");
            aABIEView.setTopLevelAbie(topLevelAbie);
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

    class ValueComparator implements Comparator<BusinessInformationEntity> {

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

    private List<AssociationBusinessInformationEntity> getASBIEsFromABIE(long abieId) {
        return asbieRepository.findByFromAbieId(abieId);
    }

    private List<BasicBusinessInformationEntity> getBBIEsFromABIE(long abieId) {
        return bbieRepository.findByFromAbieId(abieId);
    }

    private List<BasicBusinessInformationEntitySupplementaryComponent> getBBIESCsFromBBIE(long bbieid) {
        return bbiescRepository.findByBbieId(bbieid);
    }

    private TopLevelAbie copyTopLevelAbie(TopLevelAbie source) {
        TopLevelAbie topLevelAbie = new TopLevelAbie();
        return topLevelAbieRepository.saveAndFlush(topLevelAbie);
    }

    private void updateTopLevelAbie(TopLevelAbie topLevelAbie, AggregateBusinessInformationEntity abie) {
        topLevelAbie.setTopLevelAbieId(abie.getAbieId());
        topLevelAbieRepository.save(topLevelAbie);
    }

    private void copyBIEs(
            int userId,
            AggregateBusinessInformationEntity oabieVO,
            AggregateBusinessInformationEntity nabieVO,
            AssociationBusinessInformationEntityProperty nasbiepVO, int groupPosition, TreeNode tNode,
            TopLevelAbie topLevelAbie) {
        List<BasicBusinessInformationEntity> basicBusinessInformationEntities = getBBIEsFromABIE(oabieVO.getAbieId());

        for (BasicBusinessInformationEntity basicBusinessInformationEntity : basicBusinessInformationEntities) {
            BasicBusinessInformationEntityProperty oBBIEPVO =
                    bbiepRepository.findOne(basicBusinessInformationEntity.getToBbiepId());
            BasicBusinessInformationEntityProperty nBBIEPVO = copyBBIEP(userId, oBBIEPVO, topLevelAbie);
            BasicBusinessInformationEntity nBasicBusinessInformationEntity =
                    copyBBIE(userId, basicBusinessInformationEntity, nabieVO.getAbieId(), nBBIEPVO.getBbiepId(), topLevelAbie);

            BasicCoreComponentProperty bccpVO = bccpRepository.findOne(nBBIEPVO.getBasedBccpId());

            ABIEView av = applicationContext.getBean(ABIEView.class, bccpVO.getPropertyTerm(),
                    nBasicBusinessInformationEntity.getBbieId(), "BBIE");
            av.setTopLevelAbie(topLevelAbieRepository.findOne(nBBIEPVO.getOwnerTopLevelAbieId()));
            av.setBbiep(nBBIEPVO);
            av.setBasicCoreComponentPropertyAndBasicBusinessInformationEntity(bccpVO, nBasicBusinessInformationEntity);

            DataType dtVO = dataTypeRepository.findOne(bccpVO.getBdtId());
            av.setBdtName(dtVO.getDen());
            av.setColor("green");
            TreeNode tNode2 = new DefaultTreeNode(av, tNode);
            List<BasicBusinessInformationEntitySupplementaryComponent> bbiesc =
                    getBBIESCsFromBBIE(basicBusinessInformationEntity.getBbieId());
            for (BasicBusinessInformationEntitySupplementaryComponent oBBIESCVO : bbiesc) {
                copyBBIESC(oBBIESCVO, nBasicBusinessInformationEntity.getBbieId(), tNode2, topLevelAbie);
            }
        }

        List<AssociationBusinessInformationEntity> asbie = getASBIEsFromABIE(oabieVO.getAbieId());
        for (AssociationBusinessInformationEntity oASBIEVO : asbie) {
            AssociationBusinessInformationEntityProperty o_next_asbiepVO =
                    asbiepRepository.findOne(oASBIEVO.getToAsbiepId());
            AggregateBusinessInformationEntity o_next_abieVO =
                    abieRepository.findOne(o_next_asbiepVO.getRoleOfAbieId());
            AggregateBusinessInformationEntity n_next_abieVO = copyABIE(userId, o_next_abieVO, topLevelAbie);
            AssociationBusinessInformationEntityProperty n_next_asbiepVO =
                    copyASBIEP(userId, o_next_asbiepVO, n_next_abieVO.getAbieId(), topLevelAbie);
            AssociationBusinessInformationEntity nASBIEVO =
                    copyASBIE(userId, oASBIEVO, nabieVO.getAbieId(), n_next_asbiepVO.getAsbiepId(), topLevelAbie);

            AssociationCoreComponentProperty asccpVO =
                    asccpRepository.findOne(n_next_asbiepVO.getBasedAsccpId());

            ABIEView av = applicationContext.getBean(ABIEView.class, asccpVO.getPropertyTerm(), nabieVO.getAbieId(), "ASBIE");
            av.setColor("blue");
            av.setTopLevelAbie(topLevelAbieRepository.findOne(n_next_abieVO.getOwnerTopLevelAbieId()));
            av.setAbie(n_next_abieVO);
            av.setAsbie(nASBIEVO);
            av.setAsbiep(n_next_asbiepVO);
            TreeNode tNode2 = new DefaultTreeNode(av, tNode);
            copyBIEs(userId, o_next_abieVO, n_next_abieVO, n_next_asbiepVO, -1, tNode2, topLevelAbie);
        }
    }

    private AggregateBusinessInformationEntity copyABIE(int userId,
                                                        AggregateBusinessInformationEntity sourceAbie,
                                                        TopLevelAbie topLevelAbie) {
        AggregateBusinessInformationEntity cloneAbie = new AggregateBusinessInformationEntity();
        String abieGuid = Utility.generateGUID();
        cloneAbie.setGuid(abieGuid);
        cloneAbie.setBasedAccId(sourceAbie.getBasedAccId());
        cloneAbie.setBizCtxId(sourceAbie.getBizCtxId());
        cloneAbie.setDefinition(sourceAbie.getDefinition());
        cloneAbie.setClientId(sourceAbie.getClientId());
        cloneAbie.setVersion(sourceAbie.getVersion());
        cloneAbie.setStatus(sourceAbie.getStatus());
        cloneAbie.setRemark(sourceAbie.getRemark());
        cloneAbie.setBizTerm(sourceAbie.getBizTerm());
        cloneAbie.setCreatedBy(userId);
        cloneAbie.setLastUpdatedBy(userId);
        cloneAbie.setState(Editing);
        cloneAbie.setOwnerTopLevelAbieId(topLevelAbie.getTopLevelAbieId());

        bieDAO.save(cloneAbie);
        abieCount++;

        return cloneAbie;
    }

    private AssociationBusinessInformationEntityProperty copyASBIEP(long userId,
                                                                    AssociationBusinessInformationEntityProperty sourceAsbiep,
                                                                    long abieId, TopLevelAbie topLevelAbie) {
        AssociationBusinessInformationEntityProperty cloneAsbiep =
                new AssociationBusinessInformationEntityProperty();
        cloneAsbiep.setGuid(Utility.generateGUID());
        cloneAsbiep.setBasedAsccpId(sourceAsbiep.getBasedAsccpId());
        cloneAsbiep.setRoleOfAbieId(abieId);
        cloneAsbiep.setCreatedBy(userId);
        cloneAsbiep.setLastUpdatedBy(userId);
        cloneAsbiep.setDefinition(sourceAsbiep.getDefinition());
        cloneAsbiep.setOwnerTopLevelAbieId(topLevelAbie.getTopLevelAbieId());

        bieDAO.save(cloneAsbiep);
        asbiepCount++;

        return cloneAsbiep;
    }

    private AssociationBusinessInformationEntity copyASBIE(long userId, AssociationBusinessInformationEntity oasbieVO,
                                                           long abieid, long asbiepid, TopLevelAbie topLevelAbie) {
        AssociationBusinessInformationEntity asbieVO = new AssociationBusinessInformationEntity();
        asbieVO.setGuid(Utility.generateGUID());
        asbieVO.setFromAbieId(abieid);
        asbieVO.setToAsbiepId(asbiepid);
        asbieVO.setBasedAsccId(oasbieVO.getBasedAsccId());
        asbieVO.setCardinalityMax(oasbieVO.getCardinalityMax());
        asbieVO.setCardinalityMin(oasbieVO.getCardinalityMin());
        asbieVO.setDefinition(oasbieVO.getDefinition());
        asbieVO.setCreatedBy(userId);
        asbieVO.setLastUpdatedBy(userId);
        asbieVO.setSeqKey(oasbieVO.getSeqKey());
        asbieVO.setOwnerTopLevelAbieId(topLevelAbie.getTopLevelAbieId());

        bieDAO.save(asbieVO);
        asbieCount++;

        return asbieVO;
    }

    private BasicBusinessInformationEntityProperty copyBBIEP(int userId,
                                                             BasicBusinessInformationEntityProperty obbiepVO,
                                                             TopLevelAbie topLevelAbie) {
        BasicBusinessInformationEntityProperty nbbiepVO = new BasicBusinessInformationEntityProperty();
        nbbiepVO.setBasedBccpId(obbiepVO.getBasedBccpId());
        nbbiepVO.setDefinition(obbiepVO.getDefinition());
        nbbiepVO.setRemark(obbiepVO.getRemark());
        nbbiepVO.setBizTerm(obbiepVO.getBizTerm());
        nbbiepVO.setGuid(Utility.generateGUID());
        nbbiepVO.setCreatedBy(userId);
        nbbiepVO.setLastUpdatedBy(userId);
        nbbiepVO.setOwnerTopLevelAbieId(topLevelAbie.getTopLevelAbieId());

        bieDAO.save(nbbiepVO);
        bbiepCount++;

        return nbbiepVO;
    }

    private BasicBusinessInformationEntity copyBBIE(long userId, BasicBusinessInformationEntity obbieVO,
                                                    long abie, long bbiep, TopLevelAbie topLevelAbie) {
        BasicBusinessInformationEntity nbbieVO = new BasicBusinessInformationEntity();
        nbbieVO.setBasedBccId(obbieVO.getBasedBccId());
        long bdtPriRestriId = obbieVO.getBdtPriRestriId();
        if (bdtPriRestriId > 0L) {
            nbbieVO.setBdtPriRestriId(bdtPriRestriId);
        }
        long codeListId = obbieVO.getCodeListId();
        if (codeListId > 0L) {
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
        nbbieVO.setOwnerTopLevelAbieId(topLevelAbie.getTopLevelAbieId());

        bieDAO.save(nbbieVO);
        bbieCount++;

        return nbbieVO;
    }

    private void copyBBIESC(BasicBusinessInformationEntitySupplementaryComponent obbiescvo,
                            long bbie, TreeNode tNode, TopLevelAbie topLevelAbie) {
        BasicBusinessInformationEntitySupplementaryComponent nbbiescVO = new BasicBusinessInformationEntitySupplementaryComponent();
        nbbiescVO.setGuid(Utility.generateGUID());
        nbbiescVO.setDtScId(obbiescvo.getDtScId());
        nbbiescVO.setDtScPriRestriId(obbiescvo.getDtScPriRestriId());
        nbbiescVO.setCodeListId(obbiescvo.getCodeListId());
        nbbiescVO.setAgencyIdListId(obbiescvo.getAgencyIdListId());
        nbbiescVO.setCardinalityMax(obbiescvo.getCardinalityMax());
        nbbiescVO.setCardinalityMin(obbiescvo.getCardinalityMin());
        nbbiescVO.setDefaultValue(obbiescvo.getDefaultValue());
        nbbiescVO.setFixedValue(obbiescvo.getFixedValue());
        nbbiescVO.setDefinition(obbiescvo.getDefinition());
        nbbiescVO.setRemark(obbiescvo.getRemark());
        nbbiescVO.setBizTerm(obbiescvo.getBizTerm());
        nbbiescVO.setBbieId(bbie);
        nbbiescVO.setOwnerTopLevelAbieId(topLevelAbie.getTopLevelAbieId());

        bieDAO.save(nbbiescVO);
        bbiescCount++;

        DataTypeSupplementaryComponent dtscvo = dtScRepository.findOne(nbbiescVO.getDtScId());

        ABIEView av = applicationContext.getBean(ABIEView.class, dtscvo.getPropertyTerm(), nbbiescVO.getBbieScId(), "BBIESC");
        av.setTopLevelAbie(topLevelAbieRepository.findOne(nbbiescVO.getOwnerTopLevelAbieId()));
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

    private void extendTopLevelABIE() {
        AggregateCoreComponent ueAcc = new AggregateCoreComponent(); // need to assign
        User curUser = new User();// need to assign

        if (ueAcc == null) {
            createNewUserExtensionGroupACC();
        } else {
            if (ueAcc.getState() == CoreComponentState.Editing && ueAcc.getOwnerUserId() == curUser.getAppUserId()) {//case #a in designDoc
                editUserExtensionGroupACC();
            } else if (ueAcc.getState() == CoreComponentState.Editing && ueAcc.getOwnerUserId() != curUser.getAppUserId()) {//case #b in designDoc
                //Show message Owner User Id
                //Return to page
            } else if (ueAcc.getState() == CoreComponentState.Candidate && ueAcc.getOwnerUserId() == curUser.getAppUserId()) {//case #c in designDoc
                //do you want to edit this extension?
                if (true) {
                    //Change state of this ACC and correspondings
                    editUserExtensionGroupACC();
                } else {
                    //Return to page
                }

            } else if (ueAcc.getState() == CoreComponentState.Candidate && ueAcc.getOwnerUserId() != curUser.getAppUserId()) {//case #d in designDoc

            } else if (ueAcc.getState() == CoreComponentState.Published) {//case #e in designDoc
                //do you want to edit already published one?
                if (true) {
                    createNewUserExtensionGroupACCRevision();
                } else {
                    //Return to page
                }
            }
        }

    }

    private void createNewUserExtensionGroupACCRevision() {
        AggregateCoreComponent ueAcc = new AggregateCoreComponent(); //need to assign

        List<AssociationCoreComponent> asccList = asccRepository.findByFromAccId(ueAcc.getAccId());
        List<BasicCoreComponent> bccList = bccRepository.findByFromAccId(ueAcc.getAccId());

        for (AssociationCoreComponent ascc : asccList) {
            ascc.setState(CoreComponentState.Editing);
            AssociationCoreComponentProperty asccp = asccpRepository.findOne(ascc.getToAsccpId());
            asccp.setState(CoreComponentState.Editing);//Editing
            ccDAO.save(ascc);
            ccDAO.save(asccp);
        }
    }

    private void createNewUserExtensionGroupACC() {
        //UI implementation is needed.
        //Delete an existing association
        //Make edit to details of existing associations
        //The user should be able to make a new revision or modification
        //Add an association, Create a new ACC, create a new ASCCP, create a new BCCP

        if (true) {
            AggregateCoreComponent eAcc = new AggregateCoreComponent(); //need to assign
            User currentLoginUser = new User(); //need to assign

            extensionService.createNewUserExtensionGroupACC(eAcc, currentLoginUser);
        }
    }

    private void editUserExtensionGroupACC() {
        //UI implementation is needed.
        //Delete an existing association
        //Make edit to details of existing associations
        //The user should be able to make a new revision or modification
        //Add an association, Create a new ACC, create a new ASCCP, create a new BCCP


    }

    public List<TopLevelConcept> getAsccpVOs() {
        return asccpVOs;
    }

    public List<String> completeInput(String query) {
        String q = (query != null) ? query.trim() : null;
        List<TopLevelConcept> topLevelConcepts = topLevelConceptRepository.findAll();

        if (StringUtils.isEmpty(q)) {
            return topLevelConcepts.stream()
                    .map(e -> e.getPropertyTerm())
                    .collect(Collectors.toList());
        } else {
            String[] split = q.split(" ");

            return topLevelConcepts.stream()
                    .map(e -> e.getPropertyTerm())
                    .distinct()
                    .filter(e -> {
                        e = e.toLowerCase();
                        for (String s : split) {
                            if (!e.contains(s.toLowerCase())) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
        }
    }

    public TopLevelConcept getSelected() {
        return selected;
    }

    public void setSelected(TopLevelConcept selected) {
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
        selected = topLevelConceptRepository.findOne(asccp.getAsccpId());
    }

    public void onBODRowSelect(SelectEvent event) {
        FacesMessage msg = new FacesMessage(((ABIEView) event.getObject()).getName(), String.valueOf(((ABIEView) event.getObject()).getName()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
        selectedBod = (ABIEView) event.getObject();
        //root = new DefaultTreeNode(selectedBod, null);

        logger.debug("#### " + selectedBod.getName());
        TopLevelAbie topLevelAbie = topLevelAbieRepository.findOne(selectedBod.getAbie().getOwnerTopLevelAbieId());

        ABIEView rootABIEView = applicationContext.getBean(ABIEView.class, selectedBod.getName(), selectedBod.getId(), "ROOT");
        rootABIEView.setTopLevelAbie(topLevelAbie);
        rootABIEView.setAbie(selectedBod.getAbie());
        root = new DefaultTreeNode(rootABIEView, null);

        aABIEView = applicationContext.getBean(ABIEView.class, selectedBod.getName(), selectedBod.getId(), "ABIE");
        aABIEView.setTopLevelAbie(topLevelAbie);
        aABIEView.setAbie(selectedBod.getAbie());
        aABIEView.setColor("blue");
        TreeNode toplevelNode = new DefaultTreeNode(aABIEView, root);

        createBIEChildren(selectedBod.getId(), toplevelNode);
    }

    private void createBIEChildren(long abieId, TreeNode tNode) {
        List<BasicBusinessInformationEntity> list_01 = bbieRepository.findByFromAbieId(abieId);
        List<AssociationBusinessInformationEntity> list_02 = asbieRepository.findByFromAbieId(abieId);

        Map<BusinessInformationEntity, Double> sequence = new HashMap();
        ValueComparator bvc = new ValueComparator(sequence);
        TreeMap<BusinessInformationEntity, Double> ordered_sequence = new TreeMap(bvc);

        for (BasicBusinessInformationEntity bbieVO : list_01) {
            double sk = bbieVO.getSeqKey();
            if (getEntityType(bbieVO.getBasedBccId()) == Attribute)
                showBBIETree(bbieVO, tNode);
            else
                sequence.put(bbieVO, sk);
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
        av.setTopLevelAbie(topLevelAbieRepository.findOne(bbiepVO.getOwnerTopLevelAbieId()));
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
        AssociationCoreComponent asccVO = asccRepository.findOne(asbieVO.getBasedAsccId());
        AggregateBusinessInformationEntity abieVO = abieRepository.findOne(asbiepVO.getRoleOfAbieId());
        AggregateCoreComponent accVO = accRepository.findOne(abieVO.getBasedAccId());

        ABIEView av = applicationContext.getBean(ABIEView.class, asccpVO.getPropertyTerm(), abieVO.getAbieId(), "ASBIE");
        av.setColor("blue");
        av.setAscc(asccVO);
        av.setAsccp(asccpVO);
        av.setAcc(accVO);
        av.setTopLevelAbie(topLevelAbieRepository.findOne(abieVO.getOwnerTopLevelAbieId()));
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
            av_01.setTopLevelAbie(topLevelAbieRepository.findOne(bbiescVO.getOwnerTopLevelAbieId()));
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

    private HashSet<Long> openedNodes = new HashSet();

    public void expand(NodeSelectEvent event) {
        TreeNode selectedTreeNode = getSelectedTreeNode();
        ABIEView abieView = (ABIEView) selectedTreeNode.getData();
        if (!openedNodes.contains(abieView.getId())) {
            openedNodes.add(abieView.getId());
            if ("ASBIE".equalsIgnoreCase(abieView.getType()))
                createBIEChildren(abieView.getAbie().getAbieId(), selectedTreeNode);
            else if ("BBIE".equalsIgnoreCase(abieView.getType()))
                createBBIESCChild(abieView.getBbie(), selectedTreeNode);
        }

        showDetails();
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
//			context.redirect(context.getRequestContextPath() + "/top_level_abie_edit.jsf");
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
                bcvVO.setBusinessContext(bcVO);
                bcvVO.setContextSchemeValue(cVO);
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
            bbieVO.setBdtPriRestri(bdtPriRestriRepository.findOne(aABIEView.getBdtPrimitiveRestrictionId()));
            bbieVO.setCodeList(null);
        } else if (aABIEView.getRestrictionType().equalsIgnoreCase("Code")) {
            if (codeList != null) {
                bbieVO.setCodeList(codeList);
                bbieVO.setBdtPriRestri(null);
            }
        }

        bieDAO.save(bbieVO);
        bieDAO.save(bbiepVO);
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
        CodeListBean ch = (CodeListBean) event.getObject();
        //codeList = ch.getSelected();
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

    public BasicCoreComponentEntityType getEntityType(long bccId) {
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
        String q = (query != null) ? query.trim() : null;
        List<CodeList> codeLists = codeListRepository.findAll();

        if (StringUtils.isEmpty(q)) {
            return codeLists.stream()
                    .map(e -> e.getName())
                    .collect(Collectors.toList());
        } else {
            String[] split = q.split(" ");

            return codeLists.stream()
                    .map(e -> e.getName())
                    .distinct()
                    .filter(e -> {
                        e = e.toLowerCase();
                        for (String s : split) {
                            if (!e.contains(s.toLowerCase())) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
        }
    }

    private void saveBBIESCChanges(ABIEView aABIEView) {
        BasicBusinessInformationEntitySupplementaryComponent bbiescVO = aABIEView.getBbiesc();
        bieDAO.save(bbiescVO);
    }

    public void closeDialog() {
        RequestContext.getCurrentInstance().closeDialog(this);
    }

    private void saveASBIEChanges(ABIEView aABIEView) {
        AssociationBusinessInformationEntity asbieVO = aABIEView.getAsbie();
        AggregateBusinessInformationEntity abieVO = aABIEView.getAbie();
        AssociationBusinessInformationEntityProperty asbiepVO = aABIEView.getAsbiep();

        bieDAO.save(asbieVO);
        bieDAO.save(asbiepVO);
        bieDAO.save(abieVO);
    }

    private void saveABIEChanges(ABIEView aABIEView) {
        AggregateBusinessInformationEntity abieVO = aABIEView.getAbie();
        bieDAO.save(abieVO);
    }

    public void createABIEExtensionLocally() {
        User currentLoginUser = getCurrentLoginUser();
        extensionService.createNewUserExtensionGroupACC(aABIEView.getAcc(), currentLoginUser);
    }

    public void createABIEExtensionGlobally() {
        User currentLoginUser = getCurrentLoginUser();
        extensionService.createNewUserExtensionGroupACC(aABIEView.getAcc(), currentLoginUser);
    }

    private User getCurrentLoginUser() {
        return userRepository.findOneByLoginId("oagis");
    }

}