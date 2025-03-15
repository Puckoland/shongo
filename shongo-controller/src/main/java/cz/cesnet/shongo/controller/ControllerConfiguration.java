package cz.cesnet.shongo.controller;

import com.google.common.base.Strings;
import cz.cesnet.shongo.PersonInformation;
import cz.cesnet.shongo.controller.booking.executable.Executable;
import cz.cesnet.shongo.controller.settings.UserSessionSettings;
import cz.cesnet.shongo.ssl.SSLCommunication;
import cz.cesnet.shongo.util.PatternParser;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.postgresql.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * Configuration for the {@link Controller}.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
@Slf4j
@Configuration
public class ControllerConfiguration
{

    @Autowired
    private Environment env;

    /**
     * Name of the controller XML configuration file.
     */
    public static final String CONFIGURATION_FILE = "configuration-file";

    /**
     * Determines whether the controller should be started as daemon without the interactive shell.
     */
    public static final String DAEMON = "daemon";

    /**
     * Time-zone in which the controller works and which is considered as default for date/times without specific zone.
     */
    public static final String TIMEZONE = "configuration.timezone";

    /**
     * Domain configuration.
     */
    public static final String DOMAIN_NAME = "configuration.domain.name";
    public static final String DOMAIN_SHORT_NAME = "configuration.domain.shortName";
    public static final String DOMAIN_ORGANIZATION = "configuration.domain.organization";

    /**
     * Database configuration.
     */
    public static final String DATABASE_DRIVER = "configuration.database.driver";
    public static final String DATABASE_URL = "configuration.database.url";
    public static final String DATABASE_USERNAME = "configuration.database.username";
    public static final String DATABASE_PASSWORD = "configuration.database.password";

    /**
     * XML-RPC configuration
     */
    public static final String RPC_HOST = "configuration.rpc.host";
    public static final String RPC_PORT = "configuration.rpc.port";
    public static final String RPC_SSL_KEYSTORE = "configuration.rpc.ssl-key-store";
    public static final String RPC_SSL_KEYSTORE_PASSWORD = "configuration.rpc.ssl-key-store-password";

    /**
     * Jade configuration.
     */
    public static final String JADE_HOST = "configuration.jade.host";
    public static final String JADE_PORT = "configuration.jade.port";
    public static final String JADE_AGENT_NAME = "configuration.jade.agent-name";
    public static final String JADE_PLATFORM_ID = "configuration.jade.platform-id";
    public static final String JADE_COMMAND_TIMEOUT = "configuration.jade.command-timeout";

    /**
     * Interdomains configuration
     */
    public static final String INTERDOMAIN = "configuration.domain.inter-domain-connection";
    public static final String INTERDOMAIN_HOST = INTERDOMAIN + ".host";
    public static final String INTERDOMAIN_PORT = INTERDOMAIN + ".port";
    public static final String INTERDOMAIN_PKI_CLIENT_AUTH = INTERDOMAIN + ".pki-client-auth";
    public static final String INTERDOMAIN_SSL_KEY_STORE = INTERDOMAIN + ".ssl-key-store";
    public static final String INTERDOMAIN_SSL_KEY_STORE_TYPE = INTERDOMAIN + ".ssl-key-store-type";
    public static final String INTERDOMAIN_SSL_KEY_STORE_PASSWORD = INTERDOMAIN + ".ssl-key-store-password";
    public static final String INTERDOMAIN_TRUSTED_CA_CERT_FILES = INTERDOMAIN + ".ssl-trust-store.ca-certificate";
    public static final String INTERDOMAIN_COMMAND_TIMEOUT = INTERDOMAIN + ".command-timeout";
    public static final String INTERDOMAIN_CACHE_REFRESH_RATE = INTERDOMAIN + ".cache-refresh-rate";
    public static final String INTERDOMAIN_BASIC_AUTH_PASSWORD = INTERDOMAIN + ".basic-auth.password";

    /**
     * Worker configuration (it runs scheduler and executor).
     */
    public static final String WORKER_PERIOD = "configuration.worker.period";
    public static final String WORKER_LOOKAHEAD = "configuration.worker.lookahead";

