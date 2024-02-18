package cz.cesnet.shongo.controller;

import cz.cesnet.shongo.util.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@ComponentScan
@ActiveProfiles("test")
@RequiredArgsConstructor
public class TestConfig
{

    private static final String CONNECTION_DRIVER = "org.hsqldb.jdbcDriver";
    private static final String CONNECTION_URL = "jdbc:hsqldb:mem:test; shutdown=true; sql.syntax_pgs=true;";

    private final ControllerConfiguration configuration;

    @Bean
    public EntityManagerFactory entityManagerFactory()
    {
        // For testing purposes use only in-memory database
        Map<String, String> properties = new HashMap<>();
        properties.put("hibernate.connection.driver_class", CONNECTION_DRIVER);
        properties.put("hibernate.connection.url", CONNECTION_URL);
        properties.put("hibernate.connection.username", "sa");
        properties.put("hibernate.connection.password", "");

        log.info("Creating entity manager factory...");
        Timer timer = new Timer();
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("controller", properties);
        log.info("Entity manager factory created in {} ms.", timer.stop());

        Controller.initializeDatabase(entityManagerFactory);

        return entityManagerFactory;
    }
}
