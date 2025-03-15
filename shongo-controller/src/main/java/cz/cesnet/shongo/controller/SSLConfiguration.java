package cz.cesnet.shongo.controller;

import cz.cesnet.shongo.ssl.ConfiguredSSLContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Configuration of SSL. Workaround for usage in {@link ConfiguredSSLContext#loadConfiguration(HierarchicalConfiguration)}
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
@Slf4j
@Configuration
public class SSLConfiguration extends CombinedConfiguration
{

    /**
     * Name of the controller XML configuration file.
     */
    public static final String CONFIGURATION_FILE = "configuration-file";

    @PostConstruct
    public void addConfigurations()
    {
        // System properties has the highest priority
        addConfiguration(new SystemConfiguration());

        // Passed configuration has lower priority
        String configurationFileName = getString(CONFIGURATION_FILE);
        if (configurationFileName != null) {
            try {
                XMLConfiguration xmlConfiguration = new XMLConfiguration();
                xmlConfiguration.setDelimiterParsingDisabled(true);
                xmlConfiguration.load(configurationFileName);
                addConfiguration(xmlConfiguration);
            }
            catch (ConfigurationException e) {
                log.warn(e.getMessage());
            }
        }

        // Default configuration has the lowest priority
        try {
            XMLConfiguration defaultConfiguration = new XMLConfiguration();
            defaultConfiguration.setDelimiterParsingDisabled(true);
            defaultConfiguration.load(getClass().getClassLoader().getResource("controller-default.cfg.xml"));
            addConfiguration(defaultConfiguration);
        }
        catch (ConfigurationException e) {
            throw new RuntimeException("Failed to load default controller configuration!", e);
        }
    }
}
