package cz.cesnet.shongo.controller.booking.alias;

import cz.cesnet.shongo.controller.booking.executable.Executable;
import cz.cesnet.shongo.controller.booking.executable.Migration;
import cz.cesnet.shongo.controller.booking.room.RoomEndpoint;
import cz.cesnet.shongo.controller.booking.reservation.Reservation;
import cz.cesnet.shongo.controller.scheduler.ReservationTask;
import cz.cesnet.shongo.controller.scheduler.SchedulerContext;
import cz.cesnet.shongo.controller.scheduler.SchedulerException;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents {@link cz.cesnet.shongo.controller.scheduler.ReservationTask} for one or multiple {@link AliasReservation}(s).
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public class AliasSetReservationTask extends ReservationTask
{
    /**
     * List of {@link AliasSpecification}s.
     */
    private List<AliasSpecification> aliasSpecifications = new ArrayList<AliasSpecification>();

    /**
     * Share created executable.
     */
    private boolean sharedExecutable = false;

    /**
     * Constructor.
     *
     * @param schedulerContext sets the {@link #schedulerContext}
     */
    public AliasSetReservationTask(SchedulerContext schedulerContext)
    {
        super(schedulerContext);
    }

    /**
     * @param aliasSpecification to be added to the {@link #aliasSpecifications}
     */
    public void addAliasSpecification(AliasSpecification aliasSpecification)
    {
        aliasSpecifications.add(aliasSpecification);
    }

    /**
     * @param sharedExecutable sets the {@link #sharedExecutable}
     */
    public void setSharedExecutable(boolean sharedExecutable)
    {
        this.sharedExecutable = sharedExecutable;
    }

    @Override
    protected Reservation allocateReservation() throws SchedulerException
    {
        validateReservationSlot(AliasReservation.class);

        if (aliasSpecifications.size() == 1) {
            AliasSpecification aliasSpecification = aliasSpecifications.get(0);
            AliasReservationTask aliasReservationTask = aliasSpecification.createReservationTask(schedulerContext);
            Reservation reservation = aliasReservationTask.perform();
            addReports(aliasReservationTask);
            return reservation;
        }
        else {

            // Process all alias specifications
            List<Reservation> createdReservations = new ArrayList<Reservation>();
            for (AliasSpecification aliasSpecification : aliasSpecifications) {
                // Allocate alias
                AliasReservationTask aliasReservationTask = aliasSpecification.createReservationTask(schedulerContext);
                Reservation reservation = aliasReservationTask.perform();
                addReports(aliasReservationTask);
                createdReservations.add(reservation);
            }

            // Allocate compound reservation
            Reservation reservation = new Reservation();
            reservation.setSlot(getInterval());
            for (Reservation createdReservation : createdReservations) {
                addChildReservation(createdReservation);
            }
            return reservation;
        }
    }

    @Override
    public void migrateReservation(Reservation oldReservation, Reservation newReservation) throws SchedulerException
    {
        Executable oldExecutable = oldReservation.getExecutable();
        Executable newExecutable = newReservation.getExecutable();
        if (oldExecutable instanceof RoomEndpoint && newExecutable instanceof RoomEndpoint) {
            if (oldExecutable.getState().isStarted()) {
                Migration migration = new Migration();
                migration.setSourceExecutable(oldExecutable);
                migration.setTargetExecutable(newExecutable);
            }
        }
        super.migrateReservation(oldReservation, newReservation);
    }
}
