package dev.deadc0de.genesis;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Test;

public class GenerationContextTest {

    @Test(expected = IllegalArgumentException.class)
    public void providingServiceFactoriesWithSameNameAndTypeThrows() {
        final ServiceFactory one = new DummyServiceFactory("service", Object.class);
        final ServiceFactory other = new DummyServiceFactory("service", Object.class);
        final GenerationContext notCreated = new GenerationContext(Stream.of(one, other));
    }

    @Test(expected = IllegalStateException.class)
    public void generatingAServiceWithoutTheAssociatedFactoryThrows() {
        final GenerationContext context = new GenerationContext(Stream.empty());
        final ServiceDescriptor service = new ServiceDescriptor("service", Collections.emptyMap(), Collections.emptyMap());
        context.generate(Object.class, service);
    }

    @Test
    public void passesItselfAsServiceGeneratorToTheFactoryMatchingTheServiceNameAndType() {
        final SpyServiceFactory factory = new SpyServiceFactory("service", Object.class);
        final GenerationContext context = new GenerationContext(Stream.of(factory));
        final ServiceDescriptor service = new ServiceDescriptor("service", Collections.emptyMap(), Collections.emptyMap());
        context.generate(Object.class, service);
        Assert.assertEquals(context, factory.capturedGenerator.get());
    }

    @Test
    public void passesTheGivenServiceDescriptorToTheFactoryMatchingTheServiceNameAndType() {
        final SpyServiceFactory factory = new SpyServiceFactory("service", Object.class);
        final GenerationContext context = new GenerationContext(Stream.of(factory));
        final ServiceDescriptor service = new ServiceDescriptor("service", Collections.emptyMap(), Collections.emptyMap());
        context.generate(Object.class, service);
        Assert.assertEquals(service, factory.capturedDescriptor.get());
    }

    @Test
    public void returnsTheServiceGeneratedByTheFactoryMatchingTheServiceNameAndType() {
        final Object expected = new Object();
        final ServiceFactory factory = new StubServiceFactory(expected, "service", Object.class);
        final GenerationContext context = new GenerationContext(Stream.of(factory));
        final ServiceDescriptor service = new ServiceDescriptor("service", Collections.emptyMap(), Collections.emptyMap());
        final Object generatedService = context.generate(Object.class, service);
        Assert.assertEquals(expected, generatedService);
    }

    private static class DummyServiceFactory extends AbstractServiceFactory {

        public DummyServiceFactory(String serviceName, Class serviceType) {
            super(serviceName, serviceType);
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
        public Object create(ServiceGenerator serviceGenerator, ServiceDescriptor serviceDescriptor) {
            capturedGenerator = Optional.of(serviceGenerator);
            capturedDescriptor = Optional.of(serviceDescriptor);
            return null;
        }
    }
}
