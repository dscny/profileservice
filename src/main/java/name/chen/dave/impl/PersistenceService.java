/**
 * Persist the histogram to disk. Periodically poll the histogram, detect whether it had changes, and
 * persist to disk.
 *
 */
package name.chen.dave.impl;

import name.chen.dave.api.DiskPersistable;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class PersistenceService {

    // assumes the current class is called MyLogger
    private final static Logger LOGGER = Logger.getLogger(PersistenceService.class.getName());

    private final ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
    private final int secondsInterval;
    private ScheduledFuture<?> scheduledFuture;
    private DiskPersistable diskPersist;
    private boolean newChanges = false;

    Runnable persistTask = () -> {
        try {
            synchronized (this) {
                if (newChanges) {
                    LOGGER.info("Persisting to local disk");
                    diskPersist.persistToLocalDisk();
                    newChanges = false;
                } else {
                    LOGGER.fine("No changes since last check. No need to persist to disk");
                }
            }
        } catch (IOException e) {
            LOGGER.warning("Error in persisting Median DB to disk");
        }
    };

    PersistenceService(DiskPersistable persistable, int secondsInterval) {
        this.diskPersist = persistable;
        this.secondsInterval = secondsInterval;
    }

    public synchronized void indicateNewChanges() {
        this.newChanges = true;
    }

    public void start() {
        if (scheduledFuture == null) {
            scheduledFuture = ses.scheduleAtFixedRate(persistTask, secondsInterval, secondsInterval, TimeUnit.SECONDS);
            LOGGER.info("Persistence service started");
        } else {
            LOGGER.warning("Not starting Persistence service, as it is already started");
        }
    }

    public void stop() {
        LOGGER.info("Stopping persistence service");
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
            scheduledFuture = null;
        } else {
            LOGGER.info("Persistence service not running");
        }
    }
}