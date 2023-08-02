package cz.cesnet.shongo.controller;

import cz.cesnet.shongo.jade.Container;
import lombok.extern.slf4j.Slf4j;

import jakarta.persistence.EntityManagerFactory;
import java.util.LinkedList;

@Slf4j
public class TestController extends Controller
{

    private static Container jadeContainerInstance;

    protected TestController(ControllerConfiguration configuration, EntityManagerFactory entityManagerFactory)
    {
        super(configuration, entityManagerFactory);
    }

    @Override
    public Container startJade()
    {
        synchronized (TestController.class) {
            if (jadeContainerInstance == null) {
                jadeContainerInstance = super.startJade();
            }
            else {
                log.info("Reusing JADE container...");
                this.jadeContainer = jadeContainerInstance;

                // Add jade agent
                addJadeAgent(configuration.getString(ControllerConfiguration.JADE_AGENT_NAME), jadeAgent);
            }
            jadeContainer.waitForJadeAgentsToStart();
            return jadeContainerInstance;
        }
    }

    @Override
    public void stop()
    {
        synchronized (TestController.class) {
            if (this.jadeContainer != null) {
                log.info("Stopping JADE agents...");
                for (String agentName : new LinkedList<>(this.jadeContainer.getAgentNames())) {
                    this.jadeContainer.removeAgent(agentName);
                }
                this.jadeContainer = null;
            }
            super.stop();
        }
    }
}
