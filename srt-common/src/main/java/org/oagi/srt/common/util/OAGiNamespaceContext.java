package org.oagi.srt.common.util;

import javax.xml.namespace.NamespaceContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.oagi.srt.common.SRTConstants.*;

public class OAGiNamespaceContext implements NamespaceContext {

    public String getNamespaceURI(String prefix) {
        switch (prefix) {
            case NS_CCTS_PREFIX:
                return NS_CCTS;
            case NS_XSD_PREFIX:
                return NS_XSD;
            case "":
            default:
                return OAGI_NS;
        }
    }

    @Override
    public String getPrefix(String namespaceURI) {
        switch (namespaceURI) {
            case NS_XSD:
                return NS_XSD_PREFIX;
            case NS_CCTS:
                return NS_CCTS_PREFIX;
            case OAGI_NS:
            default:
                return "";
        }
    }

    @Override
    public Iterator getPrefixes(String namespaceURI) {
        List<String> prefixes;
        switch (namespaceURI) {
            case NS_XSD:
                prefixes = Arrays.asList(NS_XSD_PREFIX, NS_XS_PREFIX);
                break;
            case NS_CCTS:
                prefixes = Arrays.asList(NS_CCTS_PREFIX);
                break;
            case OAGI_NS:
            case "":
            default:
                prefixes = Arrays.asList("");
        }
        return prefixes.iterator();
    }
}
