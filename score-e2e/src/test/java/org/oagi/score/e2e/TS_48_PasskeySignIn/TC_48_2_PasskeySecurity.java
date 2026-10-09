package org.oagi.score.e2e.TS_48_PasskeySignIn;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.impl.AuthenticatedApiClient;
import org.oagi.score.e2e.impl.AuthenticatedApiClient.ApiResponse;
import org.oagi.score.e2e.obj.AppUserObject;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The server-side rules around passkeys, driven through the REST API with the cookies of a browser that signed in:
 * registration only from the application's own origin, administrator-only passkey maintenance, account deletion once a
 * passkey registration has been started, and the limits on the unauthenticated passkey endpoints.
 * <p>
 * The tests that register need an instance with passkey sign-in configured for the origin in
 * {@code score-e2e.properties}; see {@link TC_48_1_PasskeySignIn}.
 */
public class TC_48_2_PasskeySecurity extends BaseTest {

    private static final String REGISTER_OPTIONS = "/api/webauthn/register/options";

    private AppUserObject appUser;

    @BeforeEach
    public void init() {
        super.init();
        appUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
    }

    @AfterEach
    public void cleanUp() {
        getAPIFactory().getApplicationSettingsAPI().deletePasskeysOfLoginId(appUser.getLoginId());
        getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(appUser.getLoginId());
    }

    private void requirePasskeysConfigured() {
        getDriver().get(getConfig().getBaseUrl().resolve("/api/info/passkey").toString());
        String body = new WebDriverWait(getDriver(), Duration.ofSeconds(20))
                .until(ExpectedConditions.presenceOfElementLocated(By.tagName("body"))).getText();
        Assumptions.assumeTrue(body.replace(" ", "").contains("\"enabled\":true"),
                "Start the instance with SCORE_PASSKEY_RP_ID and SCORE_PASSKEY_ALLOWED_ORIGINS to run this test.");
        Assumptions.assumeTrue(getConfig().getBooleanProperty("org.oagi.score.e2e.passkeys.enabled"),
                "Set org.oagi.score.e2e.passkeys.enabled = true in score-e2e.properties to run this test.");
    }

    private AuthenticatedApiClient signedInClient(AppUserObject user) {
        loginPage().signIn(user.getLoginId(), user.getPassword());
        return new AuthenticatedApiClient(getDriver(), getConfig().getBaseUrl());
    }

    /** The origin the browser sends: scheme, host and port of the application's address. */
    private String ownOrigin() {
        URI base = getConfig().getBaseUrl();
        return base.getScheme() + "://" + base.getAuthority();
    }

    @Test
    @DisplayName("TC_48_2_TA_1")
    public void registration_is_refused_from_another_origin() {
        requirePasskeysConfigured();
        AuthenticatedApiClient api = signedInClient(appUser);

        for (String origin : new String[]{"https://evil.example.com", "null"}) {
            ApiResponse response = api.postJson(REGISTER_OPTIONS, "{}", "Origin", origin);
            assertEquals(403, response.statusCode(), "Origin " + origin);
        }
        assertFalse(getAPIFactory().getApplicationSettingsAPI().hasPasskeyRecords(appUser.getAppUserId()),
                "A refused request must not start a registration");
    }

    @Test
    @DisplayName("TC_48_2_TA_2")
    public void registration_is_accepted_from_the_applications_own_origin() {
        requirePasskeysConfigured();
        AuthenticatedApiClient api = signedInClient(appUser);

        ApiResponse response = api.postJson(REGISTER_OPTIONS, "{}", "Origin", ownOrigin());

        assertEquals(200, response.statusCode(), response.body());
        assertTrue(response.body().contains("challenge"), response.body());
    }

    @Test
    @DisplayName("TC_48_2_TA_3")
    public void administrator_can_delete_an_account_whose_passkey_registration_was_started() {
        requirePasskeysConfigured();
        AuthenticatedApiClient userApi = signedInClient(appUser);
        assertEquals(200, userApi.postJson(REGISTER_OPTIONS, "{}", "Origin", ownOrigin()).statusCode());
        assertTrue(getAPIFactory().getApplicationSettingsAPI().hasPasskeyRecords(appUser.getAppUserId()),
                "Starting a registration stores the WebAuthn identity before any passkey exists");

        getDriver().get(getConfig().getBaseUrl().resolve("/logout").toString());
        AppUserObject admin = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(true);
        try {
            AuthenticatedApiClient adminApi = signedInClient(admin);

            ApiResponse response = adminApi.delete("/api/accounts/" + appUser.getAppUserId());

            assertTrue(response.statusCode() >= 200 && response.statusCode() < 300,
                    "Deleting the account should succeed, was " + response.statusCode() + ": " + response.body());
            assertNull(getAPIFactory().getAppUserAPI().getAppUserByLoginID(appUser.getLoginId()));
            assertFalse(getAPIFactory().getApplicationSettingsAPI().hasPasskeyRecords(appUser.getAppUserId()));
        } finally {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(admin.getLoginId());
        }
    }

    @Test
    @DisplayName("TC_48_2_TA_4")
    public void passkey_lookup_is_limited_per_login_id() {
        AuthenticatedApiClient api = signedInClient(appUser);
        String body = "{\"username\":\"" + UUID.randomUUID() + "\"}";

        ApiResponse first = api.postJson("/api/info/passkey/lookup", body);
        assertEquals(200, first.statusCode(), first.body());
        assertTrue(first.body().contains("\"passkey\""), first.body());

        // 10 lookups per login ID and minute by default; a few more than that must be refused.
        boolean refused = false;
        for (int i = 0; i < 15 && !refused; i++) {
            refused = api.postJson("/api/info/passkey/lookup", body).statusCode() == 429;
        }
        assertTrue(refused, "Repeated lookups of one login ID should be answered with 429");

        // Another login ID has its own allowance.
        ApiResponse other = api.postJson("/api/info/passkey/lookup",
                "{\"username\":\"" + UUID.randomUUID() + "\"}");
        assertEquals(200, other.statusCode(), other.body());
    }

    @Test
    @DisplayName("TC_48_2_TA_5")
    public void only_an_administrator_can_remove_passkeys_of_other_accounts() {
        AuthenticatedApiClient userApi = signedInClient(appUser);
        ApiResponse byUser = userApi.delete("/api/passkey-admin/users/" + appUser.getAppUserId());
        assertTrue(byUser.statusCode() >= 400 && byUser.statusCode() < 500,
                "A non-administrator must be refused, was " + byUser.statusCode());
        ApiResponse staleByUser = userApi.delete("/api/passkey-admin/stale");
        assertTrue(staleByUser.statusCode() >= 400 && staleByUser.statusCode() < 500,
                "A non-administrator must be refused, was " + staleByUser.statusCode());

        getDriver().get(getConfig().getBaseUrl().resolve("/logout").toString());
        AppUserObject admin = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(true);
        try {
            AuthenticatedApiClient adminApi = signedInClient(admin);
            ApiResponse byAdmin = adminApi.delete("/api/passkey-admin/users/" + appUser.getAppUserId());
            assertEquals(200, byAdmin.statusCode(), byAdmin.body());
            assertTrue(byAdmin.body().contains("\"removed\""), byAdmin.body());
        } finally {
            getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(admin.getLoginId());
        }
    }

}
