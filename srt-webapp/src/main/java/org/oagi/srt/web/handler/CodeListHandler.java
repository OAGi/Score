package org.oagi.srt.web.handler;

import org.oagi.srt.repository.entity.CodeList;
import org.oagi.srt.repository.entity.CodeListValue;
import org.oagi.srt.service.CodeListService;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
public class CodeListHandler extends UIHandler {

    @Autowired
    private CodeListService codeListService;

    private List<CodeList> codeLists = new ArrayList();
    private List<CodeListValue> codeListValues = new ArrayList();

    private CodeList codeList = new CodeList();
    private CodeListValue codeListValueVO = new CodeListValue();
    private boolean extensible;
    private String basedCodeListName;

    private CodeList selected;
    private List<CodeListValue> selectedCodeListValue;

    public CodeListValue getCodeListValue() {
        return codeListValueVO;
    }

    public void setCodeListValue(CodeListValue codeListValueVO) {
        this.codeListValueVO = codeListValueVO;
    }

    @PostConstruct
    public void init() {
        super.init();
        codeLists = codeListService.findAll(Sort.Direction.DESC, "creationTimestamp");
    }

    public List<CodeListValue> getSelectedCodeListValue() {
        return selectedCodeListValue;
    }

    public void setSelectedCodeListValue(List<CodeListValue> selectedCodeListValue) {
        this.selectedCodeListValue = selectedCodeListValue;
    }

    public List<CodeListValue> getCodeListValues() {
        return codeListValues;
    }

    public void setCodeListValues(List<CodeListValue> codeListValues) {
        this.codeListValues = codeListValues;
    }

    public List<CodeList> getCodeLists() {
        return codeLists;
    }

    public void setCodeLists(List<CodeList> codeLists) {
        this.codeLists = codeLists;
    }

    public CodeList getCodeList() {
        return codeList;
    }

    public void setCodeList(CodeList codeList) {
        this.codeList = codeList;
    }

    public boolean isExtensible() {
        return extensible;
    }

    public void setExtensible(boolean extensible) {
        this.extensible = extensible;
    }

    public String getBasedCodeListName() {
        return basedCodeListName;
    }

    public void setBasedCodeListName(String basedCodeListName) {
        this.basedCodeListName = basedCodeListName;
    }

    public void chooseBasedCode() {
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("modal", true);
        options.put("draggable", true);
        options.put("resizable", true);
        options.put("contentHeight", 800);
        RequestContext.getCurrentInstance().openDialog("code_list_create_select", options, null);
    }

    public void addCodeListValue() {
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("modal", true);
        options.put("draggable", true);
        options.put("resizable", true);
        options.put("contentHeight", 300);
        RequestContext.getCurrentInstance().openDialog("code_list_add_code_list_value", options, null);
    }

    public void addNewCodeListValue() {
        codeListValueVO.setExtensionIndicator(true);
        codeListValueVO.setUsedIndicator(true);
        codeListValueVO.setDisabled(false);
        codeListValueVO.setColor("green");
        closeDialog();
    }

