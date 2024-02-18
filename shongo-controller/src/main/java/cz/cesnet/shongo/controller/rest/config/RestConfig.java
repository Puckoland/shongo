package cz.cesnet.shongo.controller.rest.config;

import com.google.common.base.Strings;
import cz.cesnet.shongo.controller.ControllerConfiguration;
import cz.cesnet.shongo.controller.domains.BasicAuthFilter;
import cz.cesnet.shongo.controller.domains.SSLClientCertFilter;
import cz.cesnet.shongo.ssl.SSLCommunication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

import javax.servlet.DispatcherType;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.EnumSet;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RestConfig implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {

    private static final String INTER_DOMAIN_API_PATH = "/domain/**";

    private final ControllerConfiguration configuration;

    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        factory.setPort(configuration.getRESTApiPort());

        setUpSsl(factory);
    }

    private void setUpSsl(ConfigurableWebServerFactory factory) {
        log.info("Setting up SSL for REST API");
        final String sslKeyStore = configuration.getRESTApiSslKeyStore();
        if (sslKeyStore != null) {
            log.debug("SSL keystore: {}", sslKeyStore);
            Ssl ssl = new Ssl();
            ssl.setKeyStore(configuration.getRESTApiSslKeyStore());
            ssl.setKeyStorePassword(configuration.getRESTApiSslKeyStorePassword());
            String keystoreType = configuration.getRESTApiSslKeyStoreType();
            if (!Strings.isNullOrEmpty(keystoreType)) {
                ssl.setKeyStoreType(keystoreType);
            }
//            setUpInterDomain(ssl);
            factory.setSsl(ssl);
        }
    }

    private void setUpInterDomain(Ssl ssl) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        if (configuration.isInterDomainConfigured()) {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null);
            // Load certificates of foreign domain's CAs
            for (String certificatePath : configuration.getForeignDomainsCaCertFiles()) {
                trustStore.setCertificateEntry(certificatePath.substring(0, certificatePath.lastIndexOf('.')),
                        SSLCommunication.readPEMCert(certificatePath));
            }
//            ssl.setTrustStore(trustStore);

            if (configuration.requiresClientPKIAuth()) {
                // Enable forced client auth
                ssl.setClientAuth(Ssl.ClientAuth.NEED);
                // Enable SSL client filter by certificates
                EnumSet<DispatcherType> filterTypes = EnumSet.of(DispatcherType.REQUEST);
//                contextHandler.addFilter(SSLClientCertFilter.class, INTER_DOMAIN_API_PATH, filterTypes);
            }
            else {
                EnumSet<DispatcherType> filterTypes = EnumSet.of(DispatcherType.REQUEST);
//                contextHandler.addFilter(BasicAuthFilter.class, INTER_DOMAIN_API_PATH, filterTypes);
            }
        }
    }
}