    /**
     * Maximum duration of reservations.
     */
    public static final String RESERVATION_ROOM_MAX_DURATION = "configuration.reservation.room.max-duration";

    /**
     * SMTP configuration.
     */
    public static final String SMTP_SENDER = "configuration.smtp.sender";
    public static final String SMTP_HOST = "configuration.smtp.host";
    public static final String SMTP_PORT = "configuration.smtp.port";
    public static final String SMTP_USERNAME = "configuration.smtp.username";
    public static final String SMTP_PASSWORD = "configuration.smtp.password";
    public static final String SMTP_SUBJECT_PREFIX = "configuration.smtp.subject-prefix";

    /**
     * CalDAV connector configuration.
     */
    public static final String CALDAV_URL = "configuration.caldav-connector.url";
    public static final String CALDAV_BASIC_AUTH_USERNAME = "configuration.caldav-connector.basic-auth.username";
    public static final String CALDAV_BASIC_AUTH_PASSWORD = "configuration.caldav-connector.basic-auth.password";



    /**
     * Period in which the executor works.
     */
    public static final String EXECUTOR_PERIOD = "configuration.executor.period";

    /**
     * Duration to modify {@link Executable} starting date/time.
     */
    public static final String EXECUTOR_EXECUTABLE_START = "configuration.executor.executable.start";

    /**
     * Duration to modify {@link Executable} ending date/time.
     */
    public static final String EXECUTOR_EXECUTABLE_END = "configuration.executor.executable.end";

    /**
     * Period in which {@link cz.cesnet.shongo.controller.executor.Executor} try to perform failed action again.
     */
    public static final String EXECUTOR_EXECUTABLE_NEXT_ATTEMPT = "configuration.executor.executable.next-attempt";

    /**
     * Maximum count of attempts for {@link cz.cesnet.shongo.controller.executor.Executor} to try to perform action.
     */
    public static final String EXECUTOR_EXECUTABLE_MAX_ATTEMPT_COUNT = "configuration.executor.executable.max-attempt-count";

    /**
     * Duration which {@link cz.cesnet.shongo.controller.executor.Executor} waits for virtual rooms to be created.
     */
    public static final String EXECUTOR_STARTING_DURATION_ROOM = "configuration.executor.starting-duration.room";

    /**
     * URL to AA server.
     */
    public static final String SECURITY_SERVER = "configuration.security.server";

    /**
     * Client id for AA server.
     */
    public static final String SECURITY_CLIENT_ID = "configuration.security.client-id";

    /**
     * Client secret for AA server.
     */
    public static final String SECURITY_CLIENT_SECRET = "configuration.security.client-secret";

    /**
     * URL to LDAP AA server.
     */
    public static final String SECURITY_LDAP_SERVER = "configuration.security.ldap.server";

    /**
     * Client secret for LDAP AA server.
     */
    public static final String SECURITY_LDAP_CLIENT_SECRET = "configuration.security.ldap.client-secret";

    /**
     * Client secret for LDAP AA server.
     */
    public static final String SECURITY_LDAP_BINDDN = "configuration.security.ldap.binddn";

    /**
     * Specifies expiration of cache for user-id by access-token.
     */
    public static final String SECURITY_EXPIRATION_USER_ID = "configuration.security.expiration.user-id";

    /**
     * Specifies expiration of cache for user information by user-id.
     */
    public static final String SECURITY_EXPIRATION_USER_INFORMATION = "configuration.security.expiration.user-information";

    /**
     * Specifies expiration of cache for user ACL by user-id.
     */
    public static final String SECURITY_EXPIRATION_ACL = "configuration.security.expiration.acl";

    /**
     * Specifies expiration of cache for user groups.
     */
    public static final String SECURITY_EXPIRATION_GROUP = "configuration.security.expiration.group";

    /**
     * Specifies filename where the root access token will be written when controller starts.
     */
    public static final String SECURITY_ROOT_ACCESS_TOKEN_FILE = "configuration.security.root-access-token";

