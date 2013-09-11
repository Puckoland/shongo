package cz.cesnet.shongo;

import cz.cesnet.shongo.report.*;

/**
 * Auto-generated implementation of {@link AbstractReportSet}.
 *
 * @author cz.cesnet.shongo.tool-report-generator
 */
public class JadeReportSet extends AbstractReportSet
{
    /**
     * Unknown error: {@link #description}
     */
    public static class UnknownErrorReport extends cz.cesnet.shongo.JadeReport implements SerializableReport
    {
        protected String description;

        public UnknownErrorReport()
        {
        }

        @Override
        public String getUniqueId()
        {
            return "unknown-error";
        }

        public UnknownErrorReport(String description)
        {
            setDescription(description);
        }

        public String getDescription()
        {
            return description;
        }

        public void setDescription(String description)
        {
            this.description = description;
        }

        @Override
        public Type getType()
        {
            return Report.Type.ERROR;
        }

        @Override
        public Resolution getResolution()
        {
            return Resolution.STOP;
        }

        @Override
        public void readParameters(ReportSerializer reportSerializer)
        {
            description = (String) reportSerializer.getParameter("description", String.class);
        }

        @Override
        public void writeParameters(ReportSerializer reportSerializer)
        {
            reportSerializer.setParameter("description", description);
        }

        @Override
        public java.util.Map<String, Object> getParameters()
        {
            java.util.Map<String, Object> parameters = new java.util.HashMap<String, Object>();
            parameters.put("description", description);
            return parameters;
        }

        @Override
        public String getMessage(MessageType messageType, Language language)
        {
            switch (messageType) {
                default:
                switch (language) {
                    default:
                        return "Unknown error: ${description}";
                }
            }
        }
    }

    /**
     * Exception for {@link UnknownErrorReport}.
     */
    public static class UnknownErrorException extends cz.cesnet.shongo.JadeException
    {
        public UnknownErrorException(UnknownErrorReport report)
        {
            this.report = report;
        }

        public UnknownErrorException(Throwable throwable, UnknownErrorReport report)
        {
            super(throwable);
            this.report = report;
        }

        public UnknownErrorException(String description)
        {
            UnknownErrorReport report = new UnknownErrorReport();
            report.setDescription(description);
            this.report = report;
        }

        public UnknownErrorException(Throwable throwable, String description)
        {
            super(throwable);
            UnknownErrorReport report = new UnknownErrorReport();
            report.setDescription(description);
            this.report = report;
        }

        public String getDescription()
        {
            return getReport().getDescription();
        }

        @Override
        public UnknownErrorReport getReport()
        {
            return (UnknownErrorReport) report;
        }
    }

    /**
     * Receiver agent {@link #receiverAgent} is not available now.
     */
    public static class AgentNotFoundReport extends cz.cesnet.shongo.JadeReport implements SerializableReport
    {
        protected String receiverAgent;

        public AgentNotFoundReport()
        {
        }

        @Override
        public String getUniqueId()
        {
            return "agent-not-found";
        }

        public AgentNotFoundReport(String receiverAgent)
        {
            setReceiverAgent(receiverAgent);
        }

        public String getReceiverAgent()
        {
            return receiverAgent;
        }

        public void setReceiverAgent(String receiverAgent)
        {
            this.receiverAgent = receiverAgent;
        }

        @Override
        public Type getType()
        {
            return Report.Type.ERROR;
        }

        @Override
        public Resolution getResolution()
        {
            return Resolution.TRY_AGAIN;
        }

        @Override
        public void readParameters(ReportSerializer reportSerializer)
        {
            receiverAgent = (String) reportSerializer.getParameter("receiverAgent", String.class);
        }

        @Override
        public void writeParameters(ReportSerializer reportSerializer)
        {
            reportSerializer.setParameter("receiverAgent", receiverAgent);
        }

        @Override
        public java.util.Map<String, Object> getParameters()
        {
            java.util.Map<String, Object> parameters = new java.util.HashMap<String, Object>();
            parameters.put("receiverAgent", receiverAgent);
            return parameters;
        }

        @Override
        public String getMessage(MessageType messageType, Language language)
        {
            switch (messageType) {
                default:
                switch (language) {
                    default:
                        return "Receiver agent ${receiverAgent} is not available now.";
                }
            }
        }
    }

