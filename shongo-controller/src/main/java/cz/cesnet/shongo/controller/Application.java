package cz.cesnet.shongo.controller;

import org.apache.commons.cli.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.AbstractEnvironment;

import jakarta.persistence.EntityManagerFactory;
import java.util.Comparator;

public class Application
{

    private static final Option OPTION_HELP = new Option(null, "help", false, "Print this usage information");
    private static final Option OPTION_HOST = OptionBuilder.withLongOpt("host")
            .withArgName("HOST")
            .hasArg()
            .withDescription("Set the local interface address on which the controller will run")
            .create("h");
    private static final Option OPTION_RPC_PORT = OptionBuilder.withLongOpt("rpc-port")
            .withArgName("PORT")
            .hasArg()
            .withDescription("Set the port on which the XML-RPC server will run")
            .create("r");
    private static final Option OPTION_JADE_PORT = OptionBuilder.withLongOpt("jade-port")
            .withArgName("PORT")
            .hasArg()
            .withDescription("Set the port on which the JADE main controller will run")
            .create("a");
    private static final Option OPTION_JADE_PLATFORM = OptionBuilder.withLongOpt("jade-platform")
            .withArgName("PLATFORM")
            .hasArg()
            .withDescription("Set the platform-id for the JADE main controller")
            .create("p");
    private static final Option OPTION_CONFIG = OptionBuilder.withLongOpt("config")
            .withArgName("FILENAME")
            .hasArg()
            .withDescription("Controller XML configuration file")
            .create("g");
    private static final Option OPTION_DAEMON = OptionBuilder.withLongOpt("daemon")
            .withDescription("Controller will be started as daemon without the interactive shell")
            .create("d");

    private static final Options OPTIONS = new Options()
            .addOption(OPTION_HELP)
            .addOption(OPTION_HOST)
            .addOption(OPTION_RPC_PORT)
            .addOption(OPTION_JADE_PORT)
            .addOption(OPTION_JADE_PLATFORM)
            .addOption(OPTION_CONFIG)
            .addOption(OPTION_DAEMON);

    public static void main(String[] args) throws Exception {
        final CommandLine commandLine = parseCommandLine(args);
        printHelp(commandLine);
        processArguments(commandLine);

        System.setProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, "production");

        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
        final EntityManagerFactory entityManagerFactory = context.getBean(EntityManagerFactory.class);
        Controller.init(entityManagerFactory);
    }

    private static CommandLine parseCommandLine(String[] args) throws ParseException
    {
        CommandLineParser parser = new PosixParser();
        return parser.parse(OPTIONS, args);
    }

    private static void printHelp(CommandLine commandLine)
    {
        if (commandLine.hasOption(OPTION_HELP.getLongOpt())) {
            HelpFormatter formatter = getHelpFormatter();
            formatter.printHelp("controller", OPTIONS);
            System.exit(0);
        }
    }

    private static void processArguments(CommandLine commandLine)
    {
        if (commandLine.hasOption(OPTION_HOST.getOpt())) {
            String host = commandLine.getOptionValue(OPTION_HOST.getOpt());
            System.setProperty(ControllerConfiguration.RPC_HOST, host);
            System.setProperty(ControllerConfiguration.JADE_HOST, host);
        }
        if (commandLine.hasOption(OPTION_RPC_PORT.getOpt())) {
            System.setProperty(ControllerConfiguration.RPC_PORT, commandLine.getOptionValue(OPTION_RPC_PORT.getOpt()));
        }
        if (commandLine.hasOption(OPTION_JADE_PORT.getOpt())) {
            System.setProperty(ControllerConfiguration.JADE_PORT, commandLine.getOptionValue(OPTION_JADE_PORT.getOpt()));
        }
        if (commandLine.hasOption(OPTION_JADE_PLATFORM.getOpt())) {
            System.setProperty(ControllerConfiguration.JADE_PLATFORM_ID, commandLine.getOptionValue(OPTION_JADE_PLATFORM.getOpt()));
        }
        if (commandLine.hasOption(OPTION_DAEMON.getOpt())) {
            System.setProperty(ControllerConfiguration.DAEMON, "true");
        }

        // Get configuration file name
        String configurationFileName = "shongo-controller.cfg.xml";
        if (commandLine.hasOption(OPTION_CONFIG.getOpt())) {
            configurationFileName = commandLine.getOptionValue(OPTION_CONFIG.getOpt());
        }
        System.setProperty(ControllerConfiguration.CONFIGURATION_FILE, configurationFileName);
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
