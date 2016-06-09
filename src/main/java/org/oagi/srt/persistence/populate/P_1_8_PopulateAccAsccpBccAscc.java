package org.oagi.srt.persistence.populate;

import com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;
import org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl;
import org.apache.xerces.impl.xs.XSAttributeDecl;
import org.apache.xerces.impl.xs.XSAttributeUseImpl;
import org.apache.xerces.impl.xs.XSComplexTypeDecl;
import org.apache.xerces.impl.xs.XSElementDecl;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.oagi.srt.Application;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.BODElementVO;
import org.oagi.srt.common.util.BODSchemaHandler;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Yunsu Lee
 * @version 1.0
 */
@Component
public class P_1_8_PopulateAccAsccpBccAscc {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DataTypeRepository dataTypeRepository;

    @Autowired
    private BasicCoreComponentPropertyRepository bccpRepository;

    @Autowired
    private AggregateCoreComponentRepository accRepository;

    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;

    @Autowired
    private BasicCoreComponentRepository bccRepository;

    @Autowired
    private AssociationCoreComponentRepository asccRepository;

    @Autowired
    private ReleaseRepository releaseRepository;

    @Autowired
    private NamespaceRepository namespaceRepository;

    private int userId;
    private int releaseId;
    private int namespaceId;

    private BODSchemaHandler bodSchemaHandler;
    private String bodPath;

    private File f1 = new File(SRTConstants.BOD_FILE_PATH_01);
    private File f2 = new File(SRTConstants.BOD_FILE_PATH_02);

    @PostConstruct
    public void init() throws Exception {
        userId = userRepository.findAppUserIdByLoginId("oagis");
        releaseId = releaseRepository.findReleaseIdByReleaseNum("10.1");
        namespaceId = namespaceRepository.findNamespaceIdByUri("http://www.openapplications.org/oagis/10");
    }

    private void populate() throws Exception {
        populate1();
        populate2();
    }

    private void populate1() throws Exception {
        List<File> files = new ArrayList();
        for (File f : getBODs(f1)) {
            files.add(f);
        }
        for (File f : getBODs(f2)) {
            files.add(f);
        }

        files.stream()
                .filter(file -> file.getName().equals("AcknowledgeInvoice.xsd"))
                .forEach(file -> {
                    System.out.println(file.getName() + " ing...");
                    try {
                        insertASCCP(file);
                    } catch (Exception e) {
                        throw new IllegalStateException("Error occurs during " + file + " processing.", e);
                    }
                });
    }

    private void populate2() throws Exception {
        List<File> files = new ArrayList();
        for (File f : getBODs(f1)) {
            files.add(f);
        }
        for (File f : getBODs(f2)) {
            files.add(f);
        }

        files.stream()
                .filter(file -> !file.getName().equals("AcknowledgeInvoice.xsd") && !file.getName().endsWith("IST.xsd"))
                .forEach(file -> {
                    System.out.println(file.getName() + " ing...");
                    try {
                        insertASCCP(file);
                    } catch (Exception e) {
                        throw new IllegalStateException("Error occurs during " + file + " processing.", e);
                    }
                });

        modifySequenceKeyforGroup();
        modifySequenceKeyforGroup_temp();
    }

    private void insertASCCP(File file) throws Exception {
        bodPath = file.getAbsolutePath();
        bodSchemaHandler = new BODSchemaHandler(bodPath);
        XSElementDecl element = bodSchemaHandler.getGlobalElementDeclaration();
        XSComplexTypeDecl complexType = bodSchemaHandler.getComplexTypeDefinition(element);
        if (bodSchemaHandler.isComplexWithoutSimpleContent(complexType.getTypeName())) {
            insertASCCP(element, complexType);
        }
    }