    public void onCodeListValueAdded(SelectEvent event) {
        CodeListHandler ch = (CodeListHandler) event.getObject();
        codeListValues.add(ch.getCodeListValue());

        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Code Values are loaded", "Code Values are loaded");
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public void onBasedCodeChosen(SelectEvent event) {
        CodeListHandler ch = (CodeListHandler) event.getObject();
        if (ch.getSelected() != null) {
            selected = ch.getSelected();
            codeListValues = codeListService.findByCodeList(selected);

            for (CodeListValue codeListValue : codeListValues) {
                if (codeListValue.isUsedIndicator() && !codeListValue.isLockedIndicator())
                    codeListValue.setColor("blue");
                else if ((!codeListValue.isUsedIndicator() && !codeListValue.isLockedIndicator()) || (codeListValue.isLockedIndicator()))
                    codeListValue.setColor("red");
            }
        }

        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Code Values are loaded", "Code Values are loaded");
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public void onEdit(CodeList obj) {
        codeList = obj;
        codeListValues = codeListService.findByCodeList(codeList);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void onDiscard(CodeList obj) {
        codeList = obj;
        codeListService.updateState(codeList, CodeList.State.Discarded);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void onDelete(CodeList obj) {
        codeList = obj;
        codeListService.updateState(codeList, CodeList.State.Deleted);
    }

    public List<String> completeInput(String query) {
        return codeListService.findDistinctNameByNameContaining(query);
    }

    public void search() {
        codeLists =
                codeListService.findByNameContainingAndStateIsPublishedAndExtensibleIndicatorIsTrue(getBasedCodeListName());
        if (codeLists.isEmpty()) {
            FacesMessage msg = new FacesMessage("[" + getBasedCodeListName() + "] No such Code List exists or not yet published or not extensible", "[" + getBasedCodeListName() + "] No such Code List exists or not yet published or not extensible");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    public void onRowSelect(SelectEvent event) {
        selected = (CodeList) event.getObject();

        FacesMessage msg = new FacesMessage(selected.getName(), String.valueOf(selected.getCodeListId()));
        FacesContext.getCurrentInstance().addMessage(null, msg);

        codeListValues = codeListService.findByCodeList(selected);
    }

    public void onRowUnselect(UnselectEvent event) {
        CodeList codeList = (CodeList) event.getObject();
        FacesMessage msg = new FacesMessage("Item Unselected", String.valueOf(codeList.getCodeListId()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
        selected = null;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void save() {
        codeList =
                codeListService.newCodeListBuilder(codeList)
                        .userId(userId)
                        .extensibleIndicator(extensible)
                        .basedCodeList(selected)
                        .build();

        for (CodeListValue codeListValue : codeListValues) {
            setIndicators(codeListValue)
                    .codeList(codeList)
                    .build();
        }
    }

    private CodeListService.CodeListValueBuilder setIndicators(CodeListValue codeListValue) {
        CodeListService.CodeListValueBuilder codeListValueBuilder =
                codeListService.newCodeListValueBuilder(codeListValue);

        switch (codeListValue.getColor()) {
            case "blue":
                codeListValueBuilder
                        .usedIndicator(true)
                        .lockedIndicator(false)
                        .extensionIndicator(false);
                break;
            case "red":
                codeListValueBuilder
                        .usedIndicator(false)
                        .lockedIndicator(true)
                        .extensionIndicator(false);
                break;
            case "orange":
                codeListValueBuilder
                        .usedIndicator(false)
                        .lockedIndicator(false)
                        .extensionIndicator(false);
                break;
            case "green":
                codeListValueBuilder
                        .usedIndicator(true)
                        .lockedIndicator(false)
                        .extensionIndicator(true);
                break;
        }

        return codeListValueBuilder;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void updateSave() {
        codeListService.update(codeList);

        for (CodeListValue codeListValue : codeListValues) {
            setIndicators(codeListValue)
                    .codeList(codeList)
                    .build();
        }

        selected = codeList;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void updatePublish() {
        codeListService.updateState(codeList, CodeList.State.Published);

        for (CodeListValue codeListValue : codeListValues) {
            setIndicators(codeListValue)
                    .codeList(codeList)
                    .build();
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void publish() {
        codeList =
                codeListService.newCodeListBuilder(codeList)
                        .userId(userId)
                        .extensibleIndicator(extensible)
                        .state(CodeList.State.Published)
                        .basedCodeList(selected)
                        .build();
    }

    public void cancel() {
        this.selected = null;
    }

    public CodeList getSelected() {
        return selected;
    }

    public void setSelected(CodeList selected) {
        this.selected = selected;
    }

    public void updateCodeListValue(int codeListValueId) {
        for (CodeListValue codeListValue : codeListValues) {
            if (codeListValue.getCodeListValueId() == codeListValueId) {
                codeListValue.setColor((codeListValue.getColor().equals("blue")) ? "orange" : "blue");
            }
        }
    }

}
