package cz.cesnet.shongo.controller.rest.models.reservationrequest;

import cz.cesnet.shongo.controller.api.AllocationState;
import cz.cesnet.shongo.controller.api.ReservationRequestSummary;
import cz.cesnet.shongo.controller.api.ReservationRequestType;
import lombok.Data;
import org.joda.time.DateTime;

/**
 * Represents reservation request's history.
 *
 * @author Filip Karnis
 */
@Data
public class ReservationRequestHistoryModel {

    private String id;
    private DateTime createdAt;
    private String createdBy;
    private ReservationRequestType type;
    private AllocationState allocationState;
    private ReservationRequestState state;

    public ReservationRequestHistoryModel(ReservationRequestSummary summary) {
        this.id = summary.getId();
        this.createdAt = summary.getDateTime();
        this.createdBy = summary.getUserId();
        this.type = summary.getType();
        this.allocationState = summary.getAllocationState();
        this.state = ReservationRequestState.fromApi(summary);
    }
}
