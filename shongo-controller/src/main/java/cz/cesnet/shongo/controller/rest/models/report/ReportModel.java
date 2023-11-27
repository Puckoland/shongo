package cz.cesnet.shongo.controller.rest.models.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents a report problem model.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 * @author Filip Karnis
 */
@Slf4j
@Data
public class ReportModel
{

    private String email;

    private String message;

    private String emailSubject = "Problem report";

    /**
     * Meta information about the report.
     */
    private MetaModel meta;

    @JsonProperty("g-recaptcha-response")
    private String recaptchaResponse;

    public String getEmailContent()
    {
        return "From: " + getEmail() + "\n\n" +
                message + "\n\n" +
                "--------------------------------------------------------------------------------\n\n" +
                meta;
    }
}
