package cz.cesnet.shongo.controller;

import cz.cesnet.shongo.controller.acl.AclEntry;
import cz.cesnet.shongo.controller.booking.executable.Executable;
import cz.cesnet.shongo.controller.executor.Executor;
import cz.cesnet.shongo.controller.scheduler.Scheduler;

/**
 * This class represents a common lock used in {@link ScheduledWorker} and {@link Executor} to not execute theirs code
 * at the same time (e.g., {@link ScheduledWorker#work} and {@link Executor#execute}).
 * <p/>
 * It is required because we don't want to execute some {@link Executable} for which hasn't been
 * created all {@link AclEntry}s yet. {@link AclEntry}s are created in the end of
 * {@link Scheduler#run} which is executed at the end of {@link ScheduledWorker#work}.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public class ThreadLock
{
    /**
     * No instance can be created.
     */
    private ThreadLock()
    {
    }
}
