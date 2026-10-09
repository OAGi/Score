package org.oagi.score.gateway.http.configuration.security.webauthn;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClientAddressResolverTest {

    private static final String PRIVATE = ClientAddressResolver.DEFAULT_TRUSTED_PROXIES;

    private static MockHttpServletRequest request(String peer, String... headers) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(peer);
        for (int i = 0; i < headers.length; i += 2) {
            request.addHeader(headers[i], headers[i + 1]);
        }
        return request;
    }

    private static String resolve(String headers, String trusted, MockHttpServletRequest request) {
        return new ClientAddressResolver(headers, trusted).resolve(request);
    }

    @Test
    void withoutAProxyHeaderTheConnectionAddressIsTheClient() {
        assertEquals("203.0.113.7", resolve("X-Forwarded-For", PRIVATE, request("203.0.113.7")));
        assertEquals("10.0.0.5", resolve("X-Forwarded-For", PRIVATE, request("10.0.0.5")));
    }

    @Test
    void aTrustedProxyHeaderGivesTheClient() {
        assertEquals("203.0.113.7",
                resolve("X-Forwarded-For", PRIVATE, request("172.18.0.2", "X-Forwarded-For", "203.0.113.7")));
    }

    @Test
    void aHeaderFromAnUntrustedPeerIsIgnored() {
        assertEquals("198.51.100.9",
                resolve("X-Forwarded-For", PRIVATE, request("198.51.100.9", "X-Forwarded-For", "203.0.113.7")));
    }

    @Test
    void aListIsReadFromTheRightSkippingTrustedProxies() {
        // 6.6.6.6 was added by the client; 203.0.113.7 is what the first proxy saw; 10.1.1.1 is a proxy
        assertEquals("203.0.113.7", resolve("X-Forwarded-For", PRIVATE,
                request("172.18.0.2", "X-Forwarded-For", "6.6.6.6, 203.0.113.7, 10.1.1.1")));
        assertEquals("203.0.113.7", resolve("X-Forwarded-For", PRIVATE,
                request("172.18.0.2", "X-Forwarded-For", "6.6.6.6", "X-Forwarded-For", "203.0.113.7, 10.1.1.1")));
    }

    @Test
    void whenEveryEntryIsATrustedProxyTheLeftmostIsTheClient() {
        assertEquals("192.168.1.20", resolve("X-Forwarded-For", PRIVATE,
                request("172.18.0.2", "X-Forwarded-For", "192.168.1.20, 10.1.1.1")));
    }

    @Test
    void theHeadersAreTriedInTheConfiguredOrder() {
        String headers = "CF-Connecting-IP, True-Client-IP, X-Forwarded-For";
        assertEquals("203.0.113.1", resolve(headers, PRIVATE, request("10.0.0.1",
                "CF-Connecting-IP", "203.0.113.1", "True-Client-IP", "203.0.113.2", "X-Forwarded-For", "203.0.113.3")));
        assertEquals("203.0.113.2", resolve(headers, PRIVATE, request("10.0.0.1",
                "True-Client-IP", "203.0.113.2", "X-Forwarded-For", "203.0.113.3")));
        assertEquals("203.0.113.3", resolve(headers, PRIVATE, request("10.0.0.1",
                "X-Forwarded-For", "203.0.113.3")));
        assertEquals("10.0.0.1", resolve(headers, PRIVATE, request("10.0.0.1")));
    }

    @Test
    void aHeaderThatIsNotAnAddressFallsThroughToTheNextOne() {
        assertEquals("203.0.113.3", resolve("CF-Connecting-IP, X-Forwarded-For", PRIVATE, request("10.0.0.1",
                "CF-Connecting-IP", "not-an-ip", "X-Forwarded-For", "203.0.113.3")));
    }

    @Test
    void anUnreadableEntryStopsTheListSoEntriesLeftOfItAreNotBelieved() {
        // the entry next to the proxy is garbage: the list cannot be trusted at all, so the connection address is used
        assertEquals("10.0.0.1", resolve("X-Forwarded-For", PRIVATE,
                request("10.0.0.1", "X-Forwarded-For", "203.0.113.3, evil.example.com")));
    }

    @Test
    void portsAndBracketsAreDropped() {
        assertEquals("203.0.113.7", resolve("X-Forwarded-For", PRIVATE,
                request("10.0.0.1", "X-Forwarded-For", "203.0.113.7:51234")));
        assertEquals("2001:db8:0:0:0:0:0:1", resolve("X-Forwarded-For", PRIVATE,
                request("10.0.0.1", "X-Forwarded-For", "[2001:db8::1]:443")));
        assertEquals("2001:db8:0:0:0:0:0:1", resolve("X-Forwarded-For", PRIVATE,
                request("10.0.0.1", "X-Forwarded-For", "2001:db8::1")));
    }

    @Test
    void noHeadersConfiguredMeansTheConnectionAddress() {
        assertEquals("10.0.0.1", resolve("", PRIVATE, request("10.0.0.1", "X-Forwarded-For", "203.0.113.7")));
    }

    @Test
    void trustedProxiesCanBeNarrowedOrWidened() {
        // a public proxy is trusted only when listed
        assertEquals("203.0.113.7", resolve("X-Forwarded-For", "198.51.100.0/24",
                request("198.51.100.9", "X-Forwarded-For", "203.0.113.7")));
        assertEquals("10.0.0.1", resolve("X-Forwarded-For", "198.51.100.0/24",
                request("10.0.0.1", "X-Forwarded-For", "203.0.113.7")));
        assertEquals("203.0.113.7", resolve("X-Forwarded-For", "*",
                request("198.51.100.9", "X-Forwarded-For", "203.0.113.7")));
    }

    @Test
    void aMistakeInTheTrustedProxiesStopsTheApplication() {
        assertThrows(IllegalStateException.class, () -> new ClientAddressResolver("X-Forwarded-For", "10.0.0.0/99"));
        assertThrows(IllegalStateException.class, () -> new ClientAddressResolver("X-Forwarded-For", "not-a-network"));
    }

    @Test
    void parseAcceptsOnlyAddressLiterals() {
        assertEquals("1.2.3.4", ClientAddressResolver.parse(" 1.2.3.4 "));
        assertNull(ClientAddressResolver.parse("example.com"));
        assertNull(ClientAddressResolver.parse("999.1.1.1:80x"));
        assertNull(ClientAddressResolver.parse(""));
        assertNull(ClientAddressResolver.parse("[::1"));
    }

}
