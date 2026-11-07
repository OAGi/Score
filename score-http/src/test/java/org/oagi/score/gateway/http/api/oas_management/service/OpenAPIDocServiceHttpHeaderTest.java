package org.oagi.score.gateway.http.api.oas_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.CreateOasDocRequest;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.UpdateOasDocRequest;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocHttpHeader;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocHttpHeader.Direction;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocId;
import org.oagi.score.gateway.http.api.oas_management.model.OasSchema;
import org.oagi.score.gateway.http.api.oas_management.repository.OasDocCommandRepository;
import org.oagi.score.gateway.http.api.oas_management.repository.OasDocQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Issue #1791: creating and updating a document checks its HTTP headers before anything is saved. */
class OpenAPIDocServiceHttpHeaderTest {

    private OpenAPIDocService service;
    private OasDocCommandRepository command;
    private ScoreUser requester;

    @BeforeEach
    void setUp() {
        service = new OpenAPIDocService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        OasDocQueryRepository query = mock(OasDocQueryRepository.class);
        command = mock(OasDocCommandRepository.class);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        requester = new ScoreUser(new UserId(BigInteger.valueOf(42)), "tester", "Tester",
                "tester@example.com", true, List.of(ScoreRole.DEVELOPER));
        when(repositoryFactory.oasDocQueryRepository(any())).thenReturn(query);
        when(repositoryFactory.oasDocCommandRepository(any())).thenReturn(command);
        when(query.checkOasDocUniqueness(any())).thenReturn(true);
    }

    private static OasDocHttpHeader header(String name, Direction direction) {
        OasDocHttpHeader header = new OasDocHttpHeader();
        header.setName(name);
        header.setSchema(OasSchema.fromJson("{\"type\":\"string\"}"));
        header.setDirection(direction);
        return header;
    }

    private CreateOasDocRequest create(List<OasDocHttpHeader> headers) {
        CreateOasDocRequest request = new CreateOasDocRequest(requester);
        request.setTitle("t");
        request.setOpenAPIVersion("3.1");
        request.setVersion("1.0");
        request.setHttpHeaders(headers);
        return request;
    }

    private UpdateOasDocRequest update(List<OasDocHttpHeader> headers) {
        UpdateOasDocRequest request = new UpdateOasDocRequest(requester);
        request.setOasDocId(new OasDocId(BigInteger.valueOf(7)));
        request.setTitle("t");
        request.setOpenAPIVersion("3.1");
        request.setVersion("1.0");
        request.setHttpHeaders(headers);
        return request;
    }

    @Test
    void aBadHeaderSavesNothingOnCreateAndOnUpdate() {
        for (List<OasDocHttpHeader> bad : List.of(
                List.of(header("trace parent", Direction.REQUEST)),
                List.of(header("traceparent", null)),
                List.of(header("traceparent", Direction.REQUEST), header("TraceParent", Direction.REQUEST)))) {
            assertThrows(IllegalArgumentException.class, () -> service.createOasDoc(requester, create(bad)));
            assertThrows(IllegalArgumentException.class, () -> service.updateOasDoc(requester, update(bad)));
        }
        verify(command, never()).createOasDoc(any());
        verify(command, never()).updateOasDoc(any());
    }

    @Test
    void aHeaderWithoutASchemaIsRejected() {
        OasDocHttpHeader noSchema = header("traceparent", Direction.REQUEST);
        noSchema.setSchema(null);
        assertThrows(IllegalArgumentException.class, () -> service.updateOasDoc(requester, update(List.of(noSchema))));
        verify(command, never()).updateOasDoc(any());
    }

    @Test
    void validHeadersReachTheRepositoryWithTheirNamesTrimmed() {
        service.createOasDoc(requester, create(List.of(header(" traceparent ", Direction.REQUEST))));
        service.updateOasDoc(requester, update(List.of(header(" traceparent ", Direction.REQUEST))));

        ArgumentCaptor<CreateOasDocRequest> created = ArgumentCaptor.forClass(CreateOasDocRequest.class);
        ArgumentCaptor<UpdateOasDocRequest> updated = ArgumentCaptor.forClass(UpdateOasDocRequest.class);
        verify(command).createOasDoc(created.capture());
        verify(command).updateOasDoc(updated.capture());
        assertEquals("traceparent", created.getValue().getHttpHeaders().get(0).getName());
        assertEquals("traceparent", updated.getValue().getHttpHeaders().get(0).getName());
    }

    @Test
    void noHeadersAtAllLeavesTheHeadersAlone() {
        service.updateOasDoc(requester, update(null));

        ArgumentCaptor<UpdateOasDocRequest> updated = ArgumentCaptor.forClass(UpdateOasDocRequest.class);
        verify(command).updateOasDoc(updated.capture());
        // null reaches the repository as null: the repository keeps the headers as they are
        assertNull(updated.getValue().getHttpHeaders());
    }

    @Test
    void aHeaderIsCheckedAgainstTheOpenApiVersionOfTheDocument() {
        OasDocHttpHeader nullableType = header("traceparent", Direction.REQUEST);
        nullableType.setSchema(OasSchema.fromJson("{\"type\":[\"string\",\"null\"]}"));

        // OpenAPI 3.1 has a list of types, 3.0 does not
        CreateOasDocRequest v31 = create(List.of(nullableType));
        v31.setOpenAPIVersion("3.1");
        service.createOasDoc(requester, v31);
        CreateOasDocRequest v30 = create(List.of(nullableType));
        v30.setOpenAPIVersion("3.0");
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> service.createOasDoc(requester, v30));
        assertTrue(e.getMessage().contains("traceparent") && e.getMessage().contains("3.0.4"), e.getMessage());

        // the same on update, which is how a document is turned from 3.1 to 3.0 with the headers it has
        UpdateOasDocRequest toV30 = update(List.of(nullableType));
        toV30.setOpenAPIVersion("3.0");
        assertThrows(IllegalArgumentException.class, () -> service.updateOasDoc(requester, toV30));
        verify(command, times(1)).createOasDoc(any());
        verify(command, never()).updateOasDoc(any());
    }
}

