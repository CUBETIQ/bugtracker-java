package com.cubetiqs.sdk.analytics.internal;

/**
 * Builds a realistic User-Agent string based on system properties.
 * Dynamically detects OS name, version, architecture, and Java version.
 */
public class UserAgentBuilder {

    /**
     * Build a realistic User-Agent string that mimics a browser.
     * Format: Mozilla/5.0 (OS) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36
     *
     * @return User-Agent string
     */
    public static String build() {
        String osName = System.getProperty("os.name", "Unknown");
        String osVersion = System.getProperty("os.version", "");
        String osArch = System.getProperty("os.arch", "");
        String javaVersion = System.getProperty("java.version", "");

        String platform = buildPlatformString(osName, osVersion, osArch);

        // return "Mozilla/5.0 (Platform) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
        return String.format(
                "Mozilla/5.0 (%s) AppleWebKit/%s (KHTML, like Gecko) Chrome/%s Safari/%s",
                platform,
                javaVersion,
                javaVersion,
                javaVersion
        );
    }

    /**
     * Build platform string based on OS.
     */
    private static String buildPlatformString(String osName, String osVersion, String osArch) {
        String lowerOsName = osName.toLowerCase();

        if (lowerOsName.contains("mac")) {
            return buildMacPlatform(osVersion);
        } else if (lowerOsName.contains("win")) {
            return buildWindowsPlatform(osVersion, osArch);
        } else if (lowerOsName.contains("linux")) {
            return buildLinuxPlatform(osArch);
        } else {
            return String.format("%s %s", osName, osVersion);
        }
    }

    /**
     * Build macOS platform string.
     * Example: Macintosh; Intel Mac OS X 10_15_7
     */
    private static String buildMacPlatform(String osVersion) {
        // Convert version like "14.0" to "10_15_7" format
        String formattedVersion = osVersion.replace('.', '_');
        String arch = System.getProperty("os.arch", "").contains("aarch64") ? "ARM" : "Intel";
        return String.format("Macintosh; %s Mac OS X %s", arch, formattedVersion);
    }

    /**
     * Build Windows platform string.
     * Example: Windows NT 10.0; Win64; x64
     */
    private static String buildWindowsPlatform(String osVersion, String osArch) {
        String arch = osArch.contains("64") ? "Win64; x64" : "Win32";
        return String.format("Windows NT %s; %s", osVersion, arch);
    }

    /**
     * Build Linux platform string.
     * Example: X11; Linux x86_64
     */
    private static String buildLinuxPlatform(String osArch) {
        return String.format("X11; Linux %s", osArch);
    }
}
