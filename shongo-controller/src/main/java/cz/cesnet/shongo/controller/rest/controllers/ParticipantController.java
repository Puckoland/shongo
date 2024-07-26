package cz.cesnet.shongo.controller.rest.controllers;

import cz.cesnet.shongo.CommonReportSet;
import cz.cesnet.shongo.ParticipantRole;
import cz.cesnet.shongo.controller.api.AbstractParticipant;
import cz.cesnet.shongo.controller.api.AbstractRoomExecutable;
import cz.cesnet.shongo.controller.api.Executable;
import cz.cesnet.shongo.controller.api.RoomExecutable;
import cz.cesnet.shongo.controller.api.RoomExecutableParticipantConfiguration;
import cz.cesnet.shongo.controller.api.SecurityToken;
import cz.cesnet.shongo.controller.api.UsedRoomExecutable;
import cz.cesnet.shongo.controller.api.request.ListResponse;
import cz.cesnet.shongo.controller.api.rpc.ExecutableService;
import cz.cesnet.shongo.controller.rest.CacheProvider;
import cz.cesnet.shongo.controller.rest.RestApiPath;
import cz.cesnet.shongo.controller.rest.RestCache;
import cz.cesnet.shongo.controller.rest.error.UnsupportedApiException;
import cz.cesnet.shongo.controller.rest.models.participant.ParticipantConfigurationModel;
import cz.cesnet.shongo.controller.rest.models.participant.ParticipantModel;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static cz.cesnet.shongo.controller.rest.config.security.AuthFilter.TOKEN;

/**
 * Rest controller for participant endpoints.
 *
 * @author Filip Karnis
 */
@RestController
@RequestMapping(RestApiPath.PARTICIPANTS)
@RequiredArgsConstructor
public class ParticipantController
{

    private final RestCache cache;
    private final ExecutableService executableService;

    @Operation(summary = "Lists reservation request participants.")
    @GetMapping
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

    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adds new participant to reservation request.")
    @PostMapping
    void addParticipant(
            @RequestAttribute(TOKEN) SecurityToken securityToken,
            @PathVariable String id,
            @RequestBody ParticipantModel newParticipant)
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
    @PutMapping(RestApiPath.PARTICIPANTS_ID_SUFFIX)
    void updateParticipant(
            @RequestAttribute(TOKEN) SecurityToken securityToken,
            @PathVariable String id,
            @PathVariable String participantId,
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
    @DeleteMapping(RestApiPath.PARTICIPANTS_ID_SUFFIX)
    void removeParticipant(
            @RequestAttribute(TOKEN) SecurityToken securityToken,
            @PathVariable String id,
            @PathVariable String participantId)
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