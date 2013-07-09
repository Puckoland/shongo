package cz.cesnet.shongo.controller.api.request;

import cz.cesnet.shongo.Technology;
import cz.cesnet.shongo.api.DataMap;
import cz.cesnet.shongo.controller.api.Reservation;
import cz.cesnet.shongo.controller.api.SecurityToken;

import java.util.*;

/**
 * {@link ListRequest} for reservations.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public class ReservationListRequest extends ListRequest
{
    private Collection<String> reservationIds = new LinkedList<String>();

    private String reservationRequestId;

    private Set<Class<? extends Reservation>> reservationClasses = new HashSet<Class<? extends Reservation>>();

    private Set<Technology> technologies = new HashSet<Technology>();

    private Sort sort;

    private Boolean sortDescending;

    public ReservationListRequest()
    {
    }

    public ReservationListRequest(SecurityToken securityToken)
    {
        super(securityToken);
    }

    public ReservationListRequest(SecurityToken securityToken, String reservationRequestId)
    {
        super(securityToken);
        this.reservationRequestId = reservationRequestId;
    }

    public Collection<String> getReservationIds()
    {
        return reservationIds;
    }

    public void setReservationIds(Collection<String> reservationIds)
    {
        this.reservationIds = reservationIds;
    }

    public void addReservationId(String reservationId)
    {
        this.reservationIds.add(reservationId);
    }

    public String getReservationRequestId()
    {
        return reservationRequestId;
    }

    public void setReservationRequestId(String reservationRequestId)
    {
        this.reservationRequestId = reservationRequestId;
    }

    public Set<Class<? extends Reservation>> getReservationClasses()
    {
        return Collections.unmodifiableSet(reservationClasses);
    }

    public void setReservationClasses(Set<Class<? extends Reservation>> reservationClasses)
    {
        this.reservationClasses = reservationClasses;
    }

    public void addReservationClass(Class<? extends Reservation> reservationClass)
    {
        this.reservationClasses.add(reservationClass);
    }

    public Set<Technology> getTechnologies()
    {
        return Collections.unmodifiableSet(technologies);
    }

    public void setTechnologies(Set<Technology> technologies)
    {
        this.technologies = technologies;
    }

    public void addTechnology(Technology technology)
    {
        technologies.add(technology);
    }

    public Sort getSort()
    {
        return sort;
    }

    public void setSort(Sort sort)
    {
        this.sort = sort;
    }

    public Boolean getSortDescending()
    {
        return sortDescending;
    }

    public void setSortDescending(Boolean sortDescending)
    {
        this.sortDescending = sortDescending;
    }

    public static enum Sort
    {
        SLOT
    }

    private static final String RESERVATION_IDS = "reservationIds";
    private static final String RESERVATION_REQUEST_ID = "reservationRequestId";
    private static final String RESERVATION_CLASSES = "reservationClasses";
    private static final String TECHNOLOGIES = "technologies";
    private static final String SORT = "sort";
    private static final String SORT_DESCENDING = "sortDescending";

    @Override
    public DataMap toData()
    {
        DataMap dataMap = super.toData();
        dataMap.set(RESERVATION_IDS, reservationIds);
        dataMap.set(RESERVATION_REQUEST_ID, reservationRequestId);
        dataMap.set(RESERVATION_CLASSES, reservationClasses);
        dataMap.set(TECHNOLOGIES, technologies);
        dataMap.set(SORT, sort);
        dataMap.set(SORT_DESCENDING, sortDescending);
        return dataMap;
    }

    @Override
    public void fromData(DataMap dataMap)
    {
        super.fromData(dataMap);
        reservationIds = dataMap.getSet(RESERVATION_IDS, String.class);
        reservationRequestId = dataMap.getString(RESERVATION_REQUEST_ID);
        reservationClasses = (Set) dataMap.getSet(RESERVATION_CLASSES, Class.class);
        technologies = dataMap.getSet(TECHNOLOGIES, Technology.class);
        sort = dataMap.getEnum(SORT, Sort.class);
        sortDescending = dataMap.getBool(SORT_DESCENDING);
    }
}