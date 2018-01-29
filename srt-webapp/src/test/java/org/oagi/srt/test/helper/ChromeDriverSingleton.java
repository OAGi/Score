package org.oagi.srt.test.helper;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

/**
 * Created by Miroslav Ljubicic.
 */
public class ChromeDriverSingleton {
    private static WebDriver instance;

    private ChromeDriverSingleton() {
    }

    public static synchronized WebDriver getInstance() {
        if (instance == null) {
            instance = new ChromeDriver();
        }
        return instance;
    }

    public static synchronized void quitDriver() {
        if (instance != null) {
            instance.quit();
            instance = null;
        }
    }
}
