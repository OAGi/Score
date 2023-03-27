package org.oagi.score.repository.provider;

import org.jooq.types.ULong;
import org.oagi.score.export.model.ModuleCCID;

public interface ModuleProvider {

    ModuleCCID findModuleAgencyIdList(ULong agencyIdListId);

    ModuleCCID findModuleCodeList(ULong codeListId);

    ModuleCCID findModuleAcc(ULong accId);

    ModuleCCID findModuleAsccp(ULong asccpId);

    ModuleCCID findModuleBccp(ULong bccpId);

    ModuleCCID findModuleDt(ULong dtId);

    ModuleCCID findModuleXbt(ULong xbtId);

    ModuleCCID findModuleBlobContent(ULong blobContentId);

}
