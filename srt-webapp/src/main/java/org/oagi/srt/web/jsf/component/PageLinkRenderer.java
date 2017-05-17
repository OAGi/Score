package org.oagi.srt.web.jsf.component;

import org.primefaces.component.api.Pageable;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import java.io.IOException;

public class PageLinkRenderer {

    public void render(FacesContext context, Pageable pageable, String linkClass, String iconClass, boolean disabled, String ariaLabel, String text) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String styleClass = disabled ? linkClass + " ui-state-disabled" : linkClass;

        writer.startElement("a", null);
        writer.writeAttribute("href", "#", null);
        writer.writeAttribute("class", styleClass, null);
        writer.writeAttribute("aria-label", ariaLabel, null);
        if (!disabled) {
            writer.writeAttribute("tabindex", 0, null);
        }

        writer.writeText(text, null);

        writer.endElement("a");
    }
}
