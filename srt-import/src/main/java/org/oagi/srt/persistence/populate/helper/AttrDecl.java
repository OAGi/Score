package org.oagi.srt.persistence.populate.helper;

import com.sun.xml.internal.xsom.XSAttributeDecl;
import com.sun.xml.internal.xsom.XSSimpleType;
import org.w3c.dom.Element;

public class AttrDecl extends AbstractDeclaration {
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
        return 0;
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
