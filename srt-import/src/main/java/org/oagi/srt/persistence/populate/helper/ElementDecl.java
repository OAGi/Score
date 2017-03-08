package org.oagi.srt.persistence.populate.helper;

import com.sun.xml.internal.xsom.XSElementDecl;
import com.sun.xml.internal.xsom.XSType;
import org.w3c.dom.Element;

public class ElementDecl extends AbstractDeclaration {

    public ElementDecl(Context context, XSElementDecl xsElementDecl, Element element) {
        super(context, xsElementDecl, element);
        setTypeDecl(xsElementDecl);
    }

    private void setTypeDecl(XSElementDecl xsElementDecl) {
        XSType xsType = xsElementDecl.getType();
        String typeName = xsType.getName();
        if (typeName == null) {
            throw new IllegalStateException();
        }
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

    @Override
    public boolean canBeAsccp() {
        return getTypeDecl().canBeAsccp();
    }
}
