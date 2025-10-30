package storage;

import dev.aikido.agent_api.storage.Packages;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class PackagesTest {

    private Packages packages;

    @BeforeEach
    void setUp() {
        packages = new Packages();
    }

    @AfterEach
    void tearDown() {
        packages.clear();
    }

    @Test
    void testAddPackage_NewPackage() {
        packages.addPackage("junit", "5.8.0");
        List<Packages.PackageInfo> result = packages.asArray();
        assertEquals(1, result.size());
        assertEquals("junit", result.get(0).name());
        assertEquals("5.8.0", result.get(0).version());
    }

    @Test
    void testAddPackage_DuplicateVersion() {
        packages.addPackage("junit", "5.8.0");
        packages.addPackage("junit", "5.8.0");
        List<Packages.PackageInfo> result = packages.asArray();
        assertEquals(1, result.size());
    }

    @Test
    void testAddPackage_DifferentVersions() {
        packages.addPackage("junit", "5.8.0");
        packages.addPackage("junit", "5.9.0");
        List<Packages.PackageInfo> result = packages.asArray();
        assertEquals(2, result.size());
    }

    @Test
    void testAddPackage_MultiplePackages() {
        packages.addPackage("junit", "5.8.0");
        packages.addPackage("mockito", "4.5.0");
        List<Packages.PackageInfo> result = packages.asArray();
        assertEquals(2, result.size());
    }

    @Test
    void testAsArray_Empty() {
        List<Packages.PackageInfo> result = packages.asArray();
        assertTrue(result.isEmpty());
    }

    @Test
    void testAsArray_OrderNotGuaranteed() {
        packages.addPackage("junit", "5.8.0");
        packages.addPackage("mockito", "4.5.0");
        packages.addPackage("junit", "5.9.0");
        List<Packages.PackageInfo> result = packages.asArray();
        assertEquals(3, result.size());
    }

    @Test
    void testClear() {
        packages.addPackage("junit", "5.8.0");
        packages.clear();
        List<Packages.PackageInfo> result = packages.asArray();
        assertTrue(result.isEmpty());
    }

    @Test
    void testRequiredAt_Timestamp() {
        packages.addPackage("junit", "5.8.0");
        List<Packages.PackageInfo> result = packages.asArray();
        assertTrue(result.get(0).requiredAt() > 0);
    }

    @Test
    void testRequiredAt_DifferentTimestamps() {
        packages.addPackage("junit", "5.8.0");
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        packages.addPackage("junit", "5.9.0");
        List<Packages.PackageInfo> result = packages.asArray();
        assertTrue(result.get(1).requiredAt() >= result.get(0).requiredAt());
    }
}
