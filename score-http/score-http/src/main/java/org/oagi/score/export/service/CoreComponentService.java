package org.oagi.score.export.service;


import org.jooq.types.ULong;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.SeqKeyRecord;
import org.oagi.score.repository.provider.DataProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CoreComponentService {

    public List<SeqKeyRecord> getCoreComponents(
            ULong accManifestId, DataProvider provider) {
        List<SeqKeyRecord> seqKeyList = provider.getSeqKeys(accManifestId);

        return sort(seqKeyList);
    }

    private List<SeqKeyRecord> sort(List<SeqKeyRecord> seqKeyList) {
        Map<ULong, SeqKeyRecord> seqKeyMap = seqKeyList.stream()
                .collect(Collectors.toMap(
                        SeqKeyRecord::getSeqKeyId, Function.identity()));

        SeqKeyRecord head = seqKeyMap.values().stream()
                .filter(e -> e.getPrevSeqKeyId() == null)
                .findFirst().orElse(null);

        List<SeqKeyRecord> sorted = new ArrayList();
        SeqKeyRecord current = head;
        while (current != null) {
            sorted.add(current);
            current = seqKeyMap.get(current.getNextSeqKeyId());
        }

        return sorted;
    }
}