    /**
     * Exception for {@link AgentNotFoundReport}.
     */
    public static class AgentNotFoundException extends cz.cesnet.shongo.JadeException
    {
        public AgentNotFoundException(AgentNotFoundReport report)
        {
            this.report = report;
        }

        public AgentNotFoundException(Throwable throwable, AgentNotFoundReport report)
        {
            super(throwable);
            this.report = report;
        }

        public AgentNotFoundException(String receiverAgent)
        {
            AgentNotFoundReport report = new AgentNotFoundReport();
            report.setReceiverAgent(receiverAgent);
            this.report = report;
        }

        public AgentNotFoundException(Throwable throwable, String receiverAgent)
        {
            super(throwable);
            AgentNotFoundReport report = new AgentNotFoundReport();
            report.setReceiverAgent(receiverAgent);
            this.report = report;
        }

        public String getReceiverAgent()
        {
            return getReport().getReceiverAgent();
        }

        @Override
        public AgentNotFoundReport getReport()
        {
            return (AgentNotFoundReport) report;
        }
    }

    /**
     * Sender agent {@link #senderAgent} is not started yet.
     */
    public static class AgentNotStartedReport extends cz.cesnet.shongo.JadeReport implements SerializableReport
    {
        protected String senderAgent;

        public AgentNotStartedReport()
        {
        }

        @Override
        public String getUniqueId()
        {
            return "agent-not-started";
        }

        public AgentNotStartedReport(String senderAgent)
        {
            setSenderAgent(senderAgent);
        }

        public String getSenderAgent()
        {
            return senderAgent;
        }

        public void setSenderAgent(String senderAgent)
        {
            this.senderAgent = senderAgent;
        }

        @Override
        public Type getType()
        {
            return Report.Type.ERROR;
        }

        @Override
        public Resolution getResolution()
        {
            return Resolution.TRY_AGAIN;
        }

        @Override
        public void readParameters(ReportSerializer reportSerializer)
        {
            senderAgent = (String) reportSerializer.getParameter("senderAgent", String.class);
        }

        @Override
        public void writeParameters(ReportSerializer reportSerializer)
        {
            reportSerializer.setParameter("senderAgent", senderAgent);
        }

        @Override
        public java.util.Map<String, Object> getParameters()
        {
            java.util.Map<String, Object> parameters = new java.util.HashMap<String, Object>();
            parameters.put("senderAgent", senderAgent);
            return parameters;
        }

        @Override
        public String getMessage(MessageType messageType, Language language)
        {
            switch (messageType) {
                default:
                switch (language) {
                    default:
                        return "Sender agent ${senderAgent} is not started yet.";
                }
            }
        }
    }

    /**
     * Exception for {@link AgentNotStartedReport}.
     */
    public static class AgentNotStartedException extends cz.cesnet.shongo.JadeException
    {
        public AgentNotStartedException(AgentNotStartedReport report)
        {
            this.report = report;
        }

        public AgentNotStartedException(Throwable throwable, AgentNotStartedReport report)
        {
            super(throwable);
            this.report = report;
        }

        public AgentNotStartedException(String senderAgent)
        {
            AgentNotStartedReport report = new AgentNotStartedReport();
            report.setSenderAgent(senderAgent);
            this.report = report;
        }

        public AgentNotStartedException(Throwable throwable, String senderAgent)
        {
            super(throwable);
            AgentNotStartedReport report = new AgentNotStartedReport();
            report.setSenderAgent(senderAgent);
            this.report = report;
        }

        public String getSenderAgent()
        {
            return getReport().getSenderAgent();
        }

        @Override
        public AgentNotStartedReport getReport()
        {
            return (AgentNotStartedReport) report;
        }
    }

    public static abstract class CommandAbstractErrorReport extends cz.cesnet.shongo.JadeReport implements SerializableReport
    {
        protected String command;

        public CommandAbstractErrorReport()
        {
        }

        @Override
        public String getUniqueId()
        {
            return "command-abstract-error";
        }

        public CommandAbstractErrorReport(String command)
        {
            setCommand(command);
        }

        public String getCommand()
        {
            return command;
        }

        public void setCommand(String command)
        {
            this.command = command;
        }
    }

    /**
     * Exception for {@link CommandAbstractErrorReport}.
     */
    public static abstract class CommandAbstractErrorException extends cz.cesnet.shongo.JadeException
    {
        public CommandAbstractErrorException()
        {
        }

        public CommandAbstractErrorException(Throwable throwable)
        {
            super(throwable);
        }

