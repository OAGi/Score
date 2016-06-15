package org.oagi.srt.persistence.populate.helper;

import com.sun.xml.internal.xsom.*;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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

    public boolean isSimpleType() {
        return xsType.isSimpleType();
    }

    public boolean hasSimpleContent() {
        return Integer.valueOf(context.evaluate("count(./xsd:simpleContent)", this.element)) > 0;
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

    public Collection<Declaration> getParticles(ParticleAction particleAction) {
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
        return getParticles(xsTerm, particleAction);
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
