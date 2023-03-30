package org.oagi.score.e2e.api;

import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.CodeListObject;
import org.oagi.score.e2e.obj.CodeListValueObject;
import org.oagi.score.e2e.obj.ReleaseObject;

import java.math.BigInteger;
import java.util.List;

/**
 * APIs for the code list value management.
 */
public interface CodeListValueAPI {

    List<CodeListValueObject> getCodeListValuesByCodeListManifestId(BigInteger codeListManifestId);

    CodeListValueObject createRandomCodeListValue(CodeListObject codeList, AppUserObject creator);

    CodeListValueObject createDerivedCodeListValue(CodeListValueObject baseCodeListValue,
                                                   CodeListObject codeList, AppUserObject creator);

    void addCodeListValueToAnotherRelease(CodeListValueObject codeListValue, CodeListObject codeList, AppUserObject creator, BigInteger newCodeListManifestId, ReleaseObject release);

    void updateCodeListValue(CodeListValueObject codeListValue);
}