        public String getCommand()
        {
            return getReport().getCommand();
        }

        @Override
        public CommandAbstractErrorReport getReport()
        {
            return (CommandAbstractErrorReport) report;
        }
    }

    /**
     * Unknown error in command {@link #command}: {@link #description}
     */
    public static class CommandUnknownErrorReport extends CommandAbstractErrorReport implements SerializableReport
    {
        protected String description;

        public CommandUnknownErrorReport()
        {
        }

        @Override
        public String getUniqueId()
        {
            return "command-unknown-error";
        }

        public CommandUnknownErrorReport(String command, String description)
        {
            setCommand(command);
            setDescription(description);
        }

        public String getDescription()
        {
            return description;
        }

        public void setDescription(String description)
        {
            this.description = description;
        }

        @Override
        public Type getType()
        {
            return Report.Type.ERROR;
        }

        @Override
        public Resolution getResolution()
        {
            return Resolution.STOP;
        }

        @Override
        public void readParameters(ReportSerializer reportSerializer)
        {
            command = (String) reportSerializer.getParameter("command", String.class);
            description = (String) reportSerializer.getParameter("description", String.class);
        }

        @Override
        public void writeParameters(ReportSerializer reportSerializer)
        {
            reportSerializer.setParameter("command", command);
            reportSerializer.setParameter("description", description);
        }

        @Override
        public java.util.Map<String, Object> getParameters()
        {
            java.util.Map<String, Object> parameters = new java.util.HashMap<String, Object>();
            parameters.put("command", command);
            parameters.put("description", description);
            return parameters;
        }

        @Override
        public String getMessage(MessageType messageType, Language language)
        {
            switch (messageType) {
                default:
                switch (language) {
                    default:
                        return "Unknown error in command ${command}: ${description}";
                }
            }
        }
    }

    /**
     * Exception for {@link CommandUnknownErrorReport}.
     */
    public static class CommandUnknownErrorException extends CommandAbstractErrorException
    {
        public CommandUnknownErrorException(CommandUnknownErrorReport report)
        {
            this.report = report;
        }

        public CommandUnknownErrorException(Throwable throwable, CommandUnknownErrorReport report)
        {
            super(throwable);
            this.report = report;
        }

        public CommandUnknownErrorException(String command, String description)
        {
            CommandUnknownErrorReport report = new CommandUnknownErrorReport();
            report.setCommand(command);
            report.setDescription(description);
            this.report = report;
        }

        public CommandUnknownErrorException(Throwable throwable, String command, String description)
        {
            super(throwable);
            CommandUnknownErrorReport report = new CommandUnknownErrorReport();
            report.setCommand(command);
            report.setDescription(description);
            this.report = report;
        }

        public String getDescription()
        {
            return getReport().getDescription();
        }

        @Override
        public CommandUnknownErrorReport getReport()
        {
            return (CommandUnknownErrorReport) report;
        }
    }

    /**
     * Command {@link #command} send to {@link #receiverAgent} has timeout.
     */
    public static class CommandTimeoutReport extends CommandAbstractErrorReport implements SerializableReport
    {
        protected String receiverAgent;

        public CommandTimeoutReport()
        {
        }

        @Override
        public String getUniqueId()
        {
            return "command-timeout";
        }

        public CommandTimeoutReport(String command, String receiverAgent)
        {
            setCommand(command);
            setReceiverAgent(receiverAgent);
        }

        public String getReceiverAgent()
        {
            return receiverAgent;
        }

        public void setReceiverAgent(String receiverAgent)
        {
            this.receiverAgent = receiverAgent;
        }

        @Override
        public Type getType()
        {
            return Report.Type.ERROR;
        }

        @Override
        public Resolution getResolution()
        {
            return Resolution.TRY_AGAIN;
        }

        @Override
        public void readParameters(ReportSerializer reportSerializer)
        {
            command = (String) reportSerializer.getParameter("command", String.class);
            receiverAgent = (String) reportSerializer.getParameter("receiverAgent", String.class);
        }

        @Override
        public void writeParameters(ReportSerializer reportSerializer)
        {
            reportSerializer.setParameter("command", command);
            reportSerializer.setParameter("receiverAgent", receiverAgent);
        }

        @Override
        public java.util.Map<String, Object> getParameters()
        {
            java.util.Map<String, Object> parameters = new java.util.HashMap<String, Object>();
            parameters.put("command", command);
            parameters.put("receiverAgent", receiverAgent);
            return parameters;
        }

