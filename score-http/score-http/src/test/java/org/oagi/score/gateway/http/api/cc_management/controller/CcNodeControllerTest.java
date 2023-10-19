package org.oagi.score.gateway.http.api.cc_management.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.ScoreHttpApplication;
import org.oagi.score.gateway.http.api.cc_management.data.*;
import org.oagi.score.gateway.http.api.cc_management.data.node.CcAccNode;
import org.oagi.score.gateway.http.configuration.WithMockScoreUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigInteger;

import static org.jooq.impl.DSL.and;
import static org.junit.jupiter.api.Assertions.fail;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.service.configuration.AppUserAuthority.DEVELOPER_GRANTED_AUTHORITY;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = ScoreHttpApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
public class CcNodeControllerTest {

    private final String METHOD_POST = "post";
    private final String METHOD_GET = "get";

    public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private DSLContext dslContext;

    private MockMvc mockMvc;

    @BeforeAll
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(this.restDocumentation))
                .apply(springSecurity())
                .alwaysDo(document(getClass().getSimpleName()))
                .build();
    }

    private BigInteger getReleaseId(String releaseNum) {
        return dslContext.select(RELEASE.RELEASE_ID)
                .from(RELEASE)
                .where(RELEASE.RELEASE_NUM.eq("Working"))
                .fetchOneInto(BigInteger.class);
    }

    @Test
    @WithMockScoreUser(username = "oagis", password = "oagis", role = DEVELOPER_GRANTED_AUTHORITY)
    public void testCreateAccForDeveloper() throws Exception {
        CcAccCreateRequest ccAccCreateRequest = new CcAccCreateRequest();
        ccAccCreateRequest.setReleaseId(getReleaseId("Working"));
        MvcResult accMvcResult = this.callApi("/core_component/acc", ccAccCreateRequest, METHOD_POST);
        CcCreateResponse ccAccResponse = objectMapping(accMvcResult, CcCreateResponse.class);
        if (ccAccResponse.getManifestId().compareTo(BigInteger.ZERO) <= -1) {
            fail("Create Acc fail: " + ccAccResponse.getManifestId());
        }
    }

    @Test
    @WithMockScoreUser(username = "oagis", password = "oagis", role = DEVELOPER_GRANTED_AUTHORITY)
    public void testCreateAsccpForDeveloper() throws Exception {
        CcAsccpCreateRequest ccAsccpCreateRequest = new CcAsccpCreateRequest();
        BigInteger releaseId = getReleaseId("Working");
        ccAsccpCreateRequest.setReleaseId(releaseId);
        ccAsccpCreateRequest.setRoleOfAccManifestId(
                dslContext.select(ACC_MANIFEST.ACC_MANIFEST_ID)
                        .from(ACC_MANIFEST)
                        .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                        .join(RELEASE).on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                        .where(and(
                                ACC.OBJECT_CLASS_TERM.eq("All Extension"),
                                RELEASE.RELEASE_ID.eq(ULong.valueOf(releaseId))
                        ))
                        .fetchOneInto(BigInteger.class)
        );
        MvcResult asccpMvcResult = this.callApi("/core_component/asccp", ccAsccpCreateRequest, METHOD_POST);
        CcCreateResponse ccAsccpResponse = objectMapping(asccpMvcResult, CcCreateResponse.class);
        if (ccAsccpResponse.getManifestId().compareTo(BigInteger.ZERO) <= -1) {
            fail("Create Asccp fail: " + ccAsccpResponse.getManifestId());
        }
    }

    @Test
    @WithMockScoreUser(username = "oagis", password = "oagis", role = DEVELOPER_GRANTED_AUTHORITY)
    public void testCreateBccpForDeveloper() throws Exception {
        CcBccpCreateRequest ccBccpCreateRequest = new CcBccpCreateRequest();
        BigInteger releaseId = getReleaseId("Working");
        ccBccpCreateRequest.setReleaseId(releaseId);
        ccBccpCreateRequest.setBdtManifestId(
                dslContext.select(DT_MANIFEST.DT_MANIFEST_ID)
                        .from(DT_MANIFEST)
                        .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                        .join(RELEASE).on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                        .where(and(
                                DT_MANIFEST.DEN.eq("Action_ Code. Type"),
                                RELEASE.RELEASE_ID.eq(ULong.valueOf(releaseId))
                        ))
                        .fetchOneInto(BigInteger.class)
        );
        MvcResult bccpMvcResult = this.callApi("/core_component/bccp", ccBccpCreateRequest, METHOD_POST);
        CcCreateResponse ccBccpResponse = objectMapping(bccpMvcResult, CcCreateResponse.class);
        if (ccBccpResponse.getManifestId().compareTo(BigInteger.ZERO) <= -1) {
            fail("Create Bccp fail: " + ccBccpResponse.getManifestId());
        }
    }

    @Test
    @WithMockScoreUser(username = "oagis", password = "oagis", role = DEVELOPER_GRANTED_AUTHORITY)
    public void testGetAccForDeveloper() throws Exception {

    }

    @Test
    @WithMockScoreUser(username = "oagis", password = "oagis", role = DEVELOPER_GRANTED_AUTHORITY)
    public void testGetAsccpForDeveloper() throws Exception {

    }

    @Test
    @WithMockScoreUser(username = "oagis", password = "oagis", role = DEVELOPER_GRANTED_AUTHORITY)
    public void testGetBccpForDeveloper() throws Exception {

    }

    @Test
    @WithMockScoreUser(username = "oagis", password = "oagis", role = DEVELOPER_GRANTED_AUTHORITY)
    public void accTest() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        BigInteger releaseId = getReleaseId("Working");

        // create Acc
        CcAccCreateRequest ccAccCreateRequest = new CcAccCreateRequest();
        ccAccCreateRequest.setReleaseId(releaseId);
        MvcResult accMvcResult = this.callApi("/core_component/acc", ccAccCreateRequest, METHOD_POST);
        CcCreateResponse ccAccResponse = objectMapping(accMvcResult, CcCreateResponse.class);
        BigInteger accManifestId = ccAccResponse.getManifestId();
        if (accManifestId.compareTo(BigInteger.ZERO) <= -1) {
            fail("Create Acc fail: " + accManifestId);
        }

        MvcResult baseAccMvcResult = this.callApi("/core_component/acc", ccAccCreateRequest, METHOD_POST);
        CcCreateResponse baseCcAccResponse = objectMapping(baseAccMvcResult, CcCreateResponse.class);
        BigInteger baseAccManifestId = baseCcAccResponse.getManifestId();
        if (baseAccManifestId.compareTo(BigInteger.ZERO) <= -1) {
            fail("Create Base Acc fail: " + accManifestId);
        }

        // get Acc node
        LinkedMultiValueMap params = new LinkedMultiValueMap();
        MvcResult currentAccMvcResult = this.callApi("/core_component/acc/" + accManifestId, params, METHOD_GET);
        CcAccNode currentAccNode = objectMapping(currentAccMvcResult, CcAccNode.class);
        BigInteger lastAccId = currentAccNode.getAccId();

        // set baseAcc
        CcSetBaseAccRequest ccSetBaseAccRequest = new CcSetBaseAccRequest();
        ccSetBaseAccRequest.setBasedAccManifestId(baseAccManifestId);
        MvcResult setBaseAccMvcResult = this.callApi("/core_component/acc/" + accManifestId + "/base", ccSetBaseAccRequest, METHOD_POST);
        CcAccNode accNode = objectMapping(setBaseAccMvcResult, CcAccNode.class);

        if (currentAccNode.getManifestId() != accNode.getManifestId()) {
            fail("Acc ManifestId changed");
        }
        checkCcIdStack(lastAccId, accNode.getAccId());
        if (accNode.getBasedAccManifestId().equals(currentAccNode.getBasedAccManifestId())) {
            fail("BasedAccId not changed");
        }
        lastAccId = accNode.getAccId();

        // create Asccp
        CcAsccpCreateRequest ccAsccpCreateRequest = new CcAsccpCreateRequest();
        ccAsccpCreateRequest.setReleaseId(releaseId);
        ccAsccpCreateRequest.setRoleOfAccManifestId(ccAccResponse.getManifestId());
        MvcResult asccpMvcResult = this.callApi("/core_component/asccp", ccAsccpCreateRequest, METHOD_POST);
        CcCreateResponse ccAsccpResponse = objectMapping(asccpMvcResult, CcCreateResponse.class);

        // append Asccp
        CcAppendRequest ccAppendAsccpRequest = new CcAppendRequest();
        ccAppendAsccpRequest.setAccManifestId(accManifestId);
        ccAppendAsccpRequest.setAsccpManifestId(ccAsccpResponse.getManifestId());
        this.callApi("/core_component/node/append", ccAppendAsccpRequest, METHOD_POST);
        MvcResult afterAppendAsccpMvcResult = this.callApi("/core_component/acc/" + accManifestId, params, METHOD_GET);
        CcAccNode afterAppendAsccpNode = objectMapping(afterAppendAsccpMvcResult, CcAccNode.class);
        //checkCcIdStack(afterAppendAsccpNode.getAccId(), lastAccId);
        lastAccId = afterAppendAsccpNode.getAccId();

        // create bccp
        CcBccpCreateRequest ccBccpCreateRequest = new CcBccpCreateRequest();
        ccBccpCreateRequest.setReleaseId(releaseId);
        ccBccpCreateRequest.setBdtManifestId(
                dslContext.select(DT_MANIFEST.DT_MANIFEST_ID)
                        .from(DT_MANIFEST)
                        .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                        .join(RELEASE).on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                        .where(and(
                                DT_MANIFEST.DEN.eq("Action_ Code. Type"),
                                RELEASE.RELEASE_ID.eq(ULong.valueOf(releaseId))
                        ))
                        .fetchOneInto(BigInteger.class)
        );
        MvcResult bccpMvcResult = this.callApi("/core_component/bccp", ccBccpCreateRequest, METHOD_POST);
        CcCreateResponse ccBccpResponse = objectMapping(bccpMvcResult, CcCreateResponse.class);

        // append bccp
        CcAppendRequest ccAppendBccpRequest = new CcAppendRequest();
        ccAppendAsccpRequest.setAccManifestId(accManifestId);
        ccAppendAsccpRequest.setBccpManifestId(ccBccpResponse.getManifestId());
        this.callApi("/core_component/node/append", ccAppendBccpRequest, METHOD_POST);
        MvcResult afterAppendBccpMvcResult = this.callApi("/core_component/acc/" + accManifestId, params, METHOD_GET);
        CcAccNode afterAppendBccpAccNode = objectMapping(afterAppendBccpMvcResult, CcAccNode.class);
        //checkCcIdStack(afterAppendBccpAccNode.getAccId(), lastAccId);
        lastAccId = afterAppendBccpAccNode.getAccId();
    }

    private MvcResult callApi(String uri, Object params, String method) throws Exception {
        if (method.equals(METHOD_POST)) {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(params);
            return this.mockMvc.perform(
                    post(uri)
                            .content(json)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().is2xxSuccessful())
                    .andReturn();
        } else {
            return this.mockMvc.perform(
                    get(uri)
                            .params((MultiValueMap<String, String>) params)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().is2xxSuccessful())
                    .andReturn();
        }
    }

    private <T> T objectMapping(MvcResult mvcResult, Class<T> valueType) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        String response = mvcResult.getResponse().getContentAsString();
        return objectMapper.readValue(response, valueType);
    }

    private void checkCcIdStack(BigInteger originCcId, BigInteger newCcId) {
        if (originCcId.equals(newCcId)) {
            fail("CcId not changed (given: " + originCcId + ", " + newCcId + ")");
        }
    }
}
