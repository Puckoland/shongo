package cz.cesnet.shongo.controller.api.rpc;

import cz.cesnet.shongo.AliasType;
import cz.cesnet.shongo.Technology;
import cz.cesnet.shongo.Temporal;
import cz.cesnet.shongo.TodoImplementException;
import cz.cesnet.shongo.api.util.Converter;
import cz.cesnet.shongo.controller.*;
import cz.cesnet.shongo.controller.api.*;
import cz.cesnet.shongo.controller.api.AliasReservation;
import cz.cesnet.shongo.controller.api.Reservation;
import cz.cesnet.shongo.controller.api.request.ListRequest;
import cz.cesnet.shongo.controller.api.request.ListResponse;
import cz.cesnet.shongo.controller.api.request.ReservationListRequest;
import cz.cesnet.shongo.controller.api.request.ReservationRequestListRequest;
import cz.cesnet.shongo.controller.authorization.Authorization;
import cz.cesnet.shongo.controller.authorization.AuthorizationManager;
import cz.cesnet.shongo.controller.common.EntityIdentifier;
import cz.cesnet.shongo.controller.request.AliasSetSpecification;
import cz.cesnet.shongo.controller.request.Allocation;
import cz.cesnet.shongo.controller.request.ReservationRequestManager;
import cz.cesnet.shongo.controller.reservation.*;
import cz.cesnet.shongo.controller.resource.Alias;
import cz.cesnet.shongo.controller.scheduler.SchedulerException;
import cz.cesnet.shongo.controller.scheduler.SpecificationCheckAvailability;
import cz.cesnet.shongo.controller.util.DatabaseFilter;
import cz.cesnet.shongo.report.Report;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import java.util.*;

