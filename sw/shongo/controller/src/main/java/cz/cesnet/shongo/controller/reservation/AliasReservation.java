package cz.cesnet.shongo.controller.reservation;

import cz.cesnet.shongo.AliasType;
import cz.cesnet.shongo.controller.common.EntityIdentifier;
import cz.cesnet.shongo.controller.resource.Alias;
import cz.cesnet.shongo.controller.resource.AliasProviderCapability;
import cz.cesnet.shongo.controller.resource.DeviceResource;
import cz.cesnet.shongo.controller.resource.Resource;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a {@link Reservation} for a {@link Alias}.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
@Entity
public class AliasReservation extends Reservation
{
    /**
     * {@link cz.cesnet.shongo.controller.resource.AliasProviderCapability} from which the alias is allocated.
     */
    private AliasProviderCapability aliasProviderCapability;

    /**
     * {@link ValueReservation} for value which is used for the {@link #aliasProviderCapability#getAliases()}
     */
    private ValueReservation valueReservation;

    /**
     * Constructor.
     */
    public AliasReservation()
    {
    }

    /**
     * @return {@link #aliasProviderCapability}
     */
    @ManyToOne(optional = false)
    @Access(AccessType.FIELD)
    public AliasProviderCapability getAliasProviderCapability()
    {
        return aliasProviderCapability;
    }

    /**
     * @param aliasProviderCapability sets the {@link #aliasProviderCapability}
     */
    public void setAliasProviderCapability(AliasProviderCapability aliasProviderCapability)
    {
        this.aliasProviderCapability = aliasProviderCapability;
    }

    /**
     * @return {@link #valueReservation}
     */
    @ManyToOne(optional = false)
    @Access(AccessType.FIELD)
    public ValueReservation getValueReservation()
    {
        return valueReservation;
    }

    /**
     * @param valueReservation sets the {@link #valueReservation}
     */
    public void setValueReservation(ValueReservation valueReservation)
    {
        this.valueReservation = valueReservation;
    }

    /**
     * @return {@link #valueReservation#getValue()}
     */
    @Transient
    public String getValue()
    {
        return valueReservation.getValue();
    }

    /**
     * @param alias to be evaluated by the {@link #valueReservation} and {@link #aliasProviderCapability}
     */
    private void evaluateAlias(Alias alias)
    {
        String aliasValue = alias.getValue();
        aliasValue = valueReservation.evaluateValue(aliasValue);
        alias.setValue(aliasValue);
        if (aliasProviderCapability.isRestrictedToResource()) {
            Resource resource = aliasProviderCapability.getResource();
            if (resource instanceof DeviceResource) {
                DeviceResource deviceResource = (DeviceResource) resource;
                deviceResource.evaluateAlias(alias);
            }
        }
    }

    /**
     * @return collection of {@link Alias}es which are allocated by the {@link #valueReservation}
     */
    @Transient
    public Collection<Alias> getAliases()
    {
        List<Alias> aliases = new ArrayList<Alias>();
        for (Alias aliasTemplate : aliasProviderCapability.getAliases()) {
            Alias alias = new Alias();
            alias.setType(aliasTemplate.getType());
            alias.setValue(aliasTemplate.getValue());
            evaluateAlias(alias);
            aliases.add(alias);
        }
        return aliases;
    }

    /**
     * @return collection of {@link Alias}es which are allocated by the {@link #valueReservation}
     */
    @Transient
    public Alias getAlias(AliasType aliasType)
    {
        for (Alias aliasTemplate : aliasProviderCapability.getAliases()) {
            if (aliasTemplate.getType().equals(aliasType)) {
                Alias alias = new Alias();
                alias.setType(aliasTemplate.getType());
                alias.setValue(aliasTemplate.getValue());
                evaluateAlias(alias);
                return alias;
            }
        }
        return null;
    }

    @Override
    public cz.cesnet.shongo.controller.api.AliasReservation toApi()
    {
        return (cz.cesnet.shongo.controller.api.AliasReservation) super.toApi();
    }

    @Override
    protected cz.cesnet.shongo.controller.api.Reservation createApi()
    {
        return new cz.cesnet.shongo.controller.api.AliasReservation();
    }

    @Override
    protected void toApi(cz.cesnet.shongo.controller.api.Reservation api)
    {
        cz.cesnet.shongo.controller.api.AliasReservation aliasReservationApi =
                (cz.cesnet.shongo.controller.api.AliasReservation) api;
        aliasReservationApi.setResourceId(EntityIdentifier.formatId(aliasProviderCapability.getResource()));
        aliasReservationApi.setResourceName(aliasProviderCapability.getResource().getName());
        aliasReservationApi.setValueReservation(valueReservation.toApi());
        for (Alias alias : getAliases()) {
            aliasReservationApi.addAlias(alias.toApi());
        }
        super.toApi(api);
    }
}
