package cz.cesnet.shongo.controller;

import cz.cesnet.shongo.Temporal;
import cz.cesnet.shongo.controller.calendar.CalendarManager;
import cz.cesnet.shongo.controller.notification.NotificationManager;
import cz.cesnet.shongo.controller.scheduler.Preprocessor;
import cz.cesnet.shongo.controller.scheduler.Scheduler;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Class which periodically runs {@link Preprocessor} and {@link Scheduler}.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
@Component
@Profile("production")
public class ScheduledWorker
{

    private static Logger logger = LoggerFactory.getLogger(ScheduledWorker.class);

    /**
     * Length of working interval which stars at "now()".
     */
    @Value("${configuration.worker.lookahead}")
    private Period lookahead;

    /**
     * @see Preprocessor
     */
    private Preprocessor preprocessor;

    /**
     * @see Scheduler
     */
    private Scheduler scheduler;

    /**
     * @see NotificationManager
     */
    private NotificationManager notificationManager;

    private CalendarManager calendarManager;

    /**
     * {@link EntityManagerFactory} for {@link Preprocessor} and {@link Scheduler}.
     */
    private EntityManagerFactory entityManagerFactory;

    /**
     * {@link DateTime} when the last cache clearing was performed.
     */
    private DateTime clearCacheDateTime;

    /**
     * Constructor.
     *
     * @param preprocessor         sets the {@link #preprocessor}
     * @param scheduler            sets the {@link #scheduler}
     * @param notificationManager  sets the {@link #notificationManager}
     * @param entityManagerFactory sets the {@link #entityManagerFactory}
     */
    public ScheduledWorker(Preprocessor preprocessor, Scheduler scheduler, NotificationManager notificationManager,
                           CalendarManager calendarManager, EntityManagerFactory entityManagerFactory)
    {
        if (preprocessor == null || scheduler == null) {
            throw new IllegalArgumentException("Preprocessor, Scheduler and EntityManagerFactory must be not-empty!");
        }
        this.preprocessor = preprocessor;
        this.scheduler = scheduler;
        this.notificationManager = notificationManager;
        this.calendarManager = calendarManager;
        this.entityManagerFactory = entityManagerFactory;
    }

    @PostConstruct
    public void init()
    {
        logger.debug("Worker started!");
    }

    @PreDestroy
    public void destroy()
    {
        executeRemainingNotifications();

        logger.debug("Worker stopped!");
    }

    @Scheduled(fixedRateString = "${configuration.worker.period}", initialDelayString = "${configuration.worker.period}")
    public void runScheduled()
    {
        work();
    }

    /**
     * Run {@link Preprocessor} and {@link Scheduler}.
     */
    private void work()
    {
        // Globally synchronized (see ThreadLock documentation)
        //logger.debug("Worker waiting for lock...........................");
        synchronized (ThreadLock.class) {
            //logger.debug("Worker lock acquired...   [[[[[")

            // We want to pre-process and schedule only reservation requests in specific interval
            Interval interval = new Interval(Temporal.nowRoundedToSeconds(), lookahead);

            EntityManager entityManager = entityManagerFactory.createEntityManager();
            EntityManager bypassEntityManager = entityManagerFactory.createEntityManager();
            Reporter reporter = Reporter.getInstance();
            try {
                // Run preprocessor, scheduler and notifications
                preprocessor.run(interval, entityManager);
                scheduler.run(interval, entityManager, bypassEntityManager);
                notificationManager.executeNotifications(entityManager);
                calendarManager.sendCalendarNotifications(entityManager);

                // Clear reporter cache once per hour
                DateTime clearCacheDateTime = Temporal.nowRoundedToHours();
                if (!clearCacheDateTime.equals(this.clearCacheDateTime)) {
                    reporter.clearCache(interval.getStart());
                    this.clearCacheDateTime = clearCacheDateTime;
                }
            }
            catch (Exception exception) {
                reporter.reportInternalError(Reporter.WORKER, exception);
            }
            finally {
                entityManager.close();
                bypassEntityManager.close();
            }

            //logger.debug("Worker releasing lock...  ]]]]]");
        }
        //logger.debug("Worker lock released...");
    }

    private void executeRemainingNotifications()
    {
        if (notificationManager.hasNotifications()) {
            logger.info("Executing remaining notifications...");
            try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
                notificationManager.executeNotifications(entityManager);
            } catch (Exception exception) {
                Reporter.getInstance().reportInternalError(Reporter.WORKER, exception);
            }
        }
    }
}
