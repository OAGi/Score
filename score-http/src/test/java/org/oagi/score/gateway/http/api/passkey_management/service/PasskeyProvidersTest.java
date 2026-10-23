package org.oagi.score.gateway.http.api.passkey_management.service;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PasskeyProvidersTest {

    /** A "none" attestation object whose authenticator data carries the given AAGUID. */
    private static byte[] attestationObjectWith(UUID aaguid) {
        ByteArrayOutputStream authData = new ByteArrayOutputStream();
        authData.writeBytes(new byte[32]);                    // rpIdHash
        authData.write(0x45);                                 // flags: UP, UV, AT
        authData.writeBytes(new byte[4]);                     // signature counter
        authData.writeBytes(ByteBuffer.allocate(16)
                .putLong(aaguid.getMostSignificantBits()).putLong(aaguid.getLeastSignificantBits()).array());
        authData.write(0);
        authData.write(4);                                    // credential ID length
        authData.writeBytes(new byte[]{1, 2, 3, 4});
        authData.writeBytes(new byte[]{(byte) 0xa5, 0x01, 0x02, 0x03, 0x26, 0x20, 0x01,
                0x21, 0x58, 0x20});                           // COSE EC2 / ES256 key, x then y
        authData.writeBytes(new byte[32]);
        authData.writeBytes(new byte[]{0x22, 0x58, 0x20});
        authData.writeBytes(new byte[32]);
        byte[] data = authData.toByteArray();

        ByteArrayOutputStream cbor = new ByteArrayOutputStream();
        cbor.write(0xa3);                                     // map(3)
        cbor.writeBytes(new byte[]{0x63, 'f', 'm', 't', 0x64, 'n', 'o', 'n', 'e'});
        cbor.writeBytes(new byte[]{0x67, 'a', 't', 't', 'S', 't', 'm', 't', (byte) 0xa0});
        cbor.writeBytes(new byte[]{0x68, 'a', 'u', 't', 'h', 'D', 'a', 't', 'a', 0x58, (byte) data.length});
        cbor.writeBytes(data);
        return cbor.toByteArray();
    }

    private static String nameOf(byte[] attestationObject) {
        return PasskeyProviders.find(attestationObject).name();
    }

    @Test
    void providesTheIconAsASvgDataUri() {
        PasskeyProviders.Provider provider = PasskeyProviders.find(
                attestationObjectWith(UUID.fromString("d548826e-79b4-db40-a3d8-11116f7e8349")));
        org.junit.jupiter.api.Assertions.assertTrue(provider.iconLight().startsWith("data:image/svg+xml;base64,")
                && provider.iconDark().startsWith("data:image/svg+xml;base64,"));
    }

    @Test
    void everyListedIconIsAnInertSvg() throws Exception {
        var document = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(new org.springframework.core.io.ClassPathResource("aaguid.json").getInputStream());
        var providers = document.path("providers");
        var icons = document.path("icons");
        org.junit.jupiter.api.Assertions.assertTrue(providers.size() > 0);
        providers.forEach(provider -> {
            for (String key : new String[]{"icon_light", "icon_dark"}) {
                if (provider.hasNonNull(key)) {
                    org.junit.jupiter.api.Assertions.assertTrue(icons.has(provider.get(key).asText()),
                            "unknown " + key + " key of " + provider.path("name").asText());
                }
            }
        });
        icons.fields().forEachRemaining(entry -> {
            String icon = entry.getValue().asText();
            org.junit.jupiter.api.Assertions.assertTrue(
                    icon.matches("^data:image/svg\\+xml;base64,[A-Za-z0-9+/=]+$"), "not a base64 SVG: " + entry.getKey());
            String svg = new String(java.util.Base64.getDecoder().decode(icon.substring(icon.indexOf(',') + 1)),
                    java.nio.charset.StandardCharsets.UTF_8).toLowerCase();
            org.junit.jupiter.api.Assertions.assertFalse(svg.contains("<script") || svg.contains("javascript:"),
                    "active content in icon " + entry.getKey());
        });
    }

    @Test
    void providersThatShareAnIconGetTheSameOne() {
        var windowsHello = java.util.List.of("08987058-cadc-4b81-b6e1-30de50dcbe96",
                "9ddd1817-af5a-4672-a2b9-3e3dd95000a9", "6028b017-b1d4-4c02-b4b3-afcdafc96bb2");
        var icons = windowsHello.stream()
                .map(g -> PasskeyProviders.find(attestationObjectWith(UUID.fromString(g))).iconLight())
                .distinct().toList();
        assertEquals(1, icons.size());
    }

    @Test
    void anUnreadableListLeavesNoNamesInsteadOfFailing() {
        assertEquals(0, PasskeyProviders.load(new org.springframework.core.io.ClassPathResource("no-such-file.json")).size());
    }

    @Test
    void namesAKnownProvider() {
        assertEquals("Bitwarden",
                nameOf(attestationObjectWith(UUID.fromString("d548826e-79b4-db40-a3d8-11116f7e8349"))));
        assertEquals("Apple Passwords",
                nameOf(attestationObjectWith(UUID.fromString("fbfc3007-154e-4ecc-8c0b-6e020557d7bd"))));
    }

    @Test
    void anUnknownProviderHasNoIconOfItsOwn() {
        org.junit.jupiter.api.Assertions.assertNull(PasskeyProviders.find(null).iconLight());
    }

    @Test
    void isUnknownForAllZerosOrAnUnlistedAuthenticator() {
        assertEquals("Unknown", nameOf(attestationObjectWith(new UUID(0, 0))));
        assertEquals("Unknown", nameOf(attestationObjectWith(UUID.fromString("11111111-2222-3333-4444-555555555555"))));
    }

    @Test
    void isUnknownWithoutReadableRegistrationData() {
        assertEquals("Unknown", nameOf(null));
        assertEquals("Unknown", nameOf(new byte[0]));
        assertEquals("Unknown", nameOf(new byte[]{1, 2, 3}));
    }

}