    /**
     * Specifies expression which decides whether user is a system administrator
     * (they can use the {@link UserSessionSettings#administrationMode}).
     */
    public static final String SECURITY_AUTHORIZATION_ADMINISTRATOR = "configuration.security.authorization.administrator";

    /**
     * Specifies expression which decides whether user is a system operator
     * (they can use the {@link UserSessionSettings#administrationMode}).
     */
    public static final String SECURITY_AUTHORIZATION_OPERATOR = "configuration.security.authorization.operator";

    /**
     * Specifies expression which decides whether user can create a reservation request.
     */
    public static final String SECURITY_AUTHORIZATION_RESERVATION = "configuration.security.authorization.reservation";

    public static final String ADMINISTRATOR = "configuration.administrator";

    /**
     * Url where user can change his settings.
     */
    public static final String NOTIFICATION_USER_SETTINGS_URL = "configuration.notification.user-settings-url";

    /**
     * Primary url of a reservation requests with "${reservationRequestId}" variable which can be used in notifications.
     */
    public static final String NOTIFICATION_RESERVATION_REQUEST_URL = "configuration.notification.reservation-request-url";


    /**
     * Primary url of a reservation requests with "${reservationRequestId}" variable which can be used in notifications.
     */
    public static final String NOTIFICATION_RESERVATION_REQUEST_CONFIRMATION_URL = "configuration.notification.reservation-request-confirmation-url";

    /**
     * Filepath for FreePBX PDF guide.
     */
    public static final String FREEPBX_PDF_GUIDE_FILEPATH = "configuration.notification.freepbx-guide-filepath";

    /**
     * Constructor.
     */
    public ControllerConfiguration()
    {
    }

    /**
     * @see {@link #getString(String)}
     */
    public Duration getDuration(String key)
    {
        String value = getString(key);
        if (value == null) {
            return null;
        }
        return Period.parse(value).toStandardDuration();
    }

    /**
     * @see {@link #getString(String)}
     */
    public Period getPeriod(String key)
    {
        String value = getString(key);
        if (value == null) {
            return null;
        }
        return Period.parse(value);
    }

    /**

     * @return timeout to receive response when performing commands from agent
     */
    public Duration getJadeCommandTimeout()
    {
        return getDuration(JADE_COMMAND_TIMEOUT);
    }

    /**
     * @return XML-RPC host
     */
    public String getRpcHost(boolean nullAsDefault)
    {
        String rpcHost = getString(RPC_HOST);
        if (rpcHost.isEmpty()) {
            if (nullAsDefault) {
                rpcHost = null;
            }
            else {
                rpcHost = "localhost";
            }
        }
        return rpcHost;
    }

    /**
     * @return XML-RPC port
     */
    public int getRpcPort()
    {
        return getInt(RPC_PORT);
    }

    /**
     * @return XML-RPC ssl key store
     */
    public String getRpcSslKeyStore()
    {
        String sslKeyStore = getString(RPC_SSL_KEYSTORE);
        if (sslKeyStore == null || sslKeyStore.trim().isEmpty()) {
            return null;
        }
        return sslKeyStore;
    }

    /**
     * @return password for XML-RPC ssl key store
     */
    public String getRpcSslKeyStorePassword()
    {
        return getString(RPC_SSL_KEYSTORE_PASSWORD);
    }

    /**
     * @return subject prefix for emails sent by SMTP
     */
    public String getSmtpSubjectPrefix()
    {
        return evaluate(getString(SMTP_SUBJECT_PREFIX));
    }

    /**
     * @return {@link #NOTIFICATION_RESERVATION_REQUEST_URL}
     */
    public String getNotificationReservationRequestUrl()
    {
        String reservationRequestUrl = getString(NOTIFICATION_RESERVATION_REQUEST_URL);
        if (reservationRequestUrl == null || reservationRequestUrl.isEmpty()) {
            return null;
        }
        return reservationRequestUrl;
    }

