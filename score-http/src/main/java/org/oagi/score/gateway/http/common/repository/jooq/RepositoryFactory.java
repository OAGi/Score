package org.oagi.score.gateway.http.common.repository.jooq;

import org.jooq.DSLContext;
import org.oagi.score.gateway.http.api.account_management.repository.AccountCommandRepository;
import org.oagi.score.gateway.http.api.account_management.repository.AccountQueryRepository;
import org.oagi.score.gateway.http.api.account_management.repository.ScoreUserQueryRepository;
import org.oagi.score.gateway.http.api.account_management.repository.jooq.JooqAccountCommandRepository;
import org.oagi.score.gateway.http.api.account_management.repository.jooq.JooqAccountQueryRepository;
import org.oagi.score.gateway.http.api.account_management.repository.jooq.JooqScoreUserQueryRepository;
import org.oagi.score.gateway.http.api.agency_id_management.repository.AgencyIdListCommandRepository;
import org.oagi.score.gateway.http.api.agency_id_management.repository.AgencyIdListQueryRepository;
import org.oagi.score.gateway.http.api.agency_id_management.repository.jooq.JooqAgencyIdListCommandRepository;
import org.oagi.score.gateway.http.api.agency_id_management.repository.jooq.JooqAgencyIdListQueryRepository;
import org.oagi.score.gateway.http.api.application_management.repository.ConfigurationCommandRepository;
import org.oagi.score.gateway.http.api.application_management.repository.ConfigurationQueryRepository;
import org.oagi.score.gateway.http.api.application_management.repository.jooq.JooqConfigurationCommandRepository;
import org.oagi.score.gateway.http.api.application_management.repository.jooq.JooqConfigurationQueryRepository;
import org.oagi.score.gateway.http.api.bie_management.repository.*;
import org.oagi.score.gateway.http.api.bie_management.repository.jooq.*;
import org.oagi.score.gateway.http.api.business_term_management.repository.BusinessTermCommandRepository;
import org.oagi.score.gateway.http.api.business_term_management.repository.BusinessTermQueryRepository;
import org.oagi.score.gateway.http.api.business_term_management.repository.jooq.JooqBusinessTermCommandRepository;
import org.oagi.score.gateway.http.api.business_term_management.repository.jooq.JooqBusinessTermQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.*;
import org.oagi.score.gateway.http.api.cc_management.repository.jooq.*;
import org.oagi.score.gateway.http.api.code_list_management.repository.CodeListCommandRepository;
import org.oagi.score.gateway.http.api.code_list_management.repository.CodeListQueryRepository;
import org.oagi.score.gateway.http.api.code_list_management.repository.jooq.JooqCodeListCommandRepository;
import org.oagi.score.gateway.http.api.code_list_management.repository.jooq.JooqCodeListQueryRepository;
import org.oagi.score.gateway.http.api.comment_management.repository.CommentCommandRepository;
import org.oagi.score.gateway.http.api.comment_management.repository.CommentQueryRepository;
import org.oagi.score.gateway.http.api.comment_management.repository.jooq.JooqCommentCommandRepository;
import org.oagi.score.gateway.http.api.comment_management.repository.jooq.JooqCommentQueryRepository;
import org.oagi.score.gateway.http.api.context_management.business_context.repository.BusinessContextCommandRepository;
import org.oagi.score.gateway.http.api.context_management.business_context.repository.BusinessContextQueryRepository;
import org.oagi.score.gateway.http.api.context_management.business_context.repository.jooq.JooqBusinessContextCommandRepository;
import org.oagi.score.gateway.http.api.context_management.business_context.repository.jooq.JooqBusinessContextQueryRepository;
import org.oagi.score.gateway.http.api.context_management.context_category.repository.ContextCategoryCommandRepository;
import org.oagi.score.gateway.http.api.context_management.context_category.repository.ContextCategoryQueryRepository;
import org.oagi.score.gateway.http.api.context_management.context_category.repository.jooq.JooqContextCategoryCommandRepository;
import org.oagi.score.gateway.http.api.context_management.context_category.repository.jooq.JooqContextCategoryQueryRepository;
import org.oagi.score.gateway.http.api.context_management.context_scheme.repository.ContextSchemeCommandRepository;
import org.oagi.score.gateway.http.api.context_management.context_scheme.repository.ContextSchemeQueryRepository;
import org.oagi.score.gateway.http.api.context_management.context_scheme.repository.jooq.JooqContextSchemeCommandRepository;
import org.oagi.score.gateway.http.api.context_management.context_scheme.repository.jooq.JooqContextSchemeQueryRepository;
import org.oagi.score.gateway.http.api.library_management.repository.LibraryCommandRepository;
import org.oagi.score.gateway.http.api.library_management.repository.LibraryQueryRepository;
import org.oagi.score.gateway.http.api.library_management.repository.jooq.JooqLibraryCommandRepository;
import org.oagi.score.gateway.http.api.library_management.repository.jooq.JooqLibraryQueryRepository;
import org.oagi.score.gateway.http.api.log_management.repository.LogCommandRepository;
import org.oagi.score.gateway.http.api.log_management.repository.jooq.JooqLogCommandRepository;
import org.oagi.score.gateway.http.api.log_management.service.LogSerializer;
import org.oagi.score.gateway.http.api.log_management.service.LogSnapshotResolver;
import org.oagi.score.gateway.http.api.message_management.repository.MessageCommandRepository;
import org.oagi.score.gateway.http.api.message_management.repository.MessageQueryRepository;
import org.oagi.score.gateway.http.api.message_management.repository.jooq.JooqMessageCommandRepository;
import org.oagi.score.gateway.http.api.message_management.repository.jooq.JooqMessageQueryRepository;
import org.oagi.score.gateway.http.api.module_management.repository.*;
import org.oagi.score.gateway.http.api.module_management.repository.jooq.*;
import org.oagi.score.gateway.http.api.namespace_management.repository.NamespaceCommandRepository;
import org.oagi.score.gateway.http.api.namespace_management.repository.NamespaceQueryRepository;
import org.oagi.score.gateway.http.api.namespace_management.repository.jooq.JooqNamespaceCommandRepository;
import org.oagi.score.gateway.http.api.namespace_management.repository.jooq.JooqNamespaceQueryRepository;
import org.oagi.score.gateway.http.api.oas_management.repository.*;
import org.oagi.score.gateway.http.api.oas_management.repository.jooq.*;
import org.oagi.score.gateway.http.api.release_management.repository.ReleaseCommandRepository;
import org.oagi.score.gateway.http.api.release_management.repository.ReleaseQueryRepository;
import org.oagi.score.gateway.http.api.release_management.repository.jooq.JooqReleaseCommandRepository;
import org.oagi.score.gateway.http.api.release_management.repository.jooq.JooqReleaseQueryRepository;
import org.oagi.score.gateway.http.api.tag_management.repository.TagCommandRepository;
import org.oagi.score.gateway.http.api.tag_management.repository.TagQueryRepository;
import org.oagi.score.gateway.http.api.tag_management.repository.jooq.JooqTagCommandRepository;
import org.oagi.score.gateway.http.api.tag_management.repository.jooq.JooqTagQueryRepository;
import org.oagi.score.gateway.http.api.tenant_management.repository.TenantCommandRepository;
import org.oagi.score.gateway.http.api.tenant_management.repository.TenantQueryRepository;
import org.oagi.score.gateway.http.api.tenant_management.repository.jooq.JooqTenantCommandRepository;
import org.oagi.score.gateway.http.api.tenant_management.repository.jooq.JooqTenantQueryRepository;
import org.oagi.score.gateway.http.api.xbt_management.repository.XbtQueryRepository;
import org.oagi.score.gateway.http.api.xbt_management.repository.jooq.JooqXbtQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class RepositoryFactory {

    private final DSLContext dslContext;

    public RepositoryFactory(@Autowired DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public ScoreUserQueryRepository scoreUserQueryRepository() {
        return new JooqScoreUserQueryRepository(dslContext, this);
    }

    public AccountCommandRepository accountCommandRepository(ScoreUser requester, PasswordEncoder passwordEncoder) {
        return new JooqAccountCommandRepository(dslContext, requester, this, passwordEncoder);
    }

    public AccountQueryRepository accountQueryRepository(ScoreUser requester) {
        return new JooqAccountQueryRepository(dslContext, requester, this);
    }

    public ConfigurationCommandRepository configurationCommandRepository(ScoreUser requester) {
        return new JooqConfigurationCommandRepository(dslContext, requester, this);
    }

    public ConfigurationQueryRepository configurationQueryRepository(ScoreUser requester) {
        return new JooqConfigurationQueryRepository(dslContext, requester, this);
    }

    public AccCommandRepository accCommandRepository(ScoreUser requester) {
        return new JooqAccCommandRepository(dslContext, requester, this, logSerializer());
    }

    public CcCommandRepository ccCommandRepository(ScoreUser requester) {
        return new JooqCcCommandRepository(dslContext, requester, this);
    }

    public AccQueryRepository accQueryRepository(ScoreUser requester) {
        return new JooqAccQueryRepository(dslContext, requester, this);
    }

    public AsccpCommandRepository asccpCommandRepository(ScoreUser requester) {
        return new JooqAsccpCommandRepository(dslContext, requester, this);
    }

    public AsccpQueryRepository asccpQueryRepository(ScoreUser requester) {
        return new JooqAsccpQueryRepository(dslContext, requester, this);
    }

    public BccpCommandRepository bccpCommandRepository(ScoreUser requester) {
        return new JooqBccpCommandRepository(dslContext, requester, this);
    }

    public BccpQueryRepository bccpQueryRepository(ScoreUser requester) {
        return new JooqBccpQueryRepository(dslContext, requester, this);
    }

    public DtCommandRepository dtCommandRepository(ScoreUser requester) {
        return new JooqDtCommandRepository(dslContext, requester, this);
    }

    public DtQueryRepository dtQueryRepository(ScoreUser requester) {
        return new JooqDtQueryRepository(dslContext, requester, this);
    }

    public CcQueryRepository ccQueryRepository(ScoreUser requester) {
        return new JooqCcQueryRepository(dslContext, requester, this);
    }

    public BlobContentQueryRepository blobContentQueryRepository(ScoreUser requester) {
        return new JooqBlobContentQueryRepository(dslContext, requester, this);
    }

    public BieCommandRepository bieCommandRepository(ScoreUser requester) {
        return new JooqBieCommandRepository(dslContext, requester, this);
    }

    public BieQueryRepository bieQueryRepository(ScoreUser requester) {
        return new JooqBieQueryRepository(dslContext, requester, this);
    }

    public ContextCategoryCommandRepository contextCategoryCommandRepository(ScoreUser requester) {
        return new JooqContextCategoryCommandRepository(dslContext, requester, this);
    }

    public ContextCategoryQueryRepository contextCategoryQueryRepository(ScoreUser requester) {
        return new JooqContextCategoryQueryRepository(dslContext, requester, this);
    }

    public ContextSchemeCommandRepository contextSchemeCommandRepository(ScoreUser requester) {
        return new JooqContextSchemeCommandRepository(dslContext, requester, this);
    }

    public ContextSchemeQueryRepository contextSchemeQueryRepository(ScoreUser requester) {
        return new JooqContextSchemeQueryRepository(dslContext, requester, this);
    }

    public BusinessContextCommandRepository businessContextCommandRepository(ScoreUser requester) {
        return new JooqBusinessContextCommandRepository(dslContext, requester, this);
    }

    public BusinessContextQueryRepository businessContextQueryRepository(ScoreUser requester) {
        return new JooqBusinessContextQueryRepository(dslContext, requester, this);
    }

    public ModuleSetCommandRepository moduleSetCommandRepository(ScoreUser requester) {
        return new JooqModuleSetCommandRepository(dslContext, requester, this);
    }

    public ModuleSetQueryRepository moduleSetQueryRepository(ScoreUser requester) {
        return new JooqModuleSetQueryRepository(dslContext, requester, this);
    }

    public ModuleCommandRepository moduleCommandRepository(ScoreUser requester) {
        return new JooqModuleCommandRepository(dslContext, requester, this);
    }

    public ModuleQueryRepository moduleQueryRepository(ScoreUser requester) {
        return new JooqModuleQueryRepository(dslContext, requester, this);
    }

    public ModuleSetReleaseCommandRepository moduleSetReleaseCommandRepository(ScoreUser requester) {
        return new JooqModuleSetReleaseCommandRepository(dslContext, requester, this);
    }

    public ModuleSetReleaseQueryRepository moduleSetReleaseQueryRepository(ScoreUser requester) {
        return new JooqModuleSetReleaseQueryRepository(dslContext, requester, this);
    }

    public ModuleManifestCommandRepository moduleManifestCommandRepository(ScoreUser requester) {
        return new JooqModuleManifestCommandRepository(dslContext, requester, this);
    }

    public ModuleManifestQueryRepository moduleManifestQueryRepository(ScoreUser requester) {
        return new JooqModuleManifestQueryRepository(dslContext, requester, this);
    }

    public NamespaceCommandRepository namespaceCommandRepository(ScoreUser requester) {
        return new JooqNamespaceCommandRepository(dslContext, requester, this);
    }

    public NamespaceQueryRepository namespaceQueryRepository(ScoreUser requester) {
        return new JooqNamespaceQueryRepository(dslContext, requester, this);
    }

    //

    public CoreComponentRepositoryForModuleSetRelease coreComponentRepositoryForModuleSetRelease(ScoreUser requester) {
        return new JooqCoreComponentRepositoryForModuleSetRelease(dslContext, requester, this);
    }

    public CodeListCommandRepository codeListCommandRepository(ScoreUser requester) {
        return new JooqCodeListCommandRepository(dslContext, requester, this);
    }

    public CodeListQueryRepository codeListQueryRepository(ScoreUser requester) {
        return new JooqCodeListQueryRepository(dslContext, requester, this);
    }

    public AgencyIdListCommandRepository agencyIdListCommandRepository(ScoreUser requester) {
        return new JooqAgencyIdListCommandRepository(dslContext, requester, this);
    }

    public AgencyIdListQueryRepository agencyIdListQueryRepository(ScoreUser requester) {
        return new JooqAgencyIdListQueryRepository(dslContext, requester, this);
    }

    public ReleaseCommandRepository releaseCommandRepository(ScoreUser requester) {
        return new JooqReleaseCommandRepository(dslContext, requester, this);
    }

    public ReleaseQueryRepository releaseQueryRepository(ScoreUser requester) {
        return new JooqReleaseQueryRepository(dslContext, requester, this);
    }

    public LibraryCommandRepository libraryCommandRepository(ScoreUser requester) {
        return new JooqLibraryCommandRepository(dslContext, requester, this);
    }

    public LibraryQueryRepository libraryQueryRepository(ScoreUser requester) {
        return new JooqLibraryQueryRepository(dslContext, requester, this);
    }

    public MessageCommandRepository messageCommandRepository(ScoreUser requester) {
        return new JooqMessageCommandRepository(dslContext, requester, this);
    }

    public MessageQueryRepository messageQueryRepository(ScoreUser requester) {
        return new JooqMessageQueryRepository(dslContext, requester, this);
    }

    public TagCommandRepository tagCommandRepository(ScoreUser requester) {
        return new JooqTagCommandRepository(dslContext, requester, this);
    }

    public TagQueryRepository tagQueryRepository(ScoreUser requester) {
        return new JooqTagQueryRepository(dslContext, requester, this);
    }

    public OpenApiDocumentCommandRepository openApiDocumentCommandRepository(ScoreUser requester) {
        return new JooqOpenApiDocumentCommandRepository(dslContext, requester, this);
    }

    public OpenApiDocumentQueryRepository openApiDocumentQueryRepository(ScoreUser requester) {
        return new JooqOpenApiDocumentQueryRepository(dslContext, requester, this);
    }

    public BiePackageCommandRepository biePackageCommandRepository(ScoreUser requester) {
        return new JooqBiePackageCommandRepository(dslContext, requester, this);
    }

    public BiePackageQueryRepository biePackageQueryRepository(ScoreUser requester) {
        return new JooqBiePackageQueryRepository(dslContext, requester, this);
    }

    public XbtQueryRepository xbtQueryRepository(ScoreUser requester) {
        return new JooqXbtQueryRepository(dslContext, requester, this);
    }

    public SeqKeyCommandRepository seqKeyCommandRepository(ScoreUser requester) {
        return new JooqSeqKeyCommandRepository(dslContext, requester, this);
    }

    public SeqKeyQueryRepository seqKeyQueryRepository(ScoreUser requester) {
        return new JooqSeqKeyQueryRepository(dslContext, requester, this);
    }

    public TopLevelAsbiepCommandRepository topLevelAsbiepCommandRepository(ScoreUser requester) {
        return new JooqTopLevelAsbiepCommandRepository(dslContext, requester, this);
    }

    public TopLevelAsbiepQueryRepository topLevelAsbiepQueryRepository(ScoreUser requester) {
        return new JooqTopLevelAsbiepQueryRepository(dslContext, requester, this);
    }

    public AbieCommandRepository abieCommandRepository(ScoreUser requester) {
        return new JooqAbieCommandRepository(dslContext, requester, this);
    }

    public AbieQueryRepository abieQueryRepository(ScoreUser requester) {
        return new JooqAbieQueryRepository(dslContext, requester, this);
    }

    public AsbieCommandRepository asbieCommandRepository(ScoreUser requester) {
        return new JooqAsbieCommandRepository(dslContext, requester, this);
    }

    public AsbieQueryRepository asbieQueryRepository(ScoreUser requester) {
        return new JooqAsbieQueryRepository(dslContext, requester, this);
    }

    public BbieCommandRepository bbieCommandRepository(ScoreUser requester) {
        return new JooqBbieCommandRepository(dslContext, requester, this);
    }

    public BbieQueryRepository bbieQueryRepository(ScoreUser requester) {
        return new JooqBbieQueryRepository(dslContext, requester, this);
    }

    public AsbiepCommandRepository asbiepCommandRepository(ScoreUser requester) {
        return new JooqAsbiepCommandRepository(dslContext, requester, this);
    }

    public AsbiepQueryRepository asbiepQueryRepository(ScoreUser requester) {
        return new JooqAsbiepQueryRepository(dslContext, requester, this);
    }

    public BbiepCommandRepository bbiepCommandRepository(ScoreUser requester) {
        return new JooqBbiepCommandRepository(dslContext, requester, this);
    }

    public BbiepQueryRepository bbiepQueryRepository(ScoreUser requester) {
        return new JooqBbiepQueryRepository(dslContext, requester, this);
    }

    public BbieScCommandRepository bbieScCommandRepository(ScoreUser requester) {
        return new JooqBbieScCommandRepository(dslContext, requester, this);
    }

    public BbieScQueryRepository bbieScQueryRepository(ScoreUser requester) {
        return new JooqBbieScQueryRepository(dslContext, requester, this);
    }

    public CommentQueryRepository commentQueryRepository(ScoreUser requester) {
        return new JooqCommentQueryRepository(dslContext, requester, this);
    }

    public CommentCommandRepository commentCommandRepository(ScoreUser requester) {
        return new JooqCommentCommandRepository(dslContext, requester, this);
    }

    public OasDocCommandRepository oasDocCommandRepository(ScoreUser requester) {
        return new JooqOasDocCommandRepository(dslContext, requester, this);
    }

    public OasDocQueryRepository oasDocQueryRepository(ScoreUser requester) {
        return new JooqOasDocQueryRepository(dslContext, requester, this);
    }

    public BieForOasDocCommandRepository bieForOasDocCommandRepository(ScoreUser requester) {
        return new JooqBieForOasDocCommandRepository(dslContext, requester, this);
    }

    public BieForOasDocQueryRepository bieForOasDocQueryRepository(ScoreUser requester) {
        return new JooqBieForOasDocQueryRepository(dslContext, requester, this);
    }

    public BusinessTermCommandRepository businessTermCommandRepository(ScoreUser requester) {
        return new JooqBusinessTermCommandRepository(dslContext, requester, this);
    }

    public BusinessTermQueryRepository businessTermQueryRepository(ScoreUser requester) {
        return new JooqBusinessTermQueryRepository(dslContext, requester, this);
    }

    public TenantCommandRepository tenantCommandRepository(ScoreUser requester) {
        return new JooqTenantCommandRepository(dslContext, requester, this);
    }

    public TenantQueryRepository tenantQueryRepository(ScoreUser requester) {
        return new JooqTenantQueryRepository(dslContext, requester, this);
    }

    public LogCommandRepository logCommandRepository(ScoreUser requester) {
        return new JooqLogCommandRepository(dslContext, requester, this, logSerializer());
    }

    // TODO: Move logSerializer() to a more appropriate location.
    // This method should not be defined here. Consider relocating it to a dedicated logging component
    // or injecting it as a dependency if needed elsewhere.
    public LogSerializer logSerializer() {
        return new LogSerializer(dslContext, new LogSnapshotResolver(dslContext, this), this);
    }
}
