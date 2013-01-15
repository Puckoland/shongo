package cz.cesnet.shongo.controller.api;

import cz.cesnet.shongo.Technology;
import cz.cesnet.shongo.api.util.Converter;
import cz.cesnet.shongo.controller.Authorization;
import cz.cesnet.shongo.controller.Component;
import cz.cesnet.shongo.controller.Configuration;
import cz.cesnet.shongo.controller.fault.ReservationRequestNotModifiableException;
import cz.cesnet.shongo.controller.request.DateTimeSlotSpecification;
import cz.cesnet.shongo.controller.request.ReservationRequestManager;
import cz.cesnet.shongo.controller.reservation.ReservationManager;
import cz.cesnet.shongo.controller.util.DatabaseFilter;
import cz.cesnet.shongo.fault.FaultException;
import cz.cesnet.shongo.fault.TodoImplementException;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.*;

/**
 * Reservation service implementation
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
     * @see cz.cesnet.shongo.controller.Authorization
     */
    private Authorization authorization;

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
    public String createReservationRequest(SecurityToken token, AbstractReservationRequest reservationRequest)
            throws FaultException
    {
        authorization.validate(token);

        if (reservationRequest == null) {
            throw new IllegalArgumentException("Reservation request should not be null.");
        }
        reservationRequest.setupNewEntity();

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();

        cz.cesnet.shongo.controller.request.AbstractReservationRequest reservationRequestImpl;
        try {
            reservationRequestImpl = cz.cesnet.shongo.controller.request.AbstractReservationRequest.createFromApi(
                    reservationRequest, entityManager);
            reservationRequestImpl.setUserId(authorization.getUserId(token));

            ReservationRequestManager reservationRequestManager = new ReservationRequestManager(entityManager);
            reservationRequestManager.create(reservationRequestImpl);

            entityManager.getTransaction().commit();
        }
        catch (Exception exception) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            if (exception instanceof FaultException) {
                throw (FaultException) exception;
            }
            else {
                throw new FaultException(exception);
            }
        }
        finally {
            entityManager.close();
        }

        return cz.cesnet.shongo.controller.Domain.getLocalDomain().formatId(reservationRequestImpl);
    }

    /**
     * Check whether {@code abstractReservationRequestImpl} can be modified or deleted.
     *
     * @param abstractReservationRequestImpl
     * @throws ReservationRequestNotModifiableException
     *
     */
    private void checkModifiableReservationRequest(
            cz.cesnet.shongo.controller.request.AbstractReservationRequest abstractReservationRequestImpl)
            throws ReservationRequestNotModifiableException
    {
        if (abstractReservationRequestImpl instanceof cz.cesnet.shongo.controller.request.ReservationRequest) {
            cz.cesnet.shongo.controller.request.ReservationRequest reservationRequestImpl =
                    (cz.cesnet.shongo.controller.request.ReservationRequest) abstractReservationRequestImpl;
            if (reservationRequestImpl.getCreatedBy() ==
                    cz.cesnet.shongo.controller.request.ReservationRequest.CreatedBy.CONTROLLER) {
                throw new ReservationRequestNotModifiableException(
                        cz.cesnet.shongo.controller.Domain.getLocalDomain().formatId(abstractReservationRequestImpl));
            }
        }
    }

    @Override
    public void modifyReservationRequest(SecurityToken token, AbstractReservationRequest reservationRequestApi)
            throws FaultException
    {
        authorization.validate(token);

        Long id = cz.cesnet.shongo.controller.Domain.getLocalDomain().parseId(reservationRequestApi.getId());

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();

        try {

            ReservationRequestManager reservationRequestManager = new ReservationRequestManager(entityManager);

            cz.cesnet.shongo.controller.request.AbstractReservationRequest reservationRequest =
                    reservationRequestManager.get(id);
            checkModifiableReservationRequest(reservationRequest);
            reservationRequest.fromApi(reservationRequestApi, entityManager);

            if (reservationRequest instanceof cz.cesnet.shongo.controller.request.ReservationRequest) {
                cz.cesnet.shongo.controller.request.ReservationRequest singleReservationRequestImpl =
                        (cz.cesnet.shongo.controller.request.ReservationRequest) reservationRequest;
                // Reservation request was modified, so we must clear it's state
                singleReservationRequestImpl.clearState();
                // Update state
                singleReservationRequestImpl.updateStateBySpecification();
            }

            reservationRequestManager.update(reservationRequest);

            entityManager.getTransaction().commit();
        }
        catch (Exception exception) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            if (exception instanceof FaultException) {
                throw (FaultException) exception;
            }
            else {
                throw new FaultException(exception);
            }
        }
        finally {
            entityManager.close();
        }
    }

    @Override
    public void deleteReservationRequest(SecurityToken token, String reservationRequestId) throws FaultException
    {
        authorization.validate(token);

        Long id = cz.cesnet.shongo.controller.Domain.getLocalDomain().parseId(reservationRequestId);

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();

        try {
            ReservationRequestManager reservationRequestManager = new ReservationRequestManager(entityManager);

            cz.cesnet.shongo.controller.request.AbstractReservationRequest reservationRequestImpl =
                    reservationRequestManager.get(id);
            checkModifiableReservationRequest(reservationRequestImpl);

            reservationRequestManager.delete(reservationRequestImpl);

            entityManager.getTransaction().commit();
        }
        catch (Exception exception) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            if (exception instanceof FaultException) {
                throw (FaultException) exception;
            }
            else {
                throw new FaultException(exception);
            }
        }
        finally {
            entityManager.close();
        }
    }

    @Override
    public Collection<ReservationRequestSummary> listReservationRequests(SecurityToken token,
            Map<String, Object> filter) throws FaultException
    {
        authorization.validate(token);

        cz.cesnet.shongo.controller.Domain localDomain = cz.cesnet.shongo.controller.Domain.getLocalDomain();

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        ReservationRequestManager reservationRequestManager = new ReservationRequestManager(entityManager);

        String userId = DatabaseFilter.getUserIdFromFilter(filter, authorization.getUserId(token));
        Set<Technology> technologies = DatabaseFilter.getTechnologiesFromFilter(filter);
        Set<Class<? extends cz.cesnet.shongo.controller.request.Specification>> specificationClasses =
                DatabaseFilter.getClassesFromFilter(filter, "specificationClass",
                        cz.cesnet.shongo.controller.request.Specification.class);
        Long providedReservationId = null;
        if (filter != null) {
            if (filter.containsKey("providedReservationId")) {
                providedReservationId = localDomain.parseId((String) Converter.convert(
                        filter.get("providedReservationId"), String.class));
            }
        }

        List<cz.cesnet.shongo.controller.request.AbstractReservationRequest> reservationRequests =
                reservationRequestManager.list(userId, technologies, specificationClasses, providedReservationId);

        List<ReservationRequestSummary> summaryList = new ArrayList<ReservationRequestSummary>();
        for (cz.cesnet.shongo.controller.request.AbstractReservationRequest abstractReservationRequest : reservationRequests) {
            ReservationRequestSummary summary = new ReservationRequestSummary();
            summary.setId(localDomain.formatId(abstractReservationRequest));
            summary.setUserId(abstractReservationRequest.getUserId());

            Interval earliestSlot = null;
            if (abstractReservationRequest instanceof cz.cesnet.shongo.controller.request.ReservationRequest) {
                cz.cesnet.shongo.controller.request.ReservationRequest reservationRequest =
                        (cz.cesnet.shongo.controller.request.ReservationRequest) abstractReservationRequest;
                earliestSlot = reservationRequest.getSlot();
                summary.setState(reservationRequest.getStateAsApi());
            }
            else if (abstractReservationRequest instanceof cz.cesnet.shongo.controller.request.ReservationRequestSet) {
                cz.cesnet.shongo.controller.request.ReservationRequestSet reservationRequestSet =
                        (cz.cesnet.shongo.controller.request.ReservationRequestSet) abstractReservationRequest;
                for (DateTimeSlotSpecification slot : reservationRequestSet.getSlots()) {
                    Interval interval = slot.getEarliest(DateTime.now());
                    if (earliestSlot == null || interval.getStart().isBefore(earliestSlot.getStart())) {
                        earliestSlot = interval;
                    }
                }
                List<cz.cesnet.shongo.controller.request.ReservationRequest> requests =
                        reservationRequestSet.getReservationRequests();
                if (earliestSlot == null && requests.size() > 0) {
                    earliestSlot = requests.get(requests.size() - 1).getSlot();
                }
                for (cz.cesnet.shongo.controller.request.ReservationRequest reservationRequest : requests) {
                    if (reservationRequest.getSlot().equals(earliestSlot)) {
                        summary.setState(reservationRequest.getStateAsApi());
                    }
                }
            }
            else if (abstractReservationRequest instanceof cz.cesnet.shongo.controller.request.PermanentReservationRequest) {
                cz.cesnet.shongo.controller.request.PermanentReservationRequest permanentReservationRequest =
                        (cz.cesnet.shongo.controller.request.PermanentReservationRequest) abstractReservationRequest;
                for (DateTimeSlotSpecification slot : permanentReservationRequest.getSlots()) {
                    Interval interval = slot.getEarliest(null);
                    if (earliestSlot == null || interval.getStart().isBefore(earliestSlot.getStart())) {
                        earliestSlot = interval;
                    }
                }

                if (permanentReservationRequest.getReservations().size() > 0) {
                    summary.setState(ReservationRequestState.ALLOCATED);
                }
                else {
                    summary.setState(ReservationRequestState.NOT_ALLOCATED);
                }
                // TODO: Implement allocation failed state to permanent reservations
            }
            else {
                throw new TodoImplementException(abstractReservationRequest.getClass().getCanonicalName());
            }

            summary.setCreated(abstractReservationRequest.getCreated());
            summary.setName(abstractReservationRequest.getName());
            if (abstractReservationRequest instanceof cz.cesnet.shongo.controller.request.NormalReservationRequest) {
                cz.cesnet.shongo.controller.request.NormalReservationRequest normalReservationRequest =
                        (cz.cesnet.shongo.controller.request.NormalReservationRequest) abstractReservationRequest;
                summary.setPurpose(normalReservationRequest.getPurpose());
                summary.setType(ReservationRequestSummary.Type.NORMAL);
            }
            if (abstractReservationRequest instanceof cz.cesnet.shongo.controller.request.PermanentReservationRequest) {
                summary.setType(ReservationRequestSummary.Type.PERMANENT);
            }
            summary.setDescription(abstractReservationRequest.getDescription());
            summary.setEarliestSlot(earliestSlot);
            summaryList.add(summary);
        }

        entityManager.close();

        return summaryList;
    }

    @Override
    public AbstractReservationRequest getReservationRequest(SecurityToken token, String reservationRequestId)
            throws FaultException
    {
        authorization.validate(token);

        Long id = cz.cesnet.shongo.controller.Domain.getLocalDomain().parseId(reservationRequestId);

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        ReservationRequestManager reservationRequestManager = new ReservationRequestManager(entityManager);

        cz.cesnet.shongo.controller.request.AbstractReservationRequest reservationRequestImpl =
                reservationRequestManager.get(id);
        AbstractReservationRequest reservationRequest = reservationRequestImpl.toApi();

        entityManager.close();

        return reservationRequest;
    }

    @Override
    public Reservation getReservation(SecurityToken token, String reservationId) throws FaultException
    {
        authorization.validate(token);

        Long id = cz.cesnet.shongo.controller.Domain.getLocalDomain().parseId(reservationId);

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        ReservationManager reservationManager = new ReservationManager(entityManager);

        cz.cesnet.shongo.controller.reservation.Reservation reservationImpl = reservationManager.get(id);
        Reservation reservation = reservationImpl.toApi();

        entityManager.close();

        return reservation;
    }

    @Override
    public Collection<Reservation> listReservations(SecurityToken token, Map<String, Object> filter)
            throws FaultException
    {
        authorization.validate(token);

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        ReservationManager reservationManager = new ReservationManager(entityManager);

        String userId = DatabaseFilter.getUserIdFromFilter(filter, authorization.getUserId(token));
        Long reservationRequestId = null;
        if (filter != null) {
            if (filter.containsKey("reservationRequestId")) {
                reservationRequestId = cz.cesnet.shongo.controller.Domain.getLocalDomain().parseId(
                        (String) Converter.convert(filter.get("reservationRequestId"), String.class));
            }
        }
        Set<Technology> technologies = DatabaseFilter.getTechnologiesFromFilter(filter);
        Set<Class<? extends cz.cesnet.shongo.controller.reservation.Reservation>> reservationClasses =
                DatabaseFilter.getClassesFromFilter(filter, "reservationClass",
                        cz.cesnet.shongo.controller.reservation.Reservation.class);

        List<cz.cesnet.shongo.controller.reservation.Reservation> reservations =
                reservationManager.list(userId, reservationRequestId, reservationClasses, technologies);
        List<Reservation> apiReservations = new ArrayList<Reservation>();
        for (cz.cesnet.shongo.controller.reservation.Reservation reservation : reservations) {
            apiReservations.add(reservation.toApi());
        }

        entityManager.close();

        return apiReservations;
    }
}
