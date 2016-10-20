package org.oagi.srt.web.jsf.beans.codelist;

import org.oagi.srt.repository.entity.CodeList;
import org.oagi.srt.repository.entity.CodeListState;
import org.oagi.srt.service.CodeListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
public class CodeListCreateFromAnotherSelectionBean extends CodeListBaseBean {

    @Autowired
    private CodeListService codeListService;

    private List<CodeList> basedCodeLists;

    @PostConstruct
    public void init() {
        basedCodeLists = codeListService.findAll().stream()
                .filter(e -> e.getState() == CodeListState.Published && e.isExtensibleIndicator())
                .collect(Collectors.toList());
    }

    public List<CodeList> getBasedCodeLists() {
        return basedCodeLists;
    }

    public void setBasedCodeLists(List<CodeList> basedCodeLists) {
        this.basedCodeLists = basedCodeLists;
    }
}
