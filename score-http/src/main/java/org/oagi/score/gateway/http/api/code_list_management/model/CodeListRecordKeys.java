package org.oagi.score.gateway.http.api.code_list_management.model;

import org.oagi.score.gateway.http.cache.RecordKeyProvider;
import org.oagi.score.gateway.http.change.RecordType;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Set;

/** The cache keys of a code list. */
@Component
public class CodeListRecordKeys implements RecordKeyProvider {

    public static String key(BigInteger codeListManifestId) {
        return "code-list:" + codeListManifestId;
    }

    /** The release of a code list, which never changes. */
    public static String releaseKey(BigInteger codeListManifestId) {
        return "code-list-release:" + codeListManifestId;
    }

    @Override
    public Set<RecordType> types() {
        return Set.of(CodeListRecordTypes.CODE_LIST);
    }

    @Override
    public void addKeys(Set<String> keys, RecordType type, BigInteger id, BigInteger parentId, BigInteger releaseId) {
        keys.add(key(id));
    }
}
