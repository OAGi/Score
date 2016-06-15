package org.oagi.srt.persistence.populate.helper;

import com.sun.xml.internal.xsom.XSDeclaration;
import com.sun.xml.internal.xsom.XSTerm;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import java.util.Collection;

public class GroupDecl extends AbstractDeclaration {

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
        return canBeAcc();
    }

    @Override
    public boolean canBeAsccp() {
        return !StringUtils.isEmpty(getName());
    }

    @Override
    public Collection<Declaration> getParticles(ParticleAction particleAction) {
        return getParticles(((XSTerm) xsDeclaration).asModelGroupDecl().getModelGroup(), particleAction);
    }
}
