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

    /**
     * Create a code list value with the given text, which a random value does not give: a long, an empty or a
     * repeated value. The other fields are random.
     *
     * @param codeList code list the value belongs to
     * @param creator  creator of the value
     * @param value    text of the value
     * @param meaning  meaning of the value
     * @return the created code list value
     */
    CodeListValueObject createCodeListValue(CodeListObject codeList, AppUserObject creator, String value, String meaning);

    CodeListValueObject createDerivedCodeListValue(CodeListValueObject baseCodeListValue,
                                                   CodeListObject codeList, AppUserObject creator);

    void addCodeListValueToAnotherRelease(CodeListValueObject codeListValue, CodeListObject codeList, AppUserObject creator, BigInteger newCodeListManifestId, ReleaseObject release);

    void updateCodeListValue(CodeListValueObject codeListValue);
}
