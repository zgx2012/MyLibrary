package cm.android.framework.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.RemoteException;

import java.util.concurrent.atomic.AtomicBoolean;

public class ServiceBidnerImpl extends cm.android.framework.core.IServiceBinder.Stub {

    private static final Logger logger = LoggerFactory.getLogger("framework");

    private final AtomicBoolean isInitAtomic = new AtomicBoolean(false);

    private final ServiceHolder serviceHolder = new ServiceHolder();

    private IServiceManager serviceManager;

    void initialize() {
        reset();
    }

    void release() {
        destroy();
        reset();
    }

    private void reset() {
        isInitAtomic.set(false);
        serviceHolder.resetService();
        serviceManager = null;
    }

    @Override
    public void initService(IServiceManager serviceManager) {
        if (serviceManager == null) {
            throw new IllegalArgumentException("serviceManger = null");
        }

        if (this.serviceManager == null) {
            this.serviceManager = serviceManager;
        }
    }

    @Override
    public final void create() {
        logger.info("isInit = " + isInitAtomic.get());
        if (!isInitAtomic.compareAndSet(false, true)) {
            return;
        }

        serviceHolder.resetService();
        try {
            serviceManager.onCreate();
        } catch (RemoteException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public final void destroy() {
        logger.info("isInit = " + isInitAtomic.get());
        if (!isInitAtomic.compareAndSet(true, false)) {
            return;
        }

        try {
            serviceManager.onDestroy();
        } catch (RemoteException e) {
            logger.error(e.getMessage(), e);
        }
        serviceHolder.resetService();
    }

    @Override
    public final void addService(String name, IManager manager) {
        serviceHolder.addService(name, manager);
    }

    @Override
    public final IManager getService(String name) {
        return serviceHolder.getService(name);
    }
}
