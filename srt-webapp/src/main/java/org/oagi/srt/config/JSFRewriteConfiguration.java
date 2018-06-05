package org.oagi.srt.config;

import org.ocpsoft.rewrite.annotation.RewriteConfiguration;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.servlet.config.HttpConfigurationProvider;
import org.ocpsoft.rewrite.servlet.config.rule.Join;

import javax.servlet.ServletContext;

@RewriteConfiguration
public class JSFRewriteConfiguration extends HttpConfigurationProvider {

    @Override
    public Configuration getConfiguration(ServletContext context) {
        return ConfigurationBuilder.begin()
                .addRule(Join.path("/").to("/index.jsf"))
                .addRule(Join.path("/signin").to("/views/user/login.jsf"))
                .addRule(Join.path("/signup").to("/views/user/join.jsf"))
                .addRule(Join.path("/preferences").to("/views/user/settings.jsf"))
                .addRule(Join.path("/data/manage").to("/views/resources/manage_data.jsf"))
                .addRule(Join.path("/account/manage").to("/views/user/manage_account.jsf"))
                .addRule(Join.path("/account/create").to("/views/user/details.jsf"))
                .addRule(Join.path("/account/{login_id}").to("/views/user/details.jsf?loginId={login_id}"))

                // Profile BIE Menu
                .addRule(Join.path("/profile_bie")
                        .to("/views/profile_bie/list.jsf"))
                .addRule(Join.path("/profile_bie/create")
                        .to("/views/profile_bie/create_bie.jsf"))
                .addRule(Join.path("/profile_bie/copy")
                        .to("/views/profile_bie/copy_bie.jsf"))
                .addRule(Join.path("/profile_bie/{top_level_abie_id}")
                        .to("/views/profile_bie/edit_bie.jsf?topLevelAbieId={top_level_abie_id}"))
                .addRule(Join.path("/profile_bie/{top_level_abie_id}/transfer_ownership")
                        .to("/views/profile_bie/transfer_bie_ownership.jsf?topLevelAbieId={top_level_abie_id}"))

                // BIE Expression Menu
                .addRule(Join.path("/profile_bie/expression/generate")
                        .to("/views/generate_oagis/generate.jsf"))

                // Context Management Menu
                .addRule(Join.path("/context_management/context_category")
                        .to("/views/context_category/list.jsf"))
                .addRule(Join.path("/context_management/context_category/create")
                        .to("/views/context_category/details.jsf"))
                .addRule(Join.path("/context_management/context_category/oagis-id-{context_category_guid}")
                        .to("/views/context_category/details.jsf?ctxCategoryGuid=oagis-id-{context_category_guid}"))
                .addRule(Join.path("/context_management/context_category/{context_category_id}")
                        .to("/views/context_category/details.jsf?ctxCategoryId={context_category_id}"))

                .addRule(Join.path("/context_management/context_scheme")
                        .to("/views/context_scheme/list.jsf"))
                .addRule(Join.path("/context_management/context_scheme/create")
                        .to("/views/context_scheme/details.jsf"))
                .addRule(Join.path("/context_management/context_scheme/oagis-id-{context_scheme_guid}")
                        .to("/views/context_scheme/details.jsf?ctxSchemeGuid=oagis-id-{context_scheme_guid}"))
                .addRule(Join.path("/context_management/context_scheme/{context_scheme_id}")
                        .to("/views/context_scheme/details.jsf?ctxSchemeId={context_scheme_id}"))

                .addRule(Join.path("/context_management/business_context")
                        .to("/views/business_context/list.jsf"))
                .addRule(Join.path("/context_management/business_context/create")
                        .to("/views/business_context/details.jsf"))
                .addRule(Join.path("/context_management/business_context/oagis-id-{business_context_guid}")
                        .to("/views/business_context/details.jsf?bizCtxGuid=oagis-id-{business_context_guid}"))
                .addRule(Join.path("/context_management/business_context/{business_context_id}")
                        .to("/views/business_context/details.jsf?bizCtxId={business_context_id}"))

                // Namespace Menu
                .addRule(Join.path("/namespace")
                        .to("/views/namespace/list.jsf"))
                .addRule(Join.path("/namespace/create")
                        .to("/views/namespace/details.jsf"))
                .addRule(Join.path("/namespace/{namespace_id}")
                        .to("/views/namespace/details.jsf?namespaceId={namespace_id}"))

                // Core Component Management Menu
                .addRule(Join.path("/core_component")
                        .to("/views/core_component/list.jsf"))
                .addRule(Join.path("/core_component/extension/{acc_id}")
                        .to("/views/core_component/extension.jsf?accId={acc_id}"))
                .addRule(Join.path("/core_component/acc/{acc_id}")
                        .to("/views/core_component/acc_details.jsf?accId={acc_id}"))
                .addRule(Join.path("/core_component/{acc_id}/transfer_ownership")
                        .to("/views/core_component/transfer_acc_ownership.jsf?accId={acc_id}"))
                .addRule(Join.path("/core_component/asccp/create")
                        .to("/views/core_component/select_acc.jsf"))
                .addRule(Join.path("/core_component/asccp/{asccp_id}")
                        .to("/views/core_component/asccp_details.jsf?asccpId={asccp_id}"))
                .addRule(Join.path("/core_component/bccp/create")
                        .to("/views/core_component/select_bdt.jsf"))
                .addRule(Join.path("/core_component/bccp/{bccp_id}")
                        .to("/views/core_component/bccp_details.jsf?bccpId={bccp_id}"))
                .addRule(Join.path("/release")
                        .to("/views/core_component/release/list.jsf"))
                .addRule(Join.path("/release/create")
                        .to("/views/core_component/release/details.jsf"))
                .addRule(Join.path("/release/{release_id}")
                        .to("/views/core_component/release/details.jsf?releaseId={release_id}"))
                .addRule(Join.path("/module")
                        .to("/views/core_component/module/list.jsf"))
                .addRule(Join.path("/module/create")
                        .to("/views/core_component/module/details.jsf"))
                .addRule(Join.path("/module/{module_id}")
                        .to("/views/core_component/module/details.jsf?moduleId={module_id}"))

                // Code List Menu
                .addRule(Join.path("/code_list")
                        .to("/views/code_list/list.jsf"))
                .addRule(Join.path("/code_list/{code_list_id}")
                        .to("/views/code_list/details.jsf?codeListId={code_list_id}"))
                .addRule(Join.path("/code_list/create/without_base")
                        .to("/views/code_list/create_wo_base.jsf"))
                .addRule(Join.path("/code_list/create/from_another")
                        .to("/views/code_list/create_from_another_selection.jsf"))
                .addRule(Join.path("/code_list/create/from_another/{code_list_id}")
                        .to("/views/code_list/create_from_another.jsf?codeListId={code_list_id}"))
                ;
    }

    @Override
    public int priority() {
        return 10;
    }
}
