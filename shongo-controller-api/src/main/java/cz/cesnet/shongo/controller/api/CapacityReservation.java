package cz.cesnet.shongo.controller.api;

import cz.cesnet.shongo.api.DataMap;
import cz.cesnet.shongo.api.Room;

/**
 * Represents a {@link CapacityReservation} for a {@link Room}.
 */
public class CapacityReservation extends ResourceReservation
{

    /**
     * Capacity used of the whole capacity.
     */
    private int capacityUsed;

    /**
     * @return {@link #capacityUsed}
     */
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

    private static final String CAPACITY_USED = "capacityUsed";

    @Override
    public DataMap toData()
    {
        DataMap dataMap = super.toData();
        dataMap.set(CAPACITY_USED, capacityUsed);
        return dataMap;
    }

    @Override
    public void fromData(DataMap dataMap)
    {
        super.fromData(dataMap);
        capacityUsed = dataMap.getInt(CAPACITY_USED);
    }
}
