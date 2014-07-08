package dev.deadc0de.genesis;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Test;

public class GenerationContextTest {

    private static final String SERVICE_NAME = "service";
    private static final ServiceDescriptor SERVICE = ServiceDescriptor.notParameterized(SERVICE_NAME);

    @Test(expected = IllegalArgumentException.class)
    public void providingServiceFactoriesWithSameNameAndTypeThrows() {
        final ServiceFactory one = new DummyServiceFactory(SERVICE_NAME, Object.class);
        final ServiceFactory other = new DummyServiceFactory(SERVICE_NAME, Object.class);
        final GenerationContext notCreated = new GenerationContext(Stream.of(one, other));
    }

    @Test(expected = IllegalStateException.class)
    public void generatingAServiceWithoutTheAssociatedFactoryThrows() {
        final GenerationContext context = new GenerationContext(Stream.empty());
        context.generate(Object.class, SERVICE);
    }

    @Test
    public void passesItselfAsServiceGeneratorToTheFactoryMatchingTheServiceNameAndType() {
        final SpyServiceFactory factory = new SpyServiceFactory(SERVICE_NAME, Object.class);
        final GenerationContext context = new GenerationContext(Stream.of(factory));
        context.generate(Object.class, SERVICE);
        Assert.assertEquals(context, factory.capturedGenerator.get());
    }

    @Test
    public void passesTheGivenServiceDescriptorToTheFactoryMatchingTheServiceNameAndType() {
        final SpyServiceFactory factory = new SpyServiceFactory(SERVICE_NAME, Object.class);
        final GenerationContext context = new GenerationContext(Stream.of(factory));
        context.generate(Object.class, SERVICE);
        Assert.assertEquals(SERVICE, factory.capturedDescriptor.get());
    }

    @Test
    public void returnsTheServiceGeneratedByTheFactoryMatchingTheServiceNameAndType() {
        final Object expected = new Object();
        final ServiceFactory factory = new StubServiceFactory(expected, SERVICE_NAME, Object.class);
        final GenerationContext context = new GenerationContext(Stream.of(factory));
        final Object generatedService = context.generate(Object.class, SERVICE);
        Assert.assertEquals(expected, generatedService);
    }

    private static class DummyServiceFactory extends AbstractServiceFactory {

        public DummyServiceFactory(String serviceName, Class serviceType) {
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
        public Object create(ServiceGenerator serviceGenerator, ServiceDescriptor service) {
            throw new UnsupportedOperationException("dummy implementation");
        }
    }

    private static class StubServiceFactory extends AbstractServiceFactory {

        private final Object service;

        public StubServiceFactory(Object service, String serviceName, Class serviceType) {
            super(serviceName, serviceType);
            this.service = service;
        }

        @Override
        public Map parameters() {
            return Collections.emptyMap();
        }

        @Override
        public Map roles() {
            return Collections.emptyMap();
        }

        @Override
        public Object create(ServiceGenerator serviceGenerator, ServiceDescriptor serviceDescriptor) {
            return service;
        }
    }

    private static class SpyServiceFactory extends AbstractServiceFactory {

        public Optional<ServiceGenerator> capturedGenerator;
        public Optional<ServiceDescriptor> capturedDescriptor;

        public SpyServiceFactory(String serviceName, Class serviceType) {
            super(serviceName, serviceType);
            capturedGenerator = Optional.empty();
            capturedDescriptor = Optional.empty();
        }

        @Override
        public Map parameters() {
            return Collections.emptyMap();
        }

        @Override
        public Map roles() {
            return Collections.emptyMap();
        }

        @Override
        public Object create(ServiceGenerator serviceGenerator, ServiceDescriptor serviceDescriptor) {
            capturedGenerator = Optional.of(serviceGenerator);
            capturedDescriptor = Optional.of(serviceDescriptor);
            return null;
        }
    }
}
