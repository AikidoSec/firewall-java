package dev.aikido.agent_api.background.cloud;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;

public final class SSLContextHelper {
    private SSLContextHelper() {}
    public static SSLContext createDefaultSSLContext() throws Exception {
        // Get the default TrustManagerFactory
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null); // Use the default trust store

        // Create an SSLContext with the default TrustManager
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

        return sslContext;
    }
}