    /**
     * @return {@link #NOTIFICATION_RESERVATION_REQUEST_CONFIRMATION_URL}
     */
    public String getNotificationReservationRequestConfirmationUrl()
    {
        String reservationRequestConfirmationUrl = getString(NOTIFICATION_RESERVATION_REQUEST_CONFIRMATION_URL);
        if (reservationRequestConfirmationUrl == null || reservationRequestConfirmationUrl.isEmpty()) {
            return null;
        }
        return reservationRequestConfirmationUrl;
    }

    /**
     * @return {@link #FREEPBX_PDF_GUIDE_FILEPATH}
     */
    public String getFreePBXPDFGuidePath()
    {
        String FreePBXGuidePath = getString(FREEPBX_PDF_GUIDE_FILEPATH);
        if (FreePBXGuidePath == null || FreePBXGuidePath.isEmpty()) {
            return null;
        }
        return FreePBXGuidePath;
    }

    /**
     * @return {@link #NOTIFICATION_USER_SETTINGS_URL}
     */
    public String getNotificationUserSettingsUrl()
    {
        String reservationRequestUrl = getString(NOTIFICATION_USER_SETTINGS_URL);
        if (reservationRequestUrl == null || reservationRequestUrl.isEmpty()) {
            return null;
        }
        return reservationRequestUrl;
    }

    /**
     * List of administrators.
     */
    private List<PersonInformation> administrators;

    /**
     * @return set of administrators to which errors are reported.
     */
    public synchronized List<PersonInformation> getAdministrators()
    {
        if (administrators == null) {
            administrators = new LinkedList<PersonInformation>();
            for (String administratorEmail : getList(ADMINISTRATOR)) {
                administrators.add(new PersonInformation()
                {
                    @Override
                    public String getFullName()
                    {
                        return "administrator";
                    }

                    @Override
                    public String getRootOrganization()
                    {
                        return null;
                    }

                    @Override
                    public String getPrimaryEmail()
                    {
                        return administratorEmail;
                    }

                    @Override
                    public String toString()
                    {
                        return getFullName();
                    }
                });
            }
        }
        return administrators;
    }

    /**
     * @return set of administrator emails to which errors are reported.
     */
    public synchronized List<String> getAdministratorEmails()
    {
        List<String> administratorEmails = new LinkedList<String>();
        for (PersonInformation administrator : getAdministrators()) {
            administratorEmails.add(administrator.getPrimaryEmail());
        }
        return administratorEmails;
    }

    /**
     * @param administrators sets the {@link #administrators}
     */
    public synchronized void setAdministrators(List<PersonInformation> administrators)
    {
        this.administrators = administrators;
    }

    /**
     * Pattern for parameters.
     */
    private static final Pattern EXPRESSION_PARAM_PATTERN = Pattern.compile("\\$\\{([^\\$]+)\\}");

    /**
     * Parser for parameters.
     */
    private static final PatternParser EXPRESSION_PATTERN_PARSER = new PatternParser(EXPRESSION_PARAM_PATTERN);

    /**
     * @param text
     * @return evaluated string
     */
    public String evaluate(String text)
    {
        if (text == null) {
            return null;
        }
        return EXPRESSION_PATTERN_PARSER.parseAndJoin(text, new PatternParser.Callback()
        {
            @Override
            public String processString(String string)
            {
                return string;
            }

            @Override
            public String processMatch(MatchResult match)
            {
                String name = match.group(1);
                if (name.equals("domain.name")) {
                    return LocalDomain.getLocalDomain().getName();
                }
                else if (name.equals("domain.shortName")) {
                    return LocalDomain.getLocalDomain().getShortName();
                }
                else {
                    throw new IllegalArgumentException("Parameter " + name + " not defined.");
                }
            }
        });
    }

