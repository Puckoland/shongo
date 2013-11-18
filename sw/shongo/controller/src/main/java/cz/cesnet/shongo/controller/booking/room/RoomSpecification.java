package cz.cesnet.shongo.controller.booking.room;

import cz.cesnet.shongo.AliasType;
import cz.cesnet.shongo.Technology;
import cz.cesnet.shongo.controller.api.Synchronization;
import cz.cesnet.shongo.controller.booking.participant.AbstractParticipant;
import cz.cesnet.shongo.controller.booking.EntityIdentifier;
import cz.cesnet.shongo.controller.booking.room.settting.RoomSetting;
import cz.cesnet.shongo.controller.booking.alias.AliasSpecification;
import cz.cesnet.shongo.controller.booking.specification.ExecutableServiceSpecification;
import cz.cesnet.shongo.controller.booking.specification.Specification;
import cz.cesnet.shongo.controller.booking.alias.Alias;
import cz.cesnet.shongo.controller.booking.resource.DeviceResource;
import cz.cesnet.shongo.controller.booking.resource.ResourceManager;
import cz.cesnet.shongo.controller.scheduler.ReservationTask;
import cz.cesnet.shongo.controller.scheduler.ReservationTaskProvider;
import cz.cesnet.shongo.controller.scheduler.SchedulerContext;
import cz.cesnet.shongo.util.ObjectHelper;

import javax.persistence.*;
import java.util.*;

