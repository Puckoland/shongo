package cz.cesnet.shongo.controller.util.converter;

import org.joda.time.Period;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToPeriodConverter implements Converter<String, Period> {

    @Override
    public Period convert(String source)
    {
        return cz.cesnet.shongo.api.Converter.convert(source, Period.class);
    }
}
