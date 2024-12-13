package dev.aikido.agent_api.background.utilities;


import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import net.openhft.chronicle.wire.DocumentContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

import static net.openhft.chronicle.queue.RollCycles.FAST_HOURLY;

public class ThreadClient {
    private static final Logger logger = LogManager.getLogger(ThreadClient.class);
    private final ChronicleQueue chronicle;
    public ThreadClient(File queueDir) throws IOException {
        chronicle = SingleChronicleQueueBuilder.single(queueDir).rollCycle(FAST_HOURLY).get();
    }
    public void send(String data) {
        try {
            ExcerptAppender appender = chronicle.createAppender();
            final DocumentContext dc = appender.writingDocument();
            dc.wire().write().text(data);
        } catch (Exception e) {
            logger.debug(e);
        }
    }
}
