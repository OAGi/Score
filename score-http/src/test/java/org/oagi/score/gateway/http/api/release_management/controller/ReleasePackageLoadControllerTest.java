package org.oagi.score.gateway.http.api.release_management.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadJob;
import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadJobId;
import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadState;
import org.oagi.score.gateway.http.api.release_management.service.package_load.ReleasePackageLoadAdvice;
import org.oagi.score.gateway.http.api.release_management.service.package_load.ReleasePackageLoadCommandService;
import org.oagi.score.gateway.http.api.release_management.service.package_load.ReleasePackageLoadPreflightReport;
import org.oagi.score.gateway.http.api.release_management.service.package_load.ReleasePackageLoadRefusedException;
import org.oagi.score.gateway.http.api.release_management.service.package_load.ReleasePackageLoadTransferSettings;
import org.oagi.score.gateway.http.api.release_management.service.package_load.ReleasePackageLoadUploads;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.configuration.handler.ScoreResponseEntityExceptionHandler;
import org.oagi.score.gateway.http.configuration.security.AccessControlAspect;
import org.oagi.score.gateway.http.configuration.security.ScoreUserArgumentResolver;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.http.MediaType;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** The routes of the loads of release packages, as they are called: what goes in and what comes out, and what is refused. */
class ReleasePackageLoadControllerTest {

    private static final ScoreUser ADMIN = new ScoreUser(new UserId(BigInteger.ONE), "admin", "Admin", "admin@example.com", true,
            List.of(ScoreRole.ADMINISTRATOR));
    private static final ScoreUser END_USER = new ScoreUser(new UserId(BigInteger.TWO), "user", "User", "user@example.com", false,
            List.of(ScoreRole.END_USER));

    private ReleasePackageLoadCommandService service;
    private ReleasePackageLoadUploads uploads;
    private SessionService sessionService;
    private MockMvc mvc;

    private static ReleasePackageLoadUploads.Upload upload(ReleasePackageLoadUploads.State state, long bytes) {
        return new ReleasePackageLoadUploads.Upload("u-1", new UserId(BigInteger.ONE), "10_0.zip", state, bytes, 100, null, "10.0", "1.2",
                Instant.now(), Path.of("/tmp/u-1.zip"));
    }

    private static ReleasePackageLoadJob job(ReleasePackageLoadState state) {
        return new ReleasePackageLoadJob(new ReleasePackageLoadJobId(BigInteger.valueOf(7)), state, "msg", "10_0.zip", "10.0", null, null,
                null, "node", new UserId(BigInteger.ONE), LocalDateTime.now(), LocalDateTime.now());
    }

    @BeforeEach
    void setUp() {
        service = mock(ReleasePackageLoadCommandService.class);
        uploads = mock(ReleasePackageLoadUploads.class);
        sessionService = mock(SessionService.class);
        signedInAs(ADMIN);
        ReleasePackageLoadController controller = new ReleasePackageLoadController();
        ReflectionTestUtils.setField(controller, "service", service);
        ReflectionTestUtils.setField(controller, "uploads", uploads);
        ReflectionTestUtils.setField(controller, "transferSettings",
                new ReleasePackageLoadTransferSettings(1234L, "", java.time.Duration.ofHours(6)));
        // As the application has it: the user of the request is given to the method, and AccessControl is checked on it.
        AspectJProxyFactory proxy = new AspectJProxyFactory(controller);
        proxy.setProxyTargetClass(true);
        proxy.addAspect(new AccessControlAspect());
        @SuppressWarnings("unchecked")
        ObjectProvider<SessionService> sessions = mock(ObjectProvider.class);
        when(sessions.getObject()).thenReturn(sessionService);
        mvc = MockMvcBuilders.standaloneSetup((Object) proxy.getProxy())
                .setCustomArgumentResolvers(new ScoreUserArgumentResolver(sessions))
                .setControllerAdvice(new ScoreResponseEntityExceptionHandler(), new ReleasePackageLoadAdvice())
                .build();
    }

    /** The user the session service gives for the authentication of the request. */
    private void signedInAs(ScoreUser user) {
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(user.username(), "x",
                AuthorityUtils.createAuthorityList("x"));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(sessionService.asScoreUser(authentication)).thenReturn(user);
    }

