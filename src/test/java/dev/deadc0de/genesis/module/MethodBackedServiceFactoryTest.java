package dev.deadc0de.genesis.module;

import dev.deadc0de.genesis.ServiceDescriptor;
import dev.deadc0de.genesis.ServiceFactory;
import dev.deadc0de.genesis.ServiceGenerator;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;

public class MethodBackedServiceFactoryTest {

    @Test
    public void factoryNameIsTheMethodName() throws NoSuchMethodException {
        final Method method = StubModule.class.getDeclaredMethod("stringService");
        final ServiceFactory factory = new MethodBackedServiceFactory(new StubModule(), method);
        Assert.assertEquals("stringService", factory.serviceName());
    }

    @Test
    public void factoryReturnTypeIsTheMethodReturnType() throws NoSuchMethodException {
        final Method method = StubModule.class.getDeclaredMethod("stringService");
        final ServiceFactory factory = new MethodBackedServiceFactory(new StubModule(), method);
    }

    @Test
    public void canAssembleFactoriesFromMethodsWithVoidReturnType() throws NoSuchMethodException {
        final Method method = StubModule.class.getDeclaredMethod("voidService");
        final ServiceFactory factory = new MethodBackedServiceFactory(new StubModule(), method);
        Assert.assertEquals(Void.TYPE, factory.serviceType());
    }

    @Test
    public void factoryReturnsTheServiceCreatedByTheModuleMethod() throws NoSuchMethodException {
        final Method method = ModuleWithInjectedObject.class.getDeclaredMethod("service");
        final Object service = new Object();
        final ServiceFactory factory = new MethodBackedServiceFactory(new ModuleWithInjectedObject(service), method);
        final ServiceDescriptor serviceDescriptor = new ServiceDescriptor("service", Collections.emptyMap(), Collections.emptyMap());
        Assert.assertEquals(service, factory.create(new DummyServiceGenerator(), serviceDescriptor));
    }

    @Test
    public void whenMethodParameterHasServiceGeneratorTypeTheOnePassedToTheFactoryIsUsedAsArgument() throws NoSuchMethodException {
        final SpyModule module = new SpyModule();
        final Method method = SpyModule.class.getDeclaredMethod("spy", ServiceGenerator.class, String.class, Object.class);
        final ServiceFactory factory = new MethodBackedServiceFactory(module, method);
        final ServiceGenerator serviceGenerator = new StubServiceGenerator();
        final ServiceDescriptor serviceDescriptor = new ServiceDescriptor("spy",
                Collections.singletonMap("parameter", "argument"),
                Collections.singletonMap("role", new ServiceDescriptor("collaborator",
                                Collections.emptyMap(),
                                Collections.emptyMap())));
        factory.create(serviceGenerator, serviceDescriptor);
        Assert.assertEquals(serviceGenerator, module.serviceGenerator.get());
    }

    @Test
    public void whenMethodParameterHasParameterAnnotationTheValueFromConfigurationMapIsUsedAsArgument() throws NoSuchMethodException {
        final SpyModule module = new SpyModule();
        final Method method = SpyModule.class.getDeclaredMethod("spy", ServiceGenerator.class, String.class, Object.class);
        final ServiceFactory factory = new MethodBackedServiceFactory(module, method);
        final ServiceGenerator serviceGenerator = new StubServiceGenerator();
        final ServiceDescriptor serviceDescriptor = new ServiceDescriptor("spy",
                Collections.singletonMap("parameter", "argument"),
                Collections.singletonMap("role", new ServiceDescriptor("collaborator",
                                Collections.emptyMap(),
                                Collections.emptyMap())));
        factory.create(serviceGenerator, serviceDescriptor);
        Assert.assertEquals("argument", module.parameter.get());
    }

    @Test(expected = IllegalStateException.class)
    public void assembledFactoryThrowsWhenMethodParameterHasParameterAnnotationButValueIsNotPresentInConfigurationMap() throws NoSuchMethodException {
        final SpyModule module = new SpyModule();
        final Method method = SpyModule.class.getDeclaredMethod("spy", ServiceGenerator.class, String.class, Object.class);
        final ServiceFactory factory = new MethodBackedServiceFactory(module, method);
        final ServiceGenerator serviceGenerator = new StubServiceGenerator();
        final ServiceDescriptor serviceDescriptor = new ServiceDescriptor("spy",
                Collections.emptyMap(),
                Collections.singletonMap("role", new ServiceDescriptor("collaborator",
                                Collections.emptyMap(),
                                Collections.emptyMap())));
        factory.create(serviceGenerator, serviceDescriptor);
    }

