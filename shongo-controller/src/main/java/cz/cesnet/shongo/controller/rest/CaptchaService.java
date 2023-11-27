package cz.cesnet.shongo.controller.rest;

import cz.cesnet.shongo.controller.rest.config.CaptchaConfig;
import cz.cesnet.shongo.controller.rest.error.ReCaptchaInvalidException;
import cz.cesnet.shongo.controller.rest.models.report.CaptchaModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CaptchaService
{

    private static final Pattern RESPONSE_PATTERN = Pattern.compile("[A-Za-z0-9_-]+");

    private final CaptchaConfig captchaSettings;
    private RestOperations restTemplate = new RestTemplate();

    public void processResponse(String response)
    {
        if (!responseSanityCheck(response)) {
            throw new ReCaptchaInvalidException("Response contains invalid characters");
        }

        URI verifyUri = URI.create(String.format(
                "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s",
                captchaSettings.getSecret(), response));

        CaptchaModel googleResponse = restTemplate.getForObject(verifyUri, CaptchaModel.class);

        assert googleResponse != null;
        if (!googleResponse.isSuccess()) {
            throw new ReCaptchaInvalidException("reCaptcha was not successfully validated");
        }
    }

    private boolean responseSanityCheck(String response)
    {
        return StringUtils.hasLength(response) && RESPONSE_PATTERN.matcher(response).matches();
    }
}