    private AssociationCoreComponentProperty insertASCCP(XSElementDecl element, XSComplexTypeDecl complexType) throws Exception {
        String name = element.getName();

        String asccpGuid = element.getFId();
        String propertyTerm = Utility.spaceSeparator(name);
        String definition = bodSchemaHandler.getAnnotation(element);

        int roleOfAccId;
        AggregateCoreComponent accVO = accRepository.findAccIdAndDenByGuid(complexType.getFId());
        if (accVO == null) {
            BCCWithAttrHolder bccWithAttrHolder = new BCCWithAttrHolder();
            InsertACCResult insertACCResult = insertACC(bccWithAttrHolder, complexType, bodPath);
            bccWithAttrHolder.save();
            accVO = insertACCResult.getAggregateCoreComponent();
        }
        roleOfAccId = accVO.getAccId();

        String den = propertyTerm + ". " + Utility.spaceSeparator(Utility.first(accVO.getDen()));
        int state = 3;
        String module = Utility.extractModuleName(bodPath);

        AssociationCoreComponentProperty asccp = new AssociationCoreComponentProperty();
        asccp.setGuid(asccpGuid);
        asccp.setPropertyTerm(propertyTerm);
        asccp.setDefinition(definition);
        asccp.setRoleOfAccId(roleOfAccId);
        asccp.setDen(den);
        asccp.setState(state);
        asccp.setModule(module);
        asccp.setCreatedBy(userId);
        asccp.setLastUpdatedBy(userId);
        asccp.setOwnerUserId(userId);
        asccp.setDeprecated(false);
        asccp.setNamespaceId(namespaceId);
        asccp.setReleaseId(releaseId);
        asccpRepository.saveAndFlush(asccp);

        return asccp;
    }

    private AssociationCoreComponentProperty insertASCCPUnderGroup(BODElementVO bodVO) throws Exception {
        String name = bodVO.getElement().getName();

        String asccpGuid = bodVO.getElement().getFId();
        String propertyTerm = Utility.spaceSeparator(name);
        String definition = bodSchemaHandler.getAnnotation(bodVO.getElement());

        AggregateCoreComponent accVO = accRepository.findAccIdAndDenByGuid(
                bodSchemaHandler.getComplexTypeDefinition(bodVO.getTypeName()).getFId());
        int roleOfAccId = accVO.getAccId();

        String den = propertyTerm + ". " + Utility.first(accVO.getDen());
        int state = 3;
        String module = Utility.extractModuleName(bodPath);

        AssociationCoreComponentProperty asccp = new AssociationCoreComponentProperty();
        asccp.setGuid(asccpGuid);
        asccp.setPropertyTerm(propertyTerm);
        asccp.setDefinition(definition);
        asccp.setRoleOfAccId(roleOfAccId);
        asccp.setDen(den);
        asccp.setState(state);
        asccp.setModule(module);
        asccp.setCreatedBy(userId);
        asccp.setLastUpdatedBy(userId);
        asccp.setOwnerUserId(userId);
        asccp.setDeprecated(false);
        asccp.setNamespaceId(namespaceId);
        asccp.setReleaseId(releaseId);
        asccpRepository.saveAndFlush(asccp);

        return asccp;
    }

    private boolean insertASCC(BODElementVO bodVO, String parentGuid, AssociationCoreComponentProperty asccpVO) throws Exception {
        AggregateCoreComponent accVO = accRepository.findAccIdAndDenByGuid(parentGuid);
        int assocFromACCId = accVO.getAccId();

        String asccGuid = (bodVO.getRef() != null) ? bodVO.getRef() : bodVO.getId();
        int assocToASCCPId = asccpVO.getAsccpId();

        if (!asccRepository.existsByGuidAndFromAccIdAndToAsccpId(asccGuid, assocFromACCId, assocToASCCPId)) {
            int cardinalityMin = bodVO.getMinOccur();
            int cardinalityMax = bodVO.getMaxOccur();

            int sequenceKey = bodVO.getOrder();
            String den = Utility.first(accVO.getDen()) + ". " + asccpVO.getDen();

            String definition = bodSchemaHandler.getAnnotation(bodVO.getElement());

            AssociationCoreComponent ascc = new AssociationCoreComponent();
            ascc.setGuid(asccGuid);
            ascc.setCardinalityMin(cardinalityMin);
            ascc.setCardinalityMax(cardinalityMax);
            ascc.setSeqKey(sequenceKey);
            ascc.setFromAccId(assocFromACCId);
            ascc.setToAsccpId(assocToASCCPId);
            ascc.setDen(den);
            ascc.setDefinition(definition);
            ascc.setState(3);
            ascc.setDeprecated(false);
            ascc.setReleaseId(releaseId);
            ascc.setCreatedBy(userId);
            ascc.setLastUpdatedBy(userId);
            ascc.setOwnerUserId(userId);
            asccRepository.save(ascc);

            return true;
        }
        return false;
    }

