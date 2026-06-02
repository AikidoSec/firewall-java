package dev.aikido.agent;

import dev.aikido.agent.wrappers.*;
import dev.aikido.agent.wrappers.executor.AbstractExecutorServiceWrapper;
import dev.aikido.agent.wrappers.executor.DelegatedExecutorServiceWrapper;
import dev.aikido.agent.wrappers.executor.ForkJoinPoolWrapper;
import dev.aikido.agent.wrappers.executor.ScheduledThreadPoolExecutorWrapper;
import dev.aikido.agent.wrappers.executor.ThreadPoolExecutorWrapper;
import dev.aikido.agent.wrappers.file.FileConstructorMultiArgumentWrapper;
import dev.aikido.agent.wrappers.file.FileConstructorSingleArgumentWrapper;
import dev.aikido.agent.wrappers.javalin.*;
import dev.aikido.agent.wrappers.jdbc.*;
import dev.aikido.agent.wrappers.spring.SpringMVCJavaxWrapper;
import dev.aikido.agent.wrappers.spring.SpringWebfluxWrapper;
import dev.aikido.agent.wrappers.spring.SpringControllerWrapper;
import dev.aikido.agent.wrappers.spring.SpringMVCJakartaWrapper;
import dev.aikido.agent.wrappers.spring.SpringMVCJavaxWrapper;
import dev.aikido.agent.wrappers.spring.SpringWebfluxWrapper;

import java.util.Arrays;
import java.util.List;

public final class Wrappers {
    private Wrappers() {}
    public static final List<Wrapper> WRAPPERS = Arrays.asList(
            new PostgresWrapper(),

            new DelegatedExecutorServiceWrapper(),
            new ThreadPoolExecutorWrapper(),
            new AbstractExecutorServiceWrapper(),
            new ForkJoinPoolWrapper(),
            new ScheduledThreadPoolExecutorWrapper(),

            new SpringMVCJakartaWrapper(),
            new SpringMVCJavaxWrapper(),
            new SpringWebfluxWrapper(),
            new SpringControllerWrapper(),
            new FileConstructorSingleArgumentWrapper(),
            new FileConstructorMultiArgumentWrapper(),
            new RuntimeExecWrapper(),
            new MysqlCJWrapper(),
            new MSSQLWrapper(),
            new MariaDBWrapper(),
            // SSRF/HTTP wrappers
            new HttpURLConnectionWrapper(),
            new InetAddressWrapper(),
            new HttpClientWrapper(),
            new HttpConnectionRedirectWrapper(),
            new HttpClientSendWrapper(),
            new OkHttpWrapper(),
            new ApacheHttpClientWrapper(),

            new PathWrapper(),
            new PathsWrapper(),
            new JavalinWrapper(),
            new JavalinDataWrapper(),
            new JavalinContextClearWrapper(),
            new SQLiteWrapper(),
            new HyperSQLWrapper()
    );
}
