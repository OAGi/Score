package org.oagi.srt.web.jsf.beans.codelist;

import org.oagi.srt.repository.entity.CodeList;
import org.oagi.srt.repository.entity.CodeListValue;
import org.oagi.srt.service.CodeListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
public class CodeListCreateFromAnotherBean extends CodeListBaseBean {

    @Autowired
    private CodeListService codeListService;

    private CodeList basedCodeList;

    @PostConstruct
    public void init() {
        Long codeListId = Long.parseLong(
                FacesContext.getCurrentInstance().getExternalContext()
                        .getRequestParameterMap().get("codeListId"));
        setBasedCodeList(codeListService.findOne(codeListId));
    }

    public CodeList getBasedCodeList() {
        return basedCodeList;
    }

    public void setBasedCodeList(CodeList basedCodeList) {
        this.basedCodeList = basedCodeList;
        CodeList codeList = new CodeList();
        codeList.setName(basedCodeList.getName());
        codeList.setListId(basedCodeList.getListId());
        codeList.setAgencyId(basedCodeList.getAgencyId());
        codeList.setVersionId(basedCodeList.getVersionId());
        codeList.setDefinition(basedCodeList.getDefinition());
        codeList.setDefinitionSource(basedCodeList.getDefinitionSource());
        codeList.setRemark(basedCodeList.getRemark());
        codeList.setBasedCodeListId(basedCodeList.getCodeListId());
        codeList.setExtensibleIndicator(basedCodeList.isExtensibleIndicator());
        setCodeList(codeList);

        setCodeListValues(
                codeListService.findByCodeList(basedCodeList).stream()
                        .map(e -> {
                            CodeListValue copy = new CodeListValue();
                            copy.setValue(e.getValue());
                            copy.setName(e.getName());
                            copy.setDefinition(e.getDefinition());
                            copy.setDefinitionSource(e.getDefinitionSource());
                            copy.setColor(e.getColor());
                            return copy;
                        }).collect(Collectors.toList())
        );
    }
}
