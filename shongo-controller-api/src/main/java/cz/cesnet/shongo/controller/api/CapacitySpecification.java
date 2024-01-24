package cz.cesnet.shongo.controller.api;

/**
 * Represents a {@link Specification} for a {@link CapacityReservation}.
 */
public class CapacitySpecification extends Specification
{

    /**
     * Capacity used of the whole capacity.
     */
    private Integer capacityUsed;

    /**
     * @return {@link #capacityUsed}
     */
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
}
