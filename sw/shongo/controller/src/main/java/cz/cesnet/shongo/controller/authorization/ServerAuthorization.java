package cz.cesnet.shongo.controller.authorization;

import cz.cesnet.shongo.CommonReportSet;
import cz.cesnet.shongo.api.UserInformation;
import cz.cesnet.shongo.controller.ControllerConfiguration;
import cz.cesnet.shongo.controller.ControllerReportSet;
import cz.cesnet.shongo.controller.api.SecurityToken;
import cz.cesnet.shongo.report.ReportRuntimeException;
import cz.cesnet.shongo.ssl.ConfiguredSSLContext;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Provides methods for performing authentication and authorization.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public class ServerAuthorization extends Authorization
{
    private static Logger logger = LoggerFactory.getLogger(ServerAuthorization.class);

    /**
     * Authentication service path in auth-server.
     */
    private static final String AUTHENTICATION_SERVICE_PATH = "/authn/oic";

    /**
     * User web service path in auth-server.
     */
    private static final String USER_SERVICE_PATH = "/perun/users";

    /**
     * Access token which won't be verified and can be used for testing purposes.
     */
    private String rootAccessToken;

    /**
     * URL to authorization server.
     */
    private String authorizationServer;

    /**
     * URL to authorization server.
     */
    private String authorizationServerHeader;

    /**
     * {@link HttpClient} for performing auth-server requests.
     */
    private HttpClient httpClient;

    /**
     * @see ObjectMapper
     */
    private ObjectMapper jsonMapper = new ObjectMapper();

    /**
     * Constructor.
     *
     * @param configuration to load authorization configuration from
     */
    private ServerAuthorization(ControllerConfiguration configuration)
    {
        super(configuration);

        // Debug HTTP requests
        //System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        //System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        //System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
        //System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");

        // Authorization server
        authorizationServer = configuration.getString(ControllerConfiguration.SECURITY_SERVER);
        if (authorizationServer == null) {
            throw new IllegalStateException("Authorization server is not set in the configuration.");
        }
        logger.info("Using authorization server '{}'.", authorizationServer);

        // Authorization header
        String clientId = configuration.getString(ControllerConfiguration.SECURITY_CLIENT_ID);
        String clientSecret = configuration.getString(ControllerConfiguration.SECURITY_CLIENT_SECRET);
        authorizationServerHeader = "id=" + clientId + ";secret=" + clientSecret;

        // Root access token
        rootAccessToken = configuration.getString(ControllerConfiguration.SECURITY_ROOT_ACCESS_TOKEN);
        adminAccessTokens.add(rootAccessToken);

        // Users with enabled adminMode
        String userIds = configuration.getString(ControllerConfiguration.SECURITY_ADMINISTRATOR_USER_ID);
        if (userIds != null) {
            for (String adminUserId : userIds.split(",")) {
                adminModeEnabledUserIds.add(adminUserId.trim());
            }
        }

        // Create http client
        httpClient = ConfiguredSSLContext.getInstance().createHttpClient();
    }

    /**
     * @param rootAccessToken sets the {@link #rootAccessToken}
     */
    public void setRootAccessToken(String rootAccessToken)
    {
        this.rootAccessToken = rootAccessToken;
    }

    /**
     * @return url to authentication service in auth-server
     */
    private String getAuthenticationUrl()
    {
        return authorizationServer + AUTHENTICATION_SERVICE_PATH;
    }

    /**
     * @return url to user service in auth-server
     */
    private String getUserServiceUrl()
    {
        return authorizationServer + USER_SERVICE_PATH;
    }

    @Override
    protected UserInformation onValidate(SecurityToken securityToken)
    {
        // Always allow testing access token
        if (rootAccessToken != null && securityToken.getAccessToken().equals(rootAccessToken)) {
            logger.trace("Access token '{}' is valid for testing.", securityToken.getAccessToken());
            return ROOT_USER_INFORMATION;
        }
        return super.onValidate(securityToken);
    }

    @Override
    protected UserInformation onGetUserInformationByAccessToken(String accessToken)
    {
        // Testing security token represents root user
        if (rootAccessToken != null && accessToken.equals(rootAccessToken)) {
            return ROOT_USER_INFORMATION;
        }

        Exception errorException = null;
        String errorReason = null;
        try {
            URIBuilder uriBuilder = new URIBuilder(getAuthenticationUrl() + "/userinfo");
            uriBuilder.setParameter("schema", "openid");
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            httpGet.setHeader("Authorization", "Bearer " + accessToken);
            HttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                JsonNode jsonNode = readJson(response.getEntity());
                return createUserInformationFromData(jsonNode);
            }
            else {
                JsonNode jsonNode = readJson(response.getEntity());
                errorReason = String.format("%s, %s",
                        jsonNode.get("error").getTextValue(), jsonNode.get("error_description").getTextValue());
            }
        }
        catch (Exception exception) {
            errorException = exception;
        }
        // Handle error
        String errorMessage = String.format("Retrieving user information by access token '%s' failed.", accessToken);
        if (errorReason != null) {
            errorMessage += " " + errorReason;
        }
        throw new RuntimeException(errorMessage, errorException);
    }

    @Override
    protected UserInformation onGetUserInformationByUserId(String userId)
    {
        try {
            HttpGet httpGet = new HttpGet(getUserServiceUrl() + "/" + userId);
            httpGet.addHeader("Authorization", authorizationServerHeader);
            HttpResponse response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                JsonNode jsonNode = readJson(response.getEntity());
                return createUserInformationFromData(jsonNode);
            }
            else {
                readContent(response.getEntity());
                if (statusCode == HttpStatus.SC_NOT_FOUND) {
                    throw new ControllerReportSet.UserNotExistException(userId);
                }
                else {
                    throw new CommonReportSet.UnknownErrorException(
                            "Retrieving user information by user-id '" + userId + "' failed: "
                                    + response.getStatusLine().toString());
                }
            }
        }
        catch (ReportRuntimeException exception) {
            throw exception;
        }
        catch (Exception exception) {
            throw new CommonReportSet.UnknownErrorException(exception,
                    "Retrieving user information by user-id '" + userId + "' failed.");
        }
    }

    @Override
    protected Collection<UserInformation> onListUserInformation()
    {
        Exception errorException = null;
        try {
            HttpGet httpGet = new HttpGet(getUserServiceUrl());
            httpGet.addHeader("Authorization", authorizationServerHeader);
            HttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                JsonNode jsonNode = readJson(response.getEntity());
                List<UserInformation> userInformationList = new LinkedList<UserInformation>();
                for (JsonNode childJsonNode : jsonNode) {
                    UserInformation userInformation = createUserInformationFromData(childJsonNode);
                    userInformationList.add(userInformation);
                }
                return userInformationList;
            }
            else {
                readContent(response.getEntity());
            }
        }
        catch (Exception exception) {
            errorException = exception;
        }
        // Handle error
        throw new RuntimeException("Retrieving user information failed.", errorException);
    }

    /**
     * @param data from authorization server
     * @return {@link UserInformation}
     */
    private static UserInformation createUserInformationFromData(JsonNode data)
    {
        if (!data.has("id")) {
            throw new IllegalArgumentException("User information must contains identifier.");
        }
        if (!data.has("given_name") || !data.has("family_name")) {
            throw new IllegalArgumentException("User information must contains given and family name.");
        }
        UserInformation userInformation = new UserInformation();
        userInformation.setUserId(data.get("id").asText());
        userInformation.setFirstName(data.get("given_name").getTextValue());
        userInformation.setLastName(data.get("family_name").getTextValue());

        if (data.has("original_id")) {
            userInformation.setOriginalId(data.get("original_id").asText());
        }
        if (data.has("organization")) {
            userInformation.setOrganization(data.get("organization").getTextValue());
        }
        if (data.has("email")) {
            String emails = data.get("email").getTextValue();
            if (emails != null) {
                for (String email : emails.split(";")) {
                    if (!email.isEmpty()) {
                        userInformation.addEmail(email);
                    }
                }
            }
        }
        return userInformation;
    }

    /**
     * @param httpEntity to be read
     * @return {@link JsonNode} from given {@code httpEntity}
     */
    private JsonNode readJson(HttpEntity httpEntity)
    {
        try {
            InputStream inputStream = httpEntity.getContent();
            try {
                return jsonMapper.readTree(inputStream);
            }
            finally {
                inputStream.close();
            }
        }
        catch (EOFException exception) {
            throw new RuntimeException("JSON is empty.", exception);
        }
        catch (IOException exception) {
            throw new RuntimeException("Reading JSON failed.", exception);
        }
    }

    /**
     * Read all content from given {@code httpEntity}.
     *
     * @param httpEntity to be read
     */
    private String readContent(HttpEntity httpEntity)
    {
        if (httpEntity != null) {
            try {
                return EntityUtils.toString(httpEntity);
            }
            catch (IOException exception) {
                throw new RuntimeException("Reading content failed.", exception);
            }
        }
        return null;
    }

    /**
     * @param httpResponse to be handled
     * @throws RuntimeException is always thrown
     */
    private <T> T handleAuthorizationRequestError(HttpResponse httpResponse)
    {
        JsonNode jsonNode = readJson(httpResponse.getEntity());
        return handleAuthorizationRequestError(jsonNode);
    }

    /**
     * @param jsonNode to be handled
     * @throws RuntimeException is always thrown
     */
    private <T> T handleAuthorizationRequestError(JsonNode jsonNode)
    {
        throw new RuntimeException(String.format("Authorization request failed: %s, %s",
                jsonNode.get("title").getTextValue(),
                jsonNode.get("detail").getTextValue()));
    }

    /**
     * @param exception to be handled
     * @throws RuntimeException is always thrown
     */
    private <T> T handleAuthorizationRequestError(Exception exception)
    {
        throw new RuntimeException(String.format("Authorization request failed. %s", exception.getMessage()));
    }

    /**
     * @return new instance of {@link ServerAuthorization}
     * @throws IllegalStateException when other {@link Authorization} already exists
     */
    public static ServerAuthorization createInstance(ControllerConfiguration configuration) throws IllegalStateException
    {
        ServerAuthorization serverAuthorization = new ServerAuthorization(configuration);
        Authorization.setInstance(serverAuthorization);
        return serverAuthorization;
    }
}