package org.oagi.srt.web.jsf.beans.codelist;

import org.apache.commons.lang3.StringUtils;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.AgencyIdListValueRepository;
import org.oagi.srt.repository.entity.AgencyIdListValue;
import org.oagi.srt.repository.entity.CodeList;
import org.oagi.srt.repository.entity.CodeListState;
import org.oagi.srt.repository.entity.CodeListValue;
import org.oagi.srt.service.CodeListService;
import org.oagi.srt.web.handler.UIHandler;
import org.primefaces.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.oagi.srt.repository.entity.CodeListValue.Color.BrightRed;

@Component
public class CodeListBaseBean extends UIHandler {

    @Autowired
    private CodeListService codeListService;

    @Autowired
    private AgencyIdListValueRepository agencyIdListValueRepository;

    private CodeList codeList;
    private CodeList basedCodeList;
    private List<CodeListValue> codeListValues = new ArrayList();
    private CodeListValue selectedCodeListValue;
    private List<CodeListValue> deleteCodeListValues = new ArrayList();

    private CodeListState state;
    private boolean confirmDifferentNameButSameIdentity;
    private boolean confirmSameListIdButDifferentIdentity;

    @PostConstruct
    public void init() {
        codeList = new CodeList();
        codeList.setListId(Utility.generateGUID());
    }

    public CodeList getCodeList() {
        return codeList;
    }

    public void setCodeList(CodeList codeList) {
        this.codeList = codeList;

        if (codeList != null) {
            long basedCodeListId = codeList.getBasedCodeListId();
            if (basedCodeListId > 0L) {
                CodeList basedCodeList = codeListService.findOne(basedCodeListId);
                setBasedCodeList(basedCodeList);
            }
        }
    }

    public CodeList getBasedCodeList() {
        return basedCodeList;
    }

    public void setBasedCodeList(CodeList basedCodeList) {
        this.basedCodeList = basedCodeList;
    }

    public AgencyIdListValue getAgencyIdListValue() {
        long agencyId = getCodeList().getAgencyId();
        if (agencyId > 0L) {
            return agencyIdListValueRepository.findOne(agencyId);
        } else {
            return null;
        }
    }

    public void setAgencyIdListValue(AgencyIdListValue agencyIdListValue) {
        if (agencyIdListValue != null) {
            getCodeList().setAgencyId(agencyIdListValue.getAgencyIdListValueId());
        }
    }

    public List<AgencyIdListValue> getAgencyIdListValues() {
        return agencyIdListValueRepository.findAll(new Sort(Sort.Direction.ASC, "agencyIdListValueId"));
    }

