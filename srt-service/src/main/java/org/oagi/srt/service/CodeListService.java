package org.oagi.srt.service;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.CodeListRepository;
import org.oagi.srt.repository.CodeListValueRepository;
import org.oagi.srt.repository.entity.CodeList;
import org.oagi.srt.repository.entity.CodeListValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CodeListService {

    @Autowired
    private CodeListRepository codeListRepository;

    @Autowired
    private CodeListValueRepository codeListValueRepository;

    public List<CodeList> findAll(Sort.Direction direction, String property) {
        return codeListRepository.findAll(new Sort(new Sort.Order(direction, property)));
    }

    public List<CodeListValue> findByCodeList(CodeList codeList) {
        long codeListId = (codeList != null) ? codeList.getCodeListId() : 0L;
        if (codeListId > 0L) {
            return codeListValueRepository.findByCodeListId(codeListId);
        } else {
            return Collections.emptyList();
        }
    }

    public CodeList findOne(long codeListId) {
        return codeListRepository.findOne(codeListId);
    }

    public void updateState(CodeList codeList, CodeList.State state) {
        if (codeList != null && state != null) {
            codeList.setState(state);
            codeListRepository.updateStateByCodeListId(state.toString(), codeList.getCodeListId());
        }
    }

    public List<String> findDistinctNameByNameContaining(String name) {
        name = (name != null) ? name.trim() : null;
        if (StringUtils.isEmpty(name)) {
            return Collections.emptyList();
        }
        List<CodeList> codeLists = codeListRepository.findByNameContaining(name);
        return codeLists.stream()
                .map(codeList -> codeList.getName())
                .distinct()
                .collect(Collectors.toList());
    }

    public List<CodeList> findByNameContainingAndStateIsPublishedAndExtensibleIndicatorIsTrue(String name) {
        name = (name != null) ? name.trim() : null;
        if (StringUtils.isEmpty(name)) {
            return Collections.emptyList();
        }
        return codeListRepository.findByNameContainingAndStateIsPublishedAndExtensibleIndicatorIsTrue(name);
    }

    public void update(CodeList codeList) {
        codeListRepository.save(codeList);
    }

    public void delete(Collection<CodeListValue> codeListValues) {
        codeListValueRepository.delete(codeListValues);
    }

    public CodeListBuilder newCodeListBuilder(CodeList codeList) {
        return new CodeListBuilder(codeList);
    }

    public class CodeListBuilder {

        private CodeList codeList;
        private long userId;
        private CodeList.State state = CodeList.State.Editing;
        private boolean extensibleIndicator;
        private CodeList basedCodeList;

        private CodeListBuilder(CodeList codeList) {
            this.codeList = codeList;
            this.extensibleIndicator = codeList.isExtensibleIndicator();
        }

        public CodeListBuilder userId(long userId) {
            this.userId = userId;
            return this;
        }

        public CodeListBuilder state(CodeList.State state) {
            this.state = state;
            return this;
        }

        public CodeListBuilder extensibleIndicator(boolean extensibleIndicator) {
            this.extensibleIndicator = extensibleIndicator;
            return this;
        }

        public CodeListBuilder basedCodeList(CodeList basedCodeList) {
            this.basedCodeList = basedCodeList;
            return this;
        }

        public CodeList build() {
            CodeList codeList = (this.codeList != null) ? this.codeList : new CodeList();
            codeList.setExtensibleIndicator(extensibleIndicator);
            if (StringUtils.isEmpty(codeList.getGuid())) {
                codeList.setGuid(Utility.generateGUID());
            }
            if (StringUtils.isEmpty(codeList.getEnumTypeGuid())) {
                codeList.setEnumTypeGuid(Utility.generateGUID());
            }
            if (basedCodeList != null) {
                codeList.setBasedCodeListId(basedCodeList.getCodeListId());
            }
            codeList.setState(state);
            if (codeList.getCreatedBy() == 0L) {
                codeList.setCreatedBy(userId);
            }
            codeList.setLastUpdatedBy(userId);

            codeListRepository.save(codeList);

            return codeList;
        }
    }

    public CodeListValueBuilder newCodeListValueBuilder(CodeListValue codeListValue) {
        return new CodeListValueBuilder(codeListValue);
    }

    public class CodeListValueBuilder {
        private CodeListValue codeListValue;
        private CodeList codeList;

        private boolean usedIndicator = true;
        private boolean lockedIndicator;
        private boolean extensionIndicator;

        private CodeListValueBuilder(CodeListValue codeListValue) {
            this.codeListValue = codeListValue;
            this.usedIndicator = codeListValue.isUsedIndicator();
            this.lockedIndicator = codeListValue.isLockedIndicator();
            this.extensionIndicator = codeListValue.isExtensionIndicator();
        }

        public CodeListValueBuilder codeList(CodeList codeList) {
            this.codeList = codeList;
            return this;
        }

        public CodeListValueBuilder usedIndicator(boolean usedIndicator) {
            this.usedIndicator = usedIndicator;
            return this;
        }

        public CodeListValueBuilder lockedIndicator(boolean lockedIndicator) {
            this.lockedIndicator = lockedIndicator;
            return this;
        }

        public CodeListValueBuilder extensionIndicator(boolean extensionIndicator) {
            this.extensionIndicator = extensionIndicator;
            return this;
        }

        public CodeListValue build() {
            CodeListValue codeListValue = (this.codeListValue != null) ? this.codeListValue : new CodeListValue();

            if (codeList != null) {
                codeListValue.setCodeListId(codeList.getCodeListId());
            }
            codeListValue.setUsedIndicator(usedIndicator);
            codeListValue.setLockedIndicator(lockedIndicator);
            codeListValue.setExtensionIndicator(extensionIndicator);

            codeListValueRepository.save(codeListValue);

            return codeListValue;
        }
    }

}
