package org.oagi.score.e2e.impl;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;

/**
 * Minimal authenticated REST client for e2e tests.
 *
 * <p>Some backend behaviours are server-side only and cannot be reached through the Selenium UI
 * (the UI hides menus, disables buttons, validates client-side, or never sends the relevant
 * request) — e.g. authorization gates, input validation, idempotent/find-or-create writes,
 * transactional rollbacks, and other negative/edge cases driven by direct API calls. To exercise
 * those, this client forwards the cookies established by a normal Selenium login and calls the
 * backend through the same {@code {baseUrl}/api/...} origin the browser uses (the Angular dev
 * server proxies {@code /api} to the backend and strips the prefix). It is generic and may be used
 * by any test suite, not just Business Term.
 *
 * <p>The SPA security chain is session-cookie based with CSRF disabled, so forwarding the browser's
 * cookies (the session cookie — {@code SESSION} under Spring Session — plus any others) is enough;
 * no CSRF token is required. Score ids serialise as bare JSON numbers ({@code @JsonValue} on
 * {@code Id.value()}), so request bodies use plain numbers.
 *
 * <p>Typical usage: sign in through the UI, then
 * {@code new AuthenticatedApiClient(getDriver(), getConfig().getBaseUrl())} and call
 * {@link #getJson}, {@link #postJson}, {@link #putJson}, {@link #delete}, {@link #deleteJson}, or
 * {@link #postCsv}; the returned {@link ApiResponse} exposes the status code, body, and headers.
 *
 * <p>Note: when running against a remote Selenium grid, the test JVM resolves {@code baseUrl}
 * itself (it does not borrow the browser container's network view), so {@code baseUrl} must be
 * reachable from the test JVM. If a local dev server binds the loopback to IPv6 only (e.g. an
 * {@code ng serve} listening on {@code [::1]}) while the test JVM prefers IPv4, run the suite with
 * {@code -Djava.net.preferIPv6Addresses=true} so {@code localhost} resolves to the address the dev
 * server is actually listening on.
 */
public class AuthenticatedApiClient {

    private final WebDriver driver;
    private final URI baseUrl;
    private final HttpClient httpClient;

    public AuthenticatedApiClient(WebDriver driver, URI baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl;
        // Force HTTP/1.1: the Angular dev-server proxy does not negotiate the HTTP/2 (h2c) upgrade
        // that HttpClient attempts by default, which otherwise hangs the request until it times out.
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    /** Outcome of an API call: status code, raw body, and response-header access. */
    public static final class ApiResponse {
        private final int statusCode;
        private final String body;
        private final java.net.http.HttpHeaders headers;

        ApiResponse(int statusCode, String body, java.net.http.HttpHeaders headers) {
            this.statusCode = statusCode;
            this.body = body;
            this.headers = headers;
        }

        public int statusCode() {
            return statusCode;
        }

        public String body() {
            return body;
        }

        public String header(String name) {
            return headers.firstValue(name).orElse(null);
        }
    }

    public ApiResponse getJson(String path) {
        return send(baseRequest(path).header("Accept", "application/json").GET().build());
    }

    public ApiResponse postJson(String path, String json) {
        return send(baseRequest(path)
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(json, StandardCharsets.UTF_8)).build());
    }

    public ApiResponse putJson(String path, String json) {
        return send(baseRequest(path)
                .header("Content-Type", "application/json")
                .PUT(BodyPublishers.ofString(json, StandardCharsets.UTF_8)).build());
    }

    public ApiResponse delete(String path) {
        return send(baseRequest(path).DELETE().build());
    }

    /** DELETE with a JSON request body (e.g. the batch-discard endpoint). */
    public ApiResponse deleteJson(String path, String json) {
        return send(baseRequest(path)
                .header("Content-Type", "application/json")
                .method("DELETE", BodyPublishers.ofString(json, StandardCharsets.UTF_8)).build());
    }

    /** Upload a CSV file as {@code multipart/form-data} with part name {@code file}. */
    public ApiResponse postCsv(String path, byte[] csv, String filename) {
        String boundary = "----scoreE2E" + Long.toHexString(System.nanoTime());
        byte[] body = multipartFilePart(boundary, "file", filename, "text/csv", csv);
        return send(baseRequest(path)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(BodyPublishers.ofByteArray(body)).build());
    }

    private HttpRequest.Builder baseRequest(String path) {
        return HttpRequest.newBuilder()
                .uri(baseUrl.resolve(path))
                .timeout(Duration.ofSeconds(60))
                .header("Cookie", cookieHeader())
                .header("X-Requested-With", "XMLHttpRequest");
    }

    /**
     * Forwards every cookie the browser currently holds (the session cookie — named {@code SESSION}
     * under Spring Session, or {@code JSESSIONID} otherwise — plus any others) so the call is
     * authenticated as the logged-in user, regardless of the exact session-cookie name.
     */
    private String cookieHeader() {
        Set<Cookie> cookies = driver.manage().getCookies();
        if (cookies == null || cookies.isEmpty()) {
            throw new IllegalStateException(
                    "No browser cookies are present; sign in through the UI before calling the API.");
        }
        StringBuilder sb = new StringBuilder();
        for (Cookie cookie : cookies) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(cookie.getName()).append('=').append(cookie.getValue());
        }
        return sb.toString();
    }

    private ApiResponse send(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return new ApiResponse(response.statusCode(), response.body(), response.headers());
        } catch (IOException e) {
            throw new RuntimeException("API request failed: " + request.method() + " " + request.uri(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("API request interrupted: " + request.method() + " " + request.uri(), e);
        }
    }

    private static byte[] multipartFilePart(String boundary, String name, String filename,
                                            String contentType, byte[] content) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String header = "--" + boundary + "\r\n"
                    + "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + filename + "\"\r\n"
                    + "Content-Type: " + contentType + "\r\n\r\n";
            out.write(header.getBytes(StandardCharsets.UTF_8));
            out.write(content);
            out.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to build multipart body", e);
        }
    }
}
