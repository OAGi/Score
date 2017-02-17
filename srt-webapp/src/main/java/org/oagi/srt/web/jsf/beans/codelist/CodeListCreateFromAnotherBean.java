package org.oagi.srt.web.jsf.beans.codelist;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.entity.CodeList;
import org.oagi.srt.repository.entity.CodeListState;
import org.oagi.srt.repository.entity.CodeListValue;
import org.oagi.srt.service.CodeListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.util.stream.Collectors;

import static org.oagi.srt.repository.entity.CodeListValue.Color.*;

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
        String codeListIdStr = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("codeListId");
        if (StringUtils.isEmpty(codeListIdStr)) {
            throw new IllegalAccessError();
        }

        Long codeListId = Long.parseLong(codeListIdStr);
        CodeList codeList = codeListService.findOne(codeListId);
        if (codeList == null) {
            throw new IllegalAccessError();
        }
        CodeListState codeListState = codeList.getState();
        if (CodeListState.Published != codeListState) {
            throw new IllegalAccessError();
        }

        setBasedCodeList(codeList);
    }

    public CodeList getBasedCodeList() {
        return basedCodeList;
    }

    public void setBasedCodeList(CodeList basedCodeList) {
        this.basedCodeList = basedCodeList;
        CodeList codeList = new CodeList();
        codeList.setName(basedCodeList.getName() + "_Extension");
        codeList.setListId(Utility.generateGUID());
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
                        .filter(e -> BrightRed != e.getColor() && DullRed != e.getColor())
                        .map(e -> {
                            CodeListValue copy = new CodeListValue();
                            copy.setValue(e.getValue());
                            copy.setName(e.getName());
                            copy.setDefinition(e.getDefinition());
                            copy.setDefinitionSource(e.getDefinitionSource());
                            CodeListValue.Color color = e.getColor();

                            if (Green == color) {
                                copy.setColor(Blue);
                            } else {
                                copy.setColor(color);
                            }

                            return copy;
                        }).collect(Collectors.toList())
        );
    }
}
