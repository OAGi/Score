package org.oagi.srt.web.jsf.beans.codelist;

import org.oagi.srt.repository.entity.CodeList;
import org.oagi.srt.service.CodeListService;
import org.oagi.srt.web.handler.UIHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
public class CodeListBean extends UIHandler {

    @Autowired
    private CodeListService codeListService;

    private String basedCodeListName;
    private List<CodeList> allCodeLists;
    private List<CodeList> codeLists = new ArrayList();

    @PostConstruct
    public void init() {
        setCodeLists(allCodeLists());
    }

    public List<CodeList> allCodeLists() {
        allCodeLists = codeListService.findAll(Sort.Direction.ASC, "name");
        return allCodeLists;
    }

    public List<CodeList> getCodeLists() {
        return codeLists;
    }

    public void setCodeLists(List<CodeList> codeLists) {
        this.codeLists = codeLists;
    }

    public String getBasedCodeListName() {
        return basedCodeListName;
    }

    public void setBasedCodeListName(String basedCodeListName) {
        this.basedCodeListName = basedCodeListName;
    }

    public List<String> completeInput(String query) {
        return allCodeLists().stream()
                .map(e -> e.getName())
                .distinct()
                .filter(s -> s.toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    public void search() {
        String basedCodeListName = getBasedCodeListName();
        if (StringUtils.isEmpty(basedCodeListName)) {
            setCodeLists(allCodeLists());
        } else {
            setCodeLists(
                    allCodeLists().stream()
                            .filter(e -> e.getName().toLowerCase().contains(basedCodeListName.toLowerCase()))
                            .collect(Collectors.toList())
            );
        }
    }

}