    @AfterEach
    void clearTheUser() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void theBytesOfTheBodyAreThePackageAndWhatIsReadOfItIsAnswered() throws Exception {
        when(uploads.receive(eq(ADMIN), eq("10_0.zip"), any(), anyLong())).thenReturn(upload(ReleasePackageLoadUploads.State.READY, 100));

        mvc.perform(put("/release-package-loads/uploads").param("fileName", "10_0.zip").contentType("application/zip")
                        .content("zip bytes".getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadId").value("u-1"))
                .andExpect(jsonPath("$.state").value("READY"))
                .andExpect(jsonPath("$.releaseNum").value("10.0"))
                .andExpect(jsonPath("$.version").value("1.2"));
        verify(uploads).receive(eq(ADMIN), eq("10_0.zip"), any(), eq(9L));
    }

    @Test
    void thePreflightIsAskedOfAPackageThatIsReadyAndIsTheReportOfIt() throws Exception {
        when(uploads.get(eq(ADMIN), eq("u-1"))).thenReturn(upload(ReleasePackageLoadUploads.State.READY, 100));
        when(service.preflight(eq(ADMIN), any())).thenReturn(new ReleasePackageLoadPreflightReport("10.0", "1.2", "V1", List.of("a problem")));

        mvc.perform(get("/release-package-loads/uploads/u-1/preflight"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ok").value(false)).andExpect(jsonPath("$.problems[0]").value("a problem"))
                .andExpect(jsonPath("$.format").value("V1"));
    }

    @Test
    void aPackageThatIsNotReadyIsNotLookedAtAndIsRefusedWithWhy() throws Exception {
        when(uploads.get(eq(ADMIN), eq("u-1"))).thenReturn(upload(ReleasePackageLoadUploads.State.RECEIVING, 10));

        mvc.perform(get("/release-package-loads/uploads/u-1/preflight"))
                .andExpect(status().isConflict()).andExpect(header().exists("X-Error-Message"));
        verifyNoInteractions(service);
    }

    @Test
    void aLoadIsStartedFromAPackageThatHasComeAndIsAnsweredWithItsRecord() throws Exception {
        ReleasePackageLoadUploads.Upload ready = upload(ReleasePackageLoadUploads.State.READY, 100);
        when(uploads.take(eq(ADMIN), eq("u-1"))).thenReturn(ready);
        when(service.start(eq(ADMIN), eq(ready.file()), eq("10_0.zip"))).thenReturn(job(ReleasePackageLoadState.PENDING));

        mvc.perform(post("/release-package-loads").contentType(MediaType.APPLICATION_JSON).content("{\"uploadId\":\"u-1\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(7)).andExpect(jsonPath("$.state").value("PENDING"))
                .andExpect(jsonPath("$.active").value(true)).andExpect(jsonPath("$.releaseNum").value("10.0"));
    }

    @Test
    void aLoadThatIsRefusedIsAConflictAndSaysWhy() throws Exception {
        ReleasePackageLoadUploads.Upload ready = upload(ReleasePackageLoadUploads.State.READY, 100);
        when(uploads.take(any(), any())).thenReturn(ready);
        when(service.start(any(), any(), any())).thenThrow(new ReleasePackageLoadRefusedException("Another load goes on."));

        mvc.perform(post("/release-package-loads").contentType(MediaType.APPLICATION_JSON).content("{\"uploadId\":\"u-1\"}"))
                .andExpect(status().isConflict()).andExpect(header().string("X-Error-Message", "Another load goes on."))
                .andExpect(content().string("Another load goes on."));
    }

    @Test
    void howALoadGoesIsAskedByItsNumberAndTheLatestAreListed() throws Exception {
        when(service.get(eq(ADMIN), eq(new ReleasePackageLoadJobId(BigInteger.valueOf(7))))).thenReturn(job(ReleasePackageLoadState.COPYING));
        when(service.recent(eq(ADMIN), eq(5))).thenReturn(List.of(job(ReleasePackageLoadState.SUCCEEDED)));

        mvc.perform(get("/release-package-loads/7")).andExpect(status().isOk()).andExpect(jsonPath("$.state").value("COPYING"))
                .andExpect(jsonPath("$.active").value(true));
        mvc.perform(get("/release-package-loads").param("limit", "5")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].state").value("SUCCEEDED")).andExpect(jsonPath("$[0].active").value(false));
    }

    @Test
    void theSwapIsUndoneByTheNumberOfTheLoad() throws Exception {
        when(service.rollback(eq(ADMIN), eq(new ReleasePackageLoadJobId(BigInteger.valueOf(7))))).thenReturn(job(ReleasePackageLoadState.SUCCEEDED));

        mvc.perform(post("/release-package-loads/7/rollback")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void theSizeAPackageMayBeIsToldToAnAdministrator() throws Exception {
        mvc.perform(get("/release-package-loads/settings")).andExpect(jsonPath("$.maxPackageSize").value(1234))
                .andExpect(jsonPath("$.available").value(true)).andExpect(jsonPath("$.problem").doesNotExist());
    }

    @Test
    void whenTheDatabaseUserMayNotMakeTheSchemasALoadNeedsItIsToldAndNothingIsReceived() throws Exception {
        when(service.unavailable(ADMIN)).thenReturn(List.of("the database user cannot make a schema"));
        org.mockito.Mockito.doThrow(new ReleasePackageLoadRefusedException("Release packages cannot be loaded here."))
                .when(service).requireAvailable(ADMIN);

        mvc.perform(get("/release-package-loads/settings")).andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false)).andExpect(jsonPath("$.problem").value("the database user cannot make a schema"));
        mvc.perform(put("/release-package-loads/uploads").param("fileName", "a.zip").content("x")).andExpect(status().isConflict());
        verify(uploads, org.mockito.Mockito.never()).receive(any(), any(), any(), anyLong());
    }

    @Test
    void whoIsNotAnAdministratorIsRefusedByEveryRouteAtOnce() throws Exception {
        signedInAs(END_USER);

        mvc.perform(get("/release-package-loads/settings")).andExpect(status().isForbidden());
        mvc.perform(put("/release-package-loads/uploads").param("fileName", "a.zip").content("x")).andExpect(status().isForbidden());
        mvc.perform(post("/release-package-loads").contentType(MediaType.APPLICATION_JSON).content("{\"uploadId\":\"u\"}")).andExpect(status().isForbidden());
        mvc.perform(get("/release-package-loads/7")).andExpect(status().isForbidden());
        mvc.perform(post("/release-package-loads/7/rollback")).andExpect(status().isForbidden());
        verifyNoInteractions(service, uploads);
    }
}
