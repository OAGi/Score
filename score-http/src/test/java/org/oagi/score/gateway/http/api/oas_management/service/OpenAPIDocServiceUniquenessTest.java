package org.oagi.score.gateway.http.api.oas_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.CreateOasDocRequest;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.UpdateOasDocRequest;
import org.oagi.score.gateway.http.api.oas_management.model.OasDoc;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocId;
import org.oagi.score.gateway.http.api.oas_management.repository.OasDocCommandRepository;
import org.oagi.score.gateway.http.api.oas_management.repository.OasDocQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Create, Update and the {@code check_uniqueness} endpoint all go through the same
 * {@code OasDocQueryRepository#checkOasDocUniqueness}, keyed on (Title, OpenAPI Version, Document
 * Version); the License Name is not part of the key.
 */
class OpenAPIDocServiceUniquenessTest {

    private OpenAPIDocService service;
    private OasDocQueryRepository query;
    private OasDocCommandRepository command;
    private ScoreUser requester;

    @BeforeEach
    void setUp() {
        service = new OpenAPIDocService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        query = mock(OasDocQueryRepository.class);
        command = mock(OasDocCommandRepository.class);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        requester = new ScoreUser(new UserId(BigInteger.valueOf(42)), "tester", "Tester",
                "tester@example.com", true, List.of(ScoreRole.DEVELOPER));
        when(repositoryFactory.oasDocQueryRepository(any())).thenReturn(query);
        when(repositoryFactory.oasDocCommandRepository(any())).thenReturn(command);
    }

    private CreateOasDocRequest createRequest(String licenseName) {
        CreateOasDocRequest request = new CreateOasDocRequest(requester);
        request.setTitle("dup title");
        request.setOpenAPIVersion("3.1");
        request.setVersion("1.0");
        request.setLicenseName(licenseName);
        return request;
    }

    private UpdateOasDocRequest updateRequest(String licenseName) {
        UpdateOasDocRequest request = new UpdateOasDocRequest(requester);
        request.setOasDocId(new OasDocId(BigInteger.valueOf(7)));
        request.setTitle("dup title");
        request.setOpenAPIVersion("3.1");
        request.setVersion("1.0");
        request.setLicenseName(licenseName);
        return request;
    }

    private OasDoc capturedProbe() {
        ArgumentCaptor<OasDoc> captor = ArgumentCaptor.forClass(OasDoc.class);
        verify(query).checkOasDocUniqueness(captor.capture());
        return captor.getValue();
    }

    @Test
    void createChecksTitleOpenApiVersionAndVersionWithoutLicenseName() {
        when(query.checkOasDocUniqueness(any())).thenReturn(true);

        service.createOasDoc(requester, createRequest("MIT"));

        OasDoc probe = capturedProbe();
        assertNull(probe.getOasDocId());
        assertEquals("dup title", probe.getTitle());
        assertEquals("3.1", probe.getOpenAPIVersion());
        assertEquals("1.0", probe.getVersion());
        assertNull(probe.getLicenseName());
        verify(command).createOasDoc(any());
    }

    @Test
    void updateChecksTheSameKeyExcludingItself() {
        when(query.checkOasDocUniqueness(any())).thenReturn(true);

        service.updateOasDoc(requester, updateRequest("MIT"));

        OasDoc probe = capturedProbe();
        assertEquals(BigInteger.valueOf(7), probe.getOasDocId());
        assertEquals("dup title", probe.getTitle());
        assertEquals("3.1", probe.getOpenAPIVersion());
        assertEquals("1.0", probe.getVersion());
        assertNull(probe.getLicenseName());
        verify(command).updateOasDoc(any());
    }

    @Test
    void createAndUpdateRejectADuplicateWithTheSameMessageAndSaveNothing() {
        when(query.checkOasDocUniqueness(any())).thenReturn(false);

        var onCreate = assertThrows(IllegalArgumentException.class,
                () -> service.createOasDoc(requester, createRequest("MIT")));
        var onUpdate = assertThrows(IllegalArgumentException.class,
                () -> service.updateOasDoc(requester, updateRequest("MIT")));

        assertEquals(onCreate.getMessage(), onUpdate.getMessage());
        assertTrue(onCreate.getMessage().contains("Title, OpenAPI Version, and Document Version"));
        verify(command, never()).createOasDoc(any());
        verify(command, never()).updateOasDoc(any());
    }

    @Test
    void checkUniquenessEndpointDelegatesToTheSameQuery() {
        when(query.checkOasDocUniqueness(any())).thenReturn(false);
        OasDoc oasDoc = new OasDoc();
        oasDoc.setTitle("dup title");
        oasDoc.setOpenAPIVersion("3.1");

        assertEquals(false, service.checkOasDocUniqueness(requester, oasDoc));
        verify(query).checkOasDocUniqueness(oasDoc);
    }

    @Test
    void createAndUpdateRejectABlankTitleAndSaveNothing() {
        for (String title : new String[]{null, "", "   "}) {
            CreateOasDocRequest create = createRequest("MIT");
            create.setTitle(title);
            UpdateOasDocRequest update = updateRequest("MIT");
            update.setTitle(title);

            assertThrows(IllegalArgumentException.class, () -> service.createOasDoc(requester, create));
            assertThrows(IllegalArgumentException.class, () -> service.updateOasDoc(requester, update));
        }
        verify(command, never()).createOasDoc(any());
        verify(command, never()).updateOasDoc(any());
    }
}
