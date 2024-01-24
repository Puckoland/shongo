package cz.cesnet.shongo.controller.booking.resource;

import cz.cesnet.shongo.controller.booking.room.RoomConfiguration;
import cz.cesnet.shongo.controller.booking.specification.Specification;
import cz.cesnet.shongo.controller.scheduler.*;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import javax.persistence.*;

/**
 * Capability tells that the resource can be allocated partially. It can be allocated multiple times at the same time.
 */
@Entity
public class CapacitySpecification extends Specification
        implements ReservationTaskProvider, SpecificationIntervalUpdater
{

    /**
     * {@link Resource} with {@link CapacityCapability} in which the {@link RoomConfiguration} should be allocated.
     */
    private Resource resource;

    /**
     * Capacity used of the whole capacity.
     */
    private Integer capacityUsed;

    /**
     * @return {@link #resource}
     */
    @OneToOne
    public Resource getResource()
    {
        return resource;
    }

    /**
     * @param resource sets the {@link #resource}
     */
    public void setResource(Resource resource)
    {
        this.resource = resource;
    }

    /**
     * @return {@link #capacityUsed}
     */
    @Column
    public Integer getCapacityUsed()
    {
        return capacityUsed;
    }

    /**
     * @param capacityUsed sets the {@link #capacityUsed}
     */
    public void setCapacityUsed(Integer capacityUsed)
    {
        this.capacityUsed = capacityUsed;
    }

    @Override
    protected cz.cesnet.shongo.controller.api.Specification createApi()
    {
        return null;
    }

    @Override
    public ReservationTask createReservationTask(SchedulerContext schedulerContext, Interval slot)
            throws SchedulerException
    {
        CapacityCapability capacityCapability = null;
        if (resource != null) {
            capacityCapability = resource.getCapabilityRequired(CapacityCapability.class);
        }

        CapacityReservationTask capacityReservationTask = new CapacityReservationTask(schedulerContext, slot);
        capacityReservationTask.setCapacityCapability(capacityCapability);
        return capacityReservationTask;
    }

    @Override
    public Interval updateInterval(Interval interval, DateTime minimumDateTime)
    {
        return null;
    }
}
