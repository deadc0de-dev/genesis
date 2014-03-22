package dev.deadc0de.genesis;

import java.util.Collections;
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
        final CapturingServiceFactory<Object> factory = new CapturingServiceFactory<>("name", Object.class);
        final GenerationContext context = new GenerationContext(Stream.of(factory));
        final ServiceDescriptor service = new ServiceDescriptor("name", Collections.emptyMap(), Collections.emptyMap());
        context.generate(Object.class, service);
        Assert.assertEquals(context, factory.capturedGenerator().get());
    }

    @Test
    public void passesTheGivenServiceDescriptorToTheFactoryMatchingTheServiceNameAndType() {
        final CapturingServiceFactory<Object> factory = new CapturingServiceFactory<>("name", Object.class);
        final GenerationContext context = new GenerationContext(Stream.of(factory));
        final ServiceDescriptor service = new ServiceDescriptor("name", Collections.emptyMap(), Collections.emptyMap());
        context.generate(Object.class, service);
        Assert.assertEquals(service, factory.capturedDescriptor().get());
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
}