    @Test
    public void whenMethodParameterHasRoleAnnotationTheServiceDescriptorFromCollaboratorsMapIsPassedToServiceGenerator() throws NoSuchMethodException {
        final SpyModule module = new SpyModule();
        final Method method = SpyModule.class.getDeclaredMethod("spy", ServiceGenerator.class, String.class, Object.class);
        final ServiceFactory factory = new MethodBackedServiceFactory(module, method);
        final SpyServiceGenerator serviceGenerator = new SpyServiceGenerator();
        final ServiceDescriptor collaboratorDescriptor = new ServiceDescriptor("collaborator",
                Collections.emptyMap(),
                Collections.emptyMap());
        final ServiceDescriptor serviceDescriptor = new ServiceDescriptor("spy",
                Collections.singletonMap("parameter", "argument"),
                Collections.singletonMap("role", collaboratorDescriptor));
        factory.create(serviceGenerator, serviceDescriptor);
        Assert.assertEquals(collaboratorDescriptor, serviceGenerator.serviceDescriptor.get());
    }

    @Test(expected = IllegalStateException.class)
    public void assembledFactoryThrowsWhenMethodParameterHasRoleAnnotationButEntryIsNotPresentInCollaboratorsMap() throws NoSuchMethodException {
        final SpyModule module = new SpyModule();
        final Method method = SpyModule.class.getDeclaredMethod("spy", ServiceGenerator.class, String.class, Object.class);
        final ServiceFactory factory = new MethodBackedServiceFactory(module, method);
        final ServiceGenerator serviceGenerator = new StubServiceGenerator();
        final ServiceDescriptor serviceDescriptor = new ServiceDescriptor("spy",
                Collections.singletonMap("parameter", "argument"),
                Collections.emptyMap());
        factory.create(serviceGenerator, serviceDescriptor);
    }

    private static class StubModule {

        public String stringService() {
            return "string";
        }

        public void voidService() {
        }
    }

    private static class ModuleWithInjectedObject {

        private final Object service;

        public ModuleWithInjectedObject(Object service) {
            this.service = service;
        }

        public Object service() {
            return service;
        }
    }

    private static class SpyModule {

        public Optional<ServiceGenerator> serviceGenerator = Optional.empty();
        public Optional<String> parameter = Optional.empty();
        public Optional<Object> collaborator = Optional.empty();

        public Object spy(
                ServiceGenerator serviceGenerator,
                @Parameter("parameter") String parameter,
                @Role("role") Object collaborator
        ) {
            this.serviceGenerator = Optional.of(serviceGenerator);
            this.parameter = Optional.of(parameter);
            this.collaborator = Optional.of(collaborator);
            return new Object();
        }
    }

    private static class DummyServiceGenerator implements ServiceGenerator {

        @Override
        public <S> S generate(Class<S> serviceType, ServiceDescriptor serviceDescriptor) {
            throw new UnsupportedOperationException("dummy implementation");
        }
    }

    private static class StubServiceGenerator implements ServiceGenerator {

        @Override
        public <S> S generate(Class<S> serviceType, ServiceDescriptor serviceDescriptor) {
            try {
                return serviceType.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                throw new IllegalArgumentException("cannot instantiate service without a default constructor", ex);
            }
        }
    }

    private static class SpyServiceGenerator extends StubServiceGenerator {

        public Optional<Class<?>> serviceType = Optional.empty();
        public Optional<ServiceDescriptor> serviceDescriptor = Optional.empty();

        @Override
        public <S> S generate(Class<S> serviceType, ServiceDescriptor serviceDescriptor) {
            this.serviceType = Optional.of(serviceType);
            this.serviceDescriptor = Optional.of(serviceDescriptor);
            return super.generate(serviceType, serviceDescriptor);
        }
    }
}
