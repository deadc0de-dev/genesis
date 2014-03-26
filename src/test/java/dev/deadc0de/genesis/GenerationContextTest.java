package dev.deadc0de.genesis;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Test;

public class GenerationContextTest {

    @Test(expected = IllegalArgumentException.class)
    public void providingServiceFactoriesWithSameNameAndTypeThrows() {
        final ServiceFactory<?> one = new DummyServiceFactory<>("name", Object.class);
        final ServiceFactory<?> other = new DummyServiceFactory<>("name", Object.class);
        final GenerationContext unused = new GenerationContext(Stream.of(one, other));
    }

    @Test(expected = IllegalStateException.class)
    public void generatingAServiceWithoutTheAssociatedFactoryThrows() {
        final GenerationContext context = new GenerationContext(Stream.empty());
        final ServiceDescriptor service = new ServiceDescriptor("name", Collections.emptyMap(), Collections.emptyMap());
        context.generate(Object.class, service);
    }

    @Test
    public void passesItselfAsServiceGeneratorToTheFactoryMatchingTheServiceNameAndType() {
        final SpyServiceFactory<Object> factory = new SpyServiceFactory<>("name", Object.class);
        final GenerationContext context = new GenerationContext(Stream.of(factory));
        final ServiceDescriptor service = new ServiceDescriptor("name", Collections.emptyMap(), Collections.emptyMap());
        context.generate(Object.class, service);
        Assert.assertEquals(context, factory.capturedGenerator.get());
    }

    @Test
    public void passesTheGivenServiceDescriptorToTheFactoryMatchingTheServiceNameAndType() {
        final SpyServiceFactory<Object> factory = new SpyServiceFactory<>("name", Object.class);
        final GenerationContext context = new GenerationContext(Stream.of(factory));
        final ServiceDescriptor service = new ServiceDescriptor("name", Collections.emptyMap(), Collections.emptyMap());
        context.generate(Object.class, service);
        Assert.assertEquals(service, factory.capturedDescriptor.get());
    }

    @Test
    public void returnsTheServiceGeneratedByTheFactoryMatchingTheServiceNameAndType() {
        final Object expected = new Object();
        final ServiceFactory<Object> factory = new StubServiceFactory<>(expected, "name", Object.class);
        final GenerationContext context = new GenerationContext(Stream.of(factory));
        final ServiceDescriptor service = new ServiceDescriptor("name", Collections.emptyMap(), Collections.emptyMap());
        final Object generatedService = context.generate(Object.class, service);
        Assert.assertEquals(expected, generatedService);
    }

    private static class DummyServiceFactory<S> extends AbstractServiceFactory<S> {

        public DummyServiceFactory(String serviceName, Class<S> serviceType) {
            super(serviceName, serviceType);
        }

        @Override
        public S create(ServiceGenerator serviceGenerator, ServiceDescriptor service) {
            throw new UnsupportedOperationException("dummy implementation");
        }
    }

    private static class StubServiceFactory<S> extends AbstractServiceFactory<S> {

        private final S service;

        public StubServiceFactory(S service, String serviceName, Class<S> serviceType) {
            super(serviceName, serviceType);
            this.service = service;
        }

        @Override
        public S create(ServiceGenerator serviceGenerator, ServiceDescriptor serviceDescriptor) {
            return service;
        }
    }

    private static class SpyServiceFactory<S> extends AbstractServiceFactory<S> {

        public Optional<ServiceGenerator> capturedGenerator;
        public Optional<ServiceDescriptor> capturedDescriptor;

        public SpyServiceFactory(String serviceName, Class<S> serviceType) {
            super(serviceName, serviceType);
            capturedGenerator = Optional.empty();
            capturedDescriptor = Optional.empty();
        }

        @Override
        public S create(ServiceGenerator serviceGenerator, ServiceDescriptor serviceDescriptor) {
            capturedGenerator = Optional.of(serviceGenerator);
            capturedDescriptor = Optional.of(serviceDescriptor);
            return null;
        }
    }
}
