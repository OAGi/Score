package org.oagi.score.e2e.impl;

import java.util.Locale;

public class SystemEnvironments {

    public static OperatingSystem getOperatingSystem() {
        String osName = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if (osName.contains("mac") || osName.contains("darwin")) {
            return OperatingSystem.MacOSX;
        } else if (osName.contains("win")) {
            return OperatingSystem.Windows;
        } else if (osName.contains("nux")) {
            return OperatingSystem.Linux;
        } else {
            return OperatingSystem.Other;
        }
    }

    public enum OperatingSystem {
        Windows,
        MacOSX,
        Linux,
        Other;
    }

}
