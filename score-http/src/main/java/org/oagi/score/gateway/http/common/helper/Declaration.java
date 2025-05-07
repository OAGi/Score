package org.oagi.score.gateway.http.common.helper;

import org.w3c.dom.Element;

import java.io.File;

public interface Declaration {
    public String getName();
    public String getId();
    public String getDefinition();
    public String getDefinitionSource();
    public File getModuleAsFile();
    public int getMinOccur();
    public int getMaxOccur();

    public boolean isGroup();

    public boolean hasRefDecl();
    public void setRefDecl(Declaration reference);
    public Declaration getRefDecl();


    public boolean isNillable();
    public String getDefaultValue();
    public Element getRawElement();
}