        @Override
        public String getMessage(MessageType messageType, Language language)
        {
            switch (messageType) {
                default:
                switch (language) {
                    default:
                        return "Command ${command} send to ${receiverAgent} has timeout.";
                }
            }
        }
    }

    /**
     * Exception for {@link CommandTimeoutReport}.
     */
    public static class CommandTimeoutException extends CommandAbstractErrorException
    {
        public CommandTimeoutException(CommandTimeoutReport report)
        {
            this.report = report;
        }

        public CommandTimeoutException(Throwable throwable, CommandTimeoutReport report)
        {
            super(throwable);
            this.report = report;
        }

        public CommandTimeoutException(String command, String receiverAgent)
        {
            CommandTimeoutReport report = new CommandTimeoutReport();
            report.setCommand(command);
            report.setReceiverAgent(receiverAgent);
            this.report = report;
        }

        public CommandTimeoutException(Throwable throwable, String command, String receiverAgent)
        {
            super(throwable);
            CommandTimeoutReport report = new CommandTimeoutReport();
            report.setCommand(command);
            report.setReceiverAgent(receiverAgent);
            this.report = report;
        }

        public String getReceiverAgent()
        {
            return getReport().getReceiverAgent();
        }

        @Override
        public CommandTimeoutReport getReport()
        {
            return (CommandTimeoutReport) report;
        }
    }

    /**
     * Receiver agent {@link #receiverAgent} doesn't implement command {@link #command}.
     */
    public static class CommandNotSupportedReport extends CommandAbstractErrorReport implements SerializableReport
    {
        protected String receiverAgent;

        public CommandNotSupportedReport()
        {
        }

        @Override
        public String getUniqueId()
        {
            return "command-not-supported";
        }

        public CommandNotSupportedReport(String command, String receiverAgent)
        {
            setCommand(command);
            setReceiverAgent(receiverAgent);
        }

        public String getReceiverAgent()
        {
            return receiverAgent;
        }

        public void setReceiverAgent(String receiverAgent)
        {
            this.receiverAgent = receiverAgent;
        }

        @Override
        public Type getType()
        {
            return Report.Type.ERROR;
        }

        @Override
        public Resolution getResolution()
        {
            return Resolution.STOP;
        }

        @Override
        public void readParameters(ReportSerializer reportSerializer)
        {
            command = (String) reportSerializer.getParameter("command", String.class);
            receiverAgent = (String) reportSerializer.getParameter("receiverAgent", String.class);
        }

        @Override
        public void writeParameters(ReportSerializer reportSerializer)
        {
            reportSerializer.setParameter("command", command);
            reportSerializer.setParameter("receiverAgent", receiverAgent);
        }

        @Override
        public java.util.Map<String, Object> getParameters()
        {
            java.util.Map<String, Object> parameters = new java.util.HashMap<String, Object>();
            parameters.put("command", command);
            parameters.put("receiverAgent", receiverAgent);
            return parameters;
        }

        @Override
        public String getMessage(MessageType messageType, Language language)
        {
            switch (messageType) {
                default:
                switch (language) {
                    default:
                        return "Receiver agent ${receiverAgent} doesn't implement command ${command}.";
                }
            }
        }
    }

    /**
     * Exception for {@link CommandNotSupportedReport}.
     */
    public static class CommandNotSupportedException extends CommandAbstractErrorException
    {
        public CommandNotSupportedException(CommandNotSupportedReport report)
        {
            this.report = report;
        }

        public CommandNotSupportedException(Throwable throwable, CommandNotSupportedReport report)
        {
            super(throwable);
            this.report = report;
        }

        public CommandNotSupportedException(String command, String receiverAgent)
        {
            CommandNotSupportedReport report = new CommandNotSupportedReport();
            report.setCommand(command);
            report.setReceiverAgent(receiverAgent);
            this.report = report;
        }

        public CommandNotSupportedException(Throwable throwable, String command, String receiverAgent)
        {
            super(throwable);
            CommandNotSupportedReport report = new CommandNotSupportedReport();
            report.setCommand(command);
            report.setReceiverAgent(receiverAgent);
            this.report = report;
        }

        public String getReceiverAgent()
        {
            return getReport().getReceiverAgent();
        }

        @Override
        public CommandNotSupportedReport getReport()
        {
            return (CommandNotSupportedReport) report;
        }
    }

