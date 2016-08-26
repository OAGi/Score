package org.oagi.srt.common.util;

import org.oagi.srt.common.SRTConstants;

import javax.xml.namespace.NamespaceContext;
import java.util.Iterator;

public class OAGiNamespaceContext implements NamespaceContext {

	public String getNamespaceURI(String prefix) {
    	if(prefix.equals(SRTConstants.NS_CCTS_PREFIX))
    		return SRTConstants.NS_CCTS; 
    	else if(prefix.equals(SRTConstants.NS_XSD_PREFIX))
    		return SRTConstants.NS_XSD; 
    	else 
    		return null; 
    }

    @Override
    public String getPrefix(String namespaceURI) {
        return null;
    }

    @Override
    public Iterator getPrefixes(String namespaceURI) {
        return null;
    }
}
