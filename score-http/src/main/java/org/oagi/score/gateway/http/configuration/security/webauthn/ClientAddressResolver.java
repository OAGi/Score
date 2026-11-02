package org.oagi.score.gateway.http.configuration.security.webauthn;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Works out the address of the client that made a request, for the passkey rate limits.
 * <p>
 * The address the server sees is that of the nearest proxy when connectCenter runs behind one. The proxy can pass the
 * client's address on in a header; which header depends on the deployment, so the headers to read are configured, in
 * order of preference ({@code score.passkey.rate-limit.client-ip-headers}, for example
 * {@code CF-Connecting-IP,True-Client-IP,X-Forwarded-For}). The first one present with a usable address wins.
 * <p>
 * A header is believed only when the request comes from a trusted proxy
 * ({@code score.passkey.rate-limit.trusted-proxies}, a list of addresses and CIDR ranges, private networks by default,
 * or {@code *} for any); otherwise anybody could claim any address. A header that holds a list (such as
 * {@code X-Forwarded-For}) is read from the right, skipping trusted proxies, so the entries a client added itself are
 * not believed. Only list headers that your own edge proxy sets or overwrites: a header that clients can send and the
 * proxy passes on unchanged can be forged.
 * <p>
 * This is applied by the passkey rate limit only. It does not change {@code getRemoteAddr()} for the rest of the
 * application, and it works the same when the WAR runs in a standalone Tomcat, where {@code server.tomcat.*} settings
 * do not apply.
 */
@Component
public class ClientAddressResolver {

    static final String DEFAULT_TRUSTED_PROXIES =
            "127.0.0.0/8,10.0.0.0/8,172.16.0.0/12,192.168.0.0/16,169.254.0.0/16,100.64.0.0/10,::1/128,fc00::/7,fe80::/10";

    private static final Pattern IPV4 = Pattern.compile("(\\d{1,3})(\\.\\d{1,3}){3}");
    private static final Pattern IPV6 = Pattern.compile("[0-9a-fA-F:.]+");

    private final Log logger = LogFactory.getLog(getClass());

    private final List<String> headers;
    private final boolean trustAny;
    private final List<IpAddressMatcher> trustedProxies;

    @Autowired
    public ClientAddressResolver(
            @Value("${score.passkey.rate-limit.client-ip-headers:X-Forwarded-For}") String headers,
            @Value("${score.passkey.rate-limit.trusted-proxies:" + DEFAULT_TRUSTED_PROXIES + "}") String trustedProxies) {
        this.headers = split(headers);
        List<String> proxies = split(trustedProxies);
        this.trustAny = proxies.contains("*");
        List<IpAddressMatcher> matchers = new ArrayList<>();
        if (!trustAny) {
            for (String proxy : proxies) {
                try {
                    matchers.add(new IpAddressMatcher(proxy));
                } catch (RuntimeException e) {
                    throw new IllegalStateException("Invalid trusted proxy '" + proxy
                            + "' in score.passkey.rate-limit.trusted-proxies: use an address, a CIDR range such as 10.0.0.0/8, or *.", e);
                }
            }
        }
        this.trustedProxies = Collections.unmodifiableList(matchers);
        logger.info("Client address for the passkey rate limits: headers " + this.headers
                + ", trusted proxies " + (trustAny ? "any" : proxies) + ".");
    }

    /**
     * @return the client's address; the address of the connection when no configured header applies
     */
    public String resolve(HttpServletRequest request) {
        String peer = request.getRemoteAddr();
        if (headers.isEmpty() || !isTrusted(peer)) {
            return peer;
        }
        for (String header : headers) {
            String address = fromHeader(request, header);
            if (address != null) {
                return address;
            }
        }
        return peer;
    }

    private String fromHeader(HttpServletRequest request, String header) {
        List<String> entries = new ArrayList<>();
        var values = request.getHeaders(header);
        while (values != null && values.hasMoreElements()) {
            entries.addAll(split(values.nextElement()));
        }
        String leftmost = null;
        for (int i = entries.size() - 1; i >= 0; i--) {
            String address = parse(entries.get(i));
            if (address == null) {
                // not an address: nothing to the left of it can be believed either
                return null;
            }
            if (!isTrusted(address)) {
                return address;
            }
            leftmost = address;
        }
        return leftmost;
    }

    private boolean isTrusted(String address) {
        if (trustAny) {
            return true;
        }
        for (IpAddressMatcher matcher : trustedProxies) {
            try {
                if (matcher.matches(address)) {
                    return true;
                }
            } catch (RuntimeException e) {
                // an address the matcher cannot compare is not trusted
            }
        }
        return false;
    }

    /**
     * @return the address in the header entry without a port or brackets, or null if it is not an IP address
     */
    static String parse(String entry) {
        String value = entry.trim();
        if (value.startsWith("[")) {
            int end = value.indexOf(']');
            if (end < 0) {
                return null;
            }
            value = value.substring(1, end);
        } else if (value.chars().filter(c -> c == ':').count() == 1) {
            value = value.substring(0, value.indexOf(':'));
        }
        boolean literal = IPV4.matcher(value).matches()
                || (value.contains(":") && IPV6.matcher(value).matches());
        if (!literal) {
            return null;
        }
        try {
            // a literal: nothing is looked up
            return InetAddress.getByName(value).getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    private static List<String> split(String value) {
        if (value == null) {
            return List.of();
        }
        return Arrays.stream(value.split("[,\\s]+"))
                .map(String::trim)
                .filter(e -> !e.isEmpty())
                .toList();
    }

}
