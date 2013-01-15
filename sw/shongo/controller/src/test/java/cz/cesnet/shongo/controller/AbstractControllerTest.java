package cz.cesnet.shongo.controller;

import cz.cesnet.shongo.controller.api.*;
import cz.cesnet.shongo.fault.FaultException;
import org.joda.time.Interval;

import javax.persistence.EntityManager;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * Abstract controller test provides a {@link Controller} instance to extending classes.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public abstract class AbstractControllerTest extends AbstractDatabaseTest
{
    /**
     * {@link SecurityToken} which is validated in the {@link #controller}
     */
    protected static final SecurityToken SECURITY_TOKEN =
            new SecurityToken("18eea565098d4620d398494b111cb87067a3b6b9");

    /**
     * @see Controller
     */
    private cz.cesnet.shongo.controller.Controller controller;

    /**
     * @see Cache
     */
    private Cache cache;

    /**
     * @see ControllerClient
     */
    private ControllerClient controllerClient;

    /**
     * @return {@link Configuration} from the {@link #controller}
     */
    public Controller getController()
    {
        return controller;
    }

    /**
     * @return {@link #controllerClient}
     */
    public ControllerClient getControllerClient()
    {
        return controllerClient;
    }

    /**
     * @return {@link ResourceService} from the {@link #controllerClient}
     */
    public ResourceService getResourceService()
    {
        return controllerClient.getService(ResourceService.class);
    }

    /**
     * @return {@link ReservationService} from the {@link #controllerClient}
     */
    public ReservationService getReservationService()
    {
        return controllerClient.getService(ReservationService.class);
    }

    /**
     * On controller initialized.
     */
    protected void onInit()
    {
        cache = new Cache();
        cache.setEntityManagerFactory(getEntityManagerFactory());
        cache.init();

        ResourceServiceImpl resourceService = new ResourceServiceImpl();
        resourceService.setCache(cache);
        controller.addService(resourceService);
        controller.addService(new ReservationServiceImpl());
    }

    /**
     * On after controller start.
     */
    protected void onStart()
    {
    }

    @Override
    public void before() throws Exception
    {
        super.before();

        // Change XML-RPC port
        System.setProperty(Configuration.RPC_PORT, "8484");
        System.setProperty(Configuration.JADE_PORT, "8585");

        // Start controller
        controller = new cz.cesnet.shongo.controller.Controller();
        controller.setDomain("cz.cesnet", "CESNET, z.s.p.o.");
        controller.setEntityManagerFactory(getEntityManagerFactory());

        onInit();

        controller.start();
        controller.startRpc();
        controller.getAuthorization().setTestingAccessToken(SECURITY_TOKEN.getAccessToken());

        // Start client
        controllerClient = new ControllerClient(controller.getRpcHost(), controller.getRpcPort());

        onStart();
    }

    @Override
    public void after()
    {
        controller.stop();

        super.after();
    }

    /**
     * Run {@link Preprocessor}.
     *
     * @param interval
     * @throws FaultException
     */
    protected void runPreprocessor(Interval interval) throws FaultException
    {
        EntityManager entityManagerForPreprocessor = getEntityManager();
        Preprocessor.createAndRun(interval, entityManagerForPreprocessor, cache);
        entityManagerForPreprocessor.close();
    }

    /**
     * Run {@link Preprocessor}.
     *
     * @throws FaultException
     */
    protected void runPreprocessor() throws FaultException
    {
        runPreprocessor(Interval.parse("0/9999"));
    }

    /**
     * Run {@link Scheduler}.
     *
     * @throws FaultException
     */
    protected void runScheduler() throws FaultException
    {
        Interval interval = Interval.parse("0/9999");

        EntityManager entityManagerForScheduler = getEntityManager();
        Scheduler.createAndRun(interval, entityManagerForScheduler, cache, controller.getNotificationManager());
        entityManagerForScheduler.close();
    }

    /**
     * Check if {@link ReservationRequest} was successfully allocated.
     *
     * @param reservationRequestId for {@link ReservationRequest} to be checked
     * @return {@link Reservation}
     * @throws Exception
     */
    protected Reservation checkAllocated(String reservationRequestId) throws Exception
    {
        AbstractReservationRequest abstractReservationRequest =
                getReservationService().getReservationRequest(SECURITY_TOKEN, reservationRequestId);
        String reservationId = null;
        if (abstractReservationRequest instanceof NormalReservationRequest) {
            ReservationRequest reservationRequest;
            if (abstractReservationRequest instanceof ReservationRequestSet) {
                ReservationRequestSet reservationRequestSet = (ReservationRequestSet) abstractReservationRequest;
                assertEquals(1, reservationRequestSet.getReservationRequests().size());
                reservationRequest = reservationRequestSet.getReservationRequests().get(0);
            }
            else {
                reservationRequest = (ReservationRequest) abstractReservationRequest;
            }
            if (reservationRequest.getState() != ReservationRequestState.ALLOCATED) {
                System.err.println(reservationRequest.getStateReport());
                Thread.sleep(100);
            }
            assertEquals("Reservation request should be in ALLOCATED state.",
                    ReservationRequestState.ALLOCATED, reservationRequest.getState());
            reservationId = reservationRequest.getReservationId();
        }
        else if (abstractReservationRequest instanceof PermanentReservationRequest) {
            PermanentReservationRequest permanentReservationRequest =
                    (PermanentReservationRequest) abstractReservationRequest;
            List<ResourceReservation> resourceReservations = permanentReservationRequest.getResourceReservations();
            assertTrue("Permanent reservation request should have at least one resource reservation.",
                    resourceReservations.size() > 0);
            reservationId = resourceReservations.get(0).getId();
        }
        assertNotNull(reservationId);
        Reservation reservation = getReservationService().getReservation(SECURITY_TOKEN, reservationId);
        assertNotNull("Reservation should be allocated for the reservation request.", reservation);
        return reservation;
    }

    /**
     * Check if {@link ReservationRequest}'s allocation failed.
     *
     * @param reservationRequestId for {@link ReservationRequest} to be checked
     * @return {@link Reservation}
     * @throws Exception
     */
    protected void checkAllocationFailed(String reservationRequestId) throws Exception
    {
        AbstractReservationRequest abstractReservationRequest = getReservationService().getReservationRequest(
                SECURITY_TOKEN, reservationRequestId);
        if (abstractReservationRequest instanceof NormalReservationRequest) {
            ReservationRequest reservationRequest;
            if (abstractReservationRequest instanceof ReservationRequestSet) {
                ReservationRequestSet reservationRequestSet = (ReservationRequestSet) abstractReservationRequest;
                assertEquals(1, reservationRequestSet.getReservationRequests().size());
                reservationRequest = reservationRequestSet.getReservationRequests().get(0);
            }
            else {
                reservationRequest = (ReservationRequest) abstractReservationRequest;
            }
            assertEquals("Reservation request should be in ALLOCATION_FAILED state.",
                    ReservationRequestState.ALLOCATION_FAILED, reservationRequest.getState());
            String reservationId = reservationRequest.getReservationId();
            assertNull("No reservation should be allocated for the reservation request.", reservationId);
        }
        else if (abstractReservationRequest instanceof PermanentReservationRequest) {
            PermanentReservationRequest permanentReservationRequest =
                    (PermanentReservationRequest) abstractReservationRequest;
            List<ResourceReservation> resourceReservations = permanentReservationRequest.getResourceReservations();
            assertEquals("Permanent reservation request should have no resource reservation.",
                    0, resourceReservations.size());
        }

    }

    /**
     * Allocate given {@code reservationRequest}.
     *
     * @param reservationRequest to be allocated
     * @return shongo-id of created or modified {@link ReservationRequest}
     * @throws Exception
     */
    protected String allocate(AbstractReservationRequest reservationRequest) throws Exception
    {
        String reservationRequestId;
        if (reservationRequest.getId() == null) {
            reservationRequestId = getReservationService().createReservationRequest(SECURITY_TOKEN, reservationRequest);
        }
        else {
            reservationRequestId = reservationRequest.getId();
            getReservationService().modifyReservationRequest(SECURITY_TOKEN, reservationRequest);
        }
        if (reservationRequest instanceof ReservationRequestSet || reservationRequest instanceof PermanentReservationRequest) {
            runPreprocessor();
        }
        runScheduler();
        return reservationRequestId;
    }

    /**
     * Reallocate given {@link AbstractReservationRequest} with given {@code reservationRequestId}.
     *
     * @param reservationRequestId to be allocated
     * @throws Exception
     */
    public void reallocate(String reservationRequestId) throws Exception
    {
        AbstractReservationRequest reservationRequest = getReservationService().getReservationRequest(SECURITY_TOKEN,
                reservationRequestId);
        getReservationService().modifyReservationRequest(SECURITY_TOKEN, reservationRequest);
        if (reservationRequest instanceof ReservationRequestSet || reservationRequest instanceof PermanentReservationRequest) {
            runPreprocessor();
        }
        runScheduler();
    }

    /**
     * Allocate given {@code reservationRequest} and call {@link #checkAllocated(String)}.
     *
     * @param reservationRequest to be allocated and checked
     * @return allocated {@link Reservation}
     * @throws Exception
     */
    protected Reservation allocateAndCheck(AbstractReservationRequest reservationRequest) throws Exception
    {
        String reservationRequestId = allocate(reservationRequest);
        return checkAllocated(reservationRequestId);
    }

    /**
     * Allocate given {@code reservationRequest} and call {@link #checkAllocated(String)}.
     *
     * @param reservationRequest to be allocated and checked
     * @return allocated {@link Reservation}
     * @throws Exception
     */
    protected void allocateAndCheckFailed(AbstractReservationRequest reservationRequest) throws Exception
    {
        String reservationRequestId = allocate(reservationRequest);
        checkAllocationFailed(reservationRequestId);
    }
}
