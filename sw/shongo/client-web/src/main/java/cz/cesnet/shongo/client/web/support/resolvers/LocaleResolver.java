package cz.cesnet.shongo.client.web.support.resolvers;

import cz.cesnet.shongo.client.web.models.UserSession;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * {@link SessionLocaleResolver} which loads/stores the {@link Locale} from/to {@link UserSession}.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public class LocaleResolver extends SessionLocaleResolver
{
    @Override
    public Locale resolveLocale(HttpServletRequest request)
    {
        UserSession userSession = UserSession.getInstance(request);
        Locale locale = userSession.getLocale();
        if (locale == null) {
            locale = determineDefaultLocale(request);
        }
        return locale;
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale)
    {
        UserSession userSession = UserSession.getInstance(request);
        userSession.setLocale(locale);
    }
}
