package org.oagi.srt.persistence.populate;

import com.sun.xml.internal.xsom.XSComplexType;
import com.sun.xml.internal.xsom.XSElementDecl;
import com.sun.xml.internal.xsom.XSModelGroupDecl;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.persistence.populate.helper.*;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.oagi.srt.common.SRTConstants.ANY_ASCCP_DEN;
import static org.oagi.srt.common.SRTConstants.PLATFORM_PATH;
import static org.oagi.srt.persistence.populate.DataImportScriptPrinter.printTitle;

@Component
public class P_1_8_1_PopulateAccAsccpBccAscc {

    private final Logger logger = LoggerFactory.getLogger(getClass());

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
    private ModuleRepository moduleRepository;

    @Autowired
    private ImportUtil importUtil;

    private File f1 = new File(SRTConstants.BOD_FILE_PATH_01);
    private File f2 = new File(SRTConstants.BOD_FILE_PATH_02);

    public static void main(String[] args) throws Exception {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ImportApplication.class, args)) {
            P_1_8_1_PopulateAccAsccpBccAscc populateAccAsccpBccAscc =
                    ctx.getBean(P_1_8_1_PopulateAccAsccpBccAscc.class);
            populateAccAsccpBccAscc.run(ctx);
        }
    }

    public void run(ApplicationContext applicationContext) throws Exception {
        logger.info("### 1.8 Start");

        printTitle("Populate ACCs, ASCCPs, BCCPs, BCCs and ASCCs top down from BODs");
        populateForAny();
        populate();

        printTitle("Populate other ACCs, ASCCPs, BCCPs, BCCs, and ASCCs unused by BODs");
        populateUnused();

        logger.info("### 1.8 End");
    }

    @Transactional(rollbackFor = Throwable.class)
    public void populateForAny() throws Exception {
        AggregateCoreComponent anyACC = new AggregateCoreComponent();
        anyACC.setGuid(Utility.generateGUID());
        anyACC.setObjectClassTerm("Any Structured Content");
        anyACC.setDen(anyACC.getObjectClassTerm() + ". Details");
        anyACC.setDefinition("This is corresponding to the xsd:any with the processContents = “strict” and any namespace.");
        anyACC.setOagisComponentType(5);
        anyACC.setOwnerUserId(importUtil.getUserId());
        anyACC.setCreatedBy(importUtil.getUserId());
        anyACC.setLastUpdatedBy(importUtil.getUserId());
        anyACC.setReleaseId(importUtil.getReleaseId());
        anyACC.setNamespaceId(importUtil.getNamespaceId());
        accRepository.saveAndFlush(anyACC);

        AssociationCoreComponentProperty anyASCCP = new AssociationCoreComponentProperty();
        anyASCCP.setGuid(Utility.generateGUID());
        anyASCCP.setPropertyTerm("Any Property");
        anyASCCP.setRoleOfAccId(anyACC.getAccId());
        anyASCCP.setDen(ANY_ASCCP_DEN);
        anyASCCP.setReusableIndicator(true);
        anyASCCP.setOwnerUserId(importUtil.getUserId());
        anyASCCP.setCreatedBy(importUtil.getUserId());
        anyASCCP.setLastUpdatedBy(importUtil.getUserId());
        anyASCCP.setReleaseId(importUtil.getReleaseId());
        anyASCCP.setNamespaceId(importUtil.getNamespaceId());
        asccpRepository.saveAndFlush(anyASCCP);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void populate() throws Exception {
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

        for (File file : files) {
            if (file.getName().equals("AcknowledgeInvoice.xsd")) {
                logger.info(file.getName() + " processing...");
                createASCCP(new Context(file, moduleRepository).getRootElementDecl());
            }
        }
    }

    private void populate2() throws Exception {
        List<File> files = new ArrayList();
        for (File f : getBODs(f1)) {
            files.add(f);
        }
        for (File f : getBODs(f2)) {
            files.add(f);
        }

        for (File file : files) {
            if (!file.getName().equals("AcknowledgeInvoice.xsd") &&
                !file.getName().endsWith("IST.xsd")) {
                logger.info(file.getName() + " processing...");
                createASCCP(new Context(file, moduleRepository).getRootElementDecl());
            }
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void populateUnused() throws Exception {
        Collection<File> targetFiles = Arrays.asList(
                new File(SRTConstants.MODEL_FOLDER_PATH, "BODs"),
                new File(SRTConstants.MODEL_FOLDER_PATH, "Nouns"),
                new File(SRTConstants.MODEL_FOLDER_PATH, PLATFORM_PATH + "/BODs"),
                new File(SRTConstants.MODEL_FOLDER_PATH, PLATFORM_PATH + "/Nouns"),
                new File(SRTConstants.MODEL_FOLDER_PATH, PLATFORM_PATH + "/Common/Components"),
                new File(SRTConstants.MODEL_FOLDER_PATH, PLATFORM_PATH + "/Extension"));
        for (File file : targetFiles) {
            populateUnusedACC(file);
        }
        for (File file : targetFiles) {
            populateUnusedASCCP(file);
        }
    }

    private void populateUnusedACC(File file) throws Exception {
        if (file == null) {
            return;
        }
        if (file.getName().endsWith("IST.xsd")) {
            return;
        }

        if (file.isDirectory()) {
            for (File child : getBODs(file)) {
                populateUnusedACC(child);
            }
        } else {
            Document document = Context.loadDocument(file);
            NodeList complexTypes = (NodeList) Context.xPath.evaluate("//xsd:complexType", document, XPathConstants.NODESET);
            for (int i = 0, len = complexTypes.getLength(); i < len; ++i) {
                Element complexType = (Element) complexTypes.item(i);
                double cnt = (Double) Context.xPath.evaluate("count(./xsd:sequence) or count(./xsd:complexContent)",
                        complexType, XPathConstants.NUMBER);
                if (cnt != 1) {
                    continue;
                }
                String guid = complexType.getAttribute("id");
                if (accRepository.existsByGuid(guid)) {
                    continue;
                }

                String name = complexType.getAttribute("name");
                String module = Utility.extractModuleName(file.getAbsolutePath());
                logger.info("Found unused ACC name " + name + ", GUID " + guid + " from " + module);

                Context context = new Context(file, moduleRepository);

                XSComplexType xsComplexType = context.getXSComplexType(SRTConstants.OAGI_NS, name);
                TypeDecl typeDecl = new TypeDecl(context, xsComplexType, complexType);
                createACC(typeDecl);
            }
        }
    }

    private void populateUnusedASCCP(File file) throws Exception {
        if (file == null) {
            return;
        }
        if (file.getName().endsWith("IST.xsd")) {
            return;
        }

        if (file.isDirectory()) {
            for (File child : getBODs(file)) {
                populateUnusedASCCP(child);
            }
        } else {
            Document document = Context.loadDocument(file);
            NodeList elements = (NodeList) Context.xPath.evaluate("//xsd:element", document, XPathConstants.NODESET);
            NodeList childOfSchemaElements = (NodeList) Context.xPath.evaluate("/xsd:schema/xsd:element", document, XPathConstants.NODESET);
            for (int i = 0, len = elements.getLength(); i < len; ++i) {
                Element element = (Element) elements.item(i);
                String guid = element.getAttribute("id");
                if (StringUtils.isEmpty(guid)) {
                    continue;
                }
                if (asccpRepository.existsByGuid(guid)) {
                    continue;
                }
                String name = element.getAttribute("name");
                if (StringUtils.isEmpty(name)) {
                    continue;
                }

                String module = Utility.extractModuleName(file.getAbsolutePath());
                Context context = new Context(file, moduleRepository);

                XSElementDecl xsElementDecl = context.getXSElementDecl(SRTConstants.OAGI_NS, name);
                ElementDecl elementDecl = new ElementDecl(context, xsElementDecl, element);
                if (!elementDecl.canBeAsccp()) {
                    continue;
                }
                logger.info("Found unused ASCCP name " + name + ", GUID " + guid + " from " + module);

                boolean isChildOfSchema = isChildOfSchema(childOfSchemaElements, guid);
                if (isChildOfSchema) {
                    createASCCP(elementDecl, true);
                } else {
                    createASCCP(elementDecl, getReusableIndicator(elementDecl.getTypeDecl()));
                }
            }

            NodeList groups = (NodeList) Context.xPath.evaluate("//xsd:group", document, XPathConstants.NODESET);
            NodeList childOfSchemaGroups = (NodeList) Context.xPath.evaluate("/xsd:schema/xsd:group", document, XPathConstants.NODESET);

            for (int i = 0, len = groups.getLength(); i < len; ++i) {
                Element group = (Element) groups.item(i);
                String guid = group.getAttribute("id");
                if (StringUtils.isEmpty(guid)) {
                    continue;
                }
                if (asccpRepository.existsByGuid(guid)) {
                    continue;
                }
                String name = group.getAttribute("name");
                if (StringUtils.isEmpty(name)) {
                    continue;
                }

                String module = Utility.extractModuleName(file.getAbsolutePath());
                Context context = new Context(file, moduleRepository);

                XSModelGroupDecl xsModelGroupDecl = context.getXSModelGroupDecl(SRTConstants.OAGI_NS, name);
                GroupDecl groupDecl = new GroupDecl(context, xsModelGroupDecl, group);
                if (!groupDecl.canBeAsccp()) {
                    continue;
                }
                logger.info("Found unused ASCCP name " + name + ", GUID " + guid + " from " + module);

                boolean isChildOfSchema = isChildOfSchema(childOfSchemaGroups, guid);
                if (isChildOfSchema) {
                    createASCCP(groupDecl, true);
                } else {
                    createASCCP(groupDecl, getReusableIndicator(groupDecl));
                }
            }

        }
    }

    private boolean isChildOfSchema(NodeList childrenList, String guid) {
        boolean isChildOfSchema = false;
        for (int j = 0; j < childrenList.getLength(); j++) {
            Element checkGroup = (Element) childrenList.item(j);
            String checkGuid = checkGroup.getAttribute("id");
            if (checkGuid.equals(guid)) {
                isChildOfSchema = true;
                break;
            }
        }
        return isChildOfSchema;
    }

    private boolean getReusableIndicator(Declaration declaration) {
        double refCnt;
        String expression = "count(.//@ref)";
        try {
            refCnt = (Double) Context.xPath.evaluate(expression, declaration.getRawElement(), XPathConstants.NUMBER);
        } catch (XPathExpressionException e) {
            throw new IllegalStateException(expression + " doesn't support exception?", e);
        }
        return (refCnt == 0) ? false : true;
    }

    private File[] getBODs(File f) {
        return f.listFiles((dir, name) -> {
            return name.matches(".*.xsd");
        });
    }

    private AssociationCoreComponentProperty createASCCP(Declaration declaration) {
        return createASCCP(declaration, null, true);
    }

    private AssociationCoreComponentProperty createASCCP(Declaration declaration, boolean reusableIndicator) {
        return createASCCP(declaration, null, reusableIndicator);
    }

    AssociationCoreComponentProperty createASCCP(Declaration declaration,
                                                 AggregateCoreComponent acc, boolean reusableIndicator) {
        String asccpGuid = declaration.getId();
        String definition = declaration.getDefinition();
        Module module = declaration.getModule();
        String propertyTerm = Utility.spaceSeparator(declaration.getName());
        if (acc == null) {
            if (declaration.isGroup()) {
                acc = createACC(declaration);
            } else {
                acc = createACC(declaration.getTypeDecl());
            }
        }
        if (acc == null) {
            throw new IllegalStateException();
        }
        long roleOfAccId = acc.getAccId();

        String den;
        if (declaration.isGroup()) {
            den = propertyTerm + ". " + propertyTerm;
        } else {
            den = propertyTerm + ". " + Utility.spaceSeparator(Utility.first(acc.getDen()));
        }

        AssociationCoreComponentProperty asccp = asccpRepository.findOneByGuid(asccpGuid);
        if (asccp != null) {
            return asccp;
        }
        asccp = new AssociationCoreComponentProperty();
        asccp.setGuid(asccpGuid);
        asccp.setPropertyTerm(propertyTerm);
        asccp.setDefinition(definition);
        asccp.setRoleOfAccId(roleOfAccId);
        asccp.setDen(den);
        asccp.setState(3);
        asccp.setModule(module);
        asccp.setCreatedBy(importUtil.getUserId());
        asccp.setLastUpdatedBy(importUtil.getUserId());
        asccp.setOwnerUserId(importUtil.getUserId());
        asccp.setDeprecated(false);
        asccp.setNamespaceId(importUtil.getNamespaceId());
        asccp.setReleaseId(importUtil.getReleaseId());
        asccp.setReusableIndicator(reusableIndicator);
        asccp.setNillable(declaration.isNillable());
        asccpRepository.saveAndFlush(asccp);

        return asccp;
    }

    private AggregateCoreComponent createACC(Declaration declaration) {
        if (declaration == null || !declaration.canBeAcc()) {
            return null;
        }

        String typeGuid = declaration.getId();
        AggregateCoreComponent aggregateCoreComponent = accRepository.findAccIdAndDenByGuid(typeGuid);
        if (aggregateCoreComponent != null) {
            return aggregateCoreComponent;
        }

        AggregateCoreComponent acc = doCreateACC(declaration);
        int sequenceKey = 1;
        Collection<Declaration> particles = declaration.getParticles(particle -> createASCCP(particle, false));
        for (Declaration asccOrBccElement : particles) {
            String guid = asccOrBccElement.getId();
            Declaration refDecl = asccOrBccElement.getRefDecl();
            boolean isLocalElement = (refDecl == null);

            AssociationCoreComponentProperty asccp;
            if (asccOrBccElement instanceof AnyDecl) {
                asccp = asccpRepository.findAny();
            } else {
                asccp = asccpRepository.findOneByGuid(guid);
            }

            if (asccp != null) {
                Declaration particle = (isLocalElement) ? asccOrBccElement : refDecl;
                if (particle.canBeAscc()) {
                    createASCC(acc, asccp, particle, sequenceKey++);
                }
            } else {
                BasicCoreComponentProperty bccp = bccpRepository.findBccpIdAndDenByGuid(guid);
                if (bccp != null) {
                    Declaration particle = (isLocalElement) ? asccOrBccElement : refDecl;
                    createBCC(acc, bccp, particle, sequenceKey++);
                } else {
                    if (asccOrBccElement.canBeAscc()) {
                        if (createASCC(acc, asccOrBccElement, sequenceKey) != null) {
                            sequenceKey++;
                        }
                    } else {
                        createBCC(acc, asccOrBccElement, sequenceKey++, 1);
                    }
                }
            }
        }

        for (AttrDecl bccpAttr : declaration.getAttributes()) {
            BasicCoreComponentProperty bccp = getOrCreateBCCP(bccpAttr, true);
            createBCC(acc, bccp, bccpAttr, 0);
        }

        return acc;
    }

    AggregateCoreComponent doCreateACC(Declaration declaration, int oagisComponentType) {
        String name = declaration.getName();
        int idx = name.lastIndexOf("Type");
        String objectClassTerm = Utility.spaceSeparator((idx == -1) ? name : name.substring(0, idx));
        String den = objectClassTerm + ". Details";

        String definition = declaration.getDefinition();
        Module module = declaration.getModule();

        AggregateCoreComponent acc = new AggregateCoreComponent();
        String typeGuid = declaration.getId();
        {
            // Exceptional Case in 'Model/Nouns/CreditTransferIST.xsd'
            if ("Customer Credit Transfer Initiation V05".equals(objectClassTerm)) {
                typeGuid = Utility.generateGUID();
            }
        }
        acc.setGuid(typeGuid);
        acc.setObjectClassTerm(objectClassTerm);
        acc.setDen(den);
        acc.setDefinition(definition);

        AggregateCoreComponent basedAcc = null;
        if (declaration instanceof TypeDecl) {
            TypeDecl baseTypeDecl = ((TypeDecl) declaration).getBaseTypeDecl();
            basedAcc = createACC(baseTypeDecl);
        }
        if (basedAcc != null) {
            acc.setBasedAccId(basedAcc.getAccId());
        }

        acc.setOagisComponentType(oagisComponentType);
        acc.setCreatedBy(importUtil.getUserId());
        acc.setLastUpdatedBy(importUtil.getUserId());
        acc.setOwnerUserId(importUtil.getUserId());
        acc.setState(3);
        acc.setModule(module);
        acc.setDeprecated(false);
        if (declaration instanceof TypeDecl) {
            acc.setAbstract(((TypeDecl) declaration).isAbstract());
        }
        acc.setNamespaceId(importUtil.getNamespaceId());
        acc.setReleaseId(importUtil.getReleaseId());
        accRepository.saveAndFlush(acc);

        return acc;
    }

    private AggregateCoreComponent doCreateACC(Declaration declaration) {
        String name = declaration.getName();
        int idx = name.lastIndexOf("Type");
        String objectClassTerm = Utility.spaceSeparator((idx == -1) ? name : name.substring(0, idx));

        int oagisComponentType = 1;
        if (objectClassTerm.endsWith("Base")) {
            oagisComponentType = 0;
        } else if (objectClassTerm.endsWith("Extension") ||
                objectClassTerm.equals("Open User Area") ||
                objectClassTerm.equals("Any User Area") ||
                objectClassTerm.equals("All Extension")) {
            oagisComponentType = 2;
        } else if (objectClassTerm.endsWith("Group")) {
            oagisComponentType = 3;
        }

        return doCreateACC(declaration, oagisComponentType);
    }

    private AssociationCoreComponent createASCC(AggregateCoreComponent fromAcc,
                                                Declaration declaration, int sequenceKey) {
        AssociationCoreComponentProperty asccp = createASCCP(declaration.getRefDecl());
        return createASCC(fromAcc, asccp, declaration, sequenceKey);
    }

    private AssociationCoreComponent createASCC(AggregateCoreComponent fromAcc,
                                                AssociationCoreComponentProperty toAsccp,
                                                Declaration declaration, int sequenceKey) {
        String guid = declaration.getId();
        long fromAccId = fromAcc.getAccId();
        long toAsccpId = toAsccp.getAsccpId();

        AssociationCoreComponent ascc =
                asccRepository.findOneByGuidAndFromAccIdAndToAsccpId(guid, fromAccId, toAsccpId);
        if (ascc != null) {
            return ascc;
        }

        int cardinalityMin = declaration.getMinOccur();
        int cardinalityMax = declaration.getMaxOccur();

        String den = Utility.first(fromAcc.getDen()) + ". " + toAsccp.getDen();
        String definition = declaration.getDefinition();

        ascc = new AssociationCoreComponent();
        ascc.setGuid(guid);
        ascc.setCardinalityMin(cardinalityMin);
        ascc.setCardinalityMax(cardinalityMax);
        ascc.setSeqKey(sequenceKey);
        ascc.setFromAccId(fromAcc.getAccId());
        ascc.setToAsccpId(toAsccp.getAsccpId());
        ascc.setDen(den);
        ascc.setDefinition(definition);
        ascc.setState(3);
        ascc.setDeprecated(false);
        ascc.setReleaseId(importUtil.getReleaseId());
        ascc.setCreatedBy(importUtil.getUserId());
        ascc.setLastUpdatedBy(importUtil.getUserId());
        ascc.setOwnerUserId(importUtil.getUserId());
        asccRepository.saveAndFlush(ascc);

        return ascc;
    }

    private boolean createBCC(AggregateCoreComponent fromAcc,
                              Declaration declaration, int sequenceKey, int entityType) {
        BasicCoreComponentProperty bccp = getBCCP(declaration);
        return createBCC(fromAcc, bccp, declaration, sequenceKey, entityType);
    }

    private boolean createBCC(AggregateCoreComponent fromAcc,
                              BasicCoreComponentProperty toBccp,
                              Declaration declaration, int entityType) {
        return createBCC(fromAcc, toBccp, declaration, 0, entityType);
    }

    private boolean createBCC(AggregateCoreComponent fromAcc,
                              BasicCoreComponentProperty toBccp,
                              Declaration declaration, int sequenceKey, int entityType) {
        String guid = declaration.getId();
        long fromAccId = fromAcc.getAccId();
        long toBccpId = toBccp.getBccpId();

        BasicCoreComponent bcc =
                bccRepository.findOneByGuidAndFromAccIdAndToBccpId(guid, fromAccId, toBccpId);
        if (bcc != null) {
            return false;
        }

        int cardinalityMin = declaration.getMinOccur();
        int cardinalityMax = declaration.getMaxOccur();

        String den = Utility.first(fromAcc.getDen()) + ". " + toBccp.getDen();

        bcc = new BasicCoreComponent();
        bcc.setGuid(guid);
        bcc.setCardinalityMin(cardinalityMin);
        bcc.setCardinalityMax(cardinalityMax);
        bcc.setFromAccId(fromAcc.getAccId());
        bcc.setToBccpId(toBccp.getBccpId());
        if (sequenceKey > 0) {
            bcc.setSeqKey(sequenceKey);
        }
        bcc.setEntityType(entityType);
        bcc.setDen(den);
        bcc.setState(3);
        bcc.setDefinition(declaration.getDefinition());
        bcc.setDeprecated(false);
        bcc.setReleaseId(importUtil.getReleaseId());
        bcc.setCreatedBy(importUtil.getUserId());
        bcc.setLastUpdatedBy(importUtil.getUserId());
        bcc.setOwnerUserId(importUtil.getUserId());
        bcc.setNillable(declaration.isNillable());
        bcc.setDefaultValue(declaration.getDefaultValue());
        bccRepository.saveAndFlush(bcc);

        return true;
    }

    private BasicCoreComponentProperty getBCCP(Declaration declaration) {
        return getOrCreateBCCP(declaration, false);
    }

    private BasicCoreComponentProperty getOrCreateBCCP(Declaration declaration, boolean createIfAbsent) {
        Declaration targetDecl = declaration.getRefDecl();
        if (targetDecl == null) {
            targetDecl = declaration;
        }

        String name = targetDecl.getName();
        String propertyTerm = Utility.spaceSeparator(name);

        TypeDecl typeDecl = targetDecl.getTypeDecl();
        String typeGuid = typeDecl.getId();
        // Exceptional Cases
        {
            if (StringUtils.isEmpty(typeGuid)) {
                switch (name) {
                    case "schemeID":
                    case "schemeVersionID":
                        typeGuid = "oagis-id-d26b22f9103744edb0a4d3728aefc26e"; // NormalizedStringType
                        break;
                }
            }
        }

        DataType dt = dataTypeRepository.findOneByGuid(typeGuid);
        if (dt == null) {
            throw new IllegalStateException("Could not find DataType by guid: " + typeGuid + " for " + name);
        }

        long bdtId = dt.getDtId();

        BasicCoreComponentProperty bccp = bccpRepository.findBccpIdAndDenByPropertyTermAndBdtId(propertyTerm, bdtId);
        if (bccp != null) {
            return bccp;
        }

        if (createIfAbsent) {
            String representationTerm = dt.getDataTypeTerm();
            String den = Utility.firstToUpperCase(propertyTerm) + ". " + representationTerm;

            bccp = new BasicCoreComponentProperty();
            String bccpGuid = Utility.generateGUID();
            bccp.setGuid(bccpGuid);
            bccp.setPropertyTerm(propertyTerm);
            bccp.setBdtId(bdtId);
            bccp.setRepresentationTerm(representationTerm);
            bccp.setDen(den);
            bccp.setState(3);
            bccp.setCreatedBy(importUtil.getUserId());
            bccp.setLastUpdatedBy(importUtil.getUserId());
            bccp.setOwnerUserId(importUtil.getUserId());
            bccp.setDeprecated(false);
            bccp.setReleaseId(importUtil.getReleaseId());
            bccp.setNamespaceId(importUtil.getNamespaceId());
            Module module = declaration.getModule();
            bccp.setModule(module);
            bccp.setNillable(declaration.isNillable());
            bccp.setDefaultValue(declaration.getDefaultValue());
            bccpRepository.saveAndFlush(bccp);
            return bccp;
        } else {
            throw new IllegalStateException("Could not find BCCP by property term '" + propertyTerm + "' and type GUID " + typeGuid);
        }
    }
}