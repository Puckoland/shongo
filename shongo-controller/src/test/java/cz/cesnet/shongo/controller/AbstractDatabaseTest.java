package cz.cesnet.shongo.controller;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Abstract database test provides the entity manager to extending classes as protected member variable.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
@SpringBootTest
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class AbstractDatabaseTest
{
    private static Logger logger = LoggerFactory.getLogger(AbstractDatabaseTest.class);

    /**
     * Single instance of entity manager factory.
     */
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    protected ControllerConfiguration configuration;

    /**
     * @return entity manager factory
     */
    protected EntityManagerFactory getEntityManagerFactory()
    {
        synchronized (AbstractControllerTest.class) {
            return entityManagerFactory;
        }
    }

    /**
     * @return entity manager
     */
    protected EntityManager createEntityManager()
    {
        synchronized (AbstractControllerTest.class) {
            return entityManagerFactory.createEntityManager();
        }
    }

    /**
     * Perform tests initialization.
     *
     * @throws Exception
     */
    @Before
    public void before() throws Exception
    {
        synchronized (AbstractControllerTest.class) {
            logger.info("Reusing existing entity manager factory.");
            clearData();
        }
    }

    /**
     * Perform tests clean-up.
     */
    @After
    public void after() throws Exception
    {
        // Do not close entity manager factory to allow re-usage of it for the next test
    }

    /**
     * Clear data in {@link #entityManagerFactory}.
     */
    protected void clearData()
    {
        synchronized (AbstractControllerTest.class) {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            try {
                logger.info("Clearing database data...");
                entityManager.getTransaction().begin();
                entityManager.createNativeQuery(
                        "TRUNCATE SCHEMA PUBLIC RESTART IDENTITY AND COMMIT NO CHECK").executeUpdate();
                entityManager.getTransaction().commit();
                logger.info("Database data cleared.");
            }
            finally {
                entityManager.close();
            }
        }
    }
}
