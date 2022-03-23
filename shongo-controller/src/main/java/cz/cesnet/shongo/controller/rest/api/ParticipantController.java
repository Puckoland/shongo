package cz.cesnet.shongo.controller.rest.api;

import cz.cesnet.shongo.CommonReportSet;
import cz.cesnet.shongo.ParticipantRole;
import cz.cesnet.shongo.api.UserInformation;
import cz.cesnet.shongo.controller.api.*;
import cz.cesnet.shongo.controller.api.request.ListResponse;
import cz.cesnet.shongo.controller.api.rpc.ExecutableService;
import cz.cesnet.shongo.controller.rest.Cache;
import cz.cesnet.shongo.controller.rest.CacheProvider;
import cz.cesnet.shongo.controller.rest.models.UnsupportedApiException;
import cz.cesnet.shongo.controller.rest.models.detail.ParticipantConfigurationModel;
import cz.cesnet.shongo.controller.rest.models.detail.ParticipantModel;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static cz.cesnet.shongo.controller.rest.auth.AuthFilter.TOKEN;

/**
 * Rest controller for participant endpoints.
 *
 * @author Filip Karnis
 */
@RestController
@RequestMapping("/api/v1/reservation_requests/{id:.+}/participants")
public class ParticipantController {

    private final Cache cache;
    private final ExecutableService executableService;

    public ParticipantController(
            @Autowired Cache cache,
            @Autowired ExecutableService executableService)
    {
        this.cache = cache;
        this.executableService = executableService;
    }

    @Operation(summary = "Lists reservation request participants.")
    @GetMapping()
    ListResponse<ParticipantModel> listRequestParticipants(
            @RequestAttribute(TOKEN) SecurityToken securityToken,
            @PathVariable String id,
            @RequestParam(value = "start", required = false) Integer start,
            @RequestParam(value = "count", required = false) Integer count)
    {
        final CacheProvider cacheProvider = new CacheProvider(cache, securityToken);

        // Get room executable
        String executableId = cache.getExecutableId(securityToken, id);
        AbstractRoomExecutable roomExecutable = getRoomExecutable(securityToken, executableId);

        List<AbstractParticipant> participants = new LinkedList<>();

        // Add reused room participants as read-only
        if (roomExecutable instanceof UsedRoomExecutable) {
            UsedRoomExecutable usedRoomExecutable = (UsedRoomExecutable) roomExecutable;
            String reusedRoomExecutableId = usedRoomExecutable.getReusedRoomExecutableId();
            RoomExecutable reusedRoomExecutable =
                    (RoomExecutable) getRoomExecutable(securityToken, reusedRoomExecutableId);
            List<AbstractParticipant> reusedRoomParticipants =
                    reusedRoomExecutable.getParticipantConfiguration().getParticipants();
            reusedRoomParticipants.sort(Comparator.comparing(o -> Integer.valueOf(o.getId())));
            reusedRoomParticipants.forEach(participant -> {
                participant.setId((String) null);
                participants.add(participant);
            });
        }

        // Add room participants
        List<AbstractParticipant> roomParticipants = roomExecutable.getParticipantConfiguration().getParticipants();
        roomParticipants.sort(Comparator.comparing(o -> Integer.valueOf(o.getId())));
        participants.addAll(roomParticipants);

        List<ParticipantModel> items = participants.stream().map(
                participant -> new ParticipantModel(participant, cacheProvider)
        ).collect(Collectors.toList());
        return ListResponse.fromRequest(start, count, items);
    }

