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

    private int userId;
    private int releaseId;

    private BODSchemaHandler bodSchemaHandler;
    private String bodPath;

    private File f1 = new File(SRTConstants.BOD_FILE_PATH_01);
    private File f2 = new File(SRTConstants.BOD_FILE_PATH_02);

    @PostConstruct
    public void init() throws Exception {
        userId = userRepository.findAppUserIdByLoginId("oagis");
        releaseId = releaseRepository.findReleaseIdByReleaseNum("10.1");
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
                .filter(file -> file.getName().endsWith("AcknowledgeInvoice.xsd"))
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
                .filter(file -> !file.getName().endsWith("AcknowledgeInvoice.xsd"))
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

    private void insertASCCP(XSElementDecl element, XSComplexTypeDecl complexType) throws Exception {
        String name = element.getName();

        if (complexType != null) {
            String asccpGuid = element.getFId();
            String propertyTerm = Utility.spaceSeparator(name);
            String definition = bodSchemaHandler.getAnnotation(element);

            int roleOfAccId;
            AggregateCoreComponent accVO = accRepository.findOneByGuid(complexType.getFId());
            if (accVO == null) {
                InsertACCResult insertACCResult = insertACC(complexType, bodPath);
                accVO = insertACCResult.getAggregateCoreComponent();
            }
            roleOfAccId = accVO.getAccId();

            String den = propertyTerm + ". " + Utility.spaceSeparator(Utility.first(accVO.getDen()));
            int state = 3;
            String module = bodPath.substring(bodPath.indexOf("Model"));
            module = module.replace("\\", "/");

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
            asccp.setNamespaceId(1); //tmp
            asccp.setReleaseId(releaseId);
            asccpRepository.save(asccp);
        }
    }

    private void insertASCCPUnderGroup(BODElementVO bodVO) throws Exception {
        String name = bodVO.getElement().getName();

        String asccpGuid = bodVO.getElement().getFId();
        String propertyTerm = Utility.spaceSeparator(name);
        String definition = bodSchemaHandler.getAnnotation(bodVO.getElement());

        AggregateCoreComponent accVO = accRepository.findOneByGuid(bodSchemaHandler.getComplexTypeDefinition(bodVO.getTypeName()).getFId());
        int roleOfAccId = accVO.getAccId();

        String den = propertyTerm + ". " + Utility.first(accVO.getDen());
        int state = 3;
        String module = bodPath.substring(bodPath.indexOf("Model"));
        module = module.replace("\\", "/");

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
        asccp.setNamespaceId(1); //tmp
        asccp.setReleaseId(releaseId);
        asccpRepository.save(asccp);
    }

    private void insertASCC(BODElementVO bodVO, String parentGuid, AssociationCoreComponentProperty asccpVO) throws Exception {

        AggregateCoreComponent accVO = accRepository.findOneByGuid(parentGuid);
        int assocFromACCId = accVO.getAccId();

        String asccGuid = (bodVO.getRef() != null) ? bodVO.getRef() : bodVO.getId();
        int assocToASCCPId = asccpVO.getAsccpId();

        if (asccRepository.findOneByGuidAndFromAccIdAndToAsccpId(asccGuid, assocFromACCId, assocToASCCPId) == null) {
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
        }
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

    private AssociationCoreComponentProperty getASCCP(String guid) throws Exception {
        return asccpRepository.findOneByGuid(guid);
    }

    private void insertBCC(BODElementVO bodVO, String parentGuid, BasicCoreComponentProperty bccpVO) throws Exception {
        if (bccpVO == null)
            return;

        String bccGuid = (bodVO.getRef() != null) ? bodVO.getRef() : bodVO.getId();
        int assocToBccpId = bccpVO.getBccpId();

        AggregateCoreComponent accVO = accRepository.findOneByGuid(parentGuid);
        int assocFromACCId = accVO.getAccId();

        if (bccRepository.findOneByGuidAndFromAccIdAndToBccpId(bccGuid, assocFromACCId, assocToBccpId) == null) {
            int cardinalityMin = bodVO.getMinOccur();
            int cardinalityMax = bodVO.getMaxOccur();
            int sequenceKey = bodVO.getOrder();

            int entityType = 1;
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

    private void insertBCCWithAttr(XSAttributeDecl xad, XSComplexTypeDecl complexType) throws Exception {
        String bccGuid = xad.getFId();

        int cardinalityMin = (xad.getFUse() == null) ? 0 : (xad.getFUse().equals("optional") || xad.getFUse().equals("prohibited")) ? 0 : (xad.getFUse().equals("required")) ? 1 : 0;
        int cardinalityMax = (xad.getFUse() == null) ? 1 : (xad.getFUse().equals("optional") || xad.getFUse().equals("required")) ? 1 : (xad.getFUse().equals("prohibited")) ? 0 : 0;
        int sequenceKey = 0;

        BasicCoreComponentProperty bccpVO = bccpRepository.findOneByPropertyTerm(Utility.spaceSeparator(xad.getName()).replace("ID", "Identifier"));
        if (bccpVO == null) {
            XSSimpleTypeDecl xtd = (XSSimpleTypeDecl) xad.getTypeDefinition();
            bccpVO = insertBCCP(xad.getName(), xtd.getFId());

            if (bccpVO == null) {
                System.err.println("BCCP creation is failed in BCC attribute creation, Note -> Name : " + xad.getName() + "   guid : " + xtd.getFId());
                return;
            }
        }
        int assocToBccpId = bccpVO.getBccpId();

        if (!xad.getName().equals("responseCode")) {
            String parentGuid = complexType.getFId();
            AggregateCoreComponent accVO = accRepository.findOneByGuid(parentGuid);
            int assocFromACCId = accVO.getAccId();

            if (bccRepository.findOneByGuidAndFromAccIdAndToBccpId(bccGuid, assocFromACCId, assocToBccpId) == null) {
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

    private BasicCoreComponentProperty insertBCCP(String name, String id) throws Exception {
        if (id == null) {
            System.err.println("!!!! id is null where name is  = " + name);
            return null;
        }

        String propertyTerm = Utility.spaceSeparator(name).replace("ID", "Identifier");

        DataType dtVO = dataTypeRepository.findOneByGuidAndType(id, 1);
        if (dtVO == null) {
            System.out.println("!!!! DT is null where name is  = " + name + " and id is = " + id);
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


    private void insertForGroup(BODElementVO bodVO, String fullFilePath, String complexTypeId, int cnt) throws Exception {
        String objectClassName = Utility.spaceSeparator(bodVO.getGroupName().substring(0, (bodVO.getGroupName().indexOf("Type") > 0) ? bodVO.getGroupName().indexOf("Type") : bodVO.getGroupName().length()));
        String den = objectClassName + ". Details";
        int oagisComponentType = 1;
        if (Utility.first(den).endsWith("Base"))
            oagisComponentType = 0;
        else if (Utility.first(den).endsWith("Extension") || Utility.first(den).equals("Open User Area") || Utility.first(den).equals("Any User Area") || Utility.first(den).equals("All Extension"))
            oagisComponentType = 2;
        else if (Utility.first(den).endsWith("Group") || objectClassName.equalsIgnoreCase("Common Time Reporting"))
            oagisComponentType = 3;
        String module = fullFilePath.substring(fullFilePath.indexOf("Model"));
        module = module.replace("\\", "/");
        insertACCForGroup(bodVO, objectClassName, den, oagisComponentType, module);

        int groupAccId = accRepository.findOneByGuid(bodVO.getGroupId()).getAccId();
        insertASCCPForGroup(bodVO, groupAccId, den, module);

        insertASCCForGroup(bodVO, complexTypeId, cnt);
    }

    private void insertACCForGroup(BODElementVO bodVO, String objectClassName, String accDen, int oagisComponentType, String module) throws Exception {
        if (getACC(bodVO.getGroupId()) == null) {
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
            acc.setNamespaceId(1); //tmp
            acc.setReleaseId(releaseId);
            accRepository.save(acc);
        }
    }

    private void insertASCCPForGroup(BODElementVO bodVO, int groupAccId, String accDen, String module) throws Exception {
        if (getASCCP(bodVO.getGroupId()) == null) {
            String propertyTerm = Utility.spaceSeparator(bodVO.getGroupName());

            AssociationCoreComponentProperty asccp = new AssociationCoreComponentProperty();
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
            asccp.setNamespaceId(1); //tmp
            asccp.setReleaseId(releaseId);
            asccpRepository.save(asccp);
        }
    }

    private void insertASCCForGroup(BODElementVO bodVO, String complexTypeId, int cnt) throws Exception {
        AssociationCoreComponentProperty asccp = getASCCP(bodVO.getGroupId());
        int assocToASCCPId = asccp.getAsccpId();

        int accId = 0;
        if (bodVO.getGroupParent() == null) {
            accId = getACC(complexTypeId).getAccId();
        } else {
            accId = getACC(bodVO.getGroupParent()).getAccId();
        }

        String asccGuid = bodVO.getGroupRef();
        int fromAccId = accId;
        int toAsccpId = assocToASCCPId;
        if (asccRepository.findOneByGuidAndFromAccIdAndToAsccpId(asccGuid, accId, assocToASCCPId) == null) {
            AssociationCoreComponent ascc = new AssociationCoreComponent();
            ascc.setGuid(asccGuid);
            ascc.setCardinalityMin(1);
            ascc.setCardinalityMax(1);
            ascc.setFromAccId(fromAccId);
            ascc.setToAsccpId(toAsccpId);
            ascc.setSeqKey(cnt); // TODO check this

            AggregateCoreComponent accVO2 = accRepository.findOne(accId);
            ascc.setDen(Utility.first(accVO2.getDen()) + ". " + asccp.getDen());
            ascc.setDefinition("Group");
            ascc.setState(3);
            ascc.setDeprecated(false);
            ascc.setReleaseId(releaseId);
            ascc.setCreatedBy(userId);
            ascc.setLastUpdatedBy(userId);
            ascc.setOwnerUserId(userId);
            asccRepository.save(ascc);
        }
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

    private InsertACCResult insertACC(XSComplexTypeDecl complexType, String fullFilePath) throws Exception {
        List<String> elements = new ArrayList();

        String accGuid = complexType.getFId();
        String objectClassName = Utility.spaceSeparator(complexType.getName().substring(0, complexType.getName().lastIndexOf("Type")));
        String den = objectClassName + ". Details";
        String definition = bodSchemaHandler.getAnnotation(complexType);

        int basedAccId = -1;
        String base = complexType.getBaseType().getName();

        if (base != null && !base.equals("anyType") && complexType.getBaseType().getTypeCategory() != XSTypeDefinition.SIMPLE_TYPE) {
            XSComplexTypeDecl baseType = bodSchemaHandler.getComplexTypeDefinition(base);

            AggregateCoreComponent accVO = accRepository.findOneByGuid(baseType.getFId());
            if (accVO == null) {
                InsertACCResult insertACCResult = insertACC(baseType, fullFilePath);
                elements = insertACCResult.getElements();
                accVO = insertACCResult.getAggregateCoreComponent();
            }
            basedAccId = accVO.getAccId();
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
        else if (Utility.first(den).endsWith("Extension") || Utility.first(den).equals("Open User Area") || Utility.first(den).equals("Any User Area") || Utility.first(den).equals("All Extension"))
            oagisComponentType = 2;
        else if (Utility.first(den).endsWith("Group"))
            oagisComponentType = 3;

        int state = 3;
        String module = fullFilePath.substring(fullFilePath.indexOf("Model"));
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
        acc.setNamespaceId(1); //tmp
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
                        AggregateCoreComponent accVO =
                                accRepository.findOneByGuid(bodSchemaHandler.getComplexTypeDefinition(bodVO.getTypeName()).getFId());
                        if (accVO == null) {
                            insertACC(bodSchemaHandler.getComplexTypeDefinition(bodVO.getTypeName()), fullFilePath);
                        }
                    }

                    // insert ACC with group id, if group parent == null, then complexType is based ACC, else parent is based ACC
                    // insert ASCCP with group id, from ACC group
                    // insert ASCC with group ref, from ACC is parent or CT, to ASCCP is group id

                    if (bodVO.getGroupId() != null && !tempGroupId.equals(bodVO.getGroupId())) {
                        tempGroupId = bodVO.getGroupId();
                        insertForGroup(bodVO, fullFilePath, complexType.getFId(), cnt);
                    }

                    AssociationCoreComponentProperty asccpVO = asccpRepository.findOneByGuid(bodVO.getId());
                    if (asccpVO == null) {
                        BasicCoreComponentProperty bccpVO = bccpRepository.findOneByGuid(bodVO.getId());
                        if (bccpVO == null) {
                            if (bodSchemaHandler.isComplexWithoutSimpleContent(bodVO.getTypeName())) {
                                if (bodVO.getGroupId() == null) {
                                    insertASCCP(bodVO.getElement(), bodSchemaHandler.getComplexTypeDefinition(bodVO.getTypeName()));
                                } else {

                                    String propertyTerm = Utility.spaceSeparator(bodVO.getGroupName());
                                    den = propertyTerm + ". " + Utility.first(den);
                                    insertASCCPUnderGroup(bodVO);
                                }

                                AssociationCoreComponentProperty asccpVO1 = asccpRepository.findOneByGuid(bodVO.getId());
                                insertASCC(bodVO, (bodVO.getGroupId() != null) ? bodVO.getGroupId() : complexType.getFId(), asccpVO1);
                            } else if (bodSchemaHandler.isComplexWithSimpleContent(bodVO.getTypeName())) {
                                bccpVO = insertBCCP(bodVO.getName(), bodVO.getId());
                                insertBCC(bodVO, (bodVO.getGroupId() != null) ? bodVO.getGroupId() : complexType.getFId(), bccpVO);
                            }
                        } else {
                            insertBCC(bodVO, (bodVO.getGroupId() != null) ? bodVO.getGroupId() : complexType.getFId(), bccpVO);
                        }
                    } else {
                        insertASCC(bodVO, (bodVO.getGroupId() != null) ? bodVO.getGroupId() : complexType.getFId(), asccpVO);
                    }

                    cnt++;
                }
            }
        }

        XSObjectList xol = complexType.getAttributeUses();
        for (int i = 0; i < xol.getLength(); i++) {
            XSAttributeUseImpl xui = (XSAttributeUseImpl) xol.get(i);
            XSAttributeDecl xad = (XSAttributeDecl) xui.getAttrDeclaration();

            insertBCCWithAttr(xad, complexType);
        }

        return new InsertACCResult(acc, elements);
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