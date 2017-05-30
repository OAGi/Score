package org.oagi.srt.web.jsf.component.resource;

import org.primefaces.application.resource.PrimeResource;
import org.primefaces.application.resource.PrimeResourceHandler;
import org.primefaces.util.Constants;

import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;

public class SRTResourceHandler extends PrimeResourceHandler {

    public SRTResourceHandler(ResourceHandler wrapped) {
        super(wrapped);
    }

    @Override
    public Resource createResource(String resourceName, String libraryName) {
        Resource resource = getWrapped().createResource(resourceName, libraryName);
        if (resource != null) {
            if (resource instanceof PrimeResource) {
                return resource;
            }
            if (libraryName != null) {
                if (libraryName.equalsIgnoreCase(Constants.LIBRARY)) {
                    return new PrimeResource(resource);
                }
            }
            if (resourceName != null) {
                if (resourceName.startsWith("srt")) {
                    return new SRTResource(resource);
                }
            }
        }

        return resource;
    }
}
