package org.oagi.srt.web.jsf.beans.codelist;

import org.oagi.srt.repository.entity.CodeList;
import org.oagi.srt.repository.entity.CodeListValue;
import org.oagi.srt.service.CodeListService;
import org.oagi.srt.web.handler.UIHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CodeListBaseBean extends UIHandler {

    @Autowired
    private CodeListService codeListService;

    private CodeList codeList = new CodeList();
    private List<CodeListValue> codeListValues = new ArrayList();
    private CodeListValue selectedCodeListValue;
    private List<CodeListValue> deleteCodeListValues = new ArrayList();

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

    public void deleteCodeListValue() {
        codeListValues.remove(selectedCodeListValue);
        deleteCodeListValues.add(selectedCodeListValue);
        selectedCodeListValue = null;
    }

    public void save() {
        update(CodeList.State.Editing);
    }

    public void publish() {
        update(CodeList.State.Published);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void update(CodeList.State state) {
        codeList = codeListService.newCodeListBuilder(codeList)
                .userId(userId)
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
    }

}
