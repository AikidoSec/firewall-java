package helpers.packages;

import dev.aikido.agent_api.helpers.packages.JarPackageScanner;
import dev.aikido.agent_api.storage.RuntimePackage;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JarPackageScannerTest {
    @Test
    void hashesJarWithoutMavenCoordinates() throws Exception {
        Path jar = Files.createTempFile("aikido-package", ".jar");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar))) {
            output.putNextEntry(new JarEntry("org/example/Demo.class"));
            output.write(new byte[] {1, 2, 3});
        }

        JarPackageScanner.JarScanResult result = JarPackageScanner.scan(jar.toUri().toString(), 123L);

        assertEquals(List.of(), result.packages());
        byte[] hash = MessageDigest.getInstance("SHA-1").digest(Files.readAllBytes(jar));
        assertEquals(toHex(hash), result.sha1());
        Files.deleteIfExists(jar);
    }

    @Test
    void readsAllMavenCoordinatesFromFlattenedUberJarWithoutHashingIt() throws IOException {
        Path jar = Files.createTempFile("aikido-uber-package", ".jar");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar))) {
            output.putNextEntry(new JarEntry("META-INF/maven/org.example/first/pom.properties"));
            output.write("groupId=org.example\nartifactId=first\nversion=1.0.0\n".getBytes(StandardCharsets.UTF_8));
            output.putNextEntry(new JarEntry("META-INF/maven/org.example/second/pom.properties"));
            output.write("groupId=org.example\nartifactId=second\nversion=2.0.0\n".getBytes(StandardCharsets.UTF_8));
        }

        JarPackageScanner.JarScanResult result = JarPackageScanner.scan(jar.toUri().toString(), 123L);

        assertEquals(List.of(
                new RuntimePackage("org.example:first", "1.0.0", 123L),
                new RuntimePackage("org.example:second", "2.0.0", 123L)
        ), result.packages());
        assertNull(result.sha1());
        Files.deleteIfExists(jar);
    }

    @Test
    void readsMavenCoordinates() throws IOException {
        Path jar = Files.createTempFile("aikido-package", ".jar");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar))) {
            output.putNextEntry(new JarEntry("META-INF/maven/org.example/demo/pom.properties"));
            output.write("groupId=org.example\nartifactId=demo\nversion=1.2.3\n"
                    .getBytes(StandardCharsets.UTF_8));
        }

        List<RuntimePackage> packages = JarPackageScanner.findMavenPackages(jar.toUri().toString(), 123L);

        assertEquals(List.of(new RuntimePackage("org.example:demo", "1.2.3", 123L)), packages);
        Files.deleteIfExists(jar);
    }

    @Test
    void readsMavenCoordinatesFromSpringBootNestedJar() throws IOException {
        Path nestedJar = Files.createTempFile("aikido-nested-package", ".jar");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(nestedJar))) {
            output.putNextEntry(new JarEntry("META-INF/maven/org.example/demo/pom.properties"));
            output.write("groupId=org.example\nartifactId=demo\nversion=1.2.3\n"
                    .getBytes(StandardCharsets.UTF_8));
        }

        Path outerJar = Files.createTempFile("aikido-spring-boot", ".jar");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(outerJar))) {
            output.putNextEntry(new JarEntry("BOOT-INF/lib/demo.jar"));
            output.write(Files.readAllBytes(nestedJar));
        }

        String codeSourceUrl = "nested:" + outerJar + "/!BOOT-INF/lib/demo.jar";
        List<RuntimePackage> packages = JarPackageScanner.findMavenPackages(codeSourceUrl, 123L);

        assertEquals(List.of(new RuntimePackage("org.example:demo", "1.2.3", 123L)), packages);
        Files.deleteIfExists(nestedJar);
        Files.deleteIfExists(outerJar);
    }

    @Test
    void ignoresManifestMetadataWithoutMavenCoordinates() throws IOException {
        Path jar = Files.createTempFile("aikido-package", ".jar");
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().putValue("Implementation-Title", "demo");
        manifest.getMainAttributes().putValue("Implementation-Version", "2.0.0");
        try (JarOutputStream ignored = new JarOutputStream(Files.newOutputStream(jar), manifest)) {}

        List<RuntimePackage> packages = JarPackageScanner.findMavenPackages(jar.toUri().toString(), 456L);

        assertEquals(List.of(), packages);
        Files.deleteIfExists(jar);
    }

    @Test
    void ignoresIncompleteMavenCoordinates() throws IOException {
        Path jar = Files.createTempFile("aikido-package", ".jar");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar))) {
            output.putNextEntry(new JarEntry("META-INF/maven/unknown/demo/pom.properties"));
            output.write("artifactId=demo\nversion=1.2.3\n".getBytes(StandardCharsets.UTF_8));
        }

        List<RuntimePackage> packages = JarPackageScanner.findMavenPackages(jar.toUri().toString(), 123L);

        assertEquals(List.of(), packages);
        Files.deleteIfExists(jar);
    }

    @Test
    void ignoresUnresolvedMavenProperties() throws IOException {
        Path jar = Files.createTempFile("aikido-package", ".jar");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar))) {
            output.putNextEntry(new JarEntry("META-INF/maven/unknown/demo/pom.properties"));
            output.write(("groupId=${project.groupId}\n"
                    + "artifactId=demo\n"
                    + "version=1.2.3\n").getBytes(StandardCharsets.UTF_8));
        }

        List<RuntimePackage> packages = JarPackageScanner.findMavenPackages(jar.toUri().toString(), 123L);

        assertEquals(List.of(), packages);
        Files.deleteIfExists(jar);
    }

    private static String toHex(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            result.append(String.format("%02x", value));
        }
        return result.toString();
    }
}
