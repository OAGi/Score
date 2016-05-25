package org.oagi.srt.web.handler;

import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.CodeListRepository;
import org.oagi.srt.repository.CodeListValueRepository;
import org.oagi.srt.repository.entity.CodeList;
import org.oagi.srt.repository.entity.CodeListValue;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
public class CodeListHandler extends UIHandler {

    @Autowired
    private CodeListRepository codeListRepository;

    @Autowired
    private CodeListValueRepository codeListValueRepository;

    private List<CodeList> codeLists = Collections.emptyList();
    private List<CodeListValue> codeListValues = Collections.emptyList();

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
        codeLists = codeListRepository.findAllOrderByCreationTimestampDesc();
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
            codeListValues = codeListValueRepository.findByCodeListId(selected.getCodeListId());

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
        codeListValues = codeListValueRepository.findByCodeListId(codeList.getCodeListId());
    }

    @Transactional(rollbackFor = Throwable.class)
    public void onDiscard(CodeList obj) {
        codeList = obj;
        codeList.setState(SRTConstants.CODE_LIST_STATE_DISCARDED);
        codeListRepository.updateStateByCodeListId(codeList.getState(), codeList.getCodeListId());
    }

    @Transactional(rollbackFor = Throwable.class)
    public void onDelete(CodeList obj) {
        codeList = obj;
        codeList.setState(SRTConstants.CODE_LIST_STATE_DELETED);
        codeListRepository.updateStateByCodeListId(codeList.getState(), codeList.getCodeListId());
    }

    public List<String> completeInput(String query) {
        codeLists = codeListRepository.findByNameContaining(query);

        return codeLists.stream().map(codeList -> codeList.getName()).collect(Collectors.toList());
    }

    public void search() {
        codeLists =
                codeListRepository.findByNameContainingAndStateIsPublishedAndExtensibleIndicatorIsTrue(getBasedCodeListName());
        if (codeLists.isEmpty()) {
            FacesMessage msg = new FacesMessage("[" + getBasedCodeListName() + "] No such Code List exists or not yet published or not extensible", "[" + getBasedCodeListName() + "] No such Code List exists or not yet published or not extensible");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    public void searchDerived(String id) {
        CodeList codeList = codeListRepository.findOne(Integer.parseInt(id));
        codeLists = (codeList != null) ? Arrays.asList(codeList) : Collections.emptyList();
        if (codeLists.isEmpty()) {
            FacesMessage msg = new FacesMessage("[" + getBasedCodeListName() + "] No such Code List exists.", "[" + getBasedCodeListName() + "] No such Code List exists.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    public void onRowSelect(SelectEvent event) {
        selected = (CodeList) event.getObject();

        FacesMessage msg = new FacesMessage(selected.getName(), String.valueOf(selected.getCodeListId()));
        FacesContext.getCurrentInstance().addMessage(null, msg);

        codeListValues = codeListValueRepository.findByCodeListId(selected.getCodeListId());
    }

    public void onRowUnselect(UnselectEvent event) {
        CodeList codeList = (CodeList) event.getObject();
        FacesMessage msg = new FacesMessage("Item Unselected", String.valueOf(codeList.getCodeListId()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
        selected = null;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void save() {
        codeList.setExtensibleIndicator(extensible);
        codeList.setGuid(Utility.generateGUID());
        codeList.setEnumTypeGuid(Utility.generateGUID());
        if (selected != null) {
            codeList.setBasedCodeListId(selected.getCodeListId());
        } else {
            codeList.setBasedCodeListId(-1);
        }
        codeList.setState(SRTConstants.CODE_LIST_STATE_EDITING);
        codeList.setCreatedBy(userId);
        codeList.setLastUpdatedBy(userId);

        codeListRepository.save(codeList);

        int codeListId = codeList.getCodeListId();

        for (CodeListValue codeListValue : codeListValues) {
            setIndicators(codeListValue);
            codeListValue.setCodeListId(codeListId);
        }
        codeListValueRepository.save(codeListValues);
    }

    private void setIndicators(CodeListValue codeListValue) {
        switch (codeListValue.getColor()) {
            case "blue":
                codeListValue.setUsedIndicator(true);
                codeListValue.setLockedIndicator(false);
                codeListValue.setExtensionIndicator(false);
                break;
            case "red":
                codeListValue.setUsedIndicator(false);
                codeListValue.setLockedIndicator(true);
                codeListValue.setExtensionIndicator(false);
                break;
            case "orange":
                codeListValue.setUsedIndicator(false);
                codeListValue.setLockedIndicator(false);
                codeListValue.setExtensionIndicator(false);
                break;
            case "green":
                codeListValue.setUsedIndicator(true);
                codeListValue.setLockedIndicator(false);
                codeListValue.setExtensionIndicator(true);
                break;
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void updateSave() {
        codeListRepository.save(codeList);

        for (CodeListValue codeListValue : codeListValues) {
            setIndicators(codeListValue);
            codeListValue.setCodeListId(codeList.getCodeListId());
            codeListValueRepository.updateCodeListIdByCodeListValueId(
                    codeListValue.getCodeListId(), codeListValue.getCodeListValueId());
        }

        selected = codeList;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void updatePublish() {
        codeListRepository.updateStateByCodeListId(SRTConstants.CODE_LIST_STATE_PUBLISHED, codeList.getCodeListId());

        for (CodeListValue codeListValue : codeListValues) {
            setIndicators(codeListValue);
            codeListValue.setCodeListId(codeList.getCodeListId());
            codeListValueRepository.updateCodeListIdByCodeListValueId(
                    codeListValue.getCodeListId(), codeListValue.getCodeListValueId());
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void publish() {
        codeList.setExtensibleIndicator(extensible);
        codeList.setGuid(Utility.generateGUID());
        codeList.setEnumTypeGuid(Utility.generateGUID());
        if (selected != null)
            codeList.setBasedCodeListId(selected.getCodeListId());
        else
            codeList.setBasedCodeListId(-1);
        codeList.setState(SRTConstants.CODE_LIST_STATE_PUBLISHED);
        codeList.setCreatedBy(userId);
        codeList.setLastUpdatedBy(userId);

        codeListRepository.save(codeList);

        int codeListId = codeList.getCodeListId();

        for (CodeListValue codeListValue : codeListValues) {
            setIndicators(codeListValue);
            codeListValue.setCodeListId(codeListId);
        }
        codeListValueRepository.save(codeListValues);
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
