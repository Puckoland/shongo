package cz.cesnet.shongo.controller.compartment;

import cz.cesnet.shongo.controller.ControllerAgent;
import cz.cesnet.shongo.controller.resource.Alias;
import cz.cesnet.shongo.jade.command.SendCommand;
import cz.cesnet.shongo.jade.ontology.Dial;
import cz.cesnet.shongo.jade.ontology.HangUpAll;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * Represents a {@link Connection} by which is establish by a {@link Alias}.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
@Entity
public class ConnectionByAlias extends Connection
{
    /**
     * {@link Alias} of the {@link Connection#endpointTo}.
     */
    private Alias alias;

    /**
     * @return {@link #alias}
     */
    @OneToOne
    @Access(AccessType.FIELD)
    public Alias getAlias()
    {
        return alias;
    }

    /**
     * @param alias sets the {@link #alias}
     */
    public void setAlias(Alias alias)
    {
        this.alias = alias;
    }

    @Override
    protected void onEstablish(CompartmentExecutor compartmentExecutor)
    {
        StringBuilder message = new StringBuilder();
        message.append(String.format("Dialing from %s to alias '%s' in technology '%s'.",
                getEndpointFrom().getReportDescription(), getAlias().getValue(),
                getAlias().getTechnology().getName()));
        compartmentExecutor.getLogger().debug(message.toString());

        if (getEndpointFrom() instanceof ManagedEndpoint) {
            ManagedEndpoint managedEndpointFrom = (ManagedEndpoint) getEndpointFrom();
            String agentName = managedEndpointFrom.getConnectorAgentName();
            ControllerAgent controllerAgent = compartmentExecutor.getControllerAgent();
            controllerAgent.performCommand(SendCommand.createSendCommand(agentName, new Dial(getAlias().toApi())));

            // TODO: store connection id
        }
    }

    @Override
    protected void onClose(CompartmentExecutor compartmentExecutor)
    {
        StringBuilder message = new StringBuilder();
        message.append(String.format("Hanging up the %s.", getEndpointFrom().getReportDescription()));
        compartmentExecutor.getLogger().debug(message.toString());

        if (getEndpointFrom() instanceof ManagedEndpoint) {
            ManagedEndpoint managedEndpointFrom = (ManagedEndpoint) getEndpointFrom();
            String agentName = managedEndpointFrom.getConnectorAgentName();
            ControllerAgent controllerAgent = compartmentExecutor.getControllerAgent();
            controllerAgent.performCommand(SendCommand.createSendCommand(agentName, new HangUpAll()));

            // TODO: use connection id to hangup
        }
    }
}