    public boolean isInterDomainConfigured()
    {
        if (getInterDomainPort() != null) {
            if (requiresClientPKIAuth() && hasInterDomainPKI()) {
                return true;
            }
            if (hasInterDomainBasicAuth()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasInterDomainPKI()
    {
        if (Strings.isNullOrEmpty(getInterDomainSslKeyStore())
                || Strings.isNullOrEmpty(getInterDomainSslKeyStoreType())
                || Strings.isNullOrEmpty(getInterDomainSslKeyStorePassword())) {
            return false;
        }
        return true;
    }

    public boolean hasInterDomainBasicAuth()
    {
        if (Strings.isNullOrEmpty(getInterDomainBasicAuthPasswordHash())) {
            return false;
        }
        return true;
    }

    public String getInterDomainHost()
    {
        return getString(ControllerConfiguration.INTERDOMAIN_HOST);
    }

    public Integer getInterDomainPort()
    {
        return getInteger(ControllerConfiguration.INTERDOMAIN_PORT, null);
    }

    /**
     * Returns true if PKI auth is selected to be used
     * @return
     */
    public boolean requiresClientPKIAuth()
    {
        return getBoolean(ControllerConfiguration.INTERDOMAIN_PKI_CLIENT_AUTH, false);
    }

    public String getInterDomainSslKeyStore()
    {
        String sslKeyStore = getString(ControllerConfiguration.INTERDOMAIN_SSL_KEY_STORE);
        if (sslKeyStore == null || sslKeyStore.trim().isEmpty()) {
            return null;
        }
        return sslKeyStore;
    }

    public String getInterDomainBasicAuthPasswordHash()
    {
        String password = getString(ControllerConfiguration.INTERDOMAIN_BASIC_AUTH_PASSWORD);
        if (Strings.isNullOrEmpty(password)) {
            return null;
        }
        return SSLCommunication.hashPassword(password.getBytes());
    }

    public String getInterDomainSslKeyStoreType() {
        return getString(ControllerConfiguration.INTERDOMAIN_SSL_KEY_STORE_TYPE);
    }

    public String getInterDomainSslKeyStorePassword() {
        return getString(ControllerConfiguration.INTERDOMAIN_SSL_KEY_STORE_PASSWORD);
    }

    public List<String> getForeignDomainsCaCertFiles() {
        return new ArrayList<>(getList(ControllerConfiguration.INTERDOMAIN_TRUSTED_CA_CERT_FILES));
    }

    public int getInterDomainCommandTimeout() {
        return (int) getDuration(ControllerConfiguration.INTERDOMAIN_COMMAND_TIMEOUT).getMillis();
    }

    public Integer getInterDomainCacheRefreshRate() {
        return getDuration(ControllerConfiguration.INTERDOMAIN_CACHE_REFRESH_RATE).toStandardSeconds().getSeconds();
    }

    public boolean hasCalDAVBasicAuth()
    {
        if (Strings.isNullOrEmpty(getCalDAVEncodedBasicAuth())) {
            return false;
        }
        return true;
    }

    public String getCalDAVEncodedBasicAuth()
    {
        String username = getString(ControllerConfiguration.CALDAV_BASIC_AUTH_USERNAME);
        String password = getString(ControllerConfiguration.CALDAV_BASIC_AUTH_PASSWORD);
        if (Strings.isNullOrEmpty(password) || Strings.isNullOrEmpty(username)) {
            return null;
        }
        String authString = username + ":" + password;
        String authStringEnc = Base64.encodeBytes(authString.getBytes(StandardCharsets.UTF_8));
        return authStringEnc;
    }

    public boolean containsKey(String key)
    {
        return env.containsProperty(key);
    }

    public List<String> getList(String key)
    {
        String[] objects = env.getProperty(key, String[].class);
        return objects != null ? Arrays.asList(objects) : List.of();
    }

    public boolean getBoolean(String key)
    {
        return env.getProperty(key, Boolean.class);
    }

    public boolean getBoolean(String key, boolean defaultValue)
    {
        return env.getProperty(key, Boolean.class, defaultValue);
    }

    public Integer getInteger(String key, Integer defaultValue)
    {
        return env.getProperty(key, Integer.class, defaultValue);
    }

    public int getInt(String key)
    {
        return env.getProperty(key, Integer.class);
    }

    public int getInt(String key, int defaultValue)
    {
        return env.getProperty(key, Integer.class, defaultValue);
    }

    public String getString(String key) {
        return env.getProperty(key);
    }

    public String getString(String key, String defaultValue) {
        return env.getProperty(key, defaultValue);
    }
}
