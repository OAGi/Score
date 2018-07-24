package org.oagi.srt.web.jsf.component.tooltip;

import org.primefaces.component.tooltip.Tooltip;
import org.primefaces.expression.SearchExpressionFacade;
import org.primefaces.renderkit.CoreRenderer;
import org.primefaces.util.ComponentUtils;
import org.primefaces.util.WidgetBuilder;

import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import java.io.IOException;

public class SRTTooltipRenderer extends CoreRenderer {

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        Tooltip tooltip = (Tooltip) component;
        String tooltipFor = tooltip.getFor();
        String index = null;
        int idx0 = tooltipFor.indexOf('[');
        int idx1 = tooltipFor.indexOf(']');
        if (idx0 != -1 && idx1 != -1) {
            index = tooltipFor.substring(idx0 + 1, idx1);
            tooltipFor = tooltipFor.substring(0, idx0);
        }
        String target = SearchExpressionFacade.resolveClientIds(
                context, component, tooltipFor);

        if (index != null) {
            target += UINamingContainer.getSeparatorChar(context) + index;
        }
        encodeMarkup(context, tooltip, target);
        encodeScript(context, tooltip, target);
    }

    protected void encodeMarkup(FacesContext context, Tooltip tooltip, String target) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        if (target != null) {
            String styleClass = tooltip.getStyleClass();
            styleClass = styleClass == null ? Tooltip.CONTAINER_CLASS : Tooltip.CONTAINER_CLASS + " " + styleClass;
            styleClass = styleClass + " ui-tooltip-" + tooltip.getPosition();

            writer.startElement("div", tooltip);
            writer.writeAttribute("id", tooltip.getClientId(context), null);
            writer.writeAttribute("class", styleClass, "styleClass");

            if (tooltip.getStyle() != null)
                writer.writeAttribute("style", tooltip.getStyle(), "style");

            writer.startElement("div", tooltip);
            writer.writeAttribute("class", "ui-tooltip-arrow", null);
            writer.endElement("div");

            writer.startElement("div", tooltip);
            writer.writeAttribute("class", "ui-tooltip-text ui-shadow ui-corner-all", null);

            if (tooltip.getChildCount() > 0) {
                renderChildren(context, tooltip);
            } else {
                String valueToRender = ComponentUtils.getValueToRender(context, tooltip);
                if (valueToRender != null) {
                    if (tooltip.isEscape())
                        writer.writeText(valueToRender, "value");
                    else
                        writer.write(valueToRender);
                }
            }

            writer.endElement("div");


            writer.endElement("div");
        }
    }

    protected void encodeScript(FacesContext context, Tooltip tooltip, String target) throws IOException {
        String clientId = tooltip.getClientId(context);
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.initWithDomReady("Tooltip", tooltip.resolveWidgetVar(), clientId)
                .attr("showEvent", tooltip.getShowEvent(), null)
                .attr("hideEvent", tooltip.getHideEvent(), null)
                .attr("showEffect", tooltip.getShowEffect(), null)
                .attr("hideEffect", tooltip.getHideEffect(), null)
                .attr("showDelay", tooltip.getShowDelay(), 150)
                .attr("hideDelay", tooltip.getHideDelay(), 0)
                .attr("target", target, null)
                .attr("globalSelector", tooltip.getGlobalSelector(), null)
                .attr("escape", tooltip.isEscape(), true)
                .attr("trackMouse", tooltip.isTrackMouse(), false)
                .attr("position", tooltip.getPosition(), "right")
                .returnCallback("beforeShow", "function()", tooltip.getBeforeShow())
                .callback("onShow", "function()", tooltip.getOnShow())
                .callback("onHide", "function()", tooltip.getOnHide());

        wb.finish();
    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        //Rendering happens on encodeEnd
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }
}
