package org.oagi.srt.web.jsf.component;

import org.primefaces.component.api.Pageable;
import org.primefaces.component.api.UIData;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.datatable.DataTableRenderer;
import org.primefaces.component.paginator.PaginatorElementRenderer;
import org.primefaces.util.ComponentUtils;
import org.primefaces.util.WidgetBuilder;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import java.io.IOException;

public class SRTDataTableRenderer extends DataTableRenderer {

    private DataRenderer dataRenderer = new DataRenderer();

    public static void addPaginatorElement(String element, PaginatorElementRenderer renderer) {
        DataRenderer.addPaginatorElement(element, renderer);
    }

    public static PaginatorElementRenderer removePaginatorElement(String element) {
        return DataRenderer.removePaginatorElement(element);
    }

    protected void encodeMarkup(FacesContext context, DataTable table) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = table.getClientId(context);
        boolean scrollable = table.isScrollable();
        boolean hasPaginator = table.isPaginator();
        String style = table.getStyle();
        String paginatorPosition = table.getPaginatorPosition();
        int frozenColumns = table.getFrozenColumns();
        boolean hasFrozenColumns = (frozenColumns != 0);
        boolean hasData = table.getRowCount() > 0 && table.getPageCount() > 1;

        //style class
        String containerClass = scrollable ? DataTable.CONTAINER_CLASS + " " + DataTable.SCROLLABLE_CONTAINER_CLASS : DataTable.CONTAINER_CLASS;
        containerClass = table.getStyleClass() != null ? containerClass + " " + table.getStyleClass() : containerClass;
        if (table.isResizableColumns()) containerClass = containerClass + " " + DataTable.RESIZABLE_CONTAINER_CLASS;
        if (table.isStickyHeader()) containerClass = containerClass + " " + DataTable.STICKY_HEADER_CLASS;
        if (ComponentUtils.isRTL(context, table)) containerClass = containerClass + " " + DataTable.RTL_CLASS;
        if (table.isReflow()) containerClass = containerClass + " " + DataTable.REFLOW_CLASS;
        if (hasFrozenColumns) containerClass = containerClass + " ui-datatable-frozencolumn";

        writer.startElement("div", table);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute("class", containerClass, "styleClass");
        if (style != null) {
            writer.writeAttribute("style", style, "style");
        }

        if (table.isReflow()) {
            encodeSortableHeaderOnReflow(context, table);
        }

        encodeFacet(context, table, table.getHeader(), DataTable.HEADER_CLASS);

        if (hasPaginator && !paginatorPosition.equalsIgnoreCase("bottom") && hasData) {
            encodePaginatorMarkup(context, table, "top");
        }

        if (scrollable) {
            encodeScrollableTable(context, table);
        } else {
            encodeRegularTable(context, table);
        }

        if (hasPaginator && !paginatorPosition.equalsIgnoreCase("top") && hasData) {
            encodePaginatorMarkup(context, table, "bottom");
        }

        encodeFacet(context, table, table.getFooter(), DataTable.FOOTER_CLASS);

        if (table.isSelectionEnabled()) {
            encodeStateHolder(context, table, table.getClientId(context) + "_selection", table.getSelectedRowKeysAsString());
        }

        if (table.isDraggableColumns()) {
            encodeStateHolder(context, table, table.getClientId(context) + "_columnOrder", null);
        }

        if (scrollable) {
            encodeStateHolder(context, table, table.getClientId(context) + "_scrollState", table.getScrollState());
        }

        writer.endElement("div");
    }

    @Override
    public void encodePaginatorMarkup(FacesContext context, Pageable pageable, String position) throws IOException {
        dataRenderer.encodePaginatorMarkup(context, pageable, position);
    }

    @Override
    public void encodePaginatorConfig(FacesContext context, Pageable pageable, WidgetBuilder wb) throws IOException {
        dataRenderer.encodePaginatorConfig(context, pageable, wb);
    }

    @Override
    public void encodeFacet(FacesContext context, UIData data, String facet, String styleClass) throws IOException {
        dataRenderer.encodeFacet(context, data, facet, styleClass);
    }
}
