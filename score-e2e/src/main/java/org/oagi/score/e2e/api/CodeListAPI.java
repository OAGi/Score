package org.oagi.score.e2e.api;

import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.CodeListObject;
import org.oagi.score.e2e.obj.NamespaceObject;
import org.oagi.score.e2e.obj.ReleaseObject;

import java.math.BigInteger;

/**
 * APIs for the code list management.
 */
public interface CodeListAPI {

    CodeListObject getCodeListByManifestId(BigInteger codeListManifestId);

    CodeListObject getCodeListByCodeListNameAndReleaseNum(String codeListName, String releaseNum);

    CodeListObject createRandomCodeList(AppUserObject creator, NamespaceObject namespace,
                                        ReleaseObject release, String state);

    CodeListObject createDerivedCodeList(CodeListObject baseCodeList,
                                         AppUserObject creator, NamespaceObject namespace,
                                         ReleaseObject release, String state);

    Boolean doesCodeListExistInTheRelease(CodeListObject codeList, String release);

    void addCodeListToAnotherRelease(CodeListObject codeList, ReleaseObject release, AppUserObject creator);

    void updateCodeList(CodeListObject codeListWIP);
}
