package org.oagi.score.e2e;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Configuration {

    private static final String SCORE_E2E_PROPERTY_FILENAME = "score-e2e.properties";

    private final Properties properties;

    public Configuration(InputStream inputStream) throws IOException {
        this.properties = new Properties();
        this.properties.load(inputStream);
    }

    public static Configuration load() {
        try (InputStream inputStream = Configuration.class.getClassLoader().getResourceAsStream(SCORE_E2E_PROPERTY_FILENAME)) {
            return new Configuration(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public URI getBaseUrl() {
        try {
            return new URI(getProperty("org.oagi.score.e2e.baseUrl"));
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    public WebDriver newWebDriver() {
        String driver = getProperty("org.oagi.score.e2e.driver");

        switch (driver) {
            case "local":
                return newLocalWebDriver();
            case "remote":
                String remoteAddress = getProperty("org.oagi.score.e2e.remoteUrl");
                try {
                    return new RemoteWebDriver(new URL(remoteAddress), newCapabilities());
                } catch (MalformedURLException e) {
                    throw new IllegalStateException(e);
                }
            default:
                throw new IllegalArgumentException("Unsupported driver: " + driver);
        }
    }

    private WebDriver newLocalWebDriver() {
        String browser = getProperty("org.oagi.score.e2e.browser");
        switch (browser) {
            case "chrome":
                return new ChromeDriver((ChromeOptions) newCapabilities());
            case "edge":
                return new EdgeDriver((EdgeOptions) newCapabilities());
        }
        throw new IllegalArgumentException("Unsupported browser: " + browser);
    }

    private Capabilities newCapabilities() {
        String browser = getProperty("org.oagi.score.e2e.browser");
        switch (browser) {
            case "chrome":
                return newChromeCapabilities();
            case "edge":
                return newEdgeCapabilities();
        }
        throw new IllegalArgumentException("Unsupported browser: " + browser);
    }

    private Capabilities newChromeCapabilities() {
        ChromeOptions chromeOptions = new ChromeOptions();
        if (getBooleanProperty("org.oagi.score.e2e.chrome.headless")) {
            chromeOptions.addArguments("--headless");
        }
        if (getBooleanProperty("org.oagi.score.e2e.chrome.disable-gpu")) {
            chromeOptions.addArguments("--disable-gpu");
        }
        // To support the docker environment.
        chromeOptions.addArguments("--remote-allow-origins=*");

        // To support TC_6_3 TC_6_3_TA_6
        Map<String, Object> chromePrefs = new HashMap<>();
        chromePrefs.put("download.default_directory",
                new File(System.getProperty("user.home"), "Downloads").getAbsolutePath());
        chromePrefs.put("safebrowsing.enabled", "true");
        chromeOptions.setExperimentalOption("prefs", chromePrefs);
        return chromeOptions;
    }

    private EdgeOptions newEdgeCapabilities() {
        EdgeOptions edgeOptions = new EdgeOptions();
        if (getBooleanProperty("org.oagi.score.e2e.edge.headless")) {
            edgeOptions.addArguments("--headless");
        }
        if (getBooleanProperty("org.oagi.score.e2e.edge.disable-gpu")) {
            edgeOptions.addArguments("--disable-gpu");
        }
        // To support the docker environment.
        edgeOptions.addArguments("--remote-allow-origins=*");

        // To support TC_6_3 TC_6_3_TA_6
        Map<String, Object> edgePrefs = new HashMap<>();
        edgePrefs.put("download.default_directory",
                new File(System.getProperty("user.home"), "Downloads").getAbsolutePath());
        edgePrefs.put("safebrowsing.enabled", "true");
        edgeOptions.setExperimentalOption("prefs", edgePrefs);
        return edgeOptions;
    }

    public String getProperty(String key) {
        return this.properties.getProperty(key);
    }

    public boolean getBooleanProperty(String key) {
        return "true".equals(this.properties.getProperty(key));
    }

    public int getIntProperty(String key) {
        return Integer.parseInt(this.properties.getProperty(key, "0"));
    }

}