    public List<AgencyIdListValue> completeAgencyIdListValue(String query) {
        List<AgencyIdListValue> agencyIdListValues = getAgencyIdListValues();
        if (StringUtils.isEmpty(query)) {
            return agencyIdListValues;
        } else {
            return agencyIdListValues.stream()
                    .filter(e -> e.getName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        }
    }

    public List<CodeListValue> getCodeListValues() {
        CodeList codeList = getCodeList();
        if (CodeListState.Published == codeList.getState()) {
            return codeListValues.stream()
                    .filter(e -> BrightRed != e.getColor())
                    .collect(Collectors.toList());
        }
        return codeListValues;
    }

    public void setCodeListValues(List<CodeListValue> codeListValues) {
        this.codeListValues = codeListValues;
    }

    public void addCodeListValue() {
        CodeListValue codeListValue = new CodeListValue();
        codeListValue.setColor(CodeListValue.Color.Green);
        codeListValue.setDisabled(false);
        codeListValues.add(codeListValue);
    }

    public CodeListValue getSelectedCodeListValue() {
        return selectedCodeListValue;
    }

    public void setSelectedCodeListValue(CodeListValue selectedCodeListValue) {
        this.selectedCodeListValue = selectedCodeListValue;
    }

    public void toggleColor(CodeListValue codeListValue) {
        switch (codeListValue.getColor()) {
            case Blue:
                codeListValue.setColor(CodeListValue.Color.DullRed);
                break;
            case DullRed:
                codeListValue.setColor(CodeListValue.Color.Blue);
                break;
            default:
                break;
        }
    }

    public boolean isConfirmDifferentNameButSameIdentity() {
        return confirmDifferentNameButSameIdentity;
    }

    public void setConfirmDifferentNameButSameIdentity(boolean confirmDifferentNameButSameIdentity) {
        this.confirmDifferentNameButSameIdentity = confirmDifferentNameButSameIdentity;
    }

    public boolean isConfirmSameListIdButDifferentIdentity() {
        return confirmSameListIdButDifferentIdentity;
    }

    public void setConfirmSameListIdButDifferentIdentity(boolean confirmSameListIdButDifferentIdentity) {
        this.confirmSameListIdButDifferentIdentity = confirmSameListIdButDifferentIdentity;
    }

    public void deleteCodeListValue() {
        codeListValues.remove(selectedCodeListValue);
        deleteCodeListValues.add(selectedCodeListValue);
        selectedCodeListValue = null;
    }

    public CodeListState getState() {
        return state;
    }

    public void setState(String state) {
        this.state = CodeListState.valueOf(state);
    }

    @Transactional(rollbackFor = Throwable.class)
    public String update() {
        if (!checkDifferentNameButSameIdentity()) {
            return null;
        }
        if (!checkDifferentListIdButSameIdentity()) {
            return null;
        }
        if (!validateCodeListValues()) {
            return null;
        }

        return forceUpdate();
    }

    @Transactional(rollbackFor = Throwable.class)
    public String forceUpdate() {
        codeList = codeListService.newCodeListBuilder(codeList)
                .userId(getCurrentUser().getAppUserId())
                .state(state)
                .build();

        for (CodeListValue codeListValue : codeListValues) {
            codeListService.newCodeListValueBuilder(codeListValue)
                    .codeList(codeList)
                    .build();
        }

        codeListService.delete(
                deleteCodeListValues.stream()
                        .filter(e -> e.getCodeListValueId() > 0L)
                        .collect(Collectors.toList())
        );

        return "/views/code_list/list.xhtml?faces-redirect=true";
    }

    private boolean validateCodeListValues() {
        Map<String, Long> result = codeListValues.stream().collect(
                Collectors.groupingBy(e -> e.getValue() == null ? "" : e.getValue(), Collectors.counting()));

        for (String value : result.keySet()) {
            if (StringUtils.isEmpty(value)) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                "Please fill out 'Code' field."));
                return false;
            }
        }
        for (Long value : result.values()) {
            if (value > 1) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                "It doesn't allow duplicate 'Code' fields."));
                return false;
            }
        }

        return true;
    }

    private boolean checkDifferentNameButSameIdentity() {
        List<CodeList> sameListIdAndAgencyIds =
                codeListService.findByListIdAndAgencyId(codeList.getListId(), codeList.getAgencyId());
        if (codeList.getCodeListId() > 0L) {
            sameListIdAndAgencyIds = sameListIdAndAgencyIds.stream()
                    .filter(e -> e.getCodeListId() != codeList.getCodeListId())
                    .collect(Collectors.toList());
        }

        if (!sameListIdAndAgencyIds.isEmpty()) {
            for (CodeList sameListIdAndAgencyId : sameListIdAndAgencyIds) {
                String a = sameListIdAndAgencyId.getVersionId();
                String b = codeList.getVersionId();
                if (StringUtils.equals(a, b)) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Can't create same identity of Code List."));
                    return false;
                }
            }
            if (!isConfirmDifferentNameButSameIdentity()) {
                for (CodeList sameListIdAndAgencyId : sameListIdAndAgencyIds) {
                    String a = sameListIdAndAgencyId.getName();
                    String b = codeList.getName();
                    if (!StringUtils.equals(a, b)) {
                        RequestContext.getCurrentInstance().execute("PF('confirmDifferentNameButSameIdentity').show()");
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean checkDifferentListIdButSameIdentity() {
        if (!isConfirmSameListIdButDifferentIdentity()) {
            List<CodeList> sameNameAndAgencyIds =
                    codeListService.findByNameAndAgencyId(codeList.getName(), codeList.getAgencyId());
            if (codeList.getCodeListId() > 0L) {
                sameNameAndAgencyIds = sameNameAndAgencyIds.stream()
                        .filter(e -> e.getCodeListId() != codeList.getCodeListId())
                        .collect(Collectors.toList());
            }

            if (!sameNameAndAgencyIds.isEmpty()) {
                for (CodeList sameNameAndAgencyId : sameNameAndAgencyIds) {
                    String a = sameNameAndAgencyId.getListId();
                    String b = codeList.getListId();
                    if (!StringUtils.equals(a, b)) {
                        RequestContext.getCurrentInstance().execute("PF('confirmDifferentListIdButSameIdentity').show()");
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public String getColor(CodeListValue codeListValue) {
        switch (codeListValue.getColor()) {
            case Blue:
                return "#0000cd";
            case BrightRed:
                return "#ff0000";
            case DullRed:
                return "#cd5c5c";
            case Green:
                return "#008000";
            default:
                throw new IllegalStateException();
        }
    }

}
