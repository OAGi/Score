package org.oagi.score.gateway.http.api.bie_management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.ScoreHttpApplication;
import org.oagi.score.gateway.http.api.bie_management.data.BieCreateRequest;
import org.oagi.score.gateway.http.api.bie_management.data.BieCreateResponse;
import org.oagi.score.gateway.http.configuration.WithMockScoreUser;
import org.oagi.score.gateway.http.helper.ScoreGuid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.jooq.impl.DSL.and;
import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.service.configuration.AppUserAuthority.DEVELOPER_GRANTED_AUTHORITY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = ScoreHttpApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
public class BieCreateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DSLContext dslContext;

    private ObjectMapper mapper = new ObjectMapper();

    private ULong testAsccpManifestId;
    private ULong testBizCtxId = ULong.valueOf(1L);

    @BeforeAll
    public void prepareRequirements() {
        testAsccpManifestId = dslContext.select(ASCCP_MANIFEST.ASCCP_MANIFEST_ID)
                .from(ASCCP_MANIFEST)
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .join(RELEASE).on(ASCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(
                        ASCCP.PROPERTY_TERM.eq("Sync Purchase Order"),
                        RELEASE.RELEASE_NUM.eq("10.6")
                ))
                .fetchOneInto(ULong.class);


        boolean exists = dslContext.selectCount()
                .from(BIZ_CTX)
                .where(BIZ_CTX.BIZ_CTX_ID.eq(testBizCtxId))
                .fetchOptionalInto(Integer.class).orElse(0) == 1;
        if (!exists) {
            ULong userId = ULong.valueOf(1L);
            LocalDateTime timestamp = LocalDateTime.now();

            testBizCtxId = dslContext.insertInto(BIZ_CTX)
                    .set(BIZ_CTX.NAME, "Test Business Context")
                    .set(BIZ_CTX.GUID, ScoreGuid.randomGuid())
                    .set(BIZ_CTX.CREATED_BY, userId)
                    .set(BIZ_CTX.LAST_UPDATED_BY, userId)
                    .set(BIZ_CTX.CREATION_TIMESTAMP, timestamp)
                    .set(BIZ_CTX.LAST_UPDATE_TIMESTAMP, timestamp)
                    .returning(BIZ_CTX.BIZ_CTX_ID).fetchOne().getBizCtxId();
        }
    }

    @Test
    @WithMockScoreUser(username = "oagis", password = "oagis", role = DEVELOPER_GRANTED_AUTHORITY)
    public void shouldCreateBie() throws Exception {
        BieCreateRequest request = new BieCreateRequest();
        request.setAsccpManifestId(testAsccpManifestId.toBigInteger());
        request.setBizCtxIds(Arrays.asList(testBizCtxId.toBigInteger()));

        BieCreateResponse response =
                mapper.readValue(
                        this.mockMvc.perform(
                                put("/profile_bie/create")
                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                        .content(mapper.writeValueAsString(request)))
                                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(),
                        BieCreateResponse.class
                );

        assertNotNull(response);
        assertTrue(response.getTopLevelAsbiepId().longValue() > 0L);
    }
}
