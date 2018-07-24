package org.oagi.srt.web.jsf.component.checkbox;

import org.primefaces.component.selectmanycheckbox.SelectManyCheckbox;
import org.primefaces.util.WidgetBuilder;

import javax.faces.component.UIInput;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.util.List;

public class SRTSelectManyCheckboxRenderer extends org.primefaces.component.selectmanycheckbox.SelectManyCheckboxRenderer {

    @Override
    protected void encodeScript(FacesContext context, SelectManyCheckbox checkbox) throws IOException {
        String clientId = checkbox.getClientId(context);
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("SelectManyCheckbox", checkbox.resolveWidgetVar(), clientId).finish();
    }

    @Override
    protected void encodeMarkup(FacesContext context, SelectManyCheckbox checkbox) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = checkbox.getClientId(context);
        List<SelectItem> selectItems = getSelectItems(context, checkbox);
        String style = checkbox.getStyle();
        String styleClass = checkbox.getStyleClass();

        writer.startElement("div", checkbox);
        writer.writeAttribute("id", clientId, "id");

        if (style != null) writer.writeAttribute("style", style, "style");
        if (styleClass != null) writer.writeAttribute("class", styleClass, "styleClass");

        if (selectItems != null && !selectItems.isEmpty()) {
            Converter converter = checkbox.getConverter();
            Object values = getValues(checkbox);
            Object submittedValues = getSubmittedValues(checkbox);

            int idx = 0;
            for (SelectItem selectItem : selectItems) {
                encodeOption(context, checkbox, values, submittedValues, converter, selectItem, idx);
                idx++;
            }
        }

        writer.endElement("div");
    }

    @Override
    protected void encodeOption(FacesContext context, UIInput component, Object values, Object submittedValues, Converter converter, SelectItem option, int idx) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        SelectManyCheckbox checkbox = (SelectManyCheckbox) component;
        String itemValueAsString = getOptionAsString(context, component, converter, option.getValue());
        String name = checkbox.getClientId(context);
        String id = name + UINamingContainer.getSeparatorChar(context) + idx;
        boolean disabled = option.isDisabled() || checkbox.isDisabled();

        Object valuesArray;
        Object itemValue;
        if (submittedValues != null) {
            valuesArray = submittedValues;
            itemValue = itemValueAsString;
        } else {
            valuesArray = values;
            itemValue = option.getValue();
        }

        boolean selected = isSelected(context, component, itemValue, valuesArray, converter);
        if (option.isNoSelectionOption() && values != null && !selected) {
            return;
        }

        writer.startElement("input", null);
        writer.writeAttribute("id", id, null);
        writer.writeAttribute("name", name, null);
        writer.writeAttribute("type", "checkbox", null);
        writer.writeAttribute("value", itemValueAsString, null);

        renderOnchange(context, checkbox);
        renderDynamicPassThruAttributes(context, checkbox);

        if (checkbox.getTabindex() != null) writer.writeAttribute("tabindex", checkbox.getTabindex(), null);
        if (selected) writer.writeAttribute("checked", "checked", null);
        if (disabled) writer.writeAttribute("disabled", "disabled", null);

        writer.endElement("input");

        //label
        writer.startElement("label", null);
        writer.writeAttribute("id", id + ":label", null);
        writer.writeAttribute("for", id, null);

        if (option.isEscape())
            writer.writeText(option.getLabel(), null);
        else
            writer.write(option.getLabel());

        writer.endElement("label");
    }
}