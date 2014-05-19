package cz.cesnet.shongo.client.web;

import cz.cesnet.shongo.api.UserInformation;
import cz.cesnet.shongo.client.web.auth.OpenIDConnectAuthenticationToken;
import cz.cesnet.shongo.client.web.models.TimeZoneModel;
import cz.cesnet.shongo.client.web.models.UserSession;
import cz.cesnet.shongo.client.web.models.UserSettingsModel;
import cz.cesnet.shongo.client.web.support.Breadcrumb;
import cz.cesnet.shongo.client.web.support.BreadcrumbItem;
import cz.cesnet.shongo.client.web.support.ReflectiveResourceBundleMessageSource;
import cz.cesnet.shongo.client.web.support.interceptors.NavigationInterceptor;
import cz.cesnet.shongo.controller.ControllerConnectException;
import cz.cesnet.shongo.controller.SystemPermission;
import cz.cesnet.shongo.controller.api.SecurityToken;
import cz.cesnet.shongo.util.DateTimeFormatter;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.NullCacheStorage;
import freemarker.cache.SoftCacheStorage;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModelException;
import org.joda.time.DateTimeZone;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

/**
 * Design for shongo-client-web.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public class Design
{
    public static final String LAYOUT_FILE_NAME = "layout.ftl";

    /**
     * @see cz.cesnet.shongo.client.web.Cache
     */
    @Resource
    protected Cache cache;

    /**
     * {@link MessageSource} used for application messages.
     */
    @Resource
    protected MessageSource applicationMessageSource;

    /**
     * @see ApplicationContext
     */
    private ApplicationContext applicationContext = new ApplicationContext();

    /**
     * Folder where design resources are stored.
     */
    private String resourcesFolder;

    /**
     * {@link MessageSource} used for design messages.
     */
    protected MessageSource layoutMessageSource;

    /**
     * Specifies whether templates should be cached.
     */
    private boolean cacheTemplates = true;

    /**
     * Template engine configuration.
     */
    private Configuration templateConfiguration = new Configuration();

    /**
     * Cache of templates.
     */
    private Map<String, Template> templateMap = new HashMap<String, Template>();

    /**
     * Constructor.
     *
     * @param configuration
     */
    public Design(ClientWebConfiguration configuration)
    {
        this.resourcesFolder = configuration.getDesignFolder();

        // Create message source
        ReflectiveResourceBundleMessageSource messageSource = new ReflectiveResourceBundleMessageSource();
        messageSource.setBasename(resourcesFolder + "/layout");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        this.layoutMessageSource = messageSource;

        // Initialize template engine
        try {
            templateConfiguration.setObjectWrapper(new freemarker.template.DefaultObjectWrapper());

            if (resourcesFolder.startsWith("file:")) {
                templateConfiguration.setTemplateLoader(new FileTemplateLoader(new File(resourcesFolder.substring(5))));
            }
            else {
                templateConfiguration.setClassForTemplateLoading(Design.class, resourcesFolder);
            }
        }
        catch (Exception exception) {
            throw new RuntimeException("Failed to initialize template engine.", exception);
        }
    }

    public void setApplicationMessageSource(MessageSource applicationMessageSource)
    {
        this.applicationMessageSource = applicationMessageSource;
    }

    /**
     * @return {@link #resourcesFolder}
     */
    public String getResourcesFolder()
    {
        return resourcesFolder;
    }

    /**
     * @return {@link #cacheTemplates}
     */
    public boolean isCacheTemplates()
    {
        return cacheTemplates;
    }

    /**
     * @param cacheTemplates sets the {@link #cacheTemplates}
     */
    public void setCacheTemplates(boolean cacheTemplates)
    {
        this.cacheTemplates = cacheTemplates;
        if (cacheTemplates) {
            templateConfiguration.setCacheStorage(new SoftCacheStorage());
        }
        else {
            // Debug
            templateConfiguration.setCacheStorage(new NullCacheStorage());
        }
    }

    /**
     * @param templateFileName
     * @return {@link Template} for given {@code templateFileName}
     */
    protected synchronized Template getTemplate(String templateFileName)
    {
        Template template = (cacheTemplates ? templateMap.get(templateFileName) : null);
        if (template == null) {
            try {
                template = templateConfiguration.getTemplate(templateFileName);
                templateMap.put(templateFileName, template);
            }
            catch (Exception exception) {
                throw new RuntimeException("Failed to get template " + templateFileName, exception);
            }
        }
        return template;
    }

    /**
     * @param template
     * @param dataModel
     * @return rendered given {@code template} for given {@code dataModel}
     */
    protected String renderTemplate(Template template, Object dataModel)
    {
        try {
            StringWriter stringWriter = new StringWriter();
            template.process(dataModel, stringWriter);
            return stringWriter.toString();
        }
        catch (TemplateException exception) {
            Throwable cause = exception.getCause();
            if (cause != null) {
                throw new RuntimeException(cause);
            }
            else {
                throw new RuntimeException(exception);
            }
        }
        catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * @param request
     * @param head
     * @param title
     * @param content
     * @return {@link cz.cesnet.shongo.client.web.Design.LayoutContext}
     */
    public LayoutContext createLayoutContext(HttpServletRequest request, String head, String title, String content)
    {
        LayoutContext layoutContext = new LayoutContext(request);
        layoutContext.head = head;
        layoutContext.title = title;
        layoutContext.content = content;
        return layoutContext;
    }

    public static class ApplicationContext
    {
        private String version;

        public ApplicationContext()
        {
            this.version = ClientWeb.getVersion();
        }

        public String getVersion()
        {
            return version;
        }
    }

    public class TemplateContext
    {
        protected String baseUrl;

        protected String requestUrl;

        protected UserSession userSession;

        protected Locale userSessionLocale;

        public TemplateContext(HttpServletRequest request, UserSession userSession)
        {
            this.baseUrl = request.getContextPath();
            this.requestUrl = (String) request.getAttribute(NavigationInterceptor.REQUEST_URL_REQUEST_ATTRIBUTE);
            this.userSession = userSession;
            this.userSessionLocale = userSession.getLocale();
        }

        public String message(String code)
        {
            return layoutMessageSource.getMessage(code, null, userSessionLocale);
        }

        public String message(String code, Object... args)
        {
            return layoutMessageSource.getMessage(code, args, userSessionLocale);
        }

        public String escapeJavaScript(String text)
        {
            text = text.replace("\"", "\\\"");
            return text;
        }

        public ApplicationContext getApp()
        {
            return applicationContext;
        }

        public class UrlContext
        {
            private String languageUrl;

            public String getHome()
            {
                return baseUrl + ClientWebUrl.HOME;
            }

            public String getChangelog()
            {
                return baseUrl + ClientWebUrl.CHANGELOG;
            }

            public String getHelp()
            {
                return baseUrl + ClientWebUrl.HELP;
            }

            public String getReport()
            {
                return baseUrl + applyBackUrl(ClientWebUrl.REPORT);
            }

            public String getResources()
            {
                return baseUrl + "/design";
            }

            public String getLanguage()
            {
                if (languageUrl == null) {
                    if (requestUrl.contains("?")) {
                        UriComponentsBuilder languageUrlBuilder = UriComponentsBuilder.fromUriString(requestUrl);
                        languageUrlBuilder.replaceQueryParam("lang", ":lang");
                        languageUrl = languageUrlBuilder.build().toUriString();
                    }
                    else {
                        languageUrl = requestUrl + "?lang=:lang";
                    }
                }
                return languageUrl;
            }

            public String getLanguageEn()
            {
                return getLanguage().replace(":lang", "en");
            }

            public String getLanguageCs()
            {
                return getLanguage().replace(":lang", "cs");
            }

            public String getLogin()
            {
                return baseUrl + ClientWebUrl.LOGIN;
            }

            public String getLogout()
            {
                return baseUrl + ClientWebUrl.LOGOUT;
            }

            public String getUserSettings()
            {
                return baseUrl + applyBackUrl(ClientWebUrl.USER_SETTINGS);
            }

            public String userSettingsAdvancedMode(boolean advanceMode)
            {
                return baseUrl + applyBackUrl(ClientWebUrl.format(ClientWebUrl.USER_SETTINGS_ATTRIBUTE, "userInterface",
                        (advanceMode ? UserSettingsModel.UserInterface.ADVANCED
                                 : UserSettingsModel.UserInterface.BEGINNER)));
            }

            public String userSettingsAdministratorMode(boolean administratorMode)
            {
                return baseUrl + applyBackUrl(ClientWebUrl.format(
                        ClientWebUrl.USER_SETTINGS_ATTRIBUTE, "administratorMode", administratorMode));
            }

            private String applyBackUrl(String url)
            {
                return url + "?back-url=" + requestUrl;
            }
        }

        private UrlContext urlContext;

        public UrlContext getUrl()
        {
            if (urlContext == null) {
                urlContext = new UrlContext();
            }
            return urlContext;
        }

        public class SessionContext
        {
            private TimezoneContext timezoneContext;

            public TimezoneContext getTimezone()
            {
                if (timezoneContext == null) {
                    timezoneContext = new TimezoneContext();
                }
                return timezoneContext;
            }

            public class TimezoneContext
            {
                public String getTitle()
                {
                    DateTimeZone timeZone = userSession.getTimeZone();
                    if (timeZone == null) {
                        return "";
                    }
                    else {
                        return TimeZoneModel.formatTimeZone(timeZone);
                    }
                }

                public String getHelp()
                {
                    DateTimeZone timeZone = userSession.getTimeZone();
                    DateTimeZone homeTimeZone = userSession.getHomeTimeZone();
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.getInstance(DateTimeFormatter.Type.SHORT);

                    StringBuilder help = new StringBuilder();
                    help.append("<table>");
                    help.append("<tr><td align='left' colspan='2'><b style='text-align: left;'>");
                    help.append(applicationMessageSource.getMessage("views.layout.timezone", null, userSessionLocale));
                    help.append("</b></td></tr>");
                    // Current timezone
                    help.append("<tr>");
                    help.append("<td style='text-align: right; vertical-align: top;'>");
                    help.append(applicationMessageSource.getMessage("views.layout.timezone.current", null,
                            userSessionLocale));
                    help.append(":</td>");
                    help.append("<td style='text-align: left;'><b>");
                    help.append(TimeZoneModel.formatTimeZone(timeZone));
                    help.append("</b>");
                    String timeZoneName = TimeZoneModel.formatTimeZoneName(timeZone, userSessionLocale);
                    if (timeZoneName != null) {
                        help.append(" (");
                        help.append(timeZoneName);
                        help.append(")");
                    }
                    // Difference between Current and Home
                    if (homeTimeZone != null && !homeTimeZone.equals(timeZone)) {
                        help.append(", ");
                        help.append("</td></tr>");
                        help.append(applicationMessageSource
                                .getMessage("views.layout.timezone.diff", null, userSessionLocale));
                        help.append(":&nbsp;");
                        help.append(dateTimeFormatter.formatDurationTime(userSession.getTimeZoneOffset()));
                    }
                    help.append("</td></tr>");
                    // Home timezone
                    if (homeTimeZone != null && !homeTimeZone.equals(timeZone)) {
                        help.append("<tr>");
                        help.append("<td style='text-align: right; vertical-align: top;'>");
                        help.append(applicationMessageSource
                                .getMessage("views.layout.timezone.home", null, userSessionLocale));
                        help.append("<td align='left'>");
                        help.append("<b>");
                        help.append(TimeZoneModel.formatTimeZone(homeTimeZone));
                        help.append("</b>");
                        String homeTimeZoneName = TimeZoneModel.formatTimeZoneName(timeZone, userSessionLocale);
                        if (homeTimeZoneName != null) {
                            help.append(" (");
                            help.append(homeTimeZoneName);
                            help.append(")");
                        }
                        help.append("</td></tr>");
                    }
                    help.append("</table>");
                    return help.toString();
                }
            }

            private LocaleContext localeContext;

            public LocaleContext getLocale()
            {
                if (localeContext == null) {
                    localeContext = new LocaleContext();
                }
                return localeContext;
            }

            public class LocaleContext
            {
                public String getTitle()
                {
                    return userSession.getLocale().getDisplayLanguage();
                }

                public String getLanguage()
                {
                    return userSession.getLocale().getLanguage();
                }
            }
        }

        private SessionContext sessionContext;

        public SessionContext getSession()
        {
            if (sessionContext == null) {
                sessionContext = new SessionContext();
            }
            return sessionContext;
        }
    }

    public class LayoutContext extends TemplateContext
    {
        private String head;

        private String title;

        private String content;

        private Breadcrumb breadcrumb;

        private LayoutContext(HttpServletRequest request, UserSession userSession)
        {
            super(request, userSession);
            this.breadcrumb = (Breadcrumb) request.getAttribute("breadcrumb");
        }

        public LayoutContext(HttpServletRequest request)
        {
            this(request, UserSession.getInstance(request));
        }

        public String getHead()
        {
            return head;
        }

        public String getTitle()
        {
            return title;
        }

        public String getContent()
        {
            return content;
        }

        public Collection<LinkContext> getLinks()
        {
            List<LinkContext> links = new LinkedList<LinkContext>();
            if (isUserAuthenticated()) {
                UserContext user = getUser();
                if (user.isAdvancedMode()) {
                    links.add(new LinkContext("navigation.reservationRequest", ClientWebUrl.RESERVATION_REQUEST_LIST_VIEW));
                }
                if (user.isAdministratorMode()) {
                    links.add(new LinkContext("navigation.roomList", ClientWebUrl.ROOM_LIST_VIEW));
                }
                links.add(new LinkContext("navigation.userSettings", getUrl().getUserSettings()));
            }
            links.add(new LinkContext("navigation.help", getUrl().getHelp()));
            return links;
        }

        public Iterator<BreadcrumbContext> getBreadcrumbs()
        {
            if (breadcrumb != null) {
                final BreadcrumbContext breadcrumbContext = new BreadcrumbContext();
                return new Iterator<BreadcrumbContext>()
                {
                    private Iterator<BreadcrumbItem> iterator = breadcrumb.iterator();

                    @Override
                    public boolean hasNext()
                    {
                        return iterator.hasNext();
                    }

                    @Override
                    public BreadcrumbContext next()
                    {
                        breadcrumbContext.breadcrumbItem = iterator.next();
                        return breadcrumbContext;
                    }

                    @Override
                    public void remove()
                    {
                        iterator.remove();
                    }

                };
            }
            else {
                // Empty iterator
                return new Iterator<BreadcrumbContext>() {
                    public boolean hasNext() { return false; }
                    public BreadcrumbContext next() { throw new NoSuchElementException(); }
                    public void remove() { throw new IllegalStateException(); }
                };
            }
        }

        public class UserContext
        {
            private SecurityToken securityToken;

            private UserInformation userInformation;

            public UserContext(SecurityToken securityToken, UserInformation userInformation)
            {
                this.securityToken = securityToken;
                this.userInformation = userInformation;
            }

            public boolean isAdvancedMode()
            {
                return userSession.isAdvancedUserInterface();
            }

            public boolean isAdministratorMode()
            {
                return userSession.isAdministratorMode();
            }

            public boolean isAdministratorModeAvailable()
            {
                return cache.hasSystemPermission(securityToken, SystemPermission.ADMINISTRATION);
            }

            public boolean isReservationAvailable()
            {
                return cache.hasSystemPermission(securityToken, SystemPermission.RESERVATION);
            }

            public String getId()
            {
                return userInformation.getUserId();
            }

            public String getName()
            {
                return userInformation.getFullName();
            }
        }

        private UserContext userContext;

        public UserContext getUser()
        {
            if (userContext == null && userSession != null) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication instanceof OpenIDConnectAuthenticationToken) {
                    OpenIDConnectAuthenticationToken token = (OpenIDConnectAuthenticationToken) authentication;
                    userContext = new UserContext(token.getSecurityToken(), token.getPrincipal());
                }
            }
            return userContext;
        }

        public boolean isUserAuthenticated()
        {
            return getUser() != null;
        }

        public class LinkContext
        {
            private String titleCode;

            private String url;

            public LinkContext(String titleCode, String url)
            {
                this.titleCode = titleCode;
                this.url = url;
            }

            public String getTitle()
            {
                return applicationMessageSource.getMessage(titleCode, null, userSessionLocale);
            }

            public String getUrl()
            {
                return baseUrl + url;
            }
        }

        public class BreadcrumbContext
        {
            private BreadcrumbItem breadcrumbItem;

            public String getUrl()
            {
                return baseUrl + breadcrumbItem.getUrl();
            }

            public String getTitle()
            {
                return applicationMessageSource.getMessage(
                        breadcrumbItem.getTitleCode(), breadcrumbItem.getTitleArguments(), userSessionLocale);
            }
        }
    }
}