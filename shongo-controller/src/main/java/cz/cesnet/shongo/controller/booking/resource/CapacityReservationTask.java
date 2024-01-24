package cz.cesnet.shongo.controller.booking.resource;

import cz.cesnet.shongo.controller.booking.reservation.Reservation;
import cz.cesnet.shongo.controller.booking.room.RoomReservation;
import cz.cesnet.shongo.controller.scheduler.ReservationTask;
import cz.cesnet.shongo.controller.scheduler.SchedulerContext;
import cz.cesnet.shongo.controller.scheduler.SchedulerException;
import org.joda.time.Interval;

/**
 * Represents {@link ReservationTask} for a {@link RoomReservation}.
 */
public class CapacityReservationTask extends ReservationTask
{

    /**
     * {@link CapacityCapability} for which the {@link CapacityReservation}
     * should be allocated.
     */
    private CapacityCapability capacityCapability = null;

    /**
     * Constructor.
     *
     * @param schedulerContext
     * @param slot
     */
    public CapacityReservationTask(SchedulerContext schedulerContext, Interval slot)
    {
        super(schedulerContext, slot);
    }

    public CapacityCapability getCapacityCapability()
    {
        return capacityCapability;
    }

    /**
     * @param capacityCapability sets the {@link #capacityCapability}
     */
    public void setCapacityCapability(CapacityCapability capacityCapability)
    {
        this.capacityCapability = capacityCapability;
    }

    @Override
    protected Reservation allocateReservation(Reservation currentReservation) throws SchedulerException
    {
        return null;
    }
}
