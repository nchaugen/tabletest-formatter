package org.tabletest.formatter.cli;

import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Supplies the CLI version from the Maven build, so {@code --version} cannot drift from the pom.
 *
 * <p>Reads {@code version.properties}, a resource filtered by Maven to contain the project version.
 */
class VersionProvider implements CommandLine.IVersionProvider {

    @Override
    public String[] getVersion() throws IOException {
        try (InputStream stream = VersionProvider.class.getResourceAsStream("version.properties")) {
            if (stream == null) {
                return new String[] {"unknown"};
            }
            Properties properties = new Properties();
            properties.load(stream);
            return new String[] {properties.getProperty("version", "unknown")};
        }
    }
}
