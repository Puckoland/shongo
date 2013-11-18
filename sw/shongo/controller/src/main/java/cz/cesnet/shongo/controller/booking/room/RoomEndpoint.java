package cz.cesnet.shongo.controller.booking.room;

import cz.cesnet.shongo.ParticipantRole;
import cz.cesnet.shongo.Technology;
import cz.cesnet.shongo.TodoImplementException;
import cz.cesnet.shongo.api.Room;
import cz.cesnet.shongo.api.UserInformation;
import cz.cesnet.shongo.controller.ControllerReportSet;
import cz.cesnet.shongo.controller.Domain;
import cz.cesnet.shongo.controller.Role;
import cz.cesnet.shongo.controller.api.AbstractRoomExecutable;
import cz.cesnet.shongo.controller.api.ExecutableConfiguration;
import cz.cesnet.shongo.controller.api.RoomExecutableParticipantConfiguration;
import cz.cesnet.shongo.controller.api.Synchronization;
import cz.cesnet.shongo.controller.authorization.Authorization;
import cz.cesnet.shongo.controller.booking.EntityIdentifier;
import cz.cesnet.shongo.controller.booking.alias.Alias;
import cz.cesnet.shongo.controller.booking.executable.Endpoint;
import cz.cesnet.shongo.controller.booking.executable.EndpointExecutableService;
import cz.cesnet.shongo.controller.booking.executable.ExecutableManager;
import cz.cesnet.shongo.controller.booking.executable.ExecutableService;
import cz.cesnet.shongo.controller.booking.participant.AbstractParticipant;
import cz.cesnet.shongo.controller.booking.participant.PersonParticipant;
import cz.cesnet.shongo.controller.booking.person.AbstractPerson;
import cz.cesnet.shongo.controller.booking.person.UserPerson;
import cz.cesnet.shongo.controller.booking.recording.RecordableEndpoint;
import cz.cesnet.shongo.controller.booking.recording.RecordingCapability;
import cz.cesnet.shongo.controller.executor.Executor;
import cz.cesnet.shongo.controller.executor.ExecutorReportSet;
import cz.cesnet.shongo.report.Report;
import cz.cesnet.shongo.report.ReportException;

import javax.persistence.*;
import java.util.*;

