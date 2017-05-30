package org.oagi.srt.web.jsf.component.autocomplete;

import org.primefaces.component.autocomplete.AutoComplete;
import org.primefaces.component.autocomplete.AutoCompleteRenderer;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import java.io.IOException;

public class SRTAutoCompleteRenderer extends AutoCompleteRenderer {

    protected void encodeDropDown(FacesContext context, AutoComplete ac) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String dropdownClass = AutoComplete.DROPDOWN_CLASS;
        boolean disabled = ac.isDisabled()||ac.isReadonly();
        if(disabled) {
            dropdownClass += " ui-state-disabled";
        }

        writer.startElement("button", ac);
        writer.writeAttribute("class", dropdownClass, null);
        writer.writeAttribute("type", "button", null);
        if(disabled) {
            writer.writeAttribute("disabled", "disabled", null);
        }
        if(ac.getTabindex() != null) {
            writer.writeAttribute("tabindex", ac.getTabindex(), null);
        }

        writer.startElement("span", null);
        writer.writeAttribute("class", "ui-button-icon-primary fa fa-white fa-caret-down", null);
        writer.endElement("span");

        writer.startElement("span", null);
        writer.writeAttribute("class", "ui-button-text", null);
        writer.write("&nbsp;");
        writer.endElement("span");


        writer.endElement("button");
    }

}
