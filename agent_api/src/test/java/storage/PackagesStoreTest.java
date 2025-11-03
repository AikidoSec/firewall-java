package storage;

import dev.aikido.agent_api.storage.Packages;
import dev.aikido.agent_api.storage.PackagesStore;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class PackagesStoreTest {

    @BeforeEach
    void setUp() {
        PackagesStore.clear();
    }

    @Test
    void testAddPackage() {
        PackagesStore.addPackage("pkg1", "1.0.0");
        List<Packages.PackageInfo> packages = PackagesStore.asArray();
        assertEquals(1, packages.size());
        assertEquals("pkg1", packages.get(0).name());
        assertEquals("1.0.0", packages.get(0).version());
    }

    @Test
    void testAddDuplicatePackageVersion() {
        PackagesStore.addPackage("pkg1", "1.0.0");
        PackagesStore.addPackage("pkg1", "1.0.0");
        List<Packages.PackageInfo> packages = PackagesStore.asArray();
        assertEquals(1, packages.size());
    }

    @Test
    void testAddMultipleVersions() {
        PackagesStore.addPackage("pkg1", "1.0.0");
        PackagesStore.addPackage("pkg1", "2.0.0");
        List<Packages.PackageInfo> packages = PackagesStore.asArray();
        assertEquals(2, packages.size());
    }

    @Test
    void testAsArrayEmpty() {
        List<Packages.PackageInfo> packages = PackagesStore.asArray();
        assertTrue(packages.isEmpty());
    }

    @Test
    void testClear() {
        PackagesStore.addPackage("pkg1", "1.0.0");
        PackagesStore.clear();
        List<Packages.PackageInfo> packages = PackagesStore.asArray();
        assertTrue(packages.isEmpty());
    }

    @Test
    void testThreadSafety() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 100; i++) {
            final int idx = i;
            executor.submit(() -> {
                PackagesStore.addPackage("pkg" + idx, "1.0.0");
            });
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        List<Packages.PackageInfo> packages = PackagesStore.asArray();
        assertEquals(100, packages.size());
    }
}
