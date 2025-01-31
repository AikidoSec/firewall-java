package dev.aikido.agent;

import dev.aikido.agent.wrappers.*;
import dev.aikido.agent.wrappers.file.FileConstructorMultiArgumentWrapper;
import dev.aikido.agent.wrappers.file.FileConstructorSingleArgumentWrapper;
import dev.aikido.agent.wrappers.javalin.*;
import dev.aikido.agent.wrappers.jdbc.MSSQLWrapper;
import dev.aikido.agent.wrappers.jdbc.MariaDBWrapper;
import dev.aikido.agent.wrappers.jdbc.MysqlCJWrapper;
import dev.aikido.agent.wrappers.jdbc.PostgresWrapper;
import dev.aikido.agent.wrappers.spring.WebfluxWrapper;
import dev.aikido.agent.wrappers.spring.SpringControllerWrapper;
import dev.aikido.agent.wrappers.spring.SpringMVCWrapper;

import java.util.Arrays;
import java.util.List;

public final class Wrappers {
    private Wrappers() {}
    public static final List<Wrapper> WRAPPERS = Arrays.asList(
            new PostgresWrapper(),
            new SpringMVCWrapper(),
            new SpringControllerWrapper(),
            new FileConstructorSingleArgumentWrapper(),
            new FileConstructorMultiArgumentWrapper(),
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
            new WebfluxWrapper(),
            new JavalinWrapper(),
            new JavalinDataWrapper(),
            new JavalinContextClearWrapper()
    );
}
