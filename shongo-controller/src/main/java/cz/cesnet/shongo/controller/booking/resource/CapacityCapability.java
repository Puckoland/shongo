package cz.cesnet.shongo.controller.booking.resource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;

/**
 * Capability tells that the resource can be allocated partially. It can be allocated multiple times at the same time.
 */
@Entity
public class CapacityCapability extends Capability
{

    @Column(nullable = false)
    private Integer capacity;

    public Integer getCapacity()
    {
        return capacity;
    }

    public void setCapacity(Integer capacity)
    {
        this.capacity = capacity;
    }

    @Override
    protected cz.cesnet.shongo.controller.api.Capability createApi()
    {
        return new cz.cesnet.shongo.controller.api.CapacityCapability();
    }

    @Override
    protected void toApi(cz.cesnet.shongo.controller.api.Capability api)
    {
        cz.cesnet.shongo.controller.api.CapacityCapability capacityCapabilityApi =
                (cz.cesnet.shongo.controller.api.CapacityCapability) api;
        capacityCapabilityApi.setId(getId());
        capacityCapabilityApi.setCapacity(getCapacity());
        super.toApi(api);
    }

    @Override
    public void fromApi(cz.cesnet.shongo.controller.api.Capability api, EntityManager entityManager)
    {
        super.fromApi(api, entityManager);

        cz.cesnet.shongo.controller.api.CapacityCapability capacityCapabilityApi =
                (cz.cesnet.shongo.controller.api.CapacityCapability) api;

        setCapacity(capacityCapabilityApi.getCapacity());
    }
}