/**
 * Represents a {@link cz.cesnet.shongo.controller.booking.specification.Specification} for {@link RoomConfiguration}.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
@Entity
public class RoomSpecification extends Specification implements ReservationTaskProvider
{
    /**
     * {@link DeviceResource} with {@link RoomProviderCapability} in which the {@link RoomConfiguration} should be allocated.
     */
    private DeviceResource deviceResource;

    /**
     * Number of participants which shall be able to join to the virtual room.
     */
    private int participantCount = 0;

    /**
     * List of {@link cz.cesnet.shongo.controller.booking.room.settting.RoomSetting}s for the {@link RoomConfiguration}
     * (e.g., {@link Technology} specific).
     */
    private List<RoomSetting> roomSettings = new ArrayList<RoomSetting>();

    /**
     * List of {@link cz.cesnet.shongo.controller.booking.alias.AliasSpecification} for {@link Alias}es which should be allocated for the room.
     */
    private List<AliasSpecification> aliasSpecifications = new ArrayList<AliasSpecification>();

    /**
     * List of {@link AbstractParticipant}s for the room.
     */
    private List<AbstractParticipant> participants = new LinkedList<AbstractParticipant>();

    /**
     * List of {@link cz.cesnet.shongo.controller.booking.specification.ExecutableServiceSpecification}s for the room.
     */
    private List<ExecutableServiceSpecification> serviceSpecifications = new LinkedList<ExecutableServiceSpecification>();

    /**
     * Constructor.
     */
    public RoomSpecification()
    {
    }

    /**
     * @return {@link #deviceResource}
     */
    @OneToOne
    public DeviceResource getDeviceResource()
    {
        return deviceResource;
    }

    /**
     * @param deviceResource sets the {@link #deviceResource}
     */
    public void setDeviceResource(DeviceResource deviceResource)
    {
        this.deviceResource = deviceResource;
    }

    /**
     * @return {@link #participantCount}
     */
    @Column(nullable = false)
    public int getParticipantCount()
    {
        return participantCount;
    }

    /**
     * @param participantCount sets the {@link #participantCount}
     */
    public void setParticipantCount(int participantCount)
    {
        this.participantCount = participantCount;
    }

    /**
     * @return {@link #roomSettings}
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @Access(AccessType.FIELD)
    public List<RoomSetting> getRoomSettings()
    {
        return roomSettings;
    }

    /**
     * @param roomSettings sets the {@link #roomSettings}
     */
    public void setRoomSettings(List<RoomSetting> roomSettings)
    {
        this.roomSettings.clear();
        for (RoomSetting roomConfiguration : roomSettings) {
            this.roomSettings.add(roomConfiguration.clone());
        }
    }

    /**
     * Remove all {@link #roomSettings}.
     */
    public void clearRoomSettings()
    {
        roomSettings.clear();
    }

    /**
     * @param roomSetting to be added to the {@link #roomSettings}
     */
    public void addRoomSetting(RoomSetting roomSetting)
    {
        roomSettings.add(roomSetting);
    }

    /**
     * @param roomSetting to be removed from the {@link #roomSettings}
     */
    public void removeRoomSetting(RoomSetting roomSetting)
    {
        roomSettings.remove(roomSetting);
    }

    /**
     * @return {@link #aliasSpecifications}
     */
    @OneToMany(cascade = CascadeType.ALL)
    @Access(AccessType.FIELD)
    public List<AliasSpecification> getAliasSpecifications()
    {
        return Collections.unmodifiableList(aliasSpecifications);
    }

    /**
     * @param aliasSpecifications sets the {@link #aliasSpecifications}
     */
    public void setAliasSpecifications(List<AliasSpecification> aliasSpecifications)
    {
        this.aliasSpecifications.clear();
        for (AliasSpecification aliasSpecification : aliasSpecifications) {
            this.aliasSpecifications.add(aliasSpecification.clone());
        }
    }

    /**
     * @param aliasSpecification to be added to the {@link #aliasSpecifications}
     */
    public void addAliasSpecification(AliasSpecification aliasSpecification)
    {
        aliasSpecifications.add(aliasSpecification);
    }

    /**
     * @param aliasSpecification to be removed from the {@link #aliasSpecifications}
     */
    public void removeAliasSpecification(AliasSpecification aliasSpecification)
    {
        aliasSpecifications.remove(aliasSpecification);
    }

    /**
     * @return {@link #participants}
     */
    @OneToMany(cascade = CascadeType.ALL)
    @Access(AccessType.FIELD)
    public List<AbstractParticipant> getParticipants()
    {
        return Collections.unmodifiableList(participants);
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
     * @return {@link #serviceSpecifications}
     */
    @OneToMany(cascade = CascadeType.ALL)
    @Access(AccessType.FIELD)
    public List<ExecutableServiceSpecification> getServiceSpecifications()
    {
        return Collections.unmodifiableList(serviceSpecifications);
    }

    /**
     * @param serviceSpecifications sets the {@link #serviceSpecifications}
     */
    public void setServiceSpecifications(List<ExecutableServiceSpecification> serviceSpecifications)
    {
        this.serviceSpecifications.clear();
        for (ExecutableServiceSpecification serviceSpecification : serviceSpecifications) {
            this.serviceSpecifications.add(serviceSpecification.clone());
        }
    }

    @Override
    public boolean synchronizeFrom(Specification specification)
    {
        RoomSpecification roomSpecification = (RoomSpecification) specification;

        boolean modified = super.synchronizeFrom(specification);
        modified |= !ObjectHelper.isSame(getParticipantCount(), roomSpecification.getParticipantCount());
        modified |= !ObjectHelper.isSame(getDeviceResource(), roomSpecification.getDeviceResource());

        setParticipantCount(roomSpecification.getParticipantCount());
        setDeviceResource(roomSpecification.getDeviceResource());

        if (!roomSettings.equals(roomSpecification.getRoomSettings())) {
            setRoomSettings(roomSpecification.getRoomSettings());
            modified = true;
        }

        if (!aliasSpecifications.equals(roomSpecification.getAliasSpecifications())) {
            setAliasSpecifications(roomSpecification.getAliasSpecifications());
            modified = true;
        }

        if (!participants.equals(roomSpecification.getParticipants())) {
            setParticipants(roomSpecification.getParticipants());
            modified = true;
        }

        if (!serviceSpecifications.equals(roomSpecification.getServiceSpecifications())) {
            setServiceSpecifications(roomSpecification.getServiceSpecifications());
            modified = true;
        }

        return modified;
    }

    @Override
    public ReservationTask createReservationTask(SchedulerContext schedulerContext)
    {
        RoomProviderCapability roomProviderCapability = null;
        if (deviceResource != null) {
            roomProviderCapability = deviceResource.getCapabilityRequired(RoomProviderCapability.class);
        }

        RoomReservationTask roomReservationTask = new RoomReservationTask(schedulerContext, getParticipantCount());
        roomReservationTask.addTechnologyVariant(getTechnologies());
        roomReservationTask.addRoomSettings(getRoomSettings());
        roomReservationTask.addAliasSpecifications(getAliasSpecifications());
        roomReservationTask.setRoomProviderCapability(roomProviderCapability);
        roomReservationTask.addParticipants(getParticipants());
        roomReservationTask.addServiceSpecifications(getServiceSpecifications());
        return roomReservationTask;
    }

    @Override
    protected cz.cesnet.shongo.controller.api.Specification createApi()
    {
        return new cz.cesnet.shongo.controller.api.RoomSpecification();
    }

    @Override
    public void toApi(cz.cesnet.shongo.controller.api.Specification specificationApi)
    {
        cz.cesnet.shongo.controller.api.RoomSpecification roomSpecificationApi =
                (cz.cesnet.shongo.controller.api.RoomSpecification) specificationApi;
        if (deviceResource != null) {
            roomSpecificationApi.setResourceId(EntityIdentifier.formatId(deviceResource));
        }
        for (Technology technology : getTechnologies()) {
            roomSpecificationApi.addTechnology(technology);
        }
        roomSpecificationApi.setParticipantCount(getParticipantCount());
        for (RoomSetting roomSetting : getRoomSettings()) {
            roomSpecificationApi.addRoomSetting(roomSetting.toApi());
        }
        for (AliasSpecification aliasSpecification : getAliasSpecifications()) {
            roomSpecificationApi.addAlias(aliasSpecification.toApi());
        }
        for (AbstractParticipant participant : getParticipants()) {
            roomSpecificationApi.addParticipant(participant.toApi());
        }
        for (ExecutableServiceSpecification serviceSpecification : getServiceSpecifications()) {
            roomSpecificationApi.addServiceSpecification(serviceSpecification.toApi());
        }
        super.toApi(specificationApi);
    }

    @Override
    public void fromApi(cz.cesnet.shongo.controller.api.Specification specificationApi,
            final EntityManager entityManager)
    {
        cz.cesnet.shongo.controller.api.RoomSpecification roomSpecificationApi =
                (cz.cesnet.shongo.controller.api.RoomSpecification) specificationApi;

        setParticipantCount(roomSpecificationApi.getParticipantCount());
        if (roomSpecificationApi.getResourceId() == null) {
            setDeviceResource(null);
        }
        else {
            Long resourceId = EntityIdentifier.parseId(cz.cesnet.shongo.controller.booking.resource.Resource.class,
                    roomSpecificationApi.getResourceId());
            ResourceManager resourceManager = new ResourceManager(entityManager);
            setDeviceResource(resourceManager.getDevice(resourceId));
        }

        Synchronization.synchronizeCollection(technologies, roomSpecificationApi.getTechnologies());
        Synchronization.synchronizeCollection(roomSettings, roomSpecificationApi.getRoomSettings(),
                new Synchronization.Handler<RoomSetting, cz.cesnet.shongo.api.RoomSetting>(
                        RoomSetting.class)
                {
                    @Override
                    public RoomSetting createFromApi(
                            cz.cesnet.shongo.api.RoomSetting objectApi)
                    {
                        return RoomSetting.createFromApi(objectApi);
                    }

                    @Override
                    public void updateFromApi(RoomSetting object,
                            cz.cesnet.shongo.api.RoomSetting objectApi)
                    {
                        object.fromApi(objectApi);
                    }
                });
        Synchronization.synchronizeCollection(aliasSpecifications, roomSpecificationApi.getAliasSpecifications(),
                new Synchronization.Handler<AliasSpecification, cz.cesnet.shongo.controller.api.AliasSpecification>(
                        AliasSpecification.class)
                {
                    @Override
                    public AliasSpecification createFromApi(
                            cz.cesnet.shongo.controller.api.AliasSpecification objectApi)
                    {
                        AliasSpecification aliasSpecification = new AliasSpecification();
                        aliasSpecification.fromApi(objectApi, entityManager);
                        return aliasSpecification;
                    }

                    @Override
                    public void updateFromApi(AliasSpecification object,
                            cz.cesnet.shongo.controller.api.AliasSpecification objectApi)
                    {
                        object.fromApi(objectApi, entityManager);
                    }
                });
        Synchronization.synchronizeCollection(participants, roomSpecificationApi.getParticipants(),
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
        Synchronization.synchronizeCollection(serviceSpecifications, roomSpecificationApi.getServiceSpecifications(),
                new Synchronization.Handler<ExecutableServiceSpecification, cz.cesnet.shongo.controller.api.ExecutableServiceSpecification>(
                        ExecutableServiceSpecification.class)
                {
                    @Override
                    public ExecutableServiceSpecification createFromApi(
                            cz.cesnet.shongo.controller.api.ExecutableServiceSpecification objectApi)
                    {
                        ExecutableServiceSpecification serviceSpecification =
                                (ExecutableServiceSpecification) ExecutableServiceSpecification.createFromApi(
                                        objectApi, entityManager);
                        serviceSpecification.fromApi(objectApi, entityManager);
                        return serviceSpecification;
                    }

                    @Override
                    public void updateFromApi(ExecutableServiceSpecification object,
                            cz.cesnet.shongo.controller.api.ExecutableServiceSpecification objectApi)
                    {
                        object.fromApi(objectApi, entityManager);
                    }
                });

        // Check alias specifications
        for (AliasSpecification aliasSpecification : aliasSpecifications) {
            Set<Technology> requestedTechnologies = new HashSet<Technology>();
            for (Technology technology : aliasSpecification.getTechnologies()) {
                requestedTechnologies.add(technology);
            }
            for (AliasType aliasType : aliasSpecification.getAliasTypes()) {
                requestedTechnologies.add(aliasType.getTechnology());
            }
            for (Technology requestedTechnology : requestedTechnologies) {
                if (!requestedTechnology.isCompatibleWith(technologies)) {
                    throw new RuntimeException("Cannot request alias in technology which the room doesn't support.");
                }
            }
        }

        super.fromApi(specificationApi, entityManager);
    }
}