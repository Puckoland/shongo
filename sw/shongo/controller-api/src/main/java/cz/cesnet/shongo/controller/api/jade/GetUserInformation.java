package cz.cesnet.shongo.controller.api.jade;

import cz.cesnet.shongo.api.CommandException;
import jade.core.Agent;

/**
 * @author Martin Srom <martin.srom@cesnet.cz>
 * @see {@link cz.cesnet.shongo.controller.api.jade.Service#getUserInformation(String)}
 */
public class GetUserInformation extends ControllerCommand
{
    private String userId;

    public GetUserInformation()
    {
    }

    public GetUserInformation(String userId)
    {
        this.userId = userId;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    @Override
    public Object execute(Service commonService, String senderAgentName) throws CommandException
    {
        return commonService.getUserInformation(userId);
    }

    @Override
    public String toString()
    {
        return String.format(GetUserInformation.class.getSimpleName() + " (userId: %s)", userId);
    }
}