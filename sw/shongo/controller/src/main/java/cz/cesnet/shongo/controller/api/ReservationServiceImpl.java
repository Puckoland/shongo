package cz.cesnet.shongo.controller.api;

import cz.cesnet.shongo.controller.Component;
import cz.cesnet.shongo.controller.Domain;
import cz.cesnet.shongo.controller.Technology;
import cz.cesnet.shongo.controller.api.util.Converter;
import cz.cesnet.shongo.controller.common.AbsoluteDateTimeSpecification;
import cz.cesnet.shongo.controller.common.PeriodicDateTimeSpecification;
import cz.cesnet.shongo.controller.request.ReservationRequestManager;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Reservation service implementation
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public class ReservationServiceImpl extends Component implements ReservationService
{
    /**
     * @see Domain
     */
    private Domain domain;


    /**
     * Constructor.
     */
    public ReservationServiceImpl()
    {
    }

    /**
     * Constructor.
     *
     * @param domain sets the {@link #domain}
     */
    public ReservationServiceImpl(Domain domain)
    {
        setDomain(domain);
    }

    /**
     * @param domain sets the {@link #domain}
     */
    public void setDomain(Domain domain)
    {
        this.domain = domain;
    }

    @Override
    public void init()
    {
        super.init();
        if (domain == null) {
            throw new IllegalStateException(getClass().getName() + " doesn't have the domain set!");
        }
    }

    @Override
    public String getServiceName()
    {
        return "Reservation";
    }


    @Override
    public String createReservationRequest(SecurityToken token, ReservationRequest reservationRequest)
            throws FaultException
    {
        reservationRequest.checkRequiredPropertiesFilled();

        EntityManager entityManager = getEntityManager();
        entityManager.getTransaction().begin();

        // Create reservation request
        cz.cesnet.shongo.controller.request.ReservationRequest reservationRequestImpl =
                new cz.cesnet.shongo.controller.request.ReservationRequest();

        // Fill common attributes
        reservationRequestImpl.setType(reservationRequest.getType());
        reservationRequestImpl.setName(reservationRequest.getName());
        reservationRequestImpl.setDescription(reservationRequest.getDescription());
        reservationRequestImpl.setPurpose(reservationRequest.getPurpose());

        // Fill requested slots
        for (DateTimeSlot dateTimeSlot : reservationRequest.getSlots()) {
            createRequestedSlotInReservationRequest(reservationRequestImpl, dateTimeSlot);
        }

        // Fill requested compartments
        for (Compartment compartment : reservationRequest.getCompartments()) {
            cz.cesnet.shongo.controller.request.Compartment compartmentImpl =
                    reservationRequestImpl.addRequestedCompartment();
            for (Person person : compartment.getPersons()) {
                compartmentImpl.addRequestedPerson(
                        new cz.cesnet.shongo.controller.common.Person(person.getName(), person.getEmail()));
            }
            for (Compartment.ResourceSpecificationMap map : compartment
                    .getResources()) {
                cz.cesnet.shongo.controller.request.ResourceSpecification resourceSpecification = null;
                if (map.containsKey("technology")) {
                    Technology technology = Converter
                            .convertStringToEnum((String) map.get("technology"), Technology.class);
                    if (map.containsKey("count")) {
                        resourceSpecification = new cz.cesnet.shongo.controller.request.ExternalEndpointSpecification(
                                technology, (Integer) map.get("count"));
                    }
                    else {
                        resourceSpecification = new cz.cesnet.shongo.controller.request.ExternalEndpointSpecification(
                                technology, (Integer) map.get("count"));
                    }
                }
                // Check resource specification existence
                if (resourceSpecification == null) {
                    throw new FaultException(ControllerFault.TODO_IMPLEMENT);
                }
                // Fill requested persons
                if (map.containsKey("persons")) {
                    for (Object object : (Object[]) map.get("persons")) {
                        cz.cesnet.shongo.controller.api.Person person =
                                Converter.convert(object, cz.cesnet.shongo.controller.api.Person.class);
                        resourceSpecification.addRequestedPerson(
                                new cz.cesnet.shongo.controller.common.Person(person.getName(), person.getEmail()));
                    }
                }
                compartmentImpl.addRequestedResource(resourceSpecification);
            }
        }

        // Save it
        ReservationRequestManager reservationRequestManager = new ReservationRequestManager(entityManager);
        reservationRequestManager.create(reservationRequestImpl);

        entityManager.getTransaction().commit();
        entityManager.close();

        // Return reservation request identifier
        return domain.formatIdentifier(reservationRequestImpl.getId());
    }

    @Override
    public void modifyReservationRequest(SecurityToken token, ReservationRequest reservationRequest)
            throws FaultException
    {
        Long reservationRequestId = domain.parseIdentifier(reservationRequest.getIdentifier());

        EntityManager entityManager = getEntityManager();
        entityManager.getTransaction().begin();

        ReservationRequestManager reservationRequestManager = new ReservationRequestManager(entityManager);

        // Get reservation request
        cz.cesnet.shongo.controller.request.ReservationRequest reservationRequestImpl =
                reservationRequestManager.get(reservationRequestId);

        // Modify common attributes
        if (reservationRequest.isPropertyFilled(ReservationRequest.TYPE)) {
            reservationRequestImpl.setType(reservationRequest.getType());
        }
        if (reservationRequest.isPropertyFilled(ReservationRequest.NAME)) {
            reservationRequestImpl.setName(reservationRequest.getName());
        }
        if (reservationRequest.isPropertyFilled(ReservationRequest.DESCRIPTION)) {
            reservationRequestImpl.setDescription(reservationRequest.getDescription());
        }
        if (reservationRequest.isPropertyFilled(ReservationRequest.PURPOSE)) {
            reservationRequestImpl.setPurpose(reservationRequest.getPurpose());
        }

        // Create/modify requested slots
        for (DateTimeSlot dateTimeSlot : reservationRequest.getSlots()) {
            // Create new requested slot
            if (reservationRequest.isCollectionItemMarkedAsNew(ReservationRequest.SLOTS, dateTimeSlot)) {
                createRequestedSlotInReservationRequest(reservationRequestImpl, dateTimeSlot);
            }
            else {
                // Modify existing requested slot
                cz.cesnet.shongo.controller.common.DateTimeSlot dateTimeSlotImpl =
                        reservationRequestImpl.getRequestedSlotById(dateTimeSlot.getIdentifierAsLong());
                dateTimeSlotImpl.setDuration(dateTimeSlot.getDuration());

                entityManager.remove(dateTimeSlotImpl.getStart());

                Object dateTime = dateTimeSlot.getStart();
                if (dateTime instanceof DateTime) {
                    dateTimeSlotImpl.setStart(new AbsoluteDateTimeSpecification((DateTime) dateTime));
                }
                else if (dateTime instanceof PeriodicDateTime) {
                    PeriodicDateTime periodic = (PeriodicDateTime) dateTime;
                    dateTimeSlotImpl.setStart(new PeriodicDateTimeSpecification(periodic.getStart(),
                            periodic.getPeriod()));
                }
            }
        }
        // Delete requested slots
        for (DateTimeSlot dateTimeSlot : reservationRequest.getCollectionItemsMarkedAsDeleted(
                ReservationRequest.SLOTS, DateTimeSlot.class)) {
            reservationRequestImpl.removeRequestedSlot(
                    reservationRequestImpl.getRequestedSlotById(dateTimeSlot.getIdentifierAsLong()));
        }

        reservationRequestManager.update(reservationRequestImpl);

        entityManager.getTransaction().commit();
        entityManager.close();
    }

    @Override
    public void deleteReservationRequest(SecurityToken token, String reservationRequestIdentifier) throws FaultException
    {
        Long reservationRequestId = domain.parseIdentifier(reservationRequestIdentifier);

        EntityManager entityManager = getEntityManager();
        entityManager.getTransaction().begin();

        ReservationRequestManager reservationRequestManager = new ReservationRequestManager(entityManager);

        // Get reservation request
        cz.cesnet.shongo.controller.request.ReservationRequest reservationRequestImpl =
                reservationRequestManager.get(reservationRequestId);
        if ( reservationRequestImpl == null ) {
            throw new FaultException(Fault.Common.RECORD_NOT_EXIST, ReservationRequest.class, reservationRequestId);
        }

        // Delete the request
        reservationRequestManager.delete(reservationRequestImpl);

        entityManager.getTransaction().commit();
        entityManager.close();
    }

    @Override
    public ReservationRequestSummary[] listReservationRequests(SecurityToken token)
    {
        EntityManager entityManager = getEntityManager();
        ReservationRequestManager reservationRequestManager = new ReservationRequestManager(entityManager);

        List<cz.cesnet.shongo.controller.request.ReservationRequest> list = reservationRequestManager.list();
        List<ReservationRequestSummary> summaryList = new ArrayList<ReservationRequestSummary>();
        for (cz.cesnet.shongo.controller.request.ReservationRequest reservationRequest : list) {
            ReservationRequestSummary summary = new ReservationRequestSummary();
            summary.setIdentifier(domain.formatIdentifier(reservationRequest.getId()));

            Interval earliestSlot = null;
            for ( cz.cesnet.shongo.controller.common.DateTimeSlot slot : reservationRequest.getRequestedSlots()) {
                Interval interval = slot.getEarliest(null);
                if (earliestSlot == null || interval.getStart().isBefore(earliestSlot.getStart())) {
                    earliestSlot = interval;
                }
            }

            summary.setType(reservationRequest.getType());
            summary.setName(reservationRequest.getName());
            summary.setPurpose(reservationRequest.getPurpose());
            summary.setDescription(reservationRequest.getDescription());
            summary.setEarliestSlot(earliestSlot);
            summaryList.add(summary);
        }
        return summaryList.toArray(new ReservationRequestSummary[summaryList.size()]);
    }

    @Override
    public cz.cesnet.shongo.controller.api.ReservationRequest getReservationRequest(SecurityToken token,
            String reservationRequestIdentifier) throws FaultException
    {
        Long reservationRequestId = domain.parseIdentifier(reservationRequestIdentifier);

        EntityManager entityManager = getEntityManager();
        ReservationRequestManager reservationRequestManager = new ReservationRequestManager(entityManager);
        cz.cesnet.shongo.controller.request.ReservationRequest reservationRequestImpl =
                reservationRequestManager.get(reservationRequestId);
        if (reservationRequestImpl == null) {
            return null;
        }

        ReservationRequest reservationRequest = new ReservationRequest();
        reservationRequest.setIdentifier(domain.formatIdentifier(reservationRequestImpl.getId()));
        reservationRequest.setType(reservationRequestImpl.getType());
        reservationRequest.setName(reservationRequestImpl.getName());
        reservationRequest.setDescription(reservationRequestImpl.getDescription());
        reservationRequest.setPurpose(reservationRequestImpl.getPurpose());

        for (cz.cesnet.shongo.controller.common.DateTimeSlot dateTimeSlotImpl :
                reservationRequestImpl.getRequestedSlots()) {

            Object start = null;
            if (dateTimeSlotImpl.getStart() instanceof AbsoluteDateTimeSpecification) {
                start = ((AbsoluteDateTimeSpecification) dateTimeSlotImpl.getStart()).getDateTime();
            }
            else if (dateTimeSlotImpl.getStart() instanceof PeriodicDateTimeSpecification) {
                PeriodicDateTimeSpecification periodicDateTimeSpecification =
                        (PeriodicDateTimeSpecification) dateTimeSlotImpl.getStart();

                start = new PeriodicDateTime(periodicDateTimeSpecification.getStart(),
                        periodicDateTimeSpecification.getPeriod());
            }
            else {
                throw new FaultException(ControllerFault.TODO_IMPLEMENT);
            }
            DateTimeSlot dateTimeSlot = new DateTimeSlot();
            dateTimeSlot.setIdentifier(dateTimeSlotImpl.getId().toString());
            dateTimeSlot.setStart(start);
            dateTimeSlot.setDuration(dateTimeSlotImpl.getDuration());
            reservationRequest.addSlot(dateTimeSlot);
        }

        for (cz.cesnet.shongo.controller.request.Compartment compartmentImpl :
                reservationRequestImpl.getRequestedCompartments()) {
            Compartment compartment = reservationRequest.addCompartment();
            for (cz.cesnet.shongo.controller.common.Person person : compartmentImpl.getRequestedPersons()) {
                compartment.addPerson(person.getName(), person.getEmail());
            }
            for (cz.cesnet.shongo.controller.request.ResourceSpecification resource :
                    compartmentImpl.getRequestedResources()) {
                if (resource instanceof cz.cesnet.shongo.controller.request.ExternalEndpointSpecification) {
                    cz.cesnet.shongo.controller.request.ExternalEndpointSpecification externalEndpointSpecification =
                            (cz.cesnet.shongo.controller.request.ExternalEndpointSpecification) resource;
                    List<cz.cesnet.shongo.controller.common.Person> resourceRequestedPersons =
                            externalEndpointSpecification.getRequestedPersons();
                    Person[] persons = new Person[resourceRequestedPersons.size()];
                    for (int index = 0; index < resourceRequestedPersons.size(); index++) {
                        cz.cesnet.shongo.controller.common.Person person = resourceRequestedPersons.get(index);
                        persons[index] = new Person(person.getName(), person.getEmail());
                    }
                    compartment.addResource(externalEndpointSpecification.getTechnologies().iterator().next(),
                            externalEndpointSpecification.getCount(), persons);
                }
            }
        }

        return reservationRequest;
    }

    /**
     * Create new requested slot in given reservation request from the given {@link DateTimeSlot}.
     *
     * @param reservationRequestImpl
     * @param dateTimeSlot
     * @throws FaultException
     */
    private void createRequestedSlotInReservationRequest(
            cz.cesnet.shongo.controller.request.ReservationRequest reservationRequestImpl, DateTimeSlot dateTimeSlot)
            throws FaultException
    {
        Object dateTime = dateTimeSlot.getStart();
        if (dateTime instanceof DateTime) {
            reservationRequestImpl.addRequestedSlot(
                    new AbsoluteDateTimeSpecification((DateTime) dateTime),
                    dateTimeSlot.getDuration());
        }
        else if (dateTime instanceof PeriodicDateTime) {
            PeriodicDateTime periodic = (PeriodicDateTime) dateTime;
            reservationRequestImpl.addRequestedSlot(
                    new PeriodicDateTimeSpecification(periodic.getStart(),
                            periodic.getPeriod()), dateTimeSlot.getDuration());
        }
        else {
            throw new FaultException(ControllerFault.Common.UNKNOWN_FAULT,
                    "Unknown date/time type.");
        }
    }
}
