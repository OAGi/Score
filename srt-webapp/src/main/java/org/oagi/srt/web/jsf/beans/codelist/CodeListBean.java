package org.oagi.srt.web.jsf.beans.codelist;

import org.oagi.srt.repository.AgencyIdListValueRepository;
import org.oagi.srt.repository.entity.AgencyIdListValue;
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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
public class CodeListBean extends UIHandler {

    @Autowired
    private CodeListService codeListService;

    @Autowired
    private AgencyIdListValueRepository agencyIdListValueRepository;

    private String codeListName;
    private List<CodeList> allCodeLists;
    private Map<Long, CodeList> allCodeListMap;
    private List<CodeList> codeLists = new ArrayList();

    @PostConstruct
    public void init() {
        setCodeLists(allCodeLists());
    }

    public List<CodeList> allCodeLists() {
        allCodeLists = codeListService.findAll(Sort.Direction.DESC, "creationTimestamp");
        allCodeListMap = allCodeLists.stream()
                .collect(Collectors.toMap(e -> e.getCodeListId(), Function.identity()));
        return allCodeLists;
    }

    public List<CodeList> getCodeLists() {
        return codeLists;
    }

    public void setCodeLists(List<CodeList> codeLists) {
        this.codeLists = codeLists;
    }

    public String getCodeListName() {
        return codeListName;
    }

    public void setCodeListName(String codeListName) {
        this.codeListName = codeListName;
    }

    public List<CodeList> completeInput(String query) {
        String q = (query != null) ? query.trim() : null;

        if (StringUtils.isEmpty(q)) {
            return allCodeLists();
        } else {
            String[] split = q.split(" ");

            return allCodeLists().stream()
                    .filter(e -> {
                        String lowerCaseName = e.getName().toLowerCase();
                        for (String s : split) {
                            if (!lowerCaseName.contains(s.toLowerCase())) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
        }
    }

    public void search() {
        String basedCodeListName = getCodeListName();
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

    public String getBasedCodeListName(long basedCodeListId) {
        if (basedCodeListId == 0L) {
            return null;
        }
        CodeList basedCodeList = allCodeListMap.get(basedCodeListId);
        return (basedCodeList != null) ? basedCodeList.getName() : null;
    }

    public String getAgencyName(long agencyIdListValueId) {
        if (agencyIdListValueId == 0L) {
            return null;
        }
        AgencyIdListValue agencyIdListValue = agencyIdListValueRepository.findOne(agencyIdListValueId);
        return (agencyIdListValue != null) ? agencyIdListValue.getName() : null;
    }
}
