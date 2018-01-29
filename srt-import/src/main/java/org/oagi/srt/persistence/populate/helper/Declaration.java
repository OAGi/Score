package org.oagi.srt.persistence.populate.helper;

import org.oagi.srt.repository.entity.Module;
import org.w3c.dom.Element;

import java.io.File;
import java.util.Collection;

public interface Declaration {
    public String getName();
    public String getId();
    public String getDefinition();
    public String getDefinitionSource();
    public Module getModule();
    public File getModuleAsFile();
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
    public boolean canBeAsccp();

    public Collection<Declaration> getParticles(ParticleAction particleAction);
    public Collection<AttrDecl> getAttributes();

    public boolean isNillable();
    public String getDefaultValue();
    public Element getRawElement();
}
