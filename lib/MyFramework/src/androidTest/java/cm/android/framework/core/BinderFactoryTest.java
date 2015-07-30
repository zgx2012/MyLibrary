package cm.android.framework.core;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.test.InstrumentationTestCase;

import java.io.FileDescriptor;

public class BinderFactoryTest extends InstrumentationTestCase {

    public void testGetProxy() throws Exception {
        TestManager manager = BinderFactory.getProxy("TestServer", TestManager.class);
        assertEquals(false, manager.get());

//        TestServer testServer = new TestServer();
//        ServiceManager.addService("TestServer", testServer);
//
//        TestManager manager2 = BinderFactory.getProxy("TestServer", TestManager.class);
//        assertEquals(false, manager2.get());
    }

    private static class TestManager implements BinderFactory.IBinderProxy {

        private boolean test;

        @Override
        public void bind(IBinder binder) {
            if (binder != null) {
                test = true;
            } else {
                test = false;
            }
        }

        public boolean get() {
            return test;
        }
    }

    private static class TestServer implements IBinder {

        @Override
        public String getInterfaceDescriptor() throws RemoteException {
            return null;
        }

        @Override
        public boolean pingBinder() {
            return false;
        }

        @Override
        public boolean isBinderAlive() {
            return false;
        }

        @Override
        public IInterface queryLocalInterface(String s) {
            return null;
        }

        @Override
        public void dump(FileDescriptor fileDescriptor, String[] strings) throws RemoteException {

        }

        @Override
        public void dumpAsync(FileDescriptor fileDescriptor, String[] strings)
                throws RemoteException {

        }

        @Override
        public boolean transact(int i, Parcel parcel, Parcel parcel1, int i1)
                throws RemoteException {
            return false;
        }

        @Override
        public void linkToDeath(DeathRecipient deathRecipient, int i) throws RemoteException {

        }

        @Override
        public boolean unlinkToDeath(DeathRecipient deathRecipient, int i) {
            return false;
        }
    }
}