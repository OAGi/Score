package org.oagi.score.gateway.http.cache;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListRecordKeys;
import org.oagi.score.gateway.http.api.cc_management.model.CcRecordKeys;
import org.oagi.score.gateway.http.api.cc_management.repository.AffectedRecordsRepository;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListRecordKeys;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceRecordKeys;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtRecordKeys;

import java.util.List;

/** The key providers of all modules, put together as the application does. */
public final class TestRecordKeys {

    private TestRecordKeys() {
    }

    public static RecordChangeKeyResolver resolver(AffectedRecordsRepository affected) {
        return new RecordChangeKeyResolver(List.of(new CcRecordKeys(affected), new NamespaceRecordKeys(),
                new CodeListRecordKeys(), new AgencyIdListRecordKeys(), new XbtRecordKeys()));
    }
}