    @Operation(summary = "Adds new participant to reservation request.")
    @PostMapping()
    void addParticipant(
            @RequestAttribute(TOKEN) SecurityToken securityToken,
            @PathVariable("id") String id,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam ParticipantRole role)
    {
        String executableId = cache.getExecutableId(securityToken, id);
        AbstractRoomExecutable roomExecutable = getRoomExecutable(securityToken, executableId);

        CacheProvider cacheProvider = new CacheProvider(cache, securityToken);
        RoomExecutableParticipantConfiguration participantConfiguration = roomExecutable.getParticipantConfiguration();

        // Initialize model from API
        ParticipantConfigurationModel participantConfigurationModel = new ParticipantConfigurationModel();
        for (AbstractParticipant existingParticipant : participantConfiguration.getParticipants()) {
            participantConfigurationModel.addParticipant(new ParticipantModel(existingParticipant, cacheProvider));
        }
        // Modify model
        final ParticipantModel newParticipant;
        if (userId != null) {
            UserInformation participantInformation = cacheProvider.getUserInformation(userId);
            newParticipant = new ParticipantModel(participantInformation, cacheProvider);
        } else {
            newParticipant = new ParticipantModel(cacheProvider);
            newParticipant.setType(ParticipantModel.Type.ANONYMOUS);
            newParticipant.setName(name);
            newParticipant.setEmail(email);
        }
        newParticipant.setRole(role);
        participantConfigurationModel.addParticipant(newParticipant);
        // Initialize API from model
        participantConfiguration.clearParticipants();
        for (ParticipantModel participantModel : participantConfigurationModel.getParticipants()) {
            participantConfiguration.addParticipant(participantModel.toApi());
        }
        executableService.modifyExecutableConfiguration(securityToken, executableId, participantConfiguration);
        cache.clearExecutable(executableId);
    }

    @Operation(summary = "Adds new participant to reservation request.")
    @PutMapping("/{participantId:.+}")
    void updateParticipant(
            @RequestAttribute(TOKEN) SecurityToken securityToken,
            @PathVariable("id") String id,
            @PathVariable("participantId") String participantId,
            @RequestParam ParticipantRole role)
    {
        String executableId = cache.getExecutableId(securityToken, id);
        AbstractRoomExecutable roomExecutable = getRoomExecutable(securityToken, executableId);

        RoomExecutableParticipantConfiguration participantConfiguration = roomExecutable.getParticipantConfiguration();

        // Modify model
        ParticipantModel oldParticipant = getParticipant(participantConfiguration, participantId, securityToken);
        oldParticipant.setRole(role);
        // Initialize API from model
        participantConfiguration.removeParticipantById(participantId);
        participantConfiguration.addParticipant(oldParticipant.toApi());

        executableService.modifyExecutableConfiguration(securityToken, executableId, participantConfiguration);
        cache.clearExecutable(executableId);
    }

    @Operation(summary = "Removes participant from reservation request.")
    @DeleteMapping("/{participantId:.+}")
    void removeParticipant(
            @RequestAttribute(TOKEN) SecurityToken securityToken,
            @PathVariable("id") String id,
            @PathVariable("participantId") String participantId)
    {
        String executableId = cache.getExecutableId(securityToken, id);
        AbstractRoomExecutable roomExecutable = getRoomExecutable(securityToken, executableId);
        RoomExecutableParticipantConfiguration participantConfiguration = roomExecutable.getParticipantConfiguration();
        ParticipantModel oldParticipant = getParticipant(participantConfiguration, participantId, securityToken);
        participantConfiguration.removeParticipantById(oldParticipant.getId());
        executableService.modifyExecutableConfiguration(securityToken, executableId, participantConfiguration);
        cache.clearExecutable(executableId);
    }

    private AbstractRoomExecutable getRoomExecutable(SecurityToken securityToken, String executableId)
    {
        Executable executable = cache.getExecutable(securityToken, executableId);
        if (executable instanceof AbstractRoomExecutable) {
            return (AbstractRoomExecutable) executable;
        }
        else {
            throw new UnsupportedApiException(executable);
        }
    }

    protected ParticipantModel getParticipant(RoomExecutableParticipantConfiguration participantConfiguration,
                                              String participantId, SecurityToken securityToken)
    {
        AbstractParticipant participant = participantConfiguration.getParticipant(participantId);
        if (participant == null) {
            throw new CommonReportSet.ObjectNotExistsException("Participant", participantId);
        }
        return new ParticipantModel(participant, new CacheProvider(cache, securityToken));
    }
}
