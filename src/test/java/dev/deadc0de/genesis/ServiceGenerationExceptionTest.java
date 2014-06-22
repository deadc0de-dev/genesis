package dev.deadc0de.genesis;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

public class ServiceGenerationExceptionTest {

    @Test
    public void whenCauseIsNotServiceGenerationExceptionThenTheGenerationStackContainsOnlyTheSourceServiceFactory() {
        final ServiceFactory<?> serviceFactory = new DummyServiceFactory();
        final ServiceGenerationException exception = new ServiceGenerationException(serviceFactory, new Throwable());
        Assert.assertEquals(Collections.singletonList(serviceFactory), exception.serviceGenerationStack());
    }

    @Test
    public void whenCauseIsServiceGenerationExceptionThenTheGenerationStackContainsTheServiceFactoriesOrderedFromInnerToOuter() {
        final ServiceFactory<?> outerServiceFactory = new DummyServiceFactory();
        final ServiceFactory<?> innerServiceFactory = new DummyServiceFactory();
        final ServiceGenerationException exception = new ServiceGenerationException(outerServiceFactory, new ServiceGenerationException(innerServiceFactory, new Throwable()));
        Assert.assertEquals(Arrays.asList(innerServiceFactory, outerServiceFactory), exception.serviceGenerationStack());
    }

    private static class DummyServiceFactory implements ServiceFactory {

        @Override
        public String serviceName() {
            throw new UnsupportedOperationException("dummy implementation");
        }

        @Override
        public Class serviceType() {
            throw new UnsupportedOperationException("dummy implementation");
        }

        @Override
        public Object create(ServiceGenerator serviceGenerator, ServiceDescriptor serviceDescriptor) {
            throw new UnsupportedOperationException("dummy implementation");
        }
    }
}
