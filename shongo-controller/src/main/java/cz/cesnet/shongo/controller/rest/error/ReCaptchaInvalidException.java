package cz.cesnet.shongo.controller.rest.error;

public class ReCaptchaInvalidException extends RuntimeException
{

    public ReCaptchaInvalidException(String message)
    {
        super(message);
    }

    public ReCaptchaInvalidException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
