package dev.aikido.agent;

import dev.aikido.agent.wrappers.*;
import dev.aikido.agent.wrappers.javalin.*;
import dev.aikido.agent.wrappers.jdbc.*;
import dev.aikido.agent.wrappers.spring.*;

import java.util.Arrays;
import java.util.List;

public final class Wrappers {
    private Wrappers() {}
    public static final List<Wrapper> WRAPPERS = Arrays.asList(
            new PostgresWrapper(),
            new SpringFrameworkWrapper(),
            new SpringFrameworkBodyWrapper(),
            new SpringFrameworkInvokeWrapper(),
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
            new JavalinWrapper(),
            new JavalinContextWrapper(),
            new JavalinPathParamWrapper()
    );
}
