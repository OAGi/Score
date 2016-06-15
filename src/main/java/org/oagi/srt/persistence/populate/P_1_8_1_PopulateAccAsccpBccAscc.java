package org.oagi.srt.persistence.populate;

import com.sun.xml.internal.xsom.*;
import com.sun.xml.internal.xsom.parser.XSOMParser;
import org.oagi.srt.Application;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.OAGiNamespaceContext;
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
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Locator;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.*;

@Component
public class P_1_8_1_PopulateAccAsccpBccAscc {

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

    private File f1 = new File(SRTConstants.BOD_FILE_PATH_01);
    private File f2 = new File(SRTConstants.BOD_FILE_PATH_02);

    private Map<String, Document> documentMap = new HashMap();

    private Document loadDocument(String uri) {
        String module = Utility.extractModuleName(uri);

        Document xmlDocument;
        if (!documentMap.containsKey(module)) {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            try {
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                try (InputStream inputStream = new URI(uri).toURL().openStream()) {
                    xmlDocument = builder.parse(inputStream);
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            documentMap.put(module, xmlDocument);
        } else {
            xmlDocument = documentMap.get(module);
        }

        return xmlDocument;
    }

    private XPath xPath;

    @PostConstruct
    public void init() throws Exception {
        userId = userRepository.findAppUserIdByLoginId("oagis");
        releaseId = releaseRepository.findReleaseIdByReleaseNum("10.1");
        namespaceId = namespaceRepository.findNamespaceIdByUri("http://www.openapplications.org/oagis/10");

        xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new OAGiNamespaceContext());
    }

    public static void main(String[] args) throws Exception {
        try (AbstractApplicationContext ctx = (AbstractApplicationContext)
                SpringApplication.run(Application.class, args);) {
            P_1_8_1_PopulateAccAsccpBccAscc populateAccAsccpBccAscc =
                    ctx.getBean(P_1_8_1_PopulateAccAsccpBccAscc.class);
            populateAccAsccpBccAscc.run(ctx);
        }
    }

    public void run(ApplicationContext applicationContext) throws Exception {
        System.out.println("### 1.8 Start");

        populate();
        populateUnused();

        System.out.println("### 1.8 End");
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
                System.out.println(file.getName() + " processing...");
                createASCCP(new Context(file).getRootElementDecl());
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
                System.out.println(file.getName() + " processing...");
                createASCCP(new Context(file).getRootElementDecl());
            }
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void populateUnused() throws Exception {
        Collection<File> targetFiles = Arrays.asList(
                new File(SRTConstants.MODEL_FOLDER_PATH, "BODs"),
                new File(SRTConstants.MODEL_FOLDER_PATH, "Nouns"),
                new File(SRTConstants.MODEL_FOLDER_PATH, "Platform/2_1/BODs"),
                new File(SRTConstants.MODEL_FOLDER_PATH, "Platform/2_1/Nouns"),
                new File(SRTConstants.MODEL_FOLDER_PATH, "Platform/2_1/Common/Components/Components.xsd"),
                new File(SRTConstants.MODEL_FOLDER_PATH, "Platform/2_1/Common/Components/Meta.xsd"),
                new File(SRTConstants.MODEL_FOLDER_PATH, "Platform/2_1/Extension/Extensions.xsd"));
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
            Document document = loadDocument(file.toURI().toString());
            NodeList complexTypes = (NodeList) xPath.evaluate("//xsd:complexType", document, XPathConstants.NODESET);
            for (int i = 0, len = complexTypes.getLength(); i < len; ++i) {
                Element complexType = (Element) complexTypes.item(i);
                double cnt = (Double) xPath.evaluate("count(./sequence) or count(./xsd:complexContent)",
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

                Context context = new Context(file);

                XSComplexType xsComplexType = context.xsSchemaSet.getComplexType(SRTConstants.OAGI_NS, name);
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
            Document document = loadDocument(file.toURI().toString());
            NodeList elements = (NodeList) xPath.evaluate("//xsd:element", document, XPathConstants.NODESET);
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
                Context context = new Context(file);

                XSElementDecl xsElementDecl = context.xsSchemaSet.getElementDecl(SRTConstants.OAGI_NS, name);
                ElementDecl elementDecl = new ElementDecl(context, xsElementDecl, element);
                if (!elementDecl.canBeAscc()) {
                    continue;
                }
                logger.info("Found unused ASCCP name " + name + ", GUID " + guid + " from " + module);

                double refCnt = (Double) xPath.evaluate("count(./@ref)", element, XPathConstants.NUMBER);
                boolean reusableIndicator = (refCnt == 0) ? false : true;

                createASCCP(elementDecl, reusableIndicator);
            }
        }
    }

    private File[] getBODs(File f) {
        return f.listFiles((dir, name) -> {
            return name.matches(".*.xsd");
        });
    }

    private class Context {
        private XSSchemaSet xsSchemaSet;
        private File file;

        public Context(File file) throws Exception {
            XSOMParser xsomParser = new XSOMParser(SAXParserFactory.newInstance());
            xsomParser.parse(file);

            xsSchemaSet = xsomParser.getResult();

            this.file = file;
        }

        private Document loadDocument(Locator locator) {
            if (locator == null) {
                return null;
            }
            String systemId = locator.getSystemId();
            return P_1_8_1_PopulateAccAsccpBccAscc.this.loadDocument(systemId);
        }

        public ElementDecl getRootElementDecl() throws Exception {
            String fileName = file.getName();
            String rootElementName = fileName.substring(0, fileName.indexOf(".xsd"));
            XSElementDecl xsElementDecl =
                    xsSchemaSet.getElementDecl(SRTConstants.OAGI_NS, rootElementName);

            Document document = loadDocument(xsElementDecl.getLocator());
            Element element = (Element)
                    xPath.evaluate("//xsd:element[@name='" + rootElementName + "']", document, XPathConstants.NODE);

            return new ElementDecl(this, xsElementDecl, element);
        }

        public String evaluate(String expression, Object item) {
            try {
                return xPath.evaluate(expression, item);
            } catch (XPathExpressionException e) {
                throw new IllegalArgumentException(e);
            }
        }

        public Element evaluateElement(String expression, Node item) {
            try {
                return (Element) xPath.evaluate(expression, item, XPathConstants.NODE);
            } catch (XPathExpressionException e) {
                throw new IllegalArgumentException(e);
            }
        }

        public Element evaluateElement(String expression, XSComponent xsComponent) {
            Document document = loadDocument(xsComponent.getLocator());
            return evaluateElement(expression, document);
        }

        public NodeList evaluateNodeList(String expression, Node item) {
            try {
                return (NodeList) xPath.evaluate(expression, item, XPathConstants.NODESET);
            } catch (XPathExpressionException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    private interface Declaration {
        public String getName();
        public String getId();
        public String getDefinition();
        public String getModule();
        public int getMinOccur();
        public int getMaxOccur();

        public boolean isGroup();

        public boolean hasRefDecl();
        public void setRefDecl(Declaration reference);
        public Declaration getRefDecl();

        public boolean hasTypeDecl();
        public void setTypeDecl(TypeDecl reference);
        public TypeDecl getTypeDecl();

        public boolean canBeAcc();
        public boolean canBeAscc();

        public Collection<Declaration> getParticles();
        public Collection<AttrDecl> getAttributes();
    }

    private abstract class AbstractDeclaration implements Declaration {
        protected Context context;
        protected XSDeclaration xsDeclaration;
        private Element element;

        private Declaration reference;
        private TypeDecl type;
        private Transformer transformer;
        private final int INDENT_AMOUNT = 2;

        public AbstractDeclaration(Context context, XSDeclaration xsDeclaration, Element element) {
            if (context == null) {
                throw new IllegalArgumentException("'context' paremeter must not be null.");
            }
            if (xsDeclaration == null) {
                throw new IllegalArgumentException("'xsDeclaration' paremeter must not be null.");
            }
            if (element == null) {
                throw new IllegalArgumentException("'element' paremeter must not be null.");
            }

            this.context = context;
            this.xsDeclaration = xsDeclaration;
            this.element = element;

            TransformerFactory transFactory = TransformerFactory.newInstance();
            try {
                transformer = transFactory.newTransformer();
            } catch (TransformerConfigurationException e) {
                throw new IllegalStateException(e);
            }
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(INDENT_AMOUNT));
        }

        @Override
        public boolean hasRefDecl() {
            return (reference != null);
        }

        @Override
        public Declaration getRefDecl() {
            return reference;
        }

        @Override
        public void setRefDecl(Declaration reference) {
            this.reference = reference;
        }

        @Override
        public boolean hasTypeDecl() {
            return (type != null);
        }

        @Override
        public TypeDecl getTypeDecl() {
            return type;
        }

        @Override
        public void setTypeDecl(TypeDecl type) {
            this.type = type;
        }

        public String getId() {
            return this.element.getAttribute("id");
        }

        public String getName() {
            return this.xsDeclaration.getName();
        }

        public String getDefinition() {
            Element element = context.evaluateElement(
                    "./xsd:annotation/xsd:documentation", this.element);
            if (element != null) {
                NodeList nodeList = context.evaluateNodeList("//text()[normalize-space()='']", element);
                for (int i = 0; i < nodeList.getLength(); ++i) {
                    Node node = nodeList.item(i);
                    node.getParentNode().removeChild(node);
                }

                try {
                    StringWriter buffer = new StringWriter();
                    transformer.transform(new DOMSource(element), new StreamResult(buffer));
                    String definition = buffer.toString();
                    definition = arrangeIndent(removeOAGiNamepsace(removeDocumentationNode(definition)));
                    return (!StringUtils.isEmpty(definition)) ? definition.trim() : null;
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
            return null;
        }

        private String removeDocumentationNode(String s) {
            if (StringUtils.isEmpty(s)) {
                return null;
            }
            int sIdx = s.indexOf('>');
            int eIdx = s.lastIndexOf("</xsd:documentation>");
            if (eIdx == -1) {
                return null;
            }
            return s.substring(sIdx + 1, eIdx);
        }

        private String removeOAGiNamepsace(String s) {
            if (StringUtils.isEmpty(s)) {
                return null;
            }
            return s.replaceAll(" xmlns=\"" + SRTConstants.OAGI_NS + "\">", ">");
        }

        private String arrangeIndent(String s) {
            if (StringUtils.isEmpty(s)) {
                return null;
            }
            String regex = "";
            for (int i = 0; i < INDENT_AMOUNT; ++i) {
                regex += " ";
            }
            return s.replaceAll(regex + "<", "<");
        }

        public int getMinOccur() {
            String minOccurs = element.getAttribute("minOccurs");
            return (!StringUtils.isEmpty(minOccurs)) ?
                    Integer.valueOf(minOccurs) : 1;
        }

        public int getMaxOccur() {
            String maxOccurs = element.getAttribute("maxOccurs");
            return (!StringUtils.isEmpty(maxOccurs)) ?
                    ("unbounded".equals(maxOccurs)) ? -1 : Integer.valueOf(maxOccurs) : 1;
        }

        @Override
        public boolean isGroup() {
            return false;
        }

        @Override
        public boolean canBeAcc() {
            return false;
        }

        @Override
        public boolean canBeAscc() {
            return false;
        }

        @Override
        public Collection<Declaration> getParticles() {
            return Collections.emptyList();
        }

        @Override
        public Collection<AttrDecl> getAttributes() {
            return Collections.emptyList();
        }

        @Override
        public String getModule() {
            String systemId = xsDeclaration.getLocator().getSystemId();
            return Utility.extractModuleName(systemId);
        }

        protected Collection<XSDeclaration> asXSDeclarations(XSTerm xsTerm) {
            if (xsTerm == null) {
                return Collections.emptyList();
            }
            if (xsTerm.isModelGroup()) {
                List<XSDeclaration> xsParticles = new ArrayList();
                for (XSParticle child : xsTerm.asModelGroup().getChildren()) {
                    xsParticles.addAll(asXSDeclarations(child.getTerm()));
                }
                return xsParticles;
            } else if (xsTerm.isElementDecl()) {
                return Arrays.asList(xsTerm.asElementDecl());
            } else if (xsTerm.isModelGroupDecl()) {
                return Arrays.asList(xsTerm.asModelGroupDecl());
            } else {
                return Collections.emptyList();
            }
        }

        protected Collection<Declaration> getParticles(XSTerm xsTerm) {
            Collection<XSDeclaration> xsDeclarations = asXSDeclarations(xsTerm);
            if (xsDeclarations.isEmpty()) {
                return Collections.emptyList();
            }

            List<Declaration> particles = new ArrayList();
            for (XSDeclaration xsDeclaration : xsDeclarations) {
                boolean isGroup = (xsDeclaration instanceof XSModelGroupDecl);
                String elementName = xsDeclaration.getName();
                Declaration particle = null;
                String expression;
                if (isGroup) {
                    expression = ".//xsd:group[@ref='" + elementName + "']";
                    Element particleElement = context.evaluateElement(expression, this.element);
                    if (particleElement != null) {
                        particle = new GroupDecl(context, xsDeclaration, particleElement);
                    }
                } else {
                    expression = ".//xsd:element[@ref='" + elementName + "']";
                    Element particleElement = context.evaluateElement(expression, this.element);
                    if (particleElement != null) {
                        particle = new ElementDecl(context, (XSElementDecl) xsDeclaration, particleElement);
                    }
                }

                boolean isLocalElement = (particle == null);
                if (isLocalElement) {
                    if (isGroup) {
                        particle = new GroupDecl(context, xsDeclaration, this.element);
                    } else {
                        expression = ".//xsd:element[@name='" + elementName + "']";
                        Element particleElement = context.evaluateElement(expression, this.element);
                        particle = new ElementDecl(context, (XSElementDecl) xsDeclaration, particleElement);
                    }

                    createASCCP(particle, false);
                }

                if (!isLocalElement) {
                    Declaration reference;
                    if (isGroup) {
                        expression = "//xsd:group[@name='" + elementName + "']";
                        XSDeclaration xsReference =
                                context.xsSchemaSet.getModelGroupDecl(SRTConstants.OAGI_NS, elementName);
                        if (xsReference == null) {
                            throw new IllegalStateException("Could not find XSDeclaration named '" + elementName + "'");
                        }
                        Element referenceElement = context.evaluateElement(expression, xsReference);
                        reference = new GroupDecl(context, xsReference, referenceElement);
                    } else {
                        expression = "//xsd:element[@name='" + elementName + "']";
                        XSDeclaration xsReference =
                                context.xsSchemaSet.getElementDecl(SRTConstants.OAGI_NS, elementName);
                        if (xsReference == null) {
                            throw new IllegalStateException("Could not find XSDeclaration named '" + elementName + "'");
                        }
                        Element referenceElement = context.evaluateElement(expression, xsReference);
                        reference = new ElementDecl(context, (XSElementDecl) xsReference, referenceElement);
                    }

                    particle.setRefDecl(reference);
                }

                particles.add(particle);
            }
            return particles;
        }
    }

    private class ElementDecl extends AbstractDeclaration {

        public ElementDecl(Context context, XSElementDecl xsElementDecl, Element element) {
            super(context, xsElementDecl, element);
            setTypeDecl(xsElementDecl);
        }

        private void setTypeDecl(XSElementDecl xsElementDecl) {
            XSType xsType = xsElementDecl.getType();
            String typeName = xsType.getName();
            String expression = null;
            if (typeName.endsWith("Group")) {
                expression = "//xsd:group[@name='" + xsType.getName() + "']";
            } else if (xsType.isComplexType()) {
                expression = "//xsd:complexType[@name='" + xsType.getName() + "']";
            } else if (xsType.isSimpleType()) {
                expression = "//xsd:simpleType[@name='" + xsType.getName() + "']";
            } else {
                return;
            }

            Element element = context.evaluateElement(expression, xsType);
            if (element == null) {
                return;
            }

            TypeDecl typeDecl = new TypeDecl(context, xsType, element);
            setTypeDecl(typeDecl);
        }

        @Override
        public boolean canBeAcc() {
            return getTypeDecl().canBeAcc();
        }

        @Override
        public boolean canBeAscc() {
            return getTypeDecl().canBeAscc();
        }
    }

    private class GroupDecl extends AbstractDeclaration {

        public GroupDecl(Context context, XSDeclaration declaration, Element element) {
            super(context, declaration, element);
        }

        @Override
        public boolean isGroup() {
            return true;
        }

        @Override
        public boolean canBeAcc() {
            return getRefDecl() == null;
        }

        @Override
        public boolean canBeAscc() {
            return getRefDecl() != null;
        }

        @Override
        public Collection<Declaration> getParticles() {
            return getParticles(((XSTerm) xsDeclaration).asModelGroupDecl().getModelGroup());
        }
    }

    public class TypeDecl extends AbstractDeclaration {
        private XSType xsType;
        private Element element;

        public TypeDecl(Context context, XSType xsType, Element element) {
            super(context, xsType, element);
            this.xsType = xsType;
            this.element = element;
        }

        public boolean isAbstract() {
            if (xsType.isComplexType()) {
                return xsType.asComplexType().isAbstract();
            }
            return false;
        }

        public boolean isComplexType() {
            return xsType.isComplexType();
        }

        public boolean hasSimpleContent() {
            return Integer.valueOf(context.evaluate("count(.//xsd:simpleContent)", this.element)) > 0;
        }

        public boolean isGroupElement() {
            return element.getNodeName().equals("group") && !StringUtils.isEmpty(element.getAttribute("name"));
        }

        @Override
        public boolean canBeAcc() {
            return (isComplexType() && !hasSimpleContent()) || isGroupElement();
        }

        @Override
        public boolean canBeAscc() {
            return (isComplexType() && !hasSimpleContent());
        }

        public TypeDecl getBaseTypeDecl() {
            XSType baseType = this.xsType.getBaseType();
            if (baseType == null) {
                return null;
            }

            String baseTypeName = baseType.getName();
            if ("anyType".equals(baseTypeName)) {
                return null;
            }

            String expression;
            if (baseType.isComplexType()) {
                expression = "//xsd:complexType[@name='" + baseTypeName + "']";
            } else if (baseType.isSimpleType()) {
                expression = "//xsd:simpleType[@name='" + baseTypeName + "']";
            } else {
                return null;
            }
            Element element = context.evaluateElement(expression, baseType);
            if (element == null) {
                return null;
            }
            return new TypeDecl(context, baseType, element);
        }

        public Collection<Declaration> getParticles() {
            XSParticle xsParticle;
            if (xsType.isComplexType()) {
                XSContentType xsContentType = xsType.asComplexType().getExplicitContent();
                if (xsContentType == null) {
                    xsContentType = xsType.asComplexType().getContentType();
                }
                xsParticle = xsContentType.asParticle();
            } else if (xsType.isSimpleType()) {
                xsParticle = xsType.asSimpleType().asParticle();
            } else {
                throw new IllegalStateException();
            }

            if (xsParticle == null) {
                return Collections.emptyList();
            }
            XSTerm xsTerm = xsParticle.getTerm();
            return getParticles(xsTerm);
        }

        public Collection<AttrDecl> getAttributes() {
            if (xsType.isComplexType()) {
                Collection<? extends XSAttributeUse> declaredAttributeUses =
                        xsType.asComplexType().getDeclaredAttributeUses();
                if (declaredAttributeUses.isEmpty()) {
                    return Collections.emptyList();
                }

                List<AttrDecl> attrDecls = new ArrayList();
                for (XSAttributeUse xsAttributeUse : declaredAttributeUses) {
                    XSAttributeDecl xsAttributeDecl = xsAttributeUse.getDecl();
                    String expression = "./xsd:attribute[@name='" + xsAttributeUse.getDecl().getName() + "']";
                    Element element = context.evaluateElement(expression, this.element);
                    if (element == null) {
                        continue;
                    }
                    attrDecls.add(new AttrDecl(context, xsAttributeDecl, element));
                }

                return attrDecls;
            } else {
                return Collections.emptyList();
            }
        }
    }

    private class AttrDecl extends AbstractDeclaration {
        private XSAttributeDecl xsAttributeDecl;
        private String use;

        public AttrDecl(Context context, XSAttributeDecl xsAttributeDecl, Element element) {
            super(context, xsAttributeDecl, element);

            this.xsAttributeDecl = xsAttributeDecl;
            this.use = element.getAttribute("use");
        }

        @Override
        public int getMinOccur() {
            if (isOptional()) {
                return 0;
            }
            if (isRequired()) {
                return 1;
            }
            if (isProhibited()) {
                return 0;
            }
            return 1;
        }

        @Override
        public int getMaxOccur() {
            if (isOptional()) {
                return 1;
            }
            if (isRequired()) {
                return 1;
            }
            if (isProhibited()) {
                return 0;
            }
            return 1;
        }

        public String getUse() {
            return use;
        }

        public boolean isRequired() {
            return "required".equals(use);
        }

        public boolean isOptional() {
            return "optional".equals(use);
        }

        public boolean isProhibited() {
            return "prohibited".equals(use);
        }

        public TypeDecl getTypeDecl() {
            XSSimpleType xsType = xsAttributeDecl.getType();
            return new TypeDecl(context, xsType,
                    context.evaluateElement("//xsd:simpleType[@name='" + xsType.getName() + "']", xsType));
        }
    }

    private AssociationCoreComponentProperty createASCCP(Declaration declaration) {
        return createASCCP(declaration, null, true);
    }

    private AssociationCoreComponentProperty createASCCP(Declaration declaration, boolean reusableIndicator) {
        return createASCCP(declaration, null, reusableIndicator);
    }

    private AssociationCoreComponentProperty createASCCP(Declaration declaration, AggregateCoreComponent acc, boolean reusableIndicator) {
        String asccpGuid = declaration.getId();
        String definition = declaration.getDefinition();
        String module = declaration.getModule();
        String propertyTerm = Utility.spaceSeparator(declaration.getName());
        if (acc == null) {
            if (declaration.isGroup()) {
                acc = getOrCreateACC(declaration);
            } else {
                acc = getOrCreateACC(declaration.getTypeDecl());
            }
        }
        if (acc == null) {
            throw new IllegalStateException();
        }
        int roleOfAccId = acc.getAccId();

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
        asccp.setCreatedBy(userId);
        asccp.setLastUpdatedBy(userId);
        asccp.setOwnerUserId(userId);
        asccp.setDeprecated(false);
        asccp.setNamespaceId(namespaceId);
        asccp.setReleaseId(releaseId);
        asccp.setReusableIndicator(reusableIndicator);
        asccpRepository.saveAndFlush(asccp);

        return asccp;
    }

    private AggregateCoreComponent getOrCreateACC(Declaration declaration) {
        if (declaration == null || !declaration.canBeAcc()) {
            return null;
        }

        String typeGuid = declaration.getId();
        AggregateCoreComponent aggregateCoreComponent = accRepository.findAccIdAndDenByGuid(typeGuid);
        if (aggregateCoreComponent != null) {
            return aggregateCoreComponent;
        }

        AggregateCoreComponent acc = createACC(declaration);
        int sequenceKey = 1;
        Collection<Declaration> particles = declaration.getParticles();
        for (Declaration asccOrBccElement : particles) {
            String guid = asccOrBccElement.getId();
            Declaration refDecl = asccOrBccElement.getRefDecl();
            boolean isLocalElement = (refDecl == null);

            AssociationCoreComponentProperty asccp = asccpRepository.findOneByGuid(guid);
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
            BasicCoreComponentProperty bccp = getOrCreateBCCP(bccpAttr);
            createBCC(acc, bccp, bccpAttr, 0);
        }

        return acc;
    }

    private AggregateCoreComponent createACC(Declaration declaration) {
        String name = declaration.getName();
        int idx = name.lastIndexOf("Type");
        String objectClassTerm = Utility.spaceSeparator((idx == -1) ? name : name.substring(0, idx));
        String den = objectClassTerm + ". Details";

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

        String definition = declaration.getDefinition();
        String module = declaration.getModule();

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
            basedAcc = getOrCreateACC(baseTypeDecl);
        }
        if (basedAcc != null) {
            acc.setBasedAccId(basedAcc.getAccId());
        }

        acc.setOagisComponentType(oagisComponentType);
        acc.setCreatedBy(userId);
        acc.setLastUpdatedBy(userId);
        acc.setOwnerUserId(userId);
        acc.setState(3);
        acc.setModule(module);
        acc.setDeprecated(false);
        if (declaration instanceof TypeDecl) {
            acc.setAbstract(((TypeDecl) declaration).isAbstract());
        }
        acc.setNamespaceId(namespaceId);
        acc.setReleaseId(releaseId);
        accRepository.saveAndFlush(acc);

        return acc;
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
        int fromAccId = fromAcc.getAccId();
        int toAsccpId = toAsccp.getAsccpId();

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
        ascc.setReleaseId(releaseId);
        ascc.setCreatedBy(userId);
        ascc.setLastUpdatedBy(userId);
        ascc.setOwnerUserId(userId);
        asccRepository.saveAndFlush(ascc);

        return ascc;
    }

    private boolean createBCC(AggregateCoreComponent fromAcc,
                              Declaration declaration, int sequenceKey, int entityType) {
        BasicCoreComponentProperty bccp = getOrCreateBCCP(declaration);
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
        int fromAccId = fromAcc.getAccId();
        int toBccpId = toBccp.getBccpId();

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
        bcc.setReleaseId(releaseId);
        bcc.setCreatedBy(userId);
        bcc.setLastUpdatedBy(userId);
        bcc.setOwnerUserId(userId);
        bccRepository.saveAndFlush(bcc);

        return true;
    }

    private BasicCoreComponentProperty getOrCreateBCCP(Declaration declaration) {
        Declaration targetDecl = declaration.getRefDecl();
        if (targetDecl == null) {
            targetDecl = declaration;
        }

        String name = targetDecl.getName();
        String propertyTerm = Utility.spaceSeparator(name).replace("ID", "Identifier");

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

        int bdtId = dt.getDtId();

        BasicCoreComponentProperty bccp = bccpRepository.findBccpIdAndDenByPropertyTermAndBdtId(propertyTerm, bdtId);
        if (bccp != null) {
            return bccp;
        }

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
        bccp.setCreatedBy(userId);
        bccp.setLastUpdatedBy(userId);
        bccp.setOwnerUserId(userId);
        bccp.setDeprecated(false);
        bccp.setReleaseId(releaseId);
        bccpRepository.saveAndFlush(bccp);
        return bccp;
    }

}