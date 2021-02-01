package org.oagi.score.gateway.http.api.agency_id_management.service;

import org.jooq.DSLContext;
import org.oagi.score.gateway.http.api.agency_id_management.data.SimpleAgencyIdListValue;
import org.oagi.score.repo.api.impl.jooq.entity.Tables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AgencyIdService {

    @Autowired
    private DSLContext dslContext;

    public List<SimpleAgencyIdListValue> getSimpleAgencyIdListValues() {
        return dslContext.select(Tables.AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID,
                Tables.AGENCY_ID_LIST_VALUE.NAME)
                .from(Tables.AGENCY_ID_LIST_VALUE)
                .fetchStreamInto(SimpleAgencyIdListValue.class)
                .sorted(Comparator.comparing(SimpleAgencyIdListValue::getName))
                .collect(Collectors.toList());
    }
}
