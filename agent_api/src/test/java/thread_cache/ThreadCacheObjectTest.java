package thread_cache;

import dev.aikido.agent_api.background.ServiceConfiguration;
import dev.aikido.agent_api.background.cloud.api.ReportingApi;
import dev.aikido.agent_api.thread_cache.ThreadCacheObject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ThreadCacheObjectTest {
    @Test
    public void update() {
        ThreadCacheObject tCache = new ThreadCacheObject(null, null, null, null, Optional.of(new ReportingApi.APIListsResponse(List.of(
                new ReportingApi.ListsResponseEntry("geoip", "description", List.of(
                        "1.2.3.4",
                        "192.168.2.1/24",
                        "fd00:1234:5678:9abc::1",
                        "fd00:3234:5678:9abc::1/64",
                        "5.6.7.8/32"
                ))
        ))));

        assertEquals(new ThreadCacheObject.BlockedResult(true, "description"), tCache.isIpBlocked("1.2.3.4"));
        assertEquals(new ThreadCacheObject.BlockedResult(false, null), tCache.isIpBlocked("2.3.4.5"));
        assertEquals(new ThreadCacheObject.BlockedResult(true, "description"), tCache.isIpBlocked("192.168.2.2"));
        assertEquals(new ThreadCacheObject.BlockedResult(true, "description"), tCache.isIpBlocked("fd00:1234:5678:9abc::1"));
        assertEquals(new ThreadCacheObject.BlockedResult(false, null), tCache.isIpBlocked("fd00:1234:5678:9abc::2"));
        assertEquals(new ThreadCacheObject.BlockedResult(true, "description"), tCache.isIpBlocked("fd00:3234:5678:9abc::1"));
        assertEquals(new ThreadCacheObject.BlockedResult(true, "description"), tCache.isIpBlocked("fd00:3234:5678:9abc::2"));
        assertEquals(new ThreadCacheObject.BlockedResult(true, "description"), tCache.isIpBlocked("5.6.7.8"));
        assertEquals(new ThreadCacheObject.BlockedResult(false, null), tCache.isIpBlocked("1.2"));
    }
}
