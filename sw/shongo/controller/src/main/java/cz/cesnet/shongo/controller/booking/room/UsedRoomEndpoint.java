    package cz.cesnet.shongo.controller.booking.room;

import cz.cesnet.shongo.Technology;
import cz.cesnet.shongo.api.Room;
import cz.cesnet.shongo.controller.booking.EntityIdentifier;
import cz.cesnet.shongo.controller.booking.resource.DeviceResource;
import cz.cesnet.shongo.controller.executor.Executor;
import cz.cesnet.shongo.controller.Reporter;
import cz.cesnet.shongo.controller.api.UsedRoomExecutable;
import cz.cesnet.shongo.controller.booking.room.settting.RoomSetting;
import cz.cesnet.shongo.controller.booking.executable.Executable;
import cz.cesnet.shongo.controller.booking.executable.ExecutableManager;
import cz.cesnet.shongo.controller.booking.executable.ManagedEndpoint;
import cz.cesnet.shongo.controller.executor.*;
import cz.cesnet.shongo.controller.booking.resource.Address;
import cz.cesnet.shongo.controller.booking.alias.Alias;
import cz.cesnet.shongo.TodoImplementException;
import cz.cesnet.shongo.controller.booking.resource.Resource;
import cz.cesnet.shongo.controller.scheduler.SchedulerException;
import cz.cesnet.shongo.report.Report;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

    /**
 * Represents a re-used {@link RoomEndpoint} for different
 * {@link RoomConfiguration}.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
@Entity
public class UsedRoomEndpoint extends RoomEndpoint implements ManagedEndpoint, Reporter.ResourceContext
{
    /**
     * {@link RoomEndpoint} which is re-used.
     */
    private RoomEndpoint roomEndpoint;

    /**
     * Specifies whether {@link #onStop} is active.
     */
    private boolean isStopping;

    /**
     * Constructor.
     */
    public UsedRoomEndpoint()
    {
    }

    /**
     * @return {@link #roomEndpoint}
     */
    @OneToOne
    @Access(AccessType.FIELD)
    public RoomEndpoint getRoomEndpoint()
    {
        return roomEndpoint;
    }

    /**
     * @param roomEndpoint sets the {@link #roomEndpoint}
     */
    public void setRoomEndpoint(RoomEndpoint roomEndpoint)
    {
        this.roomEndpoint = roomEndpoint;
    }

    /**
     * @return merged {@link RoomConfiguration} of {@link #roomConfiguration} and {@link #roomEndpoint#roomConfiguration}
     */
    @Transient
    private RoomConfiguration getMergedRoomConfiguration()
    {
        RoomConfiguration roomConfiguration = getRoomConfiguration();
        RoomConfiguration roomEndpointConfiguration = roomEndpoint.getRoomConfiguration();
        RoomConfiguration mergedRoomConfiguration = new RoomConfiguration();
        mergedRoomConfiguration.setLicenseCount(
                roomConfiguration.getLicenseCount() + roomEndpointConfiguration.getLicenseCount());
        mergedRoomConfiguration.setTechnologies(roomConfiguration.getTechnologies());
        mergedRoomConfiguration.setRoomSettings(roomConfiguration.getRoomSettings());
        if (roomEndpointConfiguration.getRoomSettings().size() > 0) {
            throw new TodoImplementException("Merging room settings.");
        }
        return mergedRoomConfiguration;
    }

    @Override
    @Transient
    public Collection<Executable> getExecutionDependencies()
    {
        List<Executable> dependencies = new ArrayList<Executable>();
        dependencies.add(roomEndpoint);
        return dependencies;
    }

    @Override
    @Transient
    public Resource getResource()
    {
        if (roomEndpoint instanceof ResourceRoomEndpoint) {
            ResourceRoomEndpoint resourceRoomEndpoint = (ResourceRoomEndpoint) roomEndpoint;
            return resourceRoomEndpoint.getResource();
        }
        else {
            throw new TodoImplementException(roomEndpoint.getClass());
        }
    }

    @Override
    protected cz.cesnet.shongo.controller.api.Executable createApi()
    {
        return new UsedRoomExecutable();
    }

    @Override
    public void toApi(cz.cesnet.shongo.controller.api.Executable executableApi, Report.UserType userType)
    {
        super.toApi(executableApi, userType);

        UsedRoomExecutable usedRoomExecutableEndpointApi =
                (UsedRoomExecutable) executableApi;

        usedRoomExecutableEndpointApi.setRoomExecutableId(EntityIdentifier.formatId(roomEndpoint));

        RoomConfiguration roomConfiguration = getMergedRoomConfiguration();
        usedRoomExecutableEndpointApi.setLicenseCount(roomConfiguration.getLicenseCount());
        for (Technology technology : roomConfiguration.getTechnologies()) {
            usedRoomExecutableEndpointApi.addTechnology(technology);
        }
        for (Alias alias : getAliases()) {
            usedRoomExecutableEndpointApi.addAlias(alias.toApi());
        }
        for (RoomSetting roomSetting : roomConfiguration.getRoomSettings()) {
            usedRoomExecutableEndpointApi.addRoomSetting(roomSetting.toApi());
        }
    }

    @Transient
    @Override
    public int getEndpointServiceCount()
    {
        return super.getEndpointServiceCount() + roomEndpoint.getEndpointServiceCount();
    }

    @Transient
    @Override
    public DeviceResource getDeviceResource()
    {
        return roomEndpoint.getDeviceResource();
    }

    @Override
    @Transient
    public String getRoomId()
    {
        return roomEndpoint.getRoomId();
    }

    @Override
    @Transient
    public boolean isStandalone()
    {
        return roomEndpoint.isStandalone();
    }

    @Override
    @Transient
    public List<Alias> getAliases()
    {
        List<Alias> aliases = new ArrayList<Alias>();
        aliases.addAll(roomEndpoint.getAliases());
        aliases.addAll(super.getAssignedAliases());
        return aliases;
    }

    @Override
    public void addAssignedAlias(Alias assignedAlias) throws SchedulerException
    {
        super.addAssignedAlias(assignedAlias);
    }

    @Override
    @Transient
    public Address getAddress()
    {
        return roomEndpoint.getAddress();
    }

    @Override
    @Transient
    public String getReportDescription()
    {
        return roomEndpoint.getReportDescription();
    }

    @Override
    @Transient
    public String getConnectorAgentName()
    {
        if (roomEndpoint instanceof ManagedEndpoint) {
            ManagedEndpoint managedEndpoint = (ManagedEndpoint) roomEndpoint;
            return managedEndpoint.getConnectorAgentName();
        }
        return null;
    }

    @Override
    public void fillRoomApi(Room roomApi, ExecutableManager executableManager)
    {
        super.fillRoomApi(roomApi, executableManager);

        // Use reused room configuration
        roomEndpoint.fillRoomApi(roomApi, executableManager);

        // Modify the room configuration (only when we aren't stopping the reused room)
        if (!isStopping) {
            RoomConfiguration roomConfiguration = getMergedRoomConfiguration();
            roomApi.setDescription(getRoomDescriptionApi());
            roomApi.setLicenseCount(roomConfiguration.getLicenseCount() + getEndpointServiceCount());
            for (RoomSetting roomSetting : roomConfiguration.getRoomSettings()) {
                roomApi.addRoomSetting(roomSetting.toApi());
            }
            for (Alias alias : getAssignedAliases()) {
                roomApi.addAlias(alias.toApi());
            }
        }
    }

    @Override
    public void modifyRoom(Room roomApi, Executor executor)
            throws ExecutorReportSet.RoomNotStartedException, ExecutorReportSet.CommandFailedException
    {
        roomEndpoint.modifyRoom(roomApi, executor);
    }

    @Override
    protected Executable.State onStart(Executor executor, ExecutableManager executableManager)
    {
        try {
            modifyRoom(getRoomApi(executableManager), executor);
            return Executable.State.STARTED;
        }
        catch (ExecutorReportSet.RoomNotStartedException exception) {
            executableManager.createExecutionReport(this, exception.getReport());
        }
        catch (ExecutorReportSet.CommandFailedException exception) {
            executableManager.createExecutionReport(this, exception.getReport());
        }
        return Executable.State.STARTING_FAILED;
    }

    @Override
    protected Executable.State onUpdate(Executor executor, ExecutableManager executableManager)
    {
        try {
            modifyRoom(getRoomApi(executableManager), executor);
            return Executable.State.STARTED;
        }
        catch (ExecutorReportSet.RoomNotStartedException exception) {
            executableManager.createExecutionReport(this, exception.getReport());
        }
        catch (ExecutorReportSet.CommandFailedException exception) {
            executableManager.createExecutionReport(this, exception.getReport());
        }
        return null;
    }

    @Override
    protected Executable.State onStop(Executor executor, ExecutableManager executableManager)
    {
        isStopping = true;
        try {
            modifyRoom(getRoomApi(executableManager), executor);
            return Executable.State.STOPPED;
        }
        catch (ExecutorReportSet.RoomNotStartedException exception) {
            executableManager.createExecutionReport(this, exception.getReport());
        }
        catch (ExecutorReportSet.CommandFailedException exception) {
            executableManager.createExecutionReport(this, exception.getReport());
        }
        finally {
            isStopping = false;
        }
        return Executable.State.STOPPING_FAILED;
    }
}
