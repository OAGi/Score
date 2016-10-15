package org.oagi.srt.web.jsf.component;

import org.primefaces.component.api.UIData;
import org.primefaces.component.paginator.PaginatorElementRenderer;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import java.io.IOException;

public class PageLinksRenderer implements PaginatorElementRenderer {

    public void render(FacesContext context, UIData uidata) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        int currentPage = uidata.getPage();
        int pageLinks = uidata.getPageLinks();
        int pageCount = uidata.getPageCount();
        int visiblePages = Math.min(pageLinks, pageCount);

        //calculate range, keep current in middle if necessary
        int start = Math.max(0, (int) Math.ceil(currentPage - ((visiblePages) / 2)));
        int end = Math.min(pageCount - 1, start + visiblePages - 1);

        //check when approaching to last page
        int delta = pageLinks - (end - start + 1);
        start = Math.max(0, start - delta);

        writer.startElement("span", null);
        writer.writeAttribute("class", UIData.PAGINATOR_PAGES_CLASS, null);

        for(int i = start; i <= end; i++){
            String styleClass = currentPage == i ? UIData.PAGINATOR_ACTIVE_PAGE_CLASS : UIData.PAGINATOR_PAGE_CLASS;

            writer.startElement("a", null);
            writer.writeAttribute("class", styleClass, null);
            writer.writeAttribute("tabindex", 0, null);
            writer.writeAttribute("href", "#", null);
            writer.writeText((i + 1), null);
            writer.endElement("a");
        }

        writer.endElement("span");
    }
}
