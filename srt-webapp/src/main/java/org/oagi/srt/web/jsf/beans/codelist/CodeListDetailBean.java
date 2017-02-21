package org.oagi.srt.web.jsf.beans.codelist;

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
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
public class CodeListDetailBean extends CodeListBaseBean {

    @Autowired
    private CodeListService codeListService;

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
        if (CodeListState.Deleted == codeListState) {
            throw new IllegalAccessError();
        }

        setCodeList(codeList);

        List<CodeListValue> codeListValues = codeListService.findByCodeList(codeList);
        if (CodeListState.Published == codeListState) {
            codeListValues = codeListValues.stream()
                    .filter(e -> CodeListValue.Color.BrightRed != e.getColor())
                    .collect(Collectors.toList());
        }
        setCodeListValues(codeListValues);
    }
}
