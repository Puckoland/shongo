package cz.cesnet.shongo.controller;

import cz.cesnet.shongo.util.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Profile("production")
@Configuration
@RequiredArgsConstructor
public class Config
{

    private final ControllerConfiguration configuration;

    @Bean
    public EntityManagerFactory entityManagerFactory()
    {
        log.debug("Creating entity manager factory...");
        Timer timer = new Timer();
        Map<String, String> properties = new HashMap<>();
        properties.put("hibernate.connection.driver_class",
                configuration.getString(ControllerConfiguration.DATABASE_DRIVER));
        properties.put("hibernate.connection.url",
                configuration.getString(ControllerConfiguration.DATABASE_URL));
        properties.put("hibernate.connection.username",
                configuration.getString(ControllerConfiguration.DATABASE_USERNAME));
        properties.put("hibernate.connection.password",
                configuration.getString(ControllerConfiguration.DATABASE_PASSWORD));
        properties.put("hibernate.dialect",
                "cz.cesnet.shongo.controller.util.CustomPostgres10Dialect");
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("controller", properties);
        log.debug("Entity manager factory created in {} ms.", timer.stop());
        return entityManagerFactory;
    }
}