/**
 * Represents an room {@link cz.cesnet.shongo.controller.booking.executable.Endpoint} in which multiple other {@link cz.cesnet.shongo.controller.booking.executable.Endpoint}s can be interconnected.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
@Entity
public abstract class RoomEndpoint extends Endpoint implements RecordableEndpoint
{
    /**
     * @see RoomConfiguration
     */
    private RoomConfiguration roomConfiguration;

    /**
     * Description of the room which can be displayed to the user.
     */
    private String roomDescription;

    /**
     * List of {@link cz.cesnet.shongo.controller.booking.participant.AbstractParticipant}s for the {@link RoomEndpoint}.
     */
    private List<AbstractParticipant> participants = new LinkedList<AbstractParticipant>();

    /**
     * @see RecordableEndpoint#getRecordingFolderId
     */
    private Map<RecordingCapability, String> recordingFolderIds = new HashMap<RecordingCapability, String>();

    /**
     * @return {@link #roomConfiguration}
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @Access(AccessType.FIELD)
    public RoomConfiguration getRoomConfiguration()
    {
        return roomConfiguration;
    }

    /**
     * @param roomConfiguration sets the {@link #roomConfiguration}
     */
    public void setRoomConfiguration(RoomConfiguration roomConfiguration)
    {
        this.roomConfiguration = roomConfiguration;
    }

    /**
     * @return {@link #roomDescription}
     */
    @Column
    public String getRoomDescription()
    {
        return roomDescription;
    }

    /**
     * @param roomDescription sets the {@link #roomDescription}
     */
    public void setRoomDescription(String roomDescription)
    {
        this.roomDescription = roomDescription;
    }

    @Transient
    public final String getRoomDescriptionApi()
    {
        if (roomDescription != null) {
            return String.format("[%s:exe:%d] %s", Domain.getLocalDomainName(), getId(), roomDescription);
        }
        else {
            return String.format("[%s:exe:%d]", Domain.getLocalDomainName(), getId());
        }
    }

    /**
     * @return {@link #participants}
     */
    @OneToMany(cascade = CascadeType.ALL)
    @Access(AccessType.FIELD)
    public List<AbstractParticipant> getParticipants()
    {
        return participants;
    }

    /**
     * @param participants sets the {@link #participants}
     */
    public void setParticipants(List<AbstractParticipant> participants)
    {
        this.participants.clear();
        for (AbstractParticipant participant : participants) {
            this.participants.add(participant.clone());
        }
    }

    /**
     * @param participant to be added to the {@link #participants}
     */
    public void addParticipant(AbstractParticipant participant)
    {
        participants.add(participant);
    }

    @ElementCollection
    @Column(name = "recording_folder_id")
    @MapKeyJoinColumn(name = "recording_capability_id")
    @Access(AccessType.FIELD)
    public Map<RecordingCapability, String> getRecordingFolderIds()
    {
        return recordingFolderIds;
    }

    @Transient
    @Override
    public String getRecordingFolderId(RecordingCapability recordingCapability)
    {
        return recordingFolderIds.get(recordingCapability);
    }

    @Transient
    @Override
    public void putRecordingFolderId(RecordingCapability recordingCapability, String recordingFolderId)
    {
        this.recordingFolderIds.put(recordingCapability, recordingFolderId);
    }

    /**
     * @return {@link RoomConfiguration#licenseCount}
     */
    @Transient
    public int getEndpointServiceCount()
    {
        int endpointServiceCount = 0;
        for (ExecutableService service : services) {
            if (service instanceof EndpointExecutableService && service.isActive()) {
                EndpointExecutableService endpointService = (EndpointExecutableService) service;
                if (endpointService.isEndpoint()) {
                    endpointServiceCount += 1;
                }
            }
        }
        return endpointServiceCount;
    }

    @Override
    @Transient
    public int getCount()
    {
        return 0;
    }

    @Override
    @Transient
    public Set<Technology> getTechnologies()
    {
        if (roomConfiguration == null) {
            throw new IllegalStateException("Room configuration hasn't been set yet.");
        }
        return roomConfiguration.getTechnologies();
    }

    @Transient
    @Override
    public Alias getRecordingAlias()
    {
        Alias callableAlias = null;
        for (Alias alias : getAliases()) {
            if (alias.isCallable()) {
                callableAlias = alias;
                break;
            }
        }
        if (callableAlias == null) {
            throw new RuntimeException("No callable alias exists for '" + EntityIdentifier.formatId(this) + ".");
        }
        return callableAlias;
    }

    @Transient
    @Override
    public String getRecordingFolderDescription()
    {
        return String.format("[%s:exe:%d][res:%d][room:%s]",
                Domain.getLocalDomainName(), getId(), getDeviceResource().getId(), getRoomId());
    }

    /**
     * @return {@link Technology} specific id of the {@link RoomConfiguration}.
     */
    @Transient
    public abstract String getRoomId();

    /**
     * @param executableManager {@link ExecutableManager} which can be used
     * @return {@link cz.cesnet.shongo.api.Room} representing the current room for the {@link RoomEndpoint}
     */
    @Transient
    public Room getRoomApi(ExecutableManager executableManager)
    {
        Room roomApi = new Room();
        fillRoomApi(roomApi, executableManager);

        // If roomApi doesn't contain any participant with ParticipantRole#ADMIN, fill the owners of this room
        Authorization authorization = Authorization.getInstance();
        if (!roomApi.hasParticipantWithRole(ParticipantRole.ADMIN)) {
            for (UserInformation executableOwner : authorization.getUsersWithRole(this, Role.OWNER)) {
                roomApi.addParticipantRole(executableOwner.getUserId(), ParticipantRole.ADMIN);
            }
        }

        return roomApi;
    }

    /**
     * @param roomApi           to be filled
     * @param executableManager {@link ExecutableManager} which can be used
     */
    public void fillRoomApi(Room roomApi, ExecutableManager executableManager)
    {
        roomApi.setDescription(getRoomDescriptionApi());
        for (AbstractParticipant participant : getParticipants()) {
            if (participant instanceof PersonParticipant) {
                PersonParticipant personParticipant = (PersonParticipant) participant;
                AbstractPerson person = personParticipant.getPerson();
                if (person instanceof UserPerson) {
                    UserPerson userPerson = (UserPerson) person;
                    roomApi.addParticipantRole(userPerson.getUserId(), personParticipant.getRole());
                }
            }
        }
    }

    @Override
    public void toApi(cz.cesnet.shongo.controller.api.Executable executableApi, Report.UserType userType)
    {
        super.toApi(executableApi, userType);

        AbstractRoomExecutable abstractRoomExecutableApi =
                (AbstractRoomExecutable) executableApi;

        RoomExecutableParticipantConfiguration participantConfiguration = new RoomExecutableParticipantConfiguration();
        for (AbstractParticipant participant : participants) {
            participantConfiguration.addParticipant(participant.toApi());
        }
        abstractRoomExecutableApi.setParticipantConfiguration(participantConfiguration);
    }

    @Override
    public boolean updateFromExecutableConfigurationApi(ExecutableConfiguration executableConfiguration,
            final EntityManager entityManager)
            throws ControllerReportSet.ExecutableInvalidConfigurationException
    {
        if (executableConfiguration instanceof RoomExecutableParticipantConfiguration) {
            RoomExecutableParticipantConfiguration participantConfiguration =
                    (RoomExecutableParticipantConfiguration) executableConfiguration;

            return Synchronization.synchronizeCollection(participants, participantConfiguration.getParticipants(),
                    new Synchronization.Handler<AbstractParticipant, cz.cesnet.shongo.controller.api.AbstractParticipant>(
                            AbstractParticipant.class)
                    {
                        @Override
                        public AbstractParticipant createFromApi(
                                cz.cesnet.shongo.controller.api.AbstractParticipant objectApi)
                        {
                            return AbstractParticipant.createFromApi(objectApi, entityManager);
                        }

                        @Override
                        public void updateFromApi(AbstractParticipant object,
                                cz.cesnet.shongo.controller.api.AbstractParticipant objectApi)
                        {
                            object.fromApi(objectApi, entityManager);
                        }
                    });
        }
        else {
            return super.updateFromExecutableConfigurationApi(executableConfiguration, entityManager);
        }
    }

    /**
     * @param roomApi  to be modified
     * @param executor to be used
     * @throws cz.cesnet.shongo.controller.executor.ExecutorReportSet.RoomNotStartedException,
     *          ExecutorReportSet.CommandFailedException
     */
    public abstract void modifyRoom(Room roomApi, Executor executor)
            throws ExecutorReportSet.RoomNotStartedException, ExecutorReportSet.CommandFailedException;

    @Override
    protected void onServiceActivation(ExecutableService service, Executor executor,
            ExecutableManager executableManager) throws ReportException
    {
        super.onServiceActivation(service, executor, executableManager);

        if (service instanceof EndpointExecutableService) {
            EndpointExecutableService endpointService = (EndpointExecutableService) service;
            if (endpointService.isEndpoint()) {
                modifyRoom(getRoomApi(executableManager), executor);
            }
        }
    }

    @Override
    protected void onServiceDeactivation(ExecutableService service, Executor executor,
            ExecutableManager executableManager) throws ReportException
    {
        if (service instanceof EndpointExecutableService) {
            EndpointExecutableService endpointService = (EndpointExecutableService) service;
            if (endpointService.isEndpoint()) {
                modifyRoom(getRoomApi(executableManager), executor);
            }
        }

        super.onServiceDeactivation(service, executor, executableManager);
    }

    @Override
    protected void onCreate()
    {
        super.onCreate();

        onUpdate();
    }

    @PreUpdate
    protected void onUpdate()
    {
        if (roomConfiguration == null) {
            throw new IllegalStateException("Room configuration should not be null.");
        }
        if (roomConfiguration.getTechnologies().size() == 0) {
            throw new IllegalStateException("Room configuration should have some technologies.");
        }
    }
}