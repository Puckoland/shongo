package cz.cesnet.shongo.controller.notification;


import cz.cesnet.shongo.controller.ControllerConfiguration;
import cz.cesnet.shongo.controller.booking.ObjectIdentifier;
import cz.cesnet.shongo.controller.booking.request.AbstractReservationRequest;
import cz.cesnet.shongo.controller.settings.UserSettingsManager;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Locale;

/**
 * {@link ConfigurableNotification} with {@link AbstractReservationRequest}.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public abstract class AbstractReservationRequestNotification extends ConfigurableNotification
{
    private String reservationRequestId;

    private String reservationRequestDescription;

    private DateTime reservationRequestUpdatedAt;

    private String reservationRequestUpdatedBy;

    /**
     * Constructor.
     *
     * @param reservationRequest
     * @param userSettingsManager
     */
    public AbstractReservationRequestNotification(AbstractReservationRequest reservationRequest,
            UserSettingsManager userSettingsManager)
    {
        super(userSettingsManager);

        if (reservationRequest != null) {
            this.reservationRequestId = ObjectIdentifier.formatId(reservationRequest);
            this.reservationRequestDescription = reservationRequest.getDescription();
            this.reservationRequestUpdatedAt = reservationRequest.getUpdatedAt();
            this.reservationRequestUpdatedBy = reservationRequest.getUpdatedBy();
        }
    }

    public String getReservationRequestId()
    {
        return reservationRequestId;
    }

    public String getReservationRequestDescription()
    {
        return reservationRequestDescription;
    }

    public DateTime getReservationRequestUpdatedAt()
    {
        return reservationRequestUpdatedAt;
    }

    public String getReservationRequestUpdatedBy()
    {
        return reservationRequestUpdatedBy;
    }

    @Override
    protected Collection<Locale> getAvailableLocals()
    {
        return NotificationMessage.AVAILABLE_LOCALES;
    }
}
