package dev.aikido.agent;

import dev.aikido.agent.wrappers.*;
import dev.aikido.agent.wrappers.javalin.*;
import dev.aikido.agent.wrappers.jdbc.MSSQLWrapper;
import dev.aikido.agent.wrappers.jdbc.MariaDBWrapper;
import dev.aikido.agent.wrappers.jdbc.MysqlCJWrapper;
import dev.aikido.agent.wrappers.jdbc.PostgresWrapper;

import java.util.Arrays;
import java.util.List;

public final class Wrappers {
    private Wrappers() {}
    public static final List<Wrapper> WRAPPERS = Arrays.asList(
            new PostgresWrapper(),
            new SpringMVCWrapper(),
            new SpringControllerWrapper(),
            new FileWrapper(),
            new URLConnectionWrapper(),
            new InetAddressWrapper(),
            new RuntimeExecWrapper(),
            new MysqlCJWrapper(),
            new MSSQLWrapper(),
            new MariaDBWrapper(),
            new HttpClientWrapper(),
            new HttpConnectionRedirectWrapper(),
            new HttpClientSendWrapper(),
            new OkHttpWrapper(),
            new ApacheHttpClientWrapper(),
            new PathWrapper(),
            new PathsWrapper(),
            new NettyWrapper(),
            new JavalinWrapper(),
            new JavalinDataWrapper(),
            new JavalinContextClearWrapper()
    );
}