    /**
     * Receiver agent {@link #receiverAgent} has refused command {@link #command}.
     */
    public static class CommandRefusedReport extends CommandAbstractErrorReport implements SerializableReport
    {
        protected String receiverAgent;

        public CommandRefusedReport()
        {
        }

        @Override
        public String getUniqueId()
        {
            return "command-refused";
        }

        public CommandRefusedReport(String command, String receiverAgent)
        {
            setCommand(command);
            setReceiverAgent(receiverAgent);
        }

        public String getReceiverAgent()
        {
            return receiverAgent;
        }

        public void setReceiverAgent(String receiverAgent)
        {
            this.receiverAgent = receiverAgent;
        }

        @Override
        public Type getType()
        {
            return Report.Type.ERROR;
        }

        @Override
        public Resolution getResolution()
        {
            return Resolution.STOP;
        }

        @Override
        public void readParameters(ReportSerializer reportSerializer)
        {
            command = (String) reportSerializer.getParameter("command", String.class);
            receiverAgent = (String) reportSerializer.getParameter("receiverAgent", String.class);
        }

        @Override
        public void writeParameters(ReportSerializer reportSerializer)
        {
            reportSerializer.setParameter("command", command);
            reportSerializer.setParameter("receiverAgent", receiverAgent);
        }

        @Override
        public java.util.Map<String, Object> getParameters()
        {
            java.util.Map<String, Object> parameters = new java.util.HashMap<String, Object>();
            parameters.put("command", command);
            parameters.put("receiverAgent", receiverAgent);
            return parameters;
        }

        @Override
        public String getMessage(MessageType messageType, Language language)
        {
            switch (messageType) {
                default:
                switch (language) {
                    default:
                        return "Receiver agent ${receiverAgent} has refused command ${command}.";
                }
            }
        }
    }

    /**
     * Exception for {@link CommandRefusedReport}.
     */
    public static class CommandRefusedException extends CommandAbstractErrorException
    {
        public CommandRefusedException(CommandRefusedReport report)
        {
            this.report = report;
        }

        public CommandRefusedException(Throwable throwable, CommandRefusedReport report)
        {
            super(throwable);
            this.report = report;
        }

        public CommandRefusedException(String command, String receiverAgent)
        {
            CommandRefusedReport report = new CommandRefusedReport();
            report.setCommand(command);
            report.setReceiverAgent(receiverAgent);
            this.report = report;
        }

        public CommandRefusedException(Throwable throwable, String command, String receiverAgent)
        {
            super(throwable);
            CommandRefusedReport report = new CommandRefusedReport();
            report.setCommand(command);
            report.setReceiverAgent(receiverAgent);
            this.report = report;
        }

        public String getReceiverAgent()
        {
            return getReport().getReceiverAgent();
        }

        @Override
        public CommandRefusedReport getReport()
        {
            return (CommandRefusedReport) report;
        }
    }

    /**
     * Receiver agent {@link #receiverAgent} didn't understand command {@link #command}.
     */
    public static class CommandNotUnderstoodReport extends CommandAbstractErrorReport implements SerializableReport
    {
        protected String receiverAgent;

        public CommandNotUnderstoodReport()
        {
        }

        @Override
        public String getUniqueId()
        {
            return "command-not-understood";
        }

        public CommandNotUnderstoodReport(String command, String receiverAgent)
        {
            setCommand(command);
            setReceiverAgent(receiverAgent);
        }

        public String getReceiverAgent()
        {
            return receiverAgent;
        }

        public void setReceiverAgent(String receiverAgent)
        {
            this.receiverAgent = receiverAgent;
        }

        @Override
        public Type getType()
        {
            return Report.Type.ERROR;
        }

        @Override
        public Resolution getResolution()
        {
            return Resolution.STOP;
        }

        @Override
        public void readParameters(ReportSerializer reportSerializer)
        {
            command = (String) reportSerializer.getParameter("command", String.class);
            receiverAgent = (String) reportSerializer.getParameter("receiverAgent", String.class);
        }

        @Override
        public void writeParameters(ReportSerializer reportSerializer)
        {
            reportSerializer.setParameter("command", command);
            reportSerializer.setParameter("receiverAgent", receiverAgent);
        }