    private void modifySequenceKeyforGroup() throws Exception {
        //modify sequenceKey for group
        List<AssociationCoreComponent> groupobjects = asccRepository.findByDefinition("Group");
        for (AssociationCoreComponent asccVO : groupobjects) {
            int ElementsInGroup = 0;
            List<AssociationCoreComponent> asccObjects = asccRepository.findByFromAccId(asccVO.getFromAccId());
            for (AssociationCoreComponent ascc : asccObjects) {
                AssociationCoreComponentProperty asccp = asccpRepository.findOne(ascc.getToAsccpId());
                if (asccp.getDefinition() != null &&
                        asccp.getDefinition().equalsIgnoreCase("Group") &&
                        asccVO.getSeqKey() > ascc.getSeqKey()) {
                    String groupname = asccp.getPropertyTerm().replaceAll(" ", "");
                    List<AssociationCoreComponent> asccObjectsinGroup = asccRepository.findByDenStartsWith(groupname);
                    List<BasicCoreComponent> bccObjectsinGroup = bccRepository.findByDenStartsWith(groupname);
                    ElementsInGroup += (asccObjectsinGroup.size() + bccObjectsinGroup.size());
                    if ((asccObjectsinGroup.size() + bccObjectsinGroup.size()) > 0) {
                        ElementsInGroup--;
                    }
                }
            }
            int new_seq = asccVO.getSeqKey() - ElementsInGroup;
            asccVO.setSeqKey(new_seq);
            asccRepository.save(asccVO);
        }
    }

