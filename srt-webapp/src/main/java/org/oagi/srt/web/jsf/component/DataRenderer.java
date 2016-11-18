package org.oagi.srt.web.jsf.component;

import org.primefaces.component.api.UIData;
import org.primefaces.component.paginator.CurrentPageReportRenderer;
import org.primefaces.component.paginator.JumpToPageDropdownRenderer;
import org.primefaces.component.paginator.PaginatorElementRenderer;
import org.primefaces.component.paginator.RowsPerPageDropdownRenderer;
import org.primefaces.renderkit.CoreRenderer;
import org.primefaces.util.MessageFactory;
import org.primefaces.util.WidgetBuilder;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DataRenderer extends CoreRenderer {

    private static Map<String, PaginatorElementRenderer> PAGINATOR_ELEMENTS;

    static {
        PAGINATOR_ELEMENTS = new HashMap<String, PaginatorElementRenderer>();
        PAGINATOR_ELEMENTS.put("{CurrentPageReport}", new CurrentPageReportRenderer());
        PAGINATOR_ELEMENTS.put("{FirstPageLink}", new org.oagi.srt.web.jsf.component.FirstPageLinkRenderer());
        PAGINATOR_ELEMENTS.put("{PreviousPageLink}", new org.oagi.srt.web.jsf.component.PrevPageLinkRenderer());
        PAGINATOR_ELEMENTS.put("{NextPageLink}", new org.oagi.srt.web.jsf.component.NextPageLinkRenderer());
        PAGINATOR_ELEMENTS.put("{LastPageLink}", new org.oagi.srt.web.jsf.component.LastPageLinkRenderer());
        PAGINATOR_ELEMENTS.put("{PageLinks}", new org.oagi.srt.web.jsf.component.PageLinksRenderer());
        PAGINATOR_ELEMENTS.put("{RowsPerPageDropdown}", new RowsPerPageDropdownRenderer());
        PAGINATOR_ELEMENTS.put("{JumpToPageDropdown}", new JumpToPageDropdownRenderer());
    }

    public static void addPaginatorElement(String element, PaginatorElementRenderer renderer) {
        PAGINATOR_ELEMENTS.put(element, renderer);
    }

    public static PaginatorElementRenderer removePaginatorElement(String element) {
        return PAGINATOR_ELEMENTS.remove(element);
    }

    protected void encodePaginatorMarkup(FacesContext context, UIData uidata, String position) throws IOException {
        if (!uidata.isPaginatorAlwaysVisible() && uidata.getPageCount() <= 1) {
            return;
        }

        ResponseWriter writer = context.getResponseWriter();
        boolean isTop = position.equals("top");

        String styleClass = isTop ? UIData.PAGINATOR_TOP_CONTAINER_CLASS : UIData.PAGINATOR_BOTTOM_CONTAINER_CLASS;
        String id = uidata.getClientId(context) + "_paginator_" + position;

        //add corners
        if (!isTop && uidata.getFooter() == null) {
            styleClass = styleClass + " ui-corner-bottom";
        } else if (isTop && uidata.getHeader() == null) {
            styleClass = styleClass + " ui-corner-top";
        }

        String ariaMessage = MessageFactory.getMessage(UIData.ARIA_HEADER_LABEL, new Object[]{});

        writer.startElement("div", null);
        writer.writeAttribute("id", id, null);
        writer.writeAttribute("class", styleClass, null);
        writer.writeAttribute("role", "navigation", null);
        writer.writeAttribute("aria-label", ariaMessage, null);

        String[] elements = uidata.getPaginatorTemplate().split(" ");
        for (String element : elements) {
            PaginatorElementRenderer renderer = PAGINATOR_ELEMENTS.get(element);
            if (renderer != null) {
                renderer.render(context, uidata);
            } else {
                UIComponent elementFacet = uidata.getFacet(element);
                if (elementFacet != null)
                    elementFacet.encodeAll(context);
                else
                    writer.write(element + " ");
            }
        }

        writer.endElement("div");
    }

    protected void encodePaginatorConfig(FacesContext context, UIData uidata, WidgetBuilder wb) throws IOException {
        String clientId = uidata.getClientId(context);
        String paginatorPosition = uidata.getPaginatorPosition();
        String paginatorContainers = null;
        String currentPageTemplate = uidata.getCurrentPageReportTemplate();

        if (paginatorPosition.equalsIgnoreCase("both"))
            paginatorContainers = "'" + clientId + "_paginator_top','" + clientId + "_paginator_bottom'";
        else
            paginatorContainers = "'" + clientId + "_paginator_" + paginatorPosition + "'";

        wb.append(",paginator:{")
                .append("id:[").append(paginatorContainers).append("]")
                .append(",rows:").append(uidata.getRows())
                .append(",rowCount:").append(uidata.getRowCount())
                .append(",page:").append(uidata.getPage());

        if (currentPageTemplate != null)
            wb.append(",currentPageTemplate:'").append(currentPageTemplate).append("'");

        if (uidata.getPageLinks() != 10)
            wb.append(",pageLinks:").append(uidata.getPageLinks());

        if (!uidata.isPaginatorAlwaysVisible())
            wb.append(",alwaysVisible:false");

        wb.append("}");
    }

    public void encodeFacet(FacesContext context, UIData data, String facet, String styleClass) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        UIComponent component = data.getFacet(facet);

        if (component != null && component.isRendered()) {
            writer.startElement("div", null);
            writer.writeAttribute("class", styleClass, null);
            component.encodeAll(context);
            writer.endElement("div");
        }
    }
}
