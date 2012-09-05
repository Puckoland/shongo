package cz.cesnet.shongo.connector;

import cz.cesnet.shongo.AliasType;
import cz.cesnet.shongo.Technology;
import cz.cesnet.shongo.api.Alias;
import cz.cesnet.shongo.api.CommandException;
import cz.cesnet.shongo.api.CommandUnsupportedException;
import cz.cesnet.shongo.connector.api.CommonService;
import cz.cesnet.shongo.connector.api.ConnectorInitException;
import cz.cesnet.shongo.connector.api.EndpointService;
import cz.cesnet.shongo.jade.Agent;
import cz.cesnet.shongo.jade.UnknownActionException;
import cz.cesnet.shongo.jade.ontology.Dial;
import jade.content.AgentAction;
import jade.content.Concept;
import jade.core.AID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Jade Agent for Device Connector
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public class ConnectorAgent extends Agent
{
    private static Logger logger = LoggerFactory.getLogger(ConnectorAgent.class);

    private CommonService connector;

    @Override
    protected void setup()
    {
        super.setup();

        registerService("connector", "Connector Service");
    }

    @Override
    protected void takeDown()
    {
        if (connector != null) {
            try {
                connector.disconnect();
            }
            catch (CommandException e) {
                // just suppress the exception, the agent is going not to be working anyway
            }
        }
        super.takeDown();
    }

    /**
     * Starts managing a device. Initializes a connector to the device.
     */
    public void manage(String connectorClass, String address, int port, String username, String password)
            throws ConnectorInitException, CommandException
    {
        try {
            Constructor co = Class.forName(connectorClass).getConstructor(null);
            connector = (CommonService) co.newInstance(null);
            if (connector == null) {
                throw new ConnectorInitException("Invalid connector class: " + connectorClass + " (must implement the CommonService interface)");
            }
        }
        catch (NoSuchMethodException e) {
            throw new ConnectorInitException(
                    "Invalid connector class: " + connectorClass + " (does not define an appropriate constructor)",
                    e
            );
        }
        catch (ClassNotFoundException e) {
            throw new ConnectorInitException("Connector class not found: " + connectorClass, e);
        }
        catch (InvocationTargetException e) {
            throw new ConnectorInitException("Connector class init failed", e);
        }
        catch (InstantiationException e) {
            throw new ConnectorInitException("Connector class init failed", e);
        }
        catch (IllegalAccessException e) {
            throw new ConnectorInitException("Connector class not accessible: " + connectorClass, e);
        }
    }

    public CommonService getConnector()
    {
        return connector;
    }

    @Override
    public Concept handleAgentAction(AgentAction action, AID sender)
            throws UnknownActionException, CommandException, CommandUnsupportedException
    {
        if (action instanceof Dial) {
            Dial cmd = (Dial) action;

            EndpointService endpoint = (EndpointService) connector;
            if (endpoint == null) {
                throw new CommandUnsupportedException("The " + Dial.class + " is supported only on an endpoint.");
            }

            logger.info("Dialing " + cmd.getNumber());
            endpoint.dial(new Alias(Technology.H323, AliasType.E164, cmd.getNumber()));

            return null;
        }

        return super.handleAgentAction(action, sender);
    }
}
