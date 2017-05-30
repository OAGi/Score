package org.oagi.srt.web.jsf.component.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.application.Resource;
import javax.faces.application.ResourceWrapper;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SRTResource extends ResourceWrapper {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Resource wrapped;
    private String version;

    public SRTResource(final Resource resource) {
        super();
        wrapped = resource;
        try {
            Path path = Paths.get(resource.getURL().toURI());
            version = "&v=" + path.toFile().lastModified();
        } catch (URISyntaxException e) {
            logger.warn("Fail to retrieve a resource file information: " + resource, e);
        }
    }

    @Override
    public Resource getWrapped() {
        return wrapped;
    }

    @Override
    public String getRequestPath() {
        return super.getRequestPath() + ((version != null) ? version : "");
    }

    @Override
    public String getContentType() {
        return getWrapped().getContentType();
    }

    @Override
    public String getLibraryName() {
        return getWrapped().getLibraryName();
    }

    @Override
    public String getResourceName() {
        return getWrapped().getResourceName();
    }

    @Override
    public void setContentType(final String contentType) {
        getWrapped().setContentType(contentType);
    }

    @Override
    public void setLibraryName(final String libraryName) {
        getWrapped().setLibraryName(libraryName);
    }

    @Override
    public void setResourceName(final String resourceName) {
        getWrapped().setResourceName(resourceName);
    }

    @Override
    public String toString() {
        return getWrapped().toString();
    }
}
