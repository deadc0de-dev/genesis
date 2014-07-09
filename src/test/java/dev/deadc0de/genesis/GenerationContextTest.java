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
    public void whenServiceFactoriesWithSameNameAndTypeAreProvidedInConstructionThenThrows() {
        final ServiceFactory one = new DummyServiceFactory(SERVICE_NAME, Object.class);
        final ServiceFactory other = new DummyServiceFactory(SERVICE_NAME, Object.class);
        final GenerationContext notCreated = new GenerationContext(Stream.of(one, other));
    }

    @Test(expected = IllegalStateException.class)
    public void whenGeneratingAServiceWithoutAFactoryOfACompatibleSubtypeThenThrows() {
        final ServiceFactory integerFactory = new DummyServiceFactory(SERVICE_NAME, Integer.class);
        final GenerationContext context = new GenerationContext(Stream.of(integerFactory));
        context.generate(Double.class, SERVICE);
    }

    @Test(expected = IllegalStateException.class)
    public void whenMoreThanOneServiceFactoryTypeIsACompatibleSubtypeThenThrows() {
        final ServiceFactory integerFactory = new DummyServiceFactory(SERVICE_NAME, Integer.class);
        final ServiceFactory doubleFactory = new DummyServiceFactory(SERVICE_NAME, Double.class);
        final GenerationContext context = new GenerationContext(Stream.of(integerFactory, doubleFactory));
        context.generate(Number.class, SERVICE);
    }

    @Test
    public void returnsTheServiceGeneratedByTheFactoryMatchingTheServiceName() {
        final Object expected = new Object();
        final ServiceFactory factory = new StubServiceFactory(expected, SERVICE_NAME, Object.class);
        final GenerationContext context = new GenerationContext(Stream.of(factory));
        final Object generatedService = context.generate(Object.class, SERVICE);
        Assert.assertEquals(expected, generatedService);
    }

    @Test
    public void returnsTheServiceGeneratedByTheFactoryWithTheWidestSubtype() {
        final Object wrong = new Object();
        final Object expected = new Object();
        final ServiceFactory objectFactory = new StubServiceFactory(wrong, SERVICE_NAME, Object.class);
        final ServiceFactory numberFactory = new StubServiceFactory(wrong, SERVICE_NAME, Number.class);
        final ServiceFactory integerFactory = new StubServiceFactory(expected, SERVICE_NAME, Integer.class);
        final GenerationContext context = new GenerationContext(Stream.of(numberFactory, integerFactory, objectFactory));
        final Object generatedService = context.generate(Object.class, SERVICE);
        Assert.assertEquals(expected, generatedService);
    }

    @Test
    public void passesItselfAsServiceGeneratorToTheChosenFactory() {
        final SpyServiceFactory factory = new SpyServiceFactory(SERVICE_NAME, Object.class);
        final GenerationContext context = new GenerationContext(Stream.of(factory));
        context.generate(Object.class, SERVICE);
        Assert.assertEquals(context, factory.capturedGenerator.get());
    }

    @Test
    public void passesTheGivenServiceDescriptorToTheChosenFactory() {
        final SpyServiceFactory factory = new SpyServiceFactory(SERVICE_NAME, Object.class);
        final GenerationContext context = new GenerationContext(Stream.of(factory));
        context.generate(Object.class, SERVICE);
        Assert.assertEquals(SERVICE, factory.capturedDescriptor.get());
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
