package cz.cesnet.shongo.controller.booking.resource;

import cz.cesnet.shongo.controller.api.Reservation;
import cz.cesnet.shongo.controller.booking.ObjectIdentifier;
import cz.cesnet.shongo.controller.booking.executable.Endpoint;
import cz.cesnet.shongo.controller.booking.reservation.TargetedReservation;

import javax.persistence.*;

/**
 * Represents a {@link cz.cesnet.shongo.controller.booking.reservation.Reservation} for a {@link Endpoint}.
 */
@Entity
public class CapacityReservation extends TargetedReservation
{

    /**
     * {@link CapacityCapability} in which the room is allocated.
     */
    private CapacityCapability capacityCapability;

    /**
     * Capacity used of the whole capacity.
     */
    private int capacityUsed;

    /**
     * Constructor.
     */
    public CapacityReservation()
    {
    }

    /**
     * @return {@link #capacityCapability}
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @Access(AccessType.FIELD)
    public CapacityCapability getCapacityCapability()
    {
        return capacityCapability;
    }

    /**
     * @param roomProviderCapability sets the {@link #capacityCapability}
     */
    public void setCapacityCapability(CapacityCapability roomProviderCapability)
    {
        this.capacityCapability = roomProviderCapability;
    }

    /**
     * @return {@link #capacityUsed}
     */
    @Column(nullable = false)
    public int getCapacityUsed()
    {
        return capacityUsed;
    }

    /**
     * @param capacityUsed sets the {@link #capacityUsed}
     */
    public void setCapacityUsed(int capacityUsed)
    {
        this.capacityUsed = capacityUsed;
    }

    @Override
    protected Reservation createApi()
    {
        return new cz.cesnet.shongo.controller.api.RoomReservation();
    }

    @Override
    protected void toApi(Reservation api, EntityManager entityManager, boolean admin)
    {
        cz.cesnet.shongo.controller.api.CapacityReservation roomReservationApi =
                (cz.cesnet.shongo.controller.api.CapacityReservation) api;
        Resource resource = getAllocatedResource();
        roomReservationApi.setResourceId(ObjectIdentifier.formatId(resource));
        roomReservationApi.setResourceName(resource.getName());
        roomReservationApi.setCapacityUsed(getCapacityUsed());
        super.toApi(api, entityManager, admin);
    }

    @Override
    @Transient
    public Long getTargetId()
    {
        return capacityCapability.getId();
    }
}