        @Override
        public java.util.Map<String, Object> getParameters()
        {
            java.util.Map<String, Object> parameters = new java.util.HashMap<String, Object>();
            parameters.put("command", command);
            parameters.put("receiverAgent", receiverAgent);
            return parameters;
        }

        @Override
        public String getMessage(MessageType messageType, Language language)
        {
            switch (messageType) {
                default:
                switch (language) {
                    default:
                        return "Receiver agent ${receiverAgent} didn't understand command ${command}.";
                }
            }
        }
    }

    /**
     * Exception for {@link CommandNotUnderstoodReport}.
     */
    public static class CommandNotUnderstoodException extends CommandAbstractErrorException
    {
        public CommandNotUnderstoodException(CommandNotUnderstoodReport report)
        {
            this.report = report;
        }

        public CommandNotUnderstoodException(Throwable throwable, CommandNotUnderstoodReport report)
        {
            super(throwable);
            this.report = report;
        }

        public CommandNotUnderstoodException(String command, String receiverAgent)
        {
            CommandNotUnderstoodReport report = new CommandNotUnderstoodReport();
            report.setCommand(command);
            report.setReceiverAgent(receiverAgent);
            this.report = report;
        }

        public CommandNotUnderstoodException(Throwable throwable, String command, String receiverAgent)
        {
            super(throwable);
            CommandNotUnderstoodReport report = new CommandNotUnderstoodReport();
            report.setCommand(command);
            report.setReceiverAgent(receiverAgent);
            this.report = report;
        }

        public String getReceiverAgent()
        {
            return getReport().getReceiverAgent();
        }

        @Override
        public CommandNotUnderstoodReport getReport()
        {
            return (CommandNotUnderstoodReport) report;
        }
    }

    /**
     * Receiver agent {@link #receiverAgent} failed to perform command {@link #command}: {@link #reason}
     */
    public static class CommandFailedReport extends CommandAbstractErrorReport implements SerializableReport
    {
        protected String receiverAgent;

        protected String reason;

        public CommandFailedReport()
        {
        }

        @Override
        public String getUniqueId()
        {
            return "command-failed";
        }

        public CommandFailedReport(String command, String receiverAgent, String reason)
        {
            setCommand(command);
            setReceiverAgent(receiverAgent);
            setReason(reason);
        }

        public String getReceiverAgent()
        {
            return receiverAgent;
        }

        public void setReceiverAgent(String receiverAgent)
        {
            this.receiverAgent = receiverAgent;
        }

        public String getReason()
        {
            return reason;
        }

        public void setReason(String reason)
        {
            this.reason = reason;
        }

        @Override
        public Type getType()
        {
            return Report.Type.ERROR;
        }

        @Override
        public Resolution getResolution()
        {
            return Resolution.STOP;
        }

        @Override
        public void readParameters(ReportSerializer reportSerializer)
        {
            command = (String) reportSerializer.getParameter("command", String.class);
            receiverAgent = (String) reportSerializer.getParameter("receiverAgent", String.class);
            reason = (String) reportSerializer.getParameter("reason", String.class);
        }

        @Override
        public void writeParameters(ReportSerializer reportSerializer)
        {
            reportSerializer.setParameter("command", command);
            reportSerializer.setParameter("receiverAgent", receiverAgent);
            reportSerializer.setParameter("reason", reason);
        }

        @Override
        public java.util.Map<String, Object> getParameters()
        {
            java.util.Map<String, Object> parameters = new java.util.HashMap<String, Object>();
            parameters.put("command", command);
            parameters.put("receiverAgent", receiverAgent);
            parameters.put("reason", reason);
            return parameters;
        }

        @Override
        public String getMessage(MessageType messageType, Language language)
        {
            switch (messageType) {
                default:
                switch (language) {
                    default:
                        return "Receiver agent ${receiverAgent} failed to perform command ${command}: ${reason}";
                }
                case USER:
                switch (language) {
                    default:
                        return "Command ${command} ended with error.";
                }
            }
        }
    }

    /**
     * Exception for {@link CommandFailedReport}.
     */
    public static class CommandFailedException extends CommandAbstractErrorException
    {
        public CommandFailedException(CommandFailedReport report)
        {
            this.report = report;
        }

        public CommandFailedException(Throwable throwable, CommandFailedReport report)
        {
            super(throwable);
            this.report = report;
        }

        public CommandFailedException(String command, String receiverAgent, String reason)
        {
            CommandFailedReport report = new CommandFailedReport();
            report.setCommand(command);
            report.setReceiverAgent(receiverAgent);
            report.setReason(reason);
            this.report = report;
        }

