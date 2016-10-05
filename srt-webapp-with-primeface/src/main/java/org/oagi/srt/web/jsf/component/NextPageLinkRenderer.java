package org.oagi.srt.web.jsf.component;

import org.primefaces.component.api.UIData;
import org.primefaces.component.paginator.PaginatorElementRenderer;
import org.primefaces.util.MessageFactory;

import javax.faces.context.FacesContext;
import java.io.IOException;

public class NextPageLinkRenderer extends PageLinkRenderer implements PaginatorElementRenderer {

    @Override
    public void render(FacesContext context, UIData uidata) throws IOException {
        int currentPage = uidata.getPage();
        int pageCount = uidata.getPageCount();

        boolean disabled = (currentPage == (pageCount - 1)) || (currentPage == 0 && pageCount == 0);

        String ariaMessage = MessageFactory.getMessage(UIData.ARIA_NEXT_PAGE_LABEL, new Object[]{});

        super.render(context, uidata, UIData.PAGINATOR_NEXT_PAGE_LINK_CLASS, UIData.PAGINATOR_NEXT_PAGE_ICON_CLASS, disabled, ariaMessage, "Next");
    }
}
