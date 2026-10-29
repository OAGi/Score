package org.oagi.score.gateway.http.api.tag_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.tag_management.controller.payload.CreateTagRequest;
import org.oagi.score.gateway.http.api.tag_management.controller.payload.UpdateTagRequest;
import org.oagi.score.gateway.http.api.tag_management.model.TagId;
import org.oagi.score.gateway.http.api.tag_management.model.TagSummaryRecord;
import org.oagi.score.gateway.http.api.tag_management.repository.TagCommandRepository;
import org.oagi.score.gateway.http.api.tag_management.repository.TagQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TagCommandServiceTest {

    private final TagCommandService service = new TagCommandService();
    private final TagCommandRepository command = mock(TagCommandRepository.class);
    private final TagQueryRepository query = mock(TagQueryRepository.class);
    private final TagId tagId = new TagId(BigInteger.TEN);

    private final CreateTagRequest createRequest = new CreateTagRequest("tag", "#000", "#fff", null);
    private final UpdateTagRequest updateRequest = new UpdateTagRequest(tagId, "tag", "#000", "#fff", null);

    @BeforeEach
    void setUp() {
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        when(repositoryFactory.tagCommandRepository(any())).thenReturn(command);
        when(repositoryFactory.tagQueryRepository(any())).thenReturn(query);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
    }

    private ScoreUser user(ScoreRole role) {
        return new ScoreUser(new UserId(BigInteger.ONE), "user", "User", null, false, List.of(role));
    }

    @Test
    void anyUserCanUpdateAndDiscard() throws Exception {
        when(query.exists(tagId)).thenReturn(true);
        for (ScoreRole role : List.of(ScoreRole.END_USER, ScoreRole.DEVELOPER, ScoreRole.ADMINISTRATOR)) {
            service.update(user(role), updateRequest);
            service.discard(user(role), tagId);
        }
        verify(command, times(3)).update(tagId, "tag", "#000", "#fff", null);
        verify(command, times(3)).delete(tagId);
    }

    @Test
    void anyUserCanCreate() {
        for (ScoreRole role : List.of(ScoreRole.END_USER, ScoreRole.DEVELOPER, ScoreRole.ADMINISTRATOR)) {
            service.create(user(role), createRequest);
        }
        verify(command, times(3)).create("tag", "#000", "#fff", null);
    }

    @Test
    void createRejectsDuplicateName() {
        when(query.getTagSummaryByName("tag")).thenReturn(new TagSummaryRecord(tagId, "tag", "#000", "#fff"));

        assertThatThrownBy(() -> service.create(user(ScoreRole.DEVELOPER), createRequest))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(command);
    }

    @Test
    void updateRejectsNameUsedByAnotherTagButAllowsOwnName() throws Exception {
        ScoreUser developer = user(ScoreRole.DEVELOPER);
        when(query.exists(tagId)).thenReturn(true);

        when(query.getTagSummaryByName("tag"))
                .thenReturn(new TagSummaryRecord(new TagId(BigInteger.TWO), "tag", "#000", "#fff"));
        assertThatThrownBy(() -> service.update(developer, updateRequest))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(command);

        when(query.getTagSummaryByName("tag")).thenReturn(new TagSummaryRecord(tagId, "tag", "#000", "#fff"));
        service.update(developer, updateRequest);
        verify(command).update(tagId, "tag", "#000", "#fff", null);
    }
}
