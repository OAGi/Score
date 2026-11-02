package org.oagi.score.gateway.http.api.namespace_management.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceDetailsRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.namespace_management.repository.NamespaceQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NamespaceCommandServiceTest {

    @Test
    void transferOwnershipOfUnknownNamespaceThrowsEmptyResultDataAccessException() {
        NamespaceCommandService service = new NamespaceCommandService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        NamespaceQueryRepository query = mock(NamespaceQueryRepository.class);
        NamespaceId namespaceId = new NamespaceId(BigInteger.valueOf(999999));
        ScoreUser admin = new ScoreUser(new UserId(BigInteger.ONE), "admin", "Admin", null, false,
                List.of(ScoreRole.ADMINISTRATOR));
        ScoreUser targetUser = new ScoreUser(new UserId(BigInteger.TEN), "target", "Target", null, false,
                List.of(ScoreRole.END_USER));

        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        when(repositoryFactory.namespaceQueryRepository(admin)).thenReturn(query);
        when(query.getNamespaceDetails(namespaceId, admin.userId())).thenReturn(null);

        assertThatThrownBy(() -> service.transferOwnership(admin, targetUser, namespaceId))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }

    @Test
    void transferOwnershipToUnknownTargetUserThrowsIllegalArgumentException() {
        NamespaceCommandService service = new NamespaceCommandService();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
        NamespaceQueryRepository query = mock(NamespaceQueryRepository.class);
        NamespaceId namespaceId = new NamespaceId(BigInteger.ONE);
        ScoreUser owner = new ScoreUser(new UserId(BigInteger.ONE), "owner", "Owner", null, false,
                List.of(ScoreRole.END_USER));
        NamespaceDetailsRecord namespaceDetails = namespaceDetails(namespaceId, owner.userId());

        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        when(repositoryFactory.namespaceQueryRepository(owner)).thenReturn(query);
        when(query.getNamespaceDetails(namespaceId, owner.userId())).thenReturn(namespaceDetails);

        assertThatThrownBy(() -> service.transferOwnership(owner, null, namespaceId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static NamespaceDetailsRecord namespaceDetails(NamespaceId namespaceId, UserId ownerUserId) {
        UserSummaryRecord owner = new UserSummaryRecord(ownerUserId, "owner", "Owner", List.of(ScoreRole.END_USER));
        return new NamespaceDetailsRecord(namespaceId, new LibraryId(BigInteger.ONE), "urn:example",
                "ex", "description", false, owner, null, null, true);
    }

}
