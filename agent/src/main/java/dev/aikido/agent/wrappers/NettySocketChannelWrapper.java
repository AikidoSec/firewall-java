package dev.aikido.agent.wrappers;

import dev.aikido.agent_api.collectors.DNSRecordCollector;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class NettySocketChannelWrapper implements Wrapper {
    // Referenced by name (not by .class): netty-transport is compileOnly here, only present on
    // the target application's classloader.
    private static final String ABSTRACT_CHANNEL_CLASS_NAME = "io.netty.channel.AbstractChannel";

    public String getName() {
        // doConnect(SocketAddress, SocketAddress) is the low-level connect entry point every
        // Netty transport implementation overrides (NioSocketChannel, EpollSocketChannel,
        // KQueueSocketChannel, ...). SocketChannelWrapper hooks java.nio.channels.SocketChannel,
        // which only NioSocketChannel extends - Reactor Netty prefers its native epoll transport
        // on Linux whenever the native library loads (the common case in production), and
        // EpollSocketChannel implements Netty's own io.netty.channel.socket.SocketChannel
        // interface instead, structurally unrelated to the JDK one despite the identical name.
        // SocketChannelWrapper never sees those connections at all; this wrapper does regardless
        // of which transport gets picked.
        return NettyConnectAdvice.class.getName();
    }

    public ElementMatcher<? super MethodDescription> getMatcher() {
        return ElementMatchers.named("doConnect").and(ElementMatchers.takesArguments(2));
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return ElementMatchers.hasSuperType(ElementMatchers.named(ABSTRACT_CHANNEL_CLASS_NAME));
    }

    public static class NettyConnectAdvice {
        // No suppress: DNSRecordCollector.reportConnect() must be able to throw
        // SSRFException/BlockedOutboundException through to actually abort the connect. It
        // already contains every other exception internally (logs and swallows), so nothing
        // unrelated escapes here.
        @Advice.OnMethodEnter
        public static void before(
                @Advice.Argument(0) SocketAddress remoteAddress
        ) throws Throwable {
            if (!(remoteAddress instanceof InetSocketAddress)) {
                return;
            }
            InetSocketAddress inetSocketAddress = (InetSocketAddress) remoteAddress;
            InetAddress resolvedAddress = inetSocketAddress.getAddress();
            if (resolvedAddress == null) {
                // Unresolved: nothing to report yet, doConnect() will fail on its own.
                return;
            }
            DNSRecordCollector.reportConnect(inetSocketAddress.getHostString(), resolvedAddress);
        }
    }
}
