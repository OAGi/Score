package org.oagi.score.gateway.http.api.module_management.model;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.blob_content.BlobContentManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.export.model.ModuleCCID;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;

import java.util.List;

public interface ModuleCcDocument extends CcDocument {

    List<ModuleCCID<AgencyIdListManifestId>> getModuleAgencyIdList();

    ModuleCCID<AgencyIdListManifestId> getModuleAgencyIdList(AgencyIdListManifestId agencyIdListManifestId);

    List<ModuleCCID<CodeListManifestId>> getModuleCodeList();

    ModuleCCID<CodeListManifestId> getModuleCodeList(CodeListManifestId codeListManifestId);

    List<ModuleCCID<AccManifestId>> getModuleAcc();

    ModuleCCID<AccManifestId> getModuleAcc(AccManifestId accManifestId);

    List<ModuleCCID<AsccpManifestId>> getModuleAsccp();

    ModuleCCID<AsccpManifestId> getModuleAsccp(AsccpManifestId asccpManifestId);

    List<ModuleCCID<BccpManifestId>> getModuleBccp();

    ModuleCCID<BccpManifestId> getModuleBccp(BccpManifestId bccpManifestId);

    List<ModuleCCID<DtManifestId>> getModuleDt();

    ModuleCCID<DtManifestId> getModuleDt(DtManifestId dtManifestId);

    List<ModuleCCID<XbtManifestId>> getModuleXbt();

    ModuleCCID<XbtManifestId> getModuleXbt(XbtManifestId xbtManifestId);

    List<ModuleCCID<BlobContentManifestId>> getModuleBlobContent();

}
