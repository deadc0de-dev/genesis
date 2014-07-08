package dev.deadc0de.genesis;

import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class AbstractServiceFactoryTest {

    private static final String SERVICE_NAME = "service";
    private static final String OTHER_SERVICE_NAME = "other service";

    @Test
    public void serviceFactoryIsNotEqualToNull() {
        final ServiceFactory serviceFactory = new DummyAbstractServiceFactory(SERVICE_NAME, Object.class);
        Assert.assertFalse(serviceFactory.equals(null));
    }

    @Test
    public void serviceFactoriesAreNotEqualWhenServiceNameIsDifferent() {
        final ServiceFactory one = new DummyAbstractServiceFactory(SERVICE_NAME, Object.class);
        final ServiceFactory other = new DummyAbstractServiceFactory(OTHER_SERVICE_NAME, Object.class);
        Assert.assertFalse(one.equals(other));
    }

    @Test
    public void serviceFactoriesAreNotEqualWhenServiceTypeIsDifferent() {
        final ServiceFactory one = new DummyAbstractServiceFactory(SERVICE_NAME, Object.class);
        final ServiceFactory other = new DummyAbstractServiceFactory(SERVICE_NAME, String.class);
        Assert.assertFalse(one.equals(other));
    }

    @Test
    public void serviceFactoriesAreEqualWhenBothServiceNameAndTypeAreEqual() {
        final ServiceFactory one = new DummyAbstractServiceFactory(SERVICE_NAME, Object.class);
        final ServiceFactory other = new DummyAbstractServiceFactory(SERVICE_NAME, Object.class);
        Assert.assertTrue(one.equals(other));
    }

    private static class DummyAbstractServiceFactory extends AbstractServiceFactory {

        public DummyAbstractServiceFactory(String serviceName, Class serviceType) {
            super(serviceName, serviceType);
        }

        @Override
        public Map parameters() {
            throw new UnsupportedOperationException("dummy implementation");
        }

        @Override
        public Map roles() {
            throw new UnsupportedOperationException("dummy implementation");
        }

        @Override
        public Object create(ServiceGenerator serviceGenerator, ServiceDescriptor serviceDescriptor) {
            throw new UnsupportedOperationException("dummy implementation");
        }
    }
}
