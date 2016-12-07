package org.oagi.srt.web.jsf.beans.codelist;

import org.apache.commons.lang3.StringUtils;
import org.oagi.srt.repository.entity.CodeList;
import org.oagi.srt.repository.entity.CodeListState;
import org.oagi.srt.repository.entity.CodeListValue;
import org.oagi.srt.service.CodeListService;
import org.oagi.srt.web.handler.UIHandler;
import org.primefaces.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CodeListBaseBean extends UIHandler {

    @Autowired
    private CodeListService codeListService;

    private CodeList codeList = new CodeList();
    private List<CodeListValue> codeListValues = new ArrayList();
    private CodeListValue selectedCodeListValue;
    private List<CodeListValue> deleteCodeListValues = new ArrayList();

    private CodeListState state;
    private boolean confirmDifferentNameButSameIdentity;
    private boolean confirmSameListIdButDifferentIdentity;

    public CodeList getCodeList() {
        return codeList;
    }

    public void setCodeList(CodeList codeList) {
        this.codeList = codeList;
    }

    public List<CodeListValue> getCodeListValues() {
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

    public void toggleColor(Long codeListValueId) {
        for (CodeListValue codeListValue : codeListValues) {
            if (codeListValue.getCodeListValueId() == codeListValueId) {
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

}