        public CommandFailedException(Throwable throwable, String command, String receiverAgent, String reason)
        {
            super(throwable);
            CommandFailedReport report = new CommandFailedReport();
            report.setCommand(command);
            report.setReceiverAgent(receiverAgent);
            report.setReason(reason);
            this.report = report;
        }

        public String getReceiverAgent()
        {
            return getReport().getReceiverAgent();
        }

        public String getReason()
        {
            return getReport().getReason();
        }

        @Override
        public CommandFailedReport getReport()
        {
            return (CommandFailedReport) report;
        }
    }

    /**
     * Sender agent {@link #senderAgent} cannot decode response from command {@link #command}.
     */
    public static class CommandResultDecodingFailedReport extends CommandAbstractErrorReport implements SerializableReport
    {
        protected String senderAgent;

        public CommandResultDecodingFailedReport()
        {
        }

        @Override
        public String getUniqueId()
        {
            return "command-result-decoding-failed";
        }

        public CommandResultDecodingFailedReport(String command, String senderAgent)
        {
            setCommand(command);
            setSenderAgent(senderAgent);
        }

        public String getSenderAgent()
        {
            return senderAgent;
        }

        public void setSenderAgent(String senderAgent)
        {
            this.senderAgent = senderAgent;
        }

        @Override
        public Type getType()
        {
            return Report.Type.ERROR;
        }

        @Override
        public Resolution getResolution()
        {
            return Resolution.STOP;
        }

        @Override
        public void readParameters(ReportSerializer reportSerializer)
        {
            command = (String) reportSerializer.getParameter("command", String.class);
            senderAgent = (String) reportSerializer.getParameter("senderAgent", String.class);
        }

        @Override
        public void writeParameters(ReportSerializer reportSerializer)
        {
            reportSerializer.setParameter("command", command);
            reportSerializer.setParameter("senderAgent", senderAgent);
        }

        @Override
        public java.util.Map<String, Object> getParameters()
        {
            java.util.Map<String, Object> parameters = new java.util.HashMap<String, Object>();
            parameters.put("command", command);
            parameters.put("senderAgent", senderAgent);
            return parameters;
        }

        @Override
        public String getMessage(MessageType messageType, Language language)
        {
            switch (messageType) {
                default:
                switch (language) {
                    default:
                        return "Sender agent ${senderAgent} cannot decode response from command ${command}.";
                }
            }
        }
    }

    /**
     * Exception for {@link CommandResultDecodingFailedReport}.
     */
    public static class CommandResultDecodingFailedException extends CommandAbstractErrorException
    {
        public CommandResultDecodingFailedException(CommandResultDecodingFailedReport report)
        {
            this.report = report;
        }

        public CommandResultDecodingFailedException(Throwable throwable, CommandResultDecodingFailedReport report)
        {
            super(throwable);
            this.report = report;
        }

        public CommandResultDecodingFailedException(String command, String senderAgent)
        {
            CommandResultDecodingFailedReport report = new CommandResultDecodingFailedReport();
            report.setCommand(command);
            report.setSenderAgent(senderAgent);
            this.report = report;
        }

        public CommandResultDecodingFailedException(Throwable throwable, String command, String senderAgent)
        {
            super(throwable);
            CommandResultDecodingFailedReport report = new CommandResultDecodingFailedReport();
            report.setCommand(command);
            report.setSenderAgent(senderAgent);
            this.report = report;
        }

        public String getSenderAgent()
        {
            return getReport().getSenderAgent();
        }

        @Override
        public CommandResultDecodingFailedReport getReport()
        {
            return (CommandResultDecodingFailedReport) report;
        }
    }

    @Override
    protected void fillReportClasses()
    {
        addReportClass(UnknownErrorReport.class);
        addReportClass(AgentNotFoundReport.class);
        addReportClass(AgentNotStartedReport.class);
        addReportClass(CommandAbstractErrorReport.class);
        addReportClass(CommandUnknownErrorReport.class);
        addReportClass(CommandTimeoutReport.class);
        addReportClass(CommandNotSupportedReport.class);
        addReportClass(CommandRefusedReport.class);
        addReportClass(CommandNotUnderstoodReport.class);
        addReportClass(CommandFailedReport.class);
        addReportClass(CommandResultDecodingFailedReport.class);
    }
}
