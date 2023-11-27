package cz.cesnet.shongo.controller.rest.config;

import cz.cesnet.shongo.controller.ControllerConfiguration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
//@RequiredArgsConstructor
public class CaptchaConfig
{

//    private final ControllerConfiguration configuration;

    private final String site = "https://www.google.com/recaptcha/api";
    private String secret;

    public CaptchaConfig()
    {
//        this.configuration = configuration;
//        this.secret = configuration.getReCaptchaPrivateKey();
    }
}
