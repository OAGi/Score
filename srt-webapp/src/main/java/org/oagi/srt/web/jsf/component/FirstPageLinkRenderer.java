package org.oagi.srt.web.jsf.component;

import org.primefaces.component.api.Pageable;
import org.primefaces.component.api.UIData;
import org.primefaces.component.paginator.PaginatorElementRenderer;
import org.primefaces.util.MessageFactory;

import javax.faces.context.FacesContext;
import java.io.IOException;

public class FirstPageLinkRenderer extends PageLinkRenderer implements PaginatorElementRenderer {

    @Override
    public void render(FacesContext context, Pageable pageable) throws IOException {
        int currentPage = pageable.getPage();
        int pageCount = pageable.getPageCount();

        boolean disabled = (currentPage == (pageCount - 1)) || (currentPage == 0 && pageCount == 0);

        String ariaMessage = MessageFactory.getMessage(UIData.ARIA_FIRST_PAGE_LABEL, new Object[]{});

        super.render(context, pageable, UIData.PAGINATOR_FIRST_PAGE_LINK_CLASS, UIData.PAGINATOR_FIRST_PAGE_ICON_CLASS, disabled, ariaMessage, "First");
    }
}
