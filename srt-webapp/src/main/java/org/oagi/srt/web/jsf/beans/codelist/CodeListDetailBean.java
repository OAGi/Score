package org.oagi.srt.web.jsf.beans.codelist;

import org.oagi.srt.repository.entity.CodeList;
import org.oagi.srt.service.CodeListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
public class CodeListDetailBean extends CodeListBaseBean {

    @Autowired
    private CodeListService codeListService;

    @PostConstruct
    public void init() {
        Long codeListId = Long.parseLong(
                FacesContext.getCurrentInstance().getExternalContext()
                        .getRequestParameterMap().get("codeListId"));
        CodeList codeList = codeListService.findOne(codeListId);
        setCodeList(codeList);
        setCodeListValues(codeListService.findByCodeList(codeList));
    }
}
