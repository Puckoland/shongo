package cz.cesnet.shongo.controller.api;

import cz.cesnet.shongo.api.DataMap;

/**
 * Capability tells that the resource can be allocated partially. It can be allocated multiple times at the same time.
 */
public class CapacityCapability extends Capability
{

    private Integer capacity;

    public Integer getCapacity()
    {
        return capacity;
    }

    public void setCapacity(Integer capacity)
    {
        this.capacity = capacity;
    }

    public static final String CAPACITY = "capacity";

    @Override
    public DataMap toData()
    {
        DataMap dataMap = super.toData();
        dataMap.set(CAPACITY, capacity);
        return dataMap;
    }

    @Override
    public void fromData(DataMap dataMap)
    {
        super.fromData(dataMap);
        capacity = dataMap.getInt(CAPACITY);
    }
}
