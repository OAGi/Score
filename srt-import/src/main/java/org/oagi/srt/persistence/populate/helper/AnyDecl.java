package org.oagi.srt.persistence.populate.helper;

import com.sun.xml.internal.xsom.XSWildcard;
import org.w3c.dom.Element;

public class AnyDecl extends AbstractDeclaration {

    public AnyDecl(Context context, XSWildcard xsWildcard, Element element) {
        super(context, xsWildcard, element);
    }

    @Override
    public boolean canBeAscc() {
        return true;
    }

}
