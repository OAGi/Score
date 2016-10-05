package org.oagi.srt.web.jsf.component;

import org.primefaces.component.api.DynamicColumn;
import org.primefaces.component.api.UIColumn;
import org.primefaces.component.api.UIData;
import org.primefaces.component.column.Column;
import org.primefaces.component.columngroup.ColumnGroup;
import org.primefaces.component.columns.Columns;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.datatable.DataTableRenderer;
import org.primefaces.component.datatable.feature.DataTableFeature;
import org.primefaces.component.datatable.feature.DataTableFeatureKey;
import org.primefaces.component.datatable.feature.RowExpandFeature;
import org.primefaces.component.datatable.feature.SortFeature;
import org.primefaces.component.paginator.PaginatorElementRenderer;
import org.primefaces.component.row.Row;
import org.primefaces.component.subtable.SubTable;
import org.primefaces.component.summaryrow.SummaryRow;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;
import org.primefaces.util.*;

import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UIPanel;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class SRTDataTableRenderer extends DataTableRenderer {

    private DataRenderer dataRenderer = new DataRenderer();

    public static void addPaginatorElement(String element, PaginatorElementRenderer renderer) {
        DataRenderer.addPaginatorElement(element, renderer);
    }

    public static PaginatorElementRenderer removePaginatorElement(String element) {
        return DataRenderer.removePaginatorElement(element);
    }

    @Override
    public void encodePaginatorMarkup(FacesContext context, UIData uidata, String position) throws IOException {
        dataRenderer.encodePaginatorMarkup(context, uidata, position);
    }

    @Override
    public void encodePaginatorConfig(FacesContext context, UIData uidata, WidgetBuilder wb) throws IOException {
        dataRenderer.encodePaginatorConfig(context, uidata, wb);
    }

    @Override
    public void encodeFacet(FacesContext context, UIData data, String facet, String styleClass) throws IOException {
        dataRenderer.encodeFacet(context, data, facet, styleClass);
    }
}
