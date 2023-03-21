package org.oagi.score.e2e.impl.api.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BCryptPasswordEncoder {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final int strength;
    private final BCryptVersion version;
    private final SecureRandom random;
    private final Pattern BCRYPT_PATTERN = Pattern.compile("\\A\\$2(a|y|b)?\\$(\\d\\d)\\$[./0-9A-Za-z]{53}");

    public BCryptPasswordEncoder() {
        this(-1);
    }

    /**
     * @param strength the log rounds to use, between 4 and 31
     */
    public BCryptPasswordEncoder(int strength) {
        this(strength, null);
    }

    /**
     * @param version the version of bcrypt, can be 2a,2b,2y
     */
    public BCryptPasswordEncoder(BCryptVersion version) {
        this(version, null);
    }

    /**
     * @param version the version of bcrypt, can be 2a,2b,2y
     * @param random  the secure random instance to use
     */
    public BCryptPasswordEncoder(BCryptVersion version, SecureRandom random) {
        this(version, -1, random);
    }

    /**
     * @param strength the log rounds to use, between 4 and 31
     * @param random   the secure random instance to use
     */
    public BCryptPasswordEncoder(int strength, SecureRandom random) {
        this(BCryptVersion.$2A, strength, random);
    }

    /**
     * @param version  the version of bcrypt, can be 2a,2b,2y
     * @param strength the log rounds to use, between 4 and 31
     */
    public BCryptPasswordEncoder(BCryptVersion version, int strength) {
        this(version, strength, null);
    }

    /**
     * @param version  the version of bcrypt, can be 2a,2b,2y
     * @param strength the log rounds to use, between 4 and 31
     * @param random   the secure random instance to use
     */
    public BCryptPasswordEncoder(BCryptVersion version, int strength, SecureRandom random) {
        if (strength != -1 && (strength < BCrypt.MIN_LOG_ROUNDS || strength > BCrypt.MAX_LOG_ROUNDS)) {
            throw new IllegalArgumentException("Bad strength");
        }
        this.version = version;
        this.strength = (strength == -1) ? 10 : strength;
        this.random = random;
    }

    public String encode(CharSequence rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("rawPassword cannot be null");
        }
        String salt = getSalt();
        return BCrypt.hashpw(rawPassword.toString(), salt);
    }

    private String getSalt() {
        if (this.random != null) {
            return BCrypt.gensalt(this.version.getVersion(), this.strength, this.random);
        }
        return BCrypt.gensalt(this.version.getVersion(), this.strength);
    }

    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("rawPassword cannot be null");
        }
        if (encodedPassword == null || encodedPassword.length() == 0) {
            this.logger.warn("Empty encoded password");
            return false;
        }
        if (!this.BCRYPT_PATTERN.matcher(encodedPassword).matches()) {
            this.logger.warn("Encoded password does not look like BCrypt");
            return false;
        }
        return BCrypt.checkpw(rawPassword.toString(), encodedPassword);
    }

    public boolean upgradeEncoding(String encodedPassword) {
        if (encodedPassword == null || encodedPassword.length() == 0) {
            this.logger.warn("Empty encoded password");
            return false;
        }
        Matcher matcher = this.BCRYPT_PATTERN.matcher(encodedPassword);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Encoded password does not look like BCrypt: " + encodedPassword);
        }
        int strength = Integer.parseInt(matcher.group(2));
        return strength < this.strength;
    }

    /**
     * Stores the default bcrypt version for use in configuration.
     *
     * @author Lin Feng
     */
    public enum BCryptVersion {

        $2A("$2a"),

        $2Y("$2y"),

        $2B("$2b");

        private final String version;

        BCryptVersion(String version) {
            this.version = version;
        }

        public String getVersion() {
            return this.version;
        }

    }

}
