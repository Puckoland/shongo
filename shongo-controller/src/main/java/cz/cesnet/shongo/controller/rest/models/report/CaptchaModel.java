package cz.cesnet.shongo.controller.rest.models.report;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaptchaModel
{

    private boolean success;
    @JsonProperty("challenge_ts")
    private String challengeTs;
    private String hostname;
    @JsonProperty("error-codes")
    private ErrorCode[] errorCodes;

    @JsonIgnore
    public boolean hasClientError()
    {
        ErrorCode[] errors = getErrorCodes();
        if (errors == null) {
            return false;
        }
        for (ErrorCode error : errors) {
            switch (error) {
                case INVALID_RESPONSE:
                case MISSING_RESPONSE:
                    return true;
            }
        }
        return false;
    }

    @Getter
    @RequiredArgsConstructor
    enum ErrorCode
    {

        MISSING_SECRET("missing-input-secret"),
        INVALID_SECRET("invalid-input-secret"),
        MISSING_RESPONSE("missing-input-response"),
        INVALID_RESPONSE("invalid-input-response"),
        BAD_REQUEST("bad-request"),
        TIMEOUT_OR_DUPLICATE("timeout-or-duplicate"),
        ;

        private final String code;

        @JsonCreator
        public static ErrorCode forValue(String code)
        {
            for (ErrorCode error : values()) {
                if (error.code.equals(code)) {
                    return error;
                }
            }
            throw new IllegalArgumentException("Unknown error code: " + code);
        }
    }
}
