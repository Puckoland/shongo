package cz.cesnet.shongo.controller;

import org.apache.commons.cli.*;
import org.springframework.core.env.AbstractEnvironment;

import java.util.Comparator;

public class Application
{

    public static void main(String[] args) throws Exception {
        // Create options
        Option optionHelp = new Option(null, "help", false, "Print this usage information");
        Option optionHost = OptionBuilder.withLongOpt("host")
                .withArgName("HOST")
                .hasArg()
                .withDescription("Set the local interface address on which the controller will run")
                .create("h");
        Option optionRpcPort = OptionBuilder.withLongOpt("rpc-port")
                .withArgName("PORT")
                .hasArg()
                .withDescription("Set the port on which the XML-RPC server will run")
                .create("r");
        Option optionJadePort = OptionBuilder.withLongOpt("jade-port")
                .withArgName("PORT")
                .hasArg()
                .withDescription("Set the port on which the JADE main controller will run")
                .create("a");
        Option optionJadePlatform = OptionBuilder.withLongOpt("jade-platform")
                .withArgName("PLATFORM")
                .hasArg()
                .withDescription("Set the platform-id for the JADE main controller")
                .create("p");
        Option optionConfig = OptionBuilder.withLongOpt("config")
                .withArgName("FILENAME")
                .hasArg()
                .withDescription("Controller XML configuration file")
                .create("g");
        Option optionDaemon = OptionBuilder.withLongOpt("daemon")
                .withDescription("Controller will be started as daemon without the interactive shell")
                .create("d");
        Options options = new Options();
        options.addOption(optionHost);
        options.addOption(optionRpcPort);
        options.addOption(optionJadePort);
        options.addOption(optionJadePlatform);
        options.addOption(optionHelp);
        options.addOption(optionConfig);
        options.addOption(optionDaemon);

        // Parse command line
        final CommandLine commandLine;
        try {
            CommandLineParser parser = new PosixParser();
            commandLine = parser.parse(options, args);
        }
        catch (ParseException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }

        // Print help
        if (commandLine.hasOption(optionHelp.getLongOpt())) {
            HelpFormatter formatter = getHelpFormatter();
            formatter.printHelp("controller", options);
            System.exit(0);
        }

        // Process parameters
        if (commandLine.hasOption(optionHost.getOpt())) {
            String host = commandLine.getOptionValue(optionHost.getOpt());
            System.setProperty(ControllerConfiguration.RPC_HOST, host);
            System.setProperty(ControllerConfiguration.JADE_HOST, host);
        }
        if (commandLine.hasOption(optionRpcPort.getOpt())) {
            System.setProperty(ControllerConfiguration.RPC_PORT, commandLine.getOptionValue(optionRpcPort.getOpt()));
        }
        if (commandLine.hasOption(optionJadePort.getOpt())) {
            System.setProperty(ControllerConfiguration.JADE_PORT, commandLine.getOptionValue(optionJadePort.getOpt()));
        }
        if (commandLine.hasOption(optionJadePlatform.getOpt())) {
            System.setProperty(ControllerConfiguration.JADE_PLATFORM_ID, commandLine.getOptionValue(optionJadePlatform.getOpt()));
        }
        if (commandLine.hasOption(optionDaemon.getOpt())) {
            System.setProperty(ControllerConfiguration.DAEMON, "true");
        }

        // Get configuration file name
        String configurationFileName = "shongo-controller.cfg.xml";
        if (commandLine.hasOption(optionConfig.getOpt())) {
            configurationFileName = commandLine.getOptionValue(optionConfig.getOpt());
        }
        System.setProperty(ControllerConfiguration.CONFIGURATION_FILE, configurationFileName);

        System.setProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, "production");
        Controller.init();
    }

    private static HelpFormatter getHelpFormatter()
    {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator((Comparator<Option>) (opt1, opt2) -> {
            if (opt1.getOpt() == null && opt2.getOpt() != null) {
                return -1;
            }
            if (opt1.getOpt() != null && opt2.getOpt() == null) {
                return 1;
            }
            if (opt1.getOpt() == null && opt2.getOpt() == null) {
                return opt1.getLongOpt().compareTo(opt2.getLongOpt());
            }
            return opt1.getOpt().compareTo(opt2.getOpt());
        });
        return formatter;
    }
}
