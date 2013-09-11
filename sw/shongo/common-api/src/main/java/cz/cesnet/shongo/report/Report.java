package cz.cesnet.shongo.report;

import java.util.Map;

/**
 * Represents a error/warning/information/debug message in Shongo which should be reported to user,
 * resource administrator and/or domain administrator.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public interface Report
{
    /**
     * @return unique id of the report
     */
    public String getUniqueId();

    /**
     * @return name of the report
     */
    public String getName();

    /**
     * @return report message
     */
    public Type getType();

    /**
     * @return {@link Map} of parameters of this {@link Report}
     */
    public Map<String, Object> getParameters();

    /**
     * @param messageType
     * @return report message of given {@code messageType}
     */
    public String getMessage(MessageType messageType, Language language);

    /**
     * @return visibility flags (e.g., {@link #VISIBLE_TO_USER}, {@link #VISIBLE_TO_DOMAIN_ADMIN})
     */
    public int getVisibleFlags();

    /**
     * @return {@link Resolution}
     */
    public Resolution getResolution();

    public static final int VISIBLE_TO_USER = 1;
    public static final int VISIBLE_TO_DOMAIN_ADMIN = 2;
    public static final int VISIBLE_TO_RESOURCE_ADMIN = 4;

    /**
     * Enumeration of all possible message types.
     */
    public static enum MessageType
    {
        /**
         * Message for normal user.
         */
        USER,

        /**
         * Message for domain administrator.
         */
        DOMAIN_ADMIN,

        /**
         * Message for resource administrator.
         */
        RESOURCE_ADMIN
    }

    /**
     * Enumeration of all possible languages for messages.
     */
    public static enum Language
    {
        CZECH("cs"),

        /**
         * Message for domain administrator.
         */
        ENGLISH("en");

        /**
         * Language code.
         */
        private String language;

        /**
         * Constructor.
         *
         * @param language sets the {@link #language}
         */
        private Language(String language)
        {
            this.language = language;
        }

        /**
         * @return {@link #language}
         */
        public String getLanguage()
        {
            return language;
        }
    }

    /**
     * Enumeration of all possible {@link cz.cesnet.shongo.report.Report} types.
     */
    public static enum Type
    {
        /**
         * Represent a failure.
         */
        ERROR,

        /**
         * Represents a warning.
         */
        WARNING,

        /**
         * Represents a information.
         */
        INFORMATION,

        /**
         * Represents a debug information.
         */
        DEBUG
    }

    /**
     * Represents what does the {@link cz.cesnet.shongo.report.Report} mean to action which cause it.
     */
    public static enum Resolution
    {
        /**
         * Not specified.
         */
        DEFAULT,

        /**
         * Means that the action which cause the {@link cz.cesnet.shongo.report.Report} should be tried again.
         */
        TRY_AGAIN,

        /**
         * Means that the action which cause the {@link cz.cesnet.shongo.report.Report} should not be tried again
         */
        STOP
    }
}
