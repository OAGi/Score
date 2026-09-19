package org.oagi.score.e2e.impl;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
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
 * reachable from the test JVM. For {@code localhost}, a connection failure is retried against the
 * numeric loopback addresses returned by the JVM, covering dev servers bound to only IPv4 or IPv6.
 */
public class AuthenticatedApiClient {

    private final WebDriver driver;
    private final URI baseUrl;
    private final HttpClient httpClient;
    private volatile String workingLocalhostAddress;

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
        boolean localhostRequest = isLocalhost(request.uri());
        String knownAddress = workingLocalhostAddress;
        if (localhostRequest && knownAddress != null) {
            request = requestWithUri(request, withHost(request.uri(), knownAddress));
        }

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return new ApiResponse(response.statusCode(), response.body(), response.headers());
        } catch (IOException e) {
            if (localhostRequest && hasConnectException(e)) {
                IOException lastFailure = e;
                workingLocalhostAddress = null;
                for (String address : localhostAddresses()) {
                    try {
                        URI alternateUri = withHost(request.uri(), address);
                        HttpResponse<String> response = httpClient.send(
                                requestWithUri(request, alternateUri), HttpResponse.BodyHandlers.ofString());
                        workingLocalhostAddress = address;
                        return new ApiResponse(response.statusCode(), response.body(), response.headers());
                    } catch (IOException retryFailure) {
                        if (!hasConnectException(retryFailure)) {
                            throw new RuntimeException(
                                    "API request failed: " + request.method() + " " + request.uri(), retryFailure);
                        }
                        lastFailure.addSuppressed(retryFailure);
                    } catch (InterruptedException retryInterrupted) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(
                                "API request interrupted: " + request.method() + " " + request.uri(),
                                retryInterrupted);
                    }
                }
                throw new RuntimeException("API request failed: " + request.method() + " " + request.uri(), lastFailure);
            }
            throw new RuntimeException("API request failed: " + request.method() + " " + request.uri(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("API request interrupted: " + request.method() + " " + request.uri(), e);
        }
    }

    /**
     * JDK HttpClient uses the JVM's address ordering for {@code localhost}. If that ordering points
     * at a loopback family the dev server is not listening on, retry with the resolved numeric
     * loopback addresses. Retry is limited to connection failures, before an HTTP request is sent.
     */
    private static boolean isLocalhost(URI uri) {
        return "localhost".equalsIgnoreCase(uri.getHost());
    }

    private static boolean hasConnectException(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConnectException) {
                return true;
            }
        }
        return false;
    }

    private static List<String> localhostAddresses() {
        List<String> addresses = new ArrayList<>();
        try {
            for (InetAddress address : InetAddress.getAllByName("localhost")) {
                if (address.isLoopbackAddress()) {
                    String hostAddress = address.getHostAddress();
                    if (!addresses.contains(hostAddress)) {
                        addresses.add(hostAddress);
                    }
                }
            }
        } catch (UnknownHostException ignored) {
            // The original localhost request already failed; the caller will report that failure.
        }
        return addresses;
    }

    private static URI withHost(URI uri, String host) {
        StringBuilder result = new StringBuilder()
                .append(uri.getScheme()).append("://");
        if (uri.getRawUserInfo() != null) {
            result.append(uri.getRawUserInfo()).append('@');
        }
        if (host.indexOf(':') >= 0) {
            result.append('[').append(host).append(']');
        } else {
            result.append(host);
        }
        if (uri.getPort() >= 0) {
            result.append(':').append(uri.getPort());
        }
        if (uri.getRawPath() != null) {
            result.append(uri.getRawPath());
        }
        if (uri.getRawQuery() != null) {
            result.append('?').append(uri.getRawQuery());
        }
        if (uri.getRawFragment() != null) {
            result.append('#').append(uri.getRawFragment());
        }
        return URI.create(result.toString());
    }

    private static HttpRequest requestWithUri(HttpRequest original, URI uri) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .expectContinue(original.expectContinue());
        original.timeout().ifPresent(builder::timeout);
        original.version().ifPresent(builder::version);
        original.headers().map().forEach((name, values) -> values.forEach(value -> builder.header(name, value)));
        builder.method(original.method(), original.bodyPublisher().orElse(BodyPublishers.noBody()));
        return builder.build();
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
