package org.oagi.srt.service;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.BusinessContextRepository;
import org.oagi.srt.repository.BusinessContextValueRepository;
import org.oagi.srt.repository.entity.BusinessContext;
import org.oagi.srt.repository.entity.BusinessContextValue;
import org.oagi.srt.repository.entity.ContextSchemeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Service
public class BusinessContextService {

    @Autowired
    private BusinessContextRepository businessContextRepository;

    @Autowired
    private BusinessContextValueRepository businessContextValueRepository;

    public List<BusinessContext> findAll(Sort.Direction direction, String property) {
        return Collections.unmodifiableList(
                businessContextRepository.findAll(new Sort(new Sort.Order(direction, property)))
        );
    }

    public List<BusinessContext> findByName(String name) {
        return businessContextRepository.findByName(name);
    }

    public List<BusinessContextValue> findByBizCtxId(long bizCtxId) {
        return businessContextValueRepository.findByBizCtxId(bizCtxId);
    }

    public BusinessContext findById(long bizCtxId) {
        return businessContextRepository.findOne(bizCtxId);
    }

    public void update(BusinessContext businessContext) {
        businessContextRepository.save(businessContext);
    }

    public BusinessContextBuilder newBusinessContextBuilder() {
        return new BusinessContextBuilder();
    }

    public class BusinessContextBuilder {
        private BusinessContextBuilder() {
        }

        private String name;
        private long userId;
        private List<ContextSchemeValue> contextSchemeValues;

        public BusinessContextBuilder name(String name) {
            this.name = name;
            return this;
        }

        public BusinessContextBuilder userId(long userId) {
            this.userId = userId;
            return this;
        }

        public BusinessContextBuilder contextSchemeValues(List<ContextSchemeValue> contextSchemeValues) {
            this.contextSchemeValues = contextSchemeValues;
            return this;
        }

        public BusinessContext build() {
            if (StringUtils.isEmpty(name)) {
                throw new IllegalStateException("'name' parameter must not be null.");
            }

            if (userId == 0) {
                throw new IllegalStateException("'userId' parameter must be positive.");
            }

            BusinessContext businessContext = new BusinessContext();
            businessContext.setName(name);
            String guid = Utility.generateGUID();
            businessContext.setGuid(guid);
            businessContext.setCreatedBy(userId);
            businessContext.setLastUpdatedBy(userId);

            businessContext = businessContextRepository.saveAndFlush(businessContext);

            if (contextSchemeValues != null && !contextSchemeValues.isEmpty()) {
                for (ContextSchemeValue contextSchemeValue : contextSchemeValues) {
                    BusinessContextValue businessContextValue = new BusinessContextValue();
                    businessContextValue.setBusinessContext(businessContext);
                    businessContextValue.setContextSchemeValue(contextSchemeValue);
                    businessContextValueRepository.save(businessContextValue);
                }
            }

            return businessContext;
        }
    }
}
