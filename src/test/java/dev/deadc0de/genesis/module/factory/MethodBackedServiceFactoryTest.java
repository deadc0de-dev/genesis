package dev.deadc0de.genesis.module.factory;

import dev.deadc0de.genesis.ServiceDescriptor;
import dev.deadc0de.genesis.ServiceFactory;
import dev.deadc0de.genesis.ServiceGenerator;
import java.lang.reflect.Method;
import java.util.function.BiFunction;
import org.junit.Assert;
import org.junit.Test;

public class MethodBackedServiceFactoryTest {

    @Test
    public void factoryNameIsTheMethodName() throws NoSuchMethodException {
        final Method method = TestModule.class.getDeclaredMethod("service");
        final ServiceFactory serviceFactory = new MethodBackedServiceFactory(new TestModule(), method, new DummyArgumentResolverFactory());
        Assert.assertEquals("service", serviceFactory.serviceName());
    }

    @Test
    public void factoryReturnTypeIsTheMethodReturnType() throws NoSuchMethodException {
        final Method method = TestModule.class.getDeclaredMethod("service");
        final ServiceFactory serviceFactory = new MethodBackedServiceFactory(new TestModule(), method, new DummyArgumentResolverFactory());
        Assert.assertEquals(Void.TYPE, serviceFactory.serviceType());
    }

    @Test
    public void factoryPassesMethodParametersToArgumentResolverFactoryInConstruction() throws NoSuchMethodException {
        final Method method = TestModule.class.getDeclaredMethod("withMethodParameter", Object.class);
        final SpyArgumentResolverFactory argumentResolverFactory = new SpyArgumentResolverFactory();
        final ServiceFactory unused = new MethodBackedServiceFactory(new TestModule(), method, argumentResolverFactory);
        Assert.assertEquals(method.getParameters()[0], argumentResolverFactory.capturedMethodParameter);
    }

    @Test
    public void factoryUsesTheArgumentResolversProvidedByTheArgumentResolverFactoryToGenerateArgumentsForTheMethod() throws NoSuchMethodException {
        final Method method = TestModule.class.getDeclaredMethod("identity", Object.class);
        final Object service = new Object();
        final ServiceFactory serviceFactory = new MethodBackedServiceFactory(new TestModule(), method, new StubArgumentResolverFactory((serviceGenerator, serviceDescriptor) -> service));
        final Object generatedService = serviceFactory.create(new DummyServiceGenerator(), ServiceDescriptor.notParameterized("identity"));
        Assert.assertEquals(service, generatedService);
    }

    @Test
    public void factoryReturnsTheServiceCreatedByTheModuleMethod() throws NoSuchMethodException {
        final Method method = ModuleWithInjectedObject.class.getDeclaredMethod("service");
        final Object service = new Object();
        final ServiceFactory serviceFactory = new MethodBackedServiceFactory(new ModuleWithInjectedObject(service), method, new DummyArgumentResolverFactory());
        Assert.assertEquals(service, serviceFactory.create(new DummyServiceGenerator(), ServiceDescriptor.notParameterized("service")));
    }

    private static class TestModule {

        public void service() {
        }

        public void withMethodParameter(Object methodParameter) {
        }

        public Object identity(Object service) {
            return service;
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

    private static class DummyServiceGenerator implements ServiceGenerator {

        @Override
        public <S> S generate(Class<S> serviceType, ServiceDescriptor serviceDescriptor) {
            throw new UnsupportedOperationException("dummy implementation");
        }
    }

    private static class DummyArgumentResolverFactory implements ArgumentResolverFactory {

        @Override
        public BiFunction<ServiceGenerator, ServiceDescriptor, Object> createArgumentResolver(java.lang.reflect.Parameter methodParameter) {
            throw new UnsupportedOperationException("dummy implementation");
        }
    }

    private static class SpyArgumentResolverFactory implements ArgumentResolverFactory {

        public java.lang.reflect.Parameter capturedMethodParameter;

        @Override
        public BiFunction<ServiceGenerator, ServiceDescriptor, Object> createArgumentResolver(java.lang.reflect.Parameter methodParameter) {
            capturedMethodParameter = methodParameter;
            return null;
        }
    }

    private static class StubArgumentResolverFactory implements ArgumentResolverFactory {

        private final BiFunction<ServiceGenerator, ServiceDescriptor, Object> argumentResolver;

        public StubArgumentResolverFactory(BiFunction<ServiceGenerator, ServiceDescriptor, Object> argumentResolver) {
            this.argumentResolver = argumentResolver;
        }

        @Override
        public BiFunction<ServiceGenerator, ServiceDescriptor, Object> createArgumentResolver(java.lang.reflect.Parameter methodParameter) {
            return argumentResolver;
        }
    }
}