    private void modifySequenceKeyforGroup_temp() throws Exception {
        //modify sequenceKey for group
        System.out.println("Modifying sequence key for groups temporarily");
        List<AssociationCoreComponent> groupobjects = asccRepository.findByDenContaining("Actual Resource Group");
        for (AssociationCoreComponent asccVO : groupobjects) {
            List<AssociationCoreComponent> asccObjects = asccRepository.findByFromAccId(asccVO.getFromAccId());
            List<BasicCoreComponent> bccObjects = bccRepository.findByFromAccId(asccVO.getFromAccId());
            for (int i = 0; i < asccObjects.size(); i++) {
                AssociationCoreComponent ascc = asccObjects.get(i);
                if (ascc.getDen() != null && ascc.getDen().contains("Actual Resource Group")) {
                    if (i + 1 < asccObjects.size()) {
                        AssociationCoreComponent ascc2 = asccObjects.get(i + 1);
                        if (ascc2.getDen() != null && ascc2.getDen().contains("Free Form Text Group")) {
                            ascc2.setSeqKey(ascc.getSeqKey() + 1);
                            asccRepository.save(ascc2);
                            for (int j = 0; j < asccObjects.size(); j++) {
                                AssociationCoreComponent changed_ascc = asccObjects.get(j);
                                if (changed_ascc.getSeqKey() >= ascc2.getSeqKey() && changed_ascc.getAsccId() != ascc2.getAsccId()) {
                                    changed_ascc.setSeqKey(changed_ascc.getSeqKey() + 1);
                                    asccRepository.save(changed_ascc);
                                }
                            }
                            for (int j = 0; j < bccObjects.size(); j++) {
                                BasicCoreComponent changed_bcc = bccObjects.get(j);
                                if (changed_bcc.getSeqKey() >= ascc2.getSeqKey()) {
                                    changed_bcc.setSeqKey(changed_bcc.getSeqKey() + 1);
                                    bccRepository.save(changed_bcc);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    private AggregateCoreComponent getACC(String guid) throws Exception {
        return accRepository.findOneByGuid(guid);
    }

    private boolean insertBCC(BODElementVO bodVO, String parentGuid, BasicCoreComponentProperty bccp) throws Exception {
        if (bccp == null)
            return false;

        String bccGuid = (bodVO.getRef() != null) ? bodVO.getRef() : bodVO.getId();
        int assocToBccpId = bccp.getBccpId();

        AggregateCoreComponent accVO = accRepository.findOneByGuid(parentGuid);
        int assocFromACCId = accVO.getAccId();

        if (!bccRepository.existsByGuidAndFromAccIdAndToBccpId(bccGuid, assocFromACCId, assocToBccpId)) {
            int cardinalityMin = bodVO.getMinOccur();
            int cardinalityMax = bodVO.getMaxOccur();
            int sequenceKey = bodVO.getOrder();

            int entityType = 1;
            String den = Utility.first(accVO.getDen()) + ". " + bccp.getDen();

            BasicCoreComponent bcc = new BasicCoreComponent();
            bcc.setGuid(bccGuid);
            bcc.setCardinalityMin(cardinalityMin);
            bcc.setCardinalityMax(cardinalityMax);
            bcc.setToBccpId(assocToBccpId);
            bcc.setFromAccId(assocFromACCId);
            bcc.setSeqKey(sequenceKey);
            bcc.setEntityType(entityType);
            bcc.setDen(den);
            bcc.setState(3);
            bcc.setDeprecated(false);
            bcc.setReleaseId(releaseId);
            bcc.setCreatedBy(userId);
            bcc.setLastUpdatedBy(userId);
            bcc.setOwnerUserId(userId);
            bccRepository.save(bcc);

            return true;
        }
        return false;
    }

    private void insertBCCWithAttr(XSAttributeDecl xad, XSComplexTypeDecl complexType) {
        String bccGuid = xad.getFId();

        int cardinalityMin = (xad.getFUse() == null) ? 0 : (xad.getFUse().equals("optional") || xad.getFUse().equals("prohibited")) ? 0 : (xad.getFUse().equals("required")) ? 1 : 0;
        int cardinalityMax = (xad.getFUse() == null) ? 1 : (xad.getFUse().equals("optional") || xad.getFUse().equals("required")) ? 1 : (xad.getFUse().equals("prohibited")) ? 0 : 0;
        int sequenceKey = 0;

        String xadName = xad.getName();
        XSSimpleTypeDecl xtd = (XSSimpleTypeDecl) xad.getTypeDefinition();
        String typeGuid = xtd.getFId();
        BasicCoreComponentProperty bccpVO = (typeGuid != null) ? bccpRepository.findOneByGuid(typeGuid) : null;
        if (bccpVO == null) {
            bccpVO = insertBCCP(xadName, typeGuid);

            if (bccpVO == null) {
                System.err.println("BCCP creation is failed in BCC attribute creation, Note -> Name : " + xadName + "   guid : " + typeGuid);
                return;
            }
        }
        int assocToBccpId = bccpVO.getBccpId();

        if (!xad.getName().equals("responseCode")) {
            String parentGuid = complexType.getFId();
            AggregateCoreComponent accVO = accRepository.findAccIdAndDenByGuid(parentGuid);
            int assocFromACCId = accVO.getAccId();

            if (!bccRepository.existsByGuidAndToBccpId(bccGuid, assocToBccpId)) {
                int entityType = 0;
                String den = Utility.first(accVO.getDen()) + ". " + bccpVO.getDen();

                BasicCoreComponent bcc = new BasicCoreComponent();
                bcc.setGuid(bccGuid);
                bcc.setCardinalityMin(cardinalityMin);
                bcc.setCardinalityMax(cardinalityMax);
                bcc.setToBccpId(assocToBccpId);
                bcc.setFromAccId(assocFromACCId);
                bcc.setSeqKey(sequenceKey);
                bcc.setEntityType(entityType);
                bcc.setDen(den);
                bcc.setState(3);
                bcc.setDeprecated(false);
                bcc.setReleaseId(releaseId);
                bcc.setCreatedBy(userId);
                bcc.setLastUpdatedBy(userId);
                bcc.setOwnerUserId(userId);
                bccRepository.save(bcc);
            }
        }
    }

    private BasicCoreComponentProperty insertBCCP(String name, String id) {
        if (id == null) {
            System.err.println("!!!! id is null where name is  = " + name);
            return null;
        }

        String propertyTerm = Utility.spaceSeparator(name).replace("ID", "Identifier");

        DataType dtVO = dataTypeRepository.findDtIdAndDataTypeTermByGuidAndType(id, 1);
        if (dtVO == null) {
            System.err.println("!!!! DT is null where name is  = " + name + " and id is = " + id);
            return null;
        }

        int bdtId = dtVO.getDtId();
        String representationTerm = dtVO.getDataTypeTerm();
        String den = Utility.firstToUpperCase(propertyTerm) + ". " + representationTerm;

        BasicCoreComponentProperty bccp = new BasicCoreComponentProperty();
        String bccpGuid = Utility.generateGUID();
        bccp.setGuid(bccpGuid);
        bccp.setPropertyTerm(propertyTerm);
        bccp.setBdtId(bdtId);
        bccp.setRepresentationTerm(representationTerm);
        bccp.setDen(den);
        bccp.setState(3);
        bccp.setCreatedBy(userId);
        bccp.setLastUpdatedBy(userId);
        bccp.setOwnerUserId(userId);
        bccp.setDeprecated(false);
        bccp.setReleaseId(releaseId);
        bccpRepository.saveAndFlush(bccp);

        return bccp;
    }


    private boolean insertForGroup(BODElementVO bodVO, String fullFilePath,
                                   String complexTypeId, int cnt) throws Exception {
        String groupName = bodVO.getGroupName();
        String objectClassName = Utility.spaceSeparator(groupName.substring(0, (groupName.indexOf("Type") > 0) ? groupName.indexOf("Type") : groupName.length()));
        String den = objectClassName + ". Details";
        int oagisComponentType = 1;
        if (Utility.first(den).endsWith("Base"))
            oagisComponentType = 0;
        else if (Utility.first(den).endsWith("Extension") ||
                Utility.first(den).equals("Open User Area") ||
                Utility.first(den).equals("Any User Area") ||
                Utility.first(den).equals("All Extension"))
            oagisComponentType = 2;
        else if (Utility.first(den).endsWith("Group") || objectClassName.equalsIgnoreCase("Common Time Reporting"))
            oagisComponentType = 3;
        String module = Utility.extractModuleName(fullFilePath);
        insertACCForGroup(bodVO, objectClassName, den, oagisComponentType, module);

        int groupAccId = accRepository.findAccIdAndDenByGuid(bodVO.getGroupId()).getAccId();
        AssociationCoreComponentProperty asccp = insertASCCPForGroup(bodVO, groupAccId, den, module);

        return insertASCCForGroup(bodVO, asccp, complexTypeId, cnt);
    }

    private void insertACCForGroup(BODElementVO bodVO, String objectClassName,
                                   String accDen, int oagisComponentType, String module) throws Exception {
        if (!accRepository.existsByGuid(bodVO.getGroupId())) {
            AggregateCoreComponent acc = new AggregateCoreComponent();
            acc.setGuid(bodVO.getGroupId());
            acc.setObjectClassTerm(objectClassName);
            acc.setDen(accDen);
            acc.setDefinition("Group");
            acc.setOagisComponentType(oagisComponentType);
            acc.setBasedAccId(-1);
            acc.setCreatedBy(userId);
            acc.setLastUpdatedBy(userId);
            acc.setOwnerUserId(userId);
            acc.setState(3);
            acc.setModule(module);
            acc.setDeprecated(false);
            acc.setAbstract(false);
            acc.setNamespaceId(namespaceId);
            acc.setReleaseId(releaseId);
            accRepository.save(acc);
        }
    }

    private AssociationCoreComponentProperty insertASCCPForGroup(
            BODElementVO bodVO, int groupAccId, String accDen, String module) throws Exception {
        AssociationCoreComponentProperty asccp = asccpRepository.findAsccpIdAndDenByGuid(bodVO.getGroupId());
        if (asccp == null) {
            String propertyTerm = Utility.spaceSeparator(bodVO.getGroupName());

            asccp = new AssociationCoreComponentProperty();
            asccp.setGuid(bodVO.getGroupId());
            asccp.setPropertyTerm(propertyTerm);
            asccp.setDefinition("Group");
            asccp.setRoleOfAccId(groupAccId);
            asccp.setDen(Utility.spaceSeparator(propertyTerm + ". " + Utility.first(accDen)));
            asccp.setState(3);
            asccp.setModule(module);
            asccp.setCreatedBy(userId);
            asccp.setLastUpdatedBy(userId);
            asccp.setOwnerUserId(userId);
            asccp.setDeprecated(false);
            asccp.setNamespaceId(namespaceId);
            asccp.setReleaseId(releaseId);
            asccpRepository.saveAndFlush(asccp);
        }

        return asccp;
    }

    private boolean insertASCCForGroup(BODElementVO bodVO, AssociationCoreComponentProperty asccp,
                                       String complexTypeId, int cnt) throws Exception {
        int assocToASCCPId = asccp.getAsccpId();

        AggregateCoreComponent acc;
        if (bodVO.getGroupParent() == null) {
            acc = accRepository.findAccIdAndDenByGuid(complexTypeId);
        } else {
            acc = accRepository.findAccIdAndDenByGuid(bodVO.getGroupParent());
        }

        int accId = acc.getAccId();
        String asccGuid = bodVO.getGroupRef();
        int fromAccId = accId;
        int toAsccpId = assocToASCCPId;
        if (!asccRepository.existsByGuidAndFromAccIdAndToAsccpId(asccGuid, accId, assocToASCCPId)) {
            AssociationCoreComponent ascc = new AssociationCoreComponent();
            ascc.setGuid(asccGuid);
            ascc.setCardinalityMin(1);
            ascc.setCardinalityMax(1);
            ascc.setFromAccId(fromAccId);
            ascc.setToAsccpId(toAsccpId);
            ascc.setSeqKey(cnt); // TODO check this

            ascc.setDen(Utility.first(acc.getDen()) + ". " + asccp.getDen());
            ascc.setDefinition("Group");
            ascc.setState(3);
            ascc.setDeprecated(false);
            ascc.setReleaseId(releaseId);
            ascc.setCreatedBy(userId);
            ascc.setLastUpdatedBy(userId);
            ascc.setOwnerUserId(userId);
            asccRepository.save(ascc);

            return true;
        }
        return false;
    }

    private class InsertACCResult {
        private AggregateCoreComponent aggregateCoreComponent;
        private List<String> elements;

        public InsertACCResult(AggregateCoreComponent aggregateCoreComponent, List<String> elements) {
            this.aggregateCoreComponent = aggregateCoreComponent;
            this.elements = elements;
        }

        public AggregateCoreComponent getAggregateCoreComponent() {
            return aggregateCoreComponent;
        }

        public List<String> getElements() {
            return elements;
        }
    }

    private InsertACCResult insertACC(BCCWithAttrHolder bccWithAttrHolder, XSComplexTypeDecl complexType, String fullFilePath) throws Exception {
        long s = System.currentTimeMillis();
        List<String> elements = new ArrayList();

        String accGuid = complexType.getFId();
        String objectClassName = Utility.spaceSeparator(complexType.getName().substring(0, complexType.getName().lastIndexOf("Type")));
        String den = objectClassName + ". Details";
        String definition = bodSchemaHandler.getAnnotation(complexType);

        int basedAccId = 0;
        String base = complexType.getBaseType().getName();

        if (base != null && !base.equals("anyType") && complexType.getBaseType().getTypeCategory() != XSTypeDefinition.SIMPLE_TYPE) {
            XSComplexTypeDecl baseType = bodSchemaHandler.getComplexTypeDefinition(base);

            AggregateCoreComponent basedAcc = accRepository.findAccIdAndDenByGuid(baseType.getFId());
            if (basedAcc == null) {
                InsertACCResult insertACCResult = insertACC(bccWithAttrHolder, baseType, fullFilePath);
                elements = insertACCResult.getElements();
                basedAccId = insertACCResult.getAggregateCoreComponent().getAccId();
            } else {
                basedAccId = basedAcc.getAccId();
            }
            XSParticle particle = bodSchemaHandler.getComplexTypeDefinition(base).getParticle();
            if (particle != null) {
                List<BODElementVO> al = bodSchemaHandler.processParticle(particle, 1);
                for (BODElementVO bodVO : al) {
                    elements.add(bodVO.getName());
                }
            }
        }

        int oagisComponentType = 1;
        if (Utility.first(den).endsWith("Base"))
            oagisComponentType = 0;
        else if (Utility.first(den).endsWith("Extension") ||
                Utility.first(den).equals("Open User Area") ||
                Utility.first(den).equals("Any User Area") ||
                Utility.first(den).equals("All Extension"))
            oagisComponentType = 2;
        else if (Utility.first(den).endsWith("Group"))
            oagisComponentType = 3;

        int state = 3;
        String module = Utility.extractModuleName(fullFilePath);
        module = module.replace("\\", "/");

        AggregateCoreComponent acc = new AggregateCoreComponent();
        acc.setGuid(accGuid);
        acc.setObjectClassTerm(objectClassName);
        acc.setDen(den);
        acc.setDefinition(definition);
        acc.setBasedAccId(basedAccId);
        acc.setOagisComponentType(oagisComponentType);
        acc.setCreatedBy(userId);
        acc.setLastUpdatedBy(userId);
        acc.setOwnerUserId(userId);
        acc.setState(state);
        acc.setModule(module);
        acc.setDeprecated(false);
        acc.setAbstract(complexType.getAbstract());
        acc.setNamespaceId(namespaceId);
        acc.setReleaseId(releaseId);
        accRepository.saveAndFlush(acc);

        XSParticle particle = complexType.getParticle();
        if (particle != null) {
            List<BODElementVO> al = bodSchemaHandler.processParticle(particle, 1);
            String tempGroupId = "";
            int cnt = 1;
            for (BODElementVO bodVO : al) {
                if (!elements.contains(bodVO.getName())) {
                    elements.add(bodVO.getName());

                    if (bodSchemaHandler.isComplexWithoutSimpleContent(bodVO.getTypeName())) {
                        if (!accRepository.existsByGuid(bodSchemaHandler.getComplexTypeDefinition(bodVO.getTypeName()).getFId())) {
                            insertACC(bccWithAttrHolder, bodSchemaHandler.getComplexTypeDefinition(bodVO.getTypeName()), fullFilePath);
                        }
                    }

                    // insert ACC with group id, if group parent == null, then complexType is based ACC, else parent is based ACC
                    // insert ASCCP with group id, from ACC group
                    // insert ASCC with group ref, from ACC is parent or CT, to ASCCP is group id

                    if (bodVO.getGroupId() != null && !tempGroupId.equals(bodVO.getGroupId())) {
                        tempGroupId = bodVO.getGroupId();
                        if (insertForGroup(bodVO, fullFilePath, complexType.getFId(), cnt)) {
                            cnt++;
                        }
                    }

                    String parentGuid = (bodVO.getGroupId() != null) ? bodVO.getGroupId() : complexType.getFId();
                    boolean result;
                    AssociationCoreComponentProperty asccp = asccpRepository.findAsccpIdAndDenByGuid(bodVO.getId());
                    if (asccp == null) {
                        BasicCoreComponentProperty bccp = bccpRepository.findBccpIdAndDenByGuid(bodVO.getId());
                        if (bccp == null) {
                            if (bodSchemaHandler.isComplexWithoutSimpleContent(bodVO.getTypeName())) {
                                if (bodVO.getGroupId() == null) {
                                    asccp = insertASCCP(bodVO.getElement(), bodSchemaHandler.getComplexTypeDefinition(bodVO.getTypeName()));
                                } else {
                                    String propertyTerm = Utility.spaceSeparator(bodVO.getGroupName());
                                    den = propertyTerm + ". " + Utility.first(den);
                                    asccp = insertASCCPUnderGroup(bodVO);
                                }

                                result = insertASCC(bodVO, parentGuid, asccp);
                            } else {
                                bccp = insertBCCP(bodVO.getName(), bodVO.getId());
                                result = insertBCC(bodVO, parentGuid, bccp);
                            }
                        } else {
                            result = insertBCC(bodVO, parentGuid, bccp);
                        }
                    } else {
                        result = insertASCC(bodVO, parentGuid, asccp);
                    }

                    if (result) {
                        cnt++;
                    }
                }
            }
        }

        XSObjectList xol = complexType.getAttributeUses();
        for (int i = 0; i < xol.getLength(); i++) {
            bccWithAttrHolder.add((XSAttributeUseImpl) xol.get(i), complexType);
        }
        return new InsertACCResult(acc, elements);
    }

    private class BCCWithAttrHolder {
        private Map<Integer, Pair> attributeUseXSComplexTypeDeclMap = new TreeMap();
        private class Pair {
            private XSAttributeUseImpl xsAttributeUse;
            private XSComplexTypeDecl xsComplexTypeDecl;

            public Pair(XSAttributeUseImpl xsAttributeUse, XSComplexTypeDecl xsComplexTypeDecl) {
                this.xsAttributeUse = xsAttributeUse;
                this.xsComplexTypeDecl = xsComplexTypeDecl;
            }
        }

        public void add(XSAttributeUseImpl xsAttributeUse, XSComplexTypeDecl xsComplexTypeDecl) {
            if (!attributeUseXSComplexTypeDeclMap.containsKey(xsAttributeUse.hashCode())) {
                attributeUseXSComplexTypeDeclMap.put(xsAttributeUse.hashCode(), new Pair(xsAttributeUse, xsComplexTypeDecl));
            }
        }

        public void save() {
            for (Pair pair : attributeUseXSComplexTypeDeclMap.values()) {
                XSAttributeUseImpl xui = pair.xsAttributeUse;
                XSAttributeDecl xad = (XSAttributeDecl) xui.getAttrDeclaration();
                XSComplexTypeDecl complexType = pair.xsComplexTypeDecl;

                insertBCCWithAttr(xad, complexType);
            }
        }
    }

    private File[] getBODs(File f) {
        return f.listFiles((dir, name) -> {
            return name.matches(".*.xsd");
        });
    }

    @Transactional(rollbackFor = Throwable.class)
    public void run(ApplicationContext applicationContext) throws Exception {
        System.out.println("### 1.8 Start");

        populate();

        System.out.println("### 1.8 End");
    }

    public static void main(String[] args) throws Exception {
        try (AbstractApplicationContext ctx = (AbstractApplicationContext)
                SpringApplication.run(Application.class, args);) {
            P_1_8_PopulateAccAsccpBccAscc populateAccAsccpBccAscc = ctx.getBean(P_1_8_PopulateAccAsccpBccAscc.class);
            populateAccAsccpBccAscc.run(ctx);
        }
    }
}