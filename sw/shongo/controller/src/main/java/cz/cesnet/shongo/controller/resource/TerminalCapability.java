package cz.cesnet.shongo.controller.resource;

import cz.cesnet.shongo.Technology;
import cz.cesnet.shongo.api.Fault;
import cz.cesnet.shongo.api.FaultException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Capability tells that the device is able to participate in a videoconference call.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
@Entity
public class TerminalCapability extends DeviceCapability
{
    /**
     * List of aliases that are permanently assigned to device.
     */
    private List<Alias> aliases = new ArrayList<Alias>();

    /**
     * @return {@link #aliases}
     */
    @OneToMany(cascade = CascadeType.ALL)
    @Access(AccessType.FIELD)
    public List<Alias> getAliases()
    {
        return aliases;
    }

    /**
     * @param aliasId
     * @return alias with given {@code aliasId}
     * @throws FaultException when the alias doesn't exist
     */
    private Alias getAliasById(Long aliasId) throws FaultException
    {
        for (Alias alias : aliases) {
            if (alias.getId().equals(aliasId)) {
                return alias;
            }
        }
        throw new FaultException(Fault.Common.RECORD_NOT_EXIST, Alias.class, aliasId);
    }

    /**
     * @param alias alias to be added to the {@link #aliases}
     */
    public void addAlias(Alias alias)
    {
        aliases.add(alias);
    }

    /**
     * @param alias alias to be removed from the {@link #aliases}
     */
    public void removeAlias(Alias alias)
    {
        aliases.remove(alias);
    }

    @Override
    public cz.cesnet.shongo.controller.api.Capability toApi() throws FaultException
    {
        cz.cesnet.shongo.controller.api.TerminalCapability api =
                new cz.cesnet.shongo.controller.api.TerminalCapability();
        api.setId(getId().intValue());
        for (Alias alias : aliases) {
            api.addAlias(alias.toApi());
        }
        toApi(api);
        return api;
    }

    @Override
    public void fromApi(cz.cesnet.shongo.controller.api.Capability api, EntityManager entityManager)
            throws FaultException
    {
        cz.cesnet.shongo.controller.api.TerminalCapability apiTerminalCapability =
                (cz.cesnet.shongo.controller.api.TerminalCapability) api;
        // Create/modify technologies
        for (cz.cesnet.shongo.api.Alias apiAlias : apiTerminalCapability.getAliases()) {
            Alias alias;
            if (api.isCollectionItemMarkedAsNew(apiTerminalCapability.ALIASES, apiAlias)) {
                alias = new Alias();
                addAlias(alias);
            }
            else {
                alias = getAliasById(apiAlias.getId().longValue());
            }
            alias.fromApi(apiAlias);
        }
        // Delete technologies
        Set<Technology> apiDeletedTechnologies = api.getCollectionItemsMarkedAsDeleted(api.TECHNOLOGIES);
        for (Technology technology : apiDeletedTechnologies) {
            removeTechnology(technology);
        }
        super.fromApi(api, entityManager);
    }
}
