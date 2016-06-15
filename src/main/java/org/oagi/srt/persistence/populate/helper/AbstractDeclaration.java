package org.oagi.srt.persistence.populate.helper;

import com.sun.xml.internal.xsom.*;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.StringWriter;
import java.net.URI;
import java.util.*;

public abstract class AbstractDeclaration implements Declaration {
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
    public boolean canBeAsccp() {
        return false;
    }

    @Override
    public Collection<Declaration> getParticles(ParticleAction particleAction) {
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

    @Override
    public File getModuleAsFile() {
        String systemId = xsDeclaration.getLocator().getSystemId();
        try {
            return new File(new URI(systemId).toURL().getFile());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
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

    protected Collection<Declaration> getParticles(XSTerm xsTerm, ParticleAction particleAction) {
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

                if (particleAction != null) {
                    particleAction.runWhenParticleIsLocalElement(particle);
                }
            }

            if (!isLocalElement) {
                Declaration reference;
                if (isGroup) {
                    expression = "//xsd:group[@name='" + elementName + "']";
                    XSDeclaration xsReference =
                            context.getModelGroupDecl(SRTConstants.OAGI_NS, elementName);
                    if (xsReference == null) {
                        throw new IllegalStateException("Could not find XSDeclaration named '" + elementName + "'");
                    }
                    Element referenceElement = context.evaluateElement(expression, xsReference);
                    reference = new GroupDecl(context, xsReference, referenceElement);
                } else {
                    expression = "//xsd:element[@name='" + elementName + "']";
                    XSDeclaration xsReference =
                            context.getElementDecl(SRTConstants.OAGI_NS, elementName);
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