/**
 * Implementation of {@link ReservationService}.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public class ReservationServiceImpl extends Component
        implements ReservationService, Component.EntityManagerFactoryAware,
                   Component.AuthorizationAware
{
    /**
     * @see javax.persistence.EntityManagerFactory
     */
    private EntityManagerFactory entityManagerFactory;

    /**
     * @see cz.cesnet.shongo.controller.authorization.Authorization
     */
    private Authorization authorization;

    /**
     * Constructor.
     */
    public ReservationServiceImpl()
    {
    }

    @Override
    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory)
    {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void setAuthorization(Authorization authorization)
    {
        this.authorization = authorization;
    }

    @Override
    public void init(Configuration configuration)
    {
        checkDependency(entityManagerFactory, EntityManagerFactory.class);
        checkDependency(authorization, Authorization.class);
        super.init(configuration);
    }

    @Override
    public String getServiceName()
    {
        return "Reservation";
    }

    @Override
    public Object checkSpecificationAvailability(SecurityToken token, Specification specificationApi, Interval slot)
    {
        String userId = authorization.validate(token);

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try {
            specificationApi.setupNewEntity();
            cz.cesnet.shongo.controller.request.Specification specification =
                    cz.cesnet.shongo.controller.request.Specification.createFromApi(specificationApi, entityManager);
            Throwable cause = null;
            if (specification instanceof SpecificationCheckAvailability) {
                SpecificationCheckAvailability checkAvailability = (SpecificationCheckAvailability) specification;
                try {
                    checkAvailability.checkAvailability(slot, entityManager);
                    return Boolean.TRUE;
                }
                catch (SchedulerException exception) {
                    return exception.getReport().getMessageRecursive(Report.MessageType.USER);
                }
                catch (UnsupportedOperationException exception) {
                    cause = exception;
                }
            }
            throw new RuntimeException(String.format("Specification '%s' cannot be checked for availability.",
                    specificationApi.getClass().getSimpleName()), cause);
        }
        finally {
            entityManager.close();
        }
    }

    @Override
    public String createReservationRequest(SecurityToken token,
            cz.cesnet.shongo.controller.api.AbstractReservationRequest reservationRequestApi)
    {
        String userId = authorization.validate(token);

        reservationRequestApi.setupNewEntity();

        // Change user id (only root can do that)
        if (reservationRequestApi.getUserId() != null && authorization.isAdmin(userId)) {
            userId = reservationRequestApi.getUserId();
        }

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        ReservationRequestManager reservationRequestManager = new ReservationRequestManager(entityManager);
        AuthorizationManager authorizationManager = new AuthorizationManager(entityManager);
        try {
            authorizationManager.beginTransaction(authorization);
            entityManager.getTransaction().begin();

            cz.cesnet.shongo.controller.request.AbstractReservationRequest reservationRequest =
                    cz.cesnet.shongo.controller.request.AbstractReservationRequest.createFromApi(
                            reservationRequestApi, entityManager);
            reservationRequest.setUserId(userId);

            reservationRequestManager.create(reservationRequest);

            authorizationManager.createAclRecord(userId, reservationRequest, Role.OWNER);

            entityManager.getTransaction().commit();
            authorizationManager.commitTransaction();

            return EntityIdentifier.formatId(reservationRequest);
        }
        finally {
            if (authorizationManager.isTransactionActive()) {
                authorizationManager.rollbackTransaction();
            }
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
        }

    }

    /**
     * Check whether {@code abstractReservationRequestImpl} can be modified or deleted.
     *
     * @param abstractReservationRequest
     */
    private boolean isModifiableReservationRequest(
            cz.cesnet.shongo.controller.request.AbstractReservationRequest abstractReservationRequest,
            ReservationManager reservationManager)
    {
        Allocation allocation = abstractReservationRequest.getAllocation();

        // Check if reservation is not created by controller
        if (abstractReservationRequest instanceof cz.cesnet.shongo.controller.request.ReservationRequest) {
            cz.cesnet.shongo.controller.request.ReservationRequest reservationRequestImpl =
                    (cz.cesnet.shongo.controller.request.ReservationRequest) abstractReservationRequest;
            if (reservationRequestImpl.getParentAllocation() != null) {
                return false;
            }
        }

        // Check child reservation requests
        for (cz.cesnet.shongo.controller.request.ReservationRequest reservationRequestImpl :
                allocation.getChildReservationRequests()) {
            if (isModifiableReservationRequest(reservationRequestImpl, reservationManager)) {
                return false;
            }
        }

        // Check allocated reservations
        for (cz.cesnet.shongo.controller.reservation.Reservation reservation : allocation.getReservations()) {
            if (reservationManager.isProvided(reservation)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Properties which can be filled to allow modification of reservation request whose reservation is provided to
     * another reservation request.
     */
    private final static Set<String> MODIFIABLE_FILLED_PROPERTIES = new HashSet<String>()
    {{
            add("id");
            add(cz.cesnet.shongo.controller.api.ReservationRequest.SLOT);
            add(cz.cesnet.shongo.controller.api.ReservationRequestSet.SLOTS);
        }};

    @Override
    public String modifyReservationRequest(SecurityToken token,
            cz.cesnet.shongo.controller.api.AbstractReservationRequest reservationRequestApi)
    {
        String userId = authorization.validate(token);

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        AuthorizationManager authorizationManager = new AuthorizationManager(entityManager);
        ReservationRequestManager reservationRequestManager = new ReservationRequestManager(entityManager);
        String reservationRequestId = reservationRequestApi.getId();
        EntityIdentifier entityId = EntityIdentifier.parse(reservationRequestId, EntityType.RESERVATION_REQUEST);

        try {
            // Get old reservation request and check permissions and restrictions for modification
            cz.cesnet.shongo.controller.request.AbstractReservationRequest oldReservationRequest =
                    reservationRequestManager.get(entityId.getPersistenceId());
            if (!authorization.hasPermission(userId, entityId, Permission.WRITE)) {
                ControllerReportSetHelper.throwSecurityNotAuthorizedFault("modify reservation request %s", entityId);
            }
            switch (oldReservationRequest.getType()) {
                case MODIFIED:
                    throw new ControllerReportSet.ReservationRequestAlreadyModifiedException(entityId.toId());
                case DELETED:
                    throw new ControllerReportSet.ReservationRequestDeletedException(entityId.toId());
            }
            ReservationManager reservationManager = new ReservationManager(entityManager);
            if (!isModifiableReservationRequest(oldReservationRequest, reservationManager)) {
                if (!MODIFIABLE_FILLED_PROPERTIES.containsAll(reservationRequestApi.getFilledProperties())) {
                    throw new ControllerReportSet.ReservationRequestNotModifiableException(entityId.toId());
                }
            }

            // Update old detached reservation request (the changes will not be serialized to database)
            oldReservationRequest.fromApi(reservationRequestApi, entityManager);

            // Create new reservation request by cloning old reservation request
            cz.cesnet.shongo.controller.request.AbstractReservationRequest newReservationRequest =
                    oldReservationRequest.clone();

            // Revert changes to old reservation request
            entityManager.clear();

            authorizationManager.beginTransaction(authorization);
            entityManager.getTransaction().begin();

            oldReservationRequest = reservationRequestManager.get(entityId.getPersistenceId());

            // Create new reservation request and update old reservation request
            reservationRequestManager.modify(oldReservationRequest, newReservationRequest);

            // Copy ACL records
            authorizationManager.copyAclRecords(oldReservationRequest, newReservationRequest);

            entityManager.getTransaction().commit();
            authorizationManager.commitTransaction();

            return EntityIdentifier.formatId(newReservationRequest);
        }
        finally {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
        }
    }

    @Override
    public void deleteReservationRequest(SecurityToken token, String reservationRequestId)
    {
        String userId = authorization.validate(token);
        EntityIdentifier entityId = EntityIdentifier.parse(reservationRequestId, EntityType.RESERVATION_REQUEST);

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        ReservationRequestManager reservationRequestManager = new ReservationRequestManager(entityManager);
        AuthorizationManager authorizationManager = new AuthorizationManager(entityManager);
        try {
            authorizationManager.beginTransaction(authorization);
            entityManager.getTransaction().begin();

            cz.cesnet.shongo.controller.request.AbstractReservationRequest reservationRequest =
                    reservationRequestManager.get(entityId.getPersistenceId());

            if (!authorization.hasPermission(userId, entityId, Permission.WRITE)) {
                ControllerReportSetHelper.throwSecurityNotAuthorizedFault("delete reservation request %s", entityId);
            }
            switch (reservationRequest.getType()) {
                case DELETED:
                    throw new ControllerReportSet.ReservationRequestDeletedException(entityId.toId());
            }
            ReservationManager reservationManager = new ReservationManager(entityManager);
            if (!isModifiableReservationRequest(reservationRequest, reservationManager)) {
                throw new ControllerReportSet.ReservationRequestNotModifiableException(
                        EntityIdentifier.formatId(reservationRequest));
            }

            reservationRequestManager.softDelete(reservationRequest, authorizationManager);

            entityManager.getTransaction().commit();
            authorizationManager.commitTransaction();
        }
        finally {
            if (authorizationManager.isTransactionActive()) {
                authorizationManager.rollbackTransaction();
            }
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
        }
    }

    @Override
    public void updateReservationRequest(SecurityToken token, String reservationRequestId)
    {
        String userId = authorization.validate(token);
        EntityIdentifier entityId = EntityIdentifier.parse(reservationRequestId, EntityType.RESERVATION_REQUEST);

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        ReservationRequestManager reservationRequestManager = new ReservationRequestManager(entityManager);
        AuthorizationManager authorizationManager = new AuthorizationManager(entityManager);
        try {
            authorizationManager.beginTransaction(authorization);
            entityManager.getTransaction().begin();

            cz.cesnet.shongo.controller.request.AbstractReservationRequest abstractReservationRequest =
                    reservationRequestManager.get(entityId.getPersistenceId());

            if (!authorization.hasPermission(userId, entityId, Permission.WRITE)) {
                ControllerReportSetHelper.throwSecurityNotAuthorizedFault("update reservation request %s", entityId);
            }

            // Update reservation requests
            if (abstractReservationRequest instanceof cz.cesnet.shongo.controller.request.ReservationRequest) {
                cz.cesnet.shongo.controller.request.ReservationRequest reservationRequest =
                        (cz.cesnet.shongo.controller.request.ReservationRequest) abstractReservationRequest;
                switch (reservationRequest.getState()) {
                    case ALLOCATION_FAILED: {
                        // Reservation request was modified, so we must clear it's state
                        reservationRequest.clearState();
                        // Update state
                        reservationRequest.updateStateBySpecification();
                    }
                }
            }

            // Update child reservation requests
            for (cz.cesnet.shongo.controller.request.ReservationRequest reservationRequest :
                    abstractReservationRequest.getAllocation().getChildReservationRequests()) {
                switch (reservationRequest.getState()) {
                    case ALLOCATION_FAILED: {
                        // Reservation request was modified, so we must clear it's state
                        reservationRequest.clearState();
                        // Update state
                        reservationRequest.updateStateBySpecification();
                    }
                }
            }

            entityManager.getTransaction().commit();
            authorizationManager.commitTransaction();
        }
        finally {
            if (authorizationManager.isTransactionActive()) {
                authorizationManager.rollbackTransaction();
            }
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
        }
    }

    @Override
    public ListResponse<ReservationRequestSummary> listReservationRequests(ReservationRequestListRequest request)
    {
        String userId = authorization.validate(request.getSecurityToken());

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            DatabaseFilter filter = new DatabaseFilter("request");

            // List only reservation requests which are CREATED (and which aren't MODIFIED or DELETED)
            filter.addFilter("request.type = :createdType", "createdType",
                    cz.cesnet.shongo.controller.request.AbstractReservationRequest.Type.CREATED);

            // List only reservation requests which aren't created for another reservation request
            filter.addFilter("TYPE(request) != ReservationRequest OR request.parentAllocation IS NULL");

            // List only reservation requests which is current user permitted to read
            filter.addIds(authorization, userId, EntityType.RESERVATION_REQUEST, Permission.READ);

            if (request.getTechnologies().size() > 0) {
                // List only reservation requests which specifies given technologies
                filter.addFilter("request IN ("
                        + "  SELECT reservationRequest"
                        + "  FROM AbstractReservationRequest reservationRequest"
                        + "  LEFT JOIN reservationRequest.specification specification"
                        + "  LEFT JOIN specification.technologies technology"
                        + "  WHERE technology IN(:technologies)"
                        + ")");
                filter.addFilterParameter("technologies", request.getTechnologies());
            }

            if (request.getSpecificationClasses().size() > 0) {
                // List only reservation requests which has specification of given classes
                filter.addFilter("request IN ("
                        + "  SELECT reservationRequest"
                        + "  FROM AbstractReservationRequest reservationRequest"
                        + "  LEFT JOIN reservationRequest.specification reservationRequestSpecification"
                        + "  WHERE TYPE(reservationRequestSpecification) IN(:classes)"
                        + ")");
                Set<Class<? extends cz.cesnet.shongo.controller.request.Specification>> specificationClasses =
                        new HashSet<Class<? extends cz.cesnet.shongo.controller.request.Specification>>();
                for (Class<? extends Specification> type : request.getSpecificationClasses()) {
                    specificationClasses.add(cz.cesnet.shongo.controller.request.Specification.getClassFromApi(type));
                }
                filter.addFilterParameter("classes", specificationClasses);
            }

            if (request.getProvidedReservationIds().size() > 0) {
                // List only reservation requests which got provided given reservation
                filter.addFilter("request IN ("
                        + "  SELECT reservationRequest"
                        + "  FROM AbstractReservationRequest reservationRequest"
                        + "  LEFT JOIN reservationRequest.providedReservations providedReservation"
                        + "  WHERE providedReservation.id IN (:providedReservationIds)"
                        + ")");
                Set<Long> providedReservationIds = new HashSet<Long>();
                for (String providedReservationId : request.getProvidedReservationIds()) {
                    providedReservationIds.add(EntityIdentifier.parseId(
                            cz.cesnet.shongo.controller.reservation.Reservation.class, providedReservationId));
                }
                filter.addFilterParameter("providedReservationIds", providedReservationIds);
            }

            // Create parts
            String querySelect = "SELECT "
                    + " request.id,"
                    + " request.userId,"
                    + " request.created,"
                    + " request.purpose,"
                    + " request.description,"
                    + " specification";
            String queryFrom = " FROM AbstractReservationRequest request"
                    + " LEFT JOIN request.specification specification";
            ListResponse<ReservationRequestSummary> response = new ListResponse<ReservationRequestSummary>();
            List<Object[]> results = performListRequest("request", querySelect, Object[].class, queryFrom,
                    filter, request, response, entityManager);

            // Fill reservation requests to response
            Set<Long> aliasReservationRequestIds = new HashSet<Long>();
            Map<Long, ReservationRequestSummary> reservationRequestById =
                    new HashMap<Long, ReservationRequestSummary>();
            for (Object[] result : results) {
                Long id = (Long) result[0];
                ReservationRequestSummary reservationRequestSummary = new ReservationRequestSummary();
                reservationRequestSummary.setId(EntityIdentifier.formatId(EntityType.RESERVATION_REQUEST, id));
                reservationRequestSummary.setUserId((String) result[1]);
                reservationRequestSummary.setCreated((DateTime) result[2]);
                reservationRequestSummary.setPurpose((ReservationRequestPurpose) result[3]);
                reservationRequestSummary.setDescription((String) result[4]);
                response.addItem(reservationRequestSummary);

                // Prepare specification
                Object specification = result[5];
                if (specification instanceof cz.cesnet.shongo.controller.request.AliasSpecification) {
                    aliasReservationRequestIds.add(id);
                }
                else if (specification instanceof cz.cesnet.shongo.controller.request.AliasSetSpecification) {
                    aliasReservationRequestIds.add(id);
                }
                else if (specification instanceof cz.cesnet.shongo.controller.request.RoomSpecification) {
                    cz.cesnet.shongo.controller.request.RoomSpecification roomSpecification =
                            (cz.cesnet.shongo.controller.request.RoomSpecification) specification;
                    ReservationRequestSummary.RoomType roomType = new ReservationRequestSummary.RoomType();
                    roomType.setParticipantCount(roomSpecification.getParticipantCount());
                    reservationRequestSummary.setType(roomType);
                }
                reservationRequestById.put(id, reservationRequestSummary);
            }

            // Fill reservation request collections
            Set<Long> reservationRequestIds = reservationRequestById.keySet();
            if (reservationRequestIds.size() > 0) {
                // Fill technologies
                List<Object[]> technologies = entityManager.createQuery(""
                        + "SELECT reservationRequest.id, technology"
                        + " FROM AbstractReservationRequest reservationRequest"
                        + " LEFT JOIN reservationRequest.specification.technologies technology"
                        + " WHERE reservationRequest.id IN(:reservationRequestIds) AND technology != null",
                        Object[].class)
                        .setParameter("reservationRequestIds", reservationRequestIds)
                        .getResultList();
                for (Object[] providedReservation : technologies) {
                    Long id = (Long) providedReservation[0];
                    Technology technology = (Technology) providedReservation[1];
                    ReservationRequestSummary reservationRequestSummary = reservationRequestById.get(id);
                    reservationRequestSummary.addTechnology(technology);
                }

                // Fill provided reservations
                List<Object[]> providedReservations = entityManager.createQuery(""
                        + "SELECT reservationRequest.id, providedReservation.id"
                        + " FROM AbstractReservationRequest reservationRequest"
                        + " LEFT JOIN reservationRequest.providedReservations providedReservation"
                        + " WHERE reservationRequest.id IN(:reservationRequestIds) AND providedReservation != null",
                        Object[].class)
                        .setParameter("reservationRequestIds", reservationRequestIds)
                        .getResultList();
                for (Object[] providedReservation : providedReservations) {
                    Long id = (Long) providedReservation[0];
                    Long providedReservationId = (Long) providedReservation[1];
                    ReservationRequestSummary reservationRequestSummary = reservationRequestById.get(id);
                    reservationRequestSummary.addProvidedReservationId(
                            EntityIdentifier.formatId(EntityType.RESERVATION, providedReservationId));
                }
            }

            // Fill aliases
            if (aliasReservationRequestIds.size() > 0) {
                // Get list of requested aliases for all reservation requests
                List<Object[]> aliasReservationRequests = entityManager.createQuery(""
                        + "SELECT reservationRequest.id, aliasType, aliasSpecification.value"
                        + " FROM AbstractReservationRequest reservationRequest, AliasSpecification aliasSpecification"
                        + " LEFT JOIN aliasSpecification.aliasTypes aliasType"
                        + " WHERE reservationRequest.id IN(:reservationRequestIds)"
                        + " AND ("
                        + "       reservationRequest.specification.id = aliasSpecification.id"
                        + "    OR aliasSpecification.id IN("
                        + "       SELECT childAliasSpecification.id FROM AliasSetSpecification aliasSetSpecification"
                        + "       LEFT JOIN aliasSetSpecification.aliasSpecifications childAliasSpecification"
                        + "       WHERE reservationRequest.specification.id = aliasSetSpecification.id)"
                        + " )",
                        Object[].class)
                        .setParameter("reservationRequestIds", aliasReservationRequestIds)
                        .getResultList();

                // Sort requested aliases for each reservation request
                Collections.sort(aliasReservationRequests, new Comparator<Object[]>()
                {
                    @Override
                    public int compare(Object[] object1, Object[] object2)
                    {
                        if (!object1[0].equals(object2[0])) {
                            // Skip same reservation request
                            return 0;
                        }
                        return ((AliasType) object1[1]).compareTo((AliasType) object2[1]);
                    }
                });

                // Fill first requested alias for each reservation request
                for (Object[] aliasReservation : aliasReservationRequests) {
                    Long id = (Long) aliasReservation[0];
                    if (!aliasReservationRequestIds.contains(id)) {
                        continue;
                    }
                    aliasReservationRequestIds.remove(id);

                    ReservationRequestSummary item = reservationRequestById.get(id);
                    ReservationRequestSummary.AliasType aliasType = new ReservationRequestSummary.AliasType();
                    aliasType.setAliasType((AliasType) aliasReservation[1]);
                    aliasType.setValue((String) aliasReservation[2]);
                    item.setType(aliasType);
                }
            }
            return response;
        }
        finally {
            entityManager.close();
        }
    }

    @Override
    public cz.cesnet.shongo.controller.api.AbstractReservationRequest getReservationRequest(SecurityToken token,
            String reservationRequestId)
    {
        String userId = authorization.validate(token);

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        ReservationRequestManager reservationRequestManager = new ReservationRequestManager(entityManager);
        EntityIdentifier entityId = EntityIdentifier.parse(reservationRequestId, EntityType.RESERVATION_REQUEST);

        try {
            cz.cesnet.shongo.controller.request.AbstractReservationRequest reservationRequest =
                    reservationRequestManager.get(entityId.getPersistenceId());

            if (!authorization.hasPermission(userId, entityId, Permission.READ)) {
                ControllerReportSetHelper.throwSecurityNotAuthorizedFault("read reservation request %s", entityId);
            }
            switch (reservationRequest.getType()) {
                case DELETED:
                    throw new ControllerReportSet.ReservationRequestDeletedException(entityId.toId());
            }

            return reservationRequest.toApi(authorization.isAdmin(userId));
        }
        finally {
            entityManager.close();
        }
    }

    @Override
    public Reservation getReservation(SecurityToken token, String reservationId)
    {
        String userId = authorization.validate(token);

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        ReservationManager reservationManager = new ReservationManager(entityManager);
        EntityIdentifier entityId = EntityIdentifier.parse(reservationId, EntityType.RESERVATION);

        try {
            cz.cesnet.shongo.controller.reservation.Reservation reservation =
                    reservationManager.get(entityId.getPersistenceId());

            if (!authorization.hasPermission(userId, entityId, Permission.READ)) {
                ControllerReportSetHelper.throwSecurityNotAuthorizedFault("read reservation %s", entityId);
            }

            return reservation.toApi(authorization.isAdmin(userId));
        }
        finally {
            entityManager.close();
        }
    }

    @Override
    public ListResponse<Reservation> listReservations(ReservationListRequest request)
    {
        String userId = authorization.validate(request.getSecurityToken());

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        ReservationManager reservationManager = new ReservationManager(entityManager);

        try {
            DatabaseFilter filter = new DatabaseFilter("reservation");

            // List only reservations which is current user permitted to read
            filter.addIds(authorization, userId, EntityType.RESERVATION, Permission.READ);

            // List only reservations which are requested
            if (request.getReservationIds().size() > 0) {
                filter.addFilter("reservation.id IN (:reservationIds)");
                Set<Long> reservationIds = new HashSet<Long>();
                for (String reservationId : request.getReservationIds()) {
                    reservationIds.add(EntityIdentifier.parseId(
                            cz.cesnet.shongo.controller.reservation.Reservation.class, reservationId));
                }
                filter.addFilterParameter("reservationIds", reservationIds);
            }

            // List only reservations of requested classes
            Set<Class<? extends Reservation>> reservationApiClasses = request.getReservationClasses();
            if (reservationApiClasses.size() > 0) {
                if (reservationApiClasses.contains(AliasReservation.class)) {
                    // List only reservations of given classes or raw reservations which have alias reservation as child
                    filter.addFilter("reservation IN ("
                            + "   SELECT mainReservation FROM Reservation mainReservation"
                            + "   LEFT JOIN mainReservation.childReservations childReservation"
                            + "   WHERE TYPE(mainReservation) IN(:classes)"
                            + "      OR (TYPE(mainReservation) = :raw AND TYPE(childReservation) = :alias)"
                            + " )");
                    filter.addFilterParameter("alias", cz.cesnet.shongo.controller.reservation.AliasReservation.class);
                    filter.addFilterParameter("raw", cz.cesnet.shongo.controller.reservation.Reservation.class);
                }
                else {
                    // List only reservations of given classes
                    filter.addFilter("TYPE(reservation) IN(:classes)");
                }
                Set<Class<? extends cz.cesnet.shongo.controller.reservation.Reservation>> reservationClasses =
                        new HashSet<Class<? extends cz.cesnet.shongo.controller.reservation.Reservation>>();
                for (Class<? extends Reservation> reservationApiClass : reservationApiClasses) {
                    reservationClasses.add(cz.cesnet.shongo.controller.reservation.Reservation.getClassFromApi(
                            reservationApiClass));
                }
                filter.addFilterParameter("classes", reservationClasses);
            }

            // List only reservations allocated for requested reservation request
            if (request.getReservationRequestId() != null) {
                // List only reservations which are allocated for reservation request with given id or child reservation requests
                filter.addFilter("reservation.allocation IS NOT NULL AND (reservation.allocation IN ("
                        + "   SELECT allocation FROM AbstractReservationRequest reservationRequest"
                        + "   LEFT JOIN reservationRequest.allocation allocation"
                        + "   WHERE reservationRequest.id = :reservationRequestId"
                        + " ) OR reservation.allocation IN ("
                        + "   SELECT childAllocation FROM AbstractReservationRequest reservationRequest"
                        + "   LEFT JOIN reservationRequest.allocation allocation"
                        + "   LEFT JOIN allocation.childReservationRequests childReservationRequest"
                        + "   LEFT JOIN childReservationRequest.allocation childAllocation"
                        + "   WHERE reservationRequest.id = :reservationRequestId"
                        + " ))");
                filter.addFilterParameter("reservationRequestId", EntityIdentifier.parseId(
                        cz.cesnet.shongo.controller.request.AbstractReservationRequest.class,
                        request.getReservationRequestId()));
            }

            ListResponse<Reservation> response = new ListResponse<Reservation>();
            List<cz.cesnet.shongo.controller.reservation.Reservation> reservations = performListRequest(
                    "reservation", "SELECT reservation", cz.cesnet.shongo.controller.reservation.Reservation.class,
                    "FROM Reservation reservation", filter, request, response, entityManager);

            // Filter reservations by technologies
            Set<Technology> technologies = request.getTechnologies();
            if (technologies.size() > 0) {
                Iterator<cz.cesnet.shongo.controller.reservation.Reservation> iterator = reservations.iterator();
                while (iterator.hasNext()) {
                    cz.cesnet.shongo.controller.reservation.Reservation reservation = iterator.next();
                    if (reservation instanceof cz.cesnet.shongo.controller.reservation.AliasReservation) {
                        cz.cesnet.shongo.controller.reservation.AliasReservation aliasReservation = (cz.cesnet.shongo.controller.reservation.AliasReservation) reservation;
                        boolean technologyFound = false;
                        for (Alias alias : aliasReservation.getAliases()) {
                            if (technologies.contains(alias.getTechnology())) {
                                technologyFound = true;
                                break;
                            }
                        }
                        if (!technologyFound) {
                            iterator.remove();
                        }
                    }
                    else if (reservation.getClass().equals(cz.cesnet.shongo.controller.reservation.Reservation.class)) {
                        boolean technologyFound = false;
                        for (cz.cesnet.shongo.controller.reservation.Reservation childReservation : reservation.getChildReservations()) {
                            if (childReservation instanceof cz.cesnet.shongo.controller.reservation.AliasReservation) {
                                cz.cesnet.shongo.controller.reservation.AliasReservation childAliasReservation = (cz.cesnet.shongo.controller.reservation.AliasReservation) childReservation;
                                for (Alias alias : childAliasReservation.getAliases()) {
                                    if (technologies.contains(alias.getTechnology())) {
                                        technologyFound = true;
                                        break;
                                    }
                                }
                            }
                            else {
                                throw new TodoImplementException(childReservation.getClass().getName());
                            }
                        }
                        if (!technologyFound) {
                            iterator.remove();
                        }
                    }
                    else {
                        throw new TodoImplementException(reservation.getClass().getName());
                    }
                }
            }

            // Fill reservations to response
            for (cz.cesnet.shongo.controller.reservation.Reservation reservation : reservations) {
                response.addItem(reservation.toApi(authorization.isAdmin(userId)));
            }
            return  response;
        }
        finally {
            entityManager.close();

        }
    }

    private  <T> List<T> performListRequest(String queryAlias, String querySelect, Class<T> querySelectClass, String queryFrom,
            DatabaseFilter filter, ListRequest listRequest, ListResponse listResponse, EntityManager entityManager)
    {
        String queryWhere = filter.toQueryWhere();

        // Create query for listing
        StringBuilder listQueryBuilder = new StringBuilder();
        listQueryBuilder.append(querySelect);
        listQueryBuilder.append(" ");
        listQueryBuilder.append(queryFrom);
        listQueryBuilder.append(" WHERE ");
        listQueryBuilder.append(queryWhere);
        TypedQuery<T> listQuery = entityManager.createQuery(listQueryBuilder.toString(), querySelectClass);

        // Create query for counting records
        StringBuilder countQueryBuilder = new StringBuilder();
        countQueryBuilder.append("SELECT COUNT(");
        countQueryBuilder.append(queryAlias);
        countQueryBuilder.append(".id) ");
        countQueryBuilder.append(queryFrom);
        countQueryBuilder.append(" WHERE ");
        countQueryBuilder.append(queryWhere);
        TypedQuery<Long> countQuery = entityManager.createQuery(countQueryBuilder.toString(), Long.class);

        // Fill filter parameters to queries
        filter.fillQueryParameters(listQuery);
        filter.fillQueryParameters(countQuery);

        // Get total record count
        Integer totalResultCount = countQuery.getSingleResult().intValue();

        // Restrict first result
        Integer firstResult = listRequest.getStart(0);
        if (firstResult < 0) {
            firstResult = 0;
        }
        listQuery.setFirstResult(firstResult);

        // Restrict result count
        Integer maxResultCount = listRequest.getCount(-1);
        if (maxResultCount != null && maxResultCount != -1) {
            if ((firstResult + maxResultCount) > totalResultCount) {
                maxResultCount = totalResultCount - firstResult;
            }
            listQuery.setMaxResults(maxResultCount);
        }

        // Setup response
        listResponse.setCount(totalResultCount);
        listResponse.setStart(firstResult);

        // List requested results
        return listQuery.getResultList();
    }
}
