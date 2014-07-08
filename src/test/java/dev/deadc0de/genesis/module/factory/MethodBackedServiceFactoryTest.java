package dev.deadc0de.genesis.module.factory;

import dev.deadc0de.genesis.ServiceDescriptor;
import dev.deadc0de.genesis.ServiceFactory;
import dev.deadc0de.genesis.ServiceGenerator;
import dev.deadc0de.genesis.module.Default;
import dev.deadc0de.genesis.module.Parameter;
import dev.deadc0de.genesis.module.Role;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    public void factoryParametersAreTheMethodParametersAnnotatedWithParameter() throws NoSuchMethodException {
        final Method method = TestModule.class.getDeclaredMethod("methodWithParameter", Object.class);
        final ServiceFactory serviceFactory = new MethodBackedServiceFactory(new TestModule(), method, new StubArgumentResolverFactory((serviceGenerator, serviceDescriptor) -> new Object()));
        final Map<String, Optional<List<String>>> expectedParameters = Collections.singletonMap(TestModule.PARAMETER_NAME, Optional.empty());
        Assert.assertEquals(expectedParameters, serviceFactory.parameters());
    }

    @Test
    public void factoryParametersAreNotTheMethodParametersNotAnnotatedWithParameter() throws NoSuchMethodException {
        final Method method = TestModule.class.getDeclaredMethod("methodWithCollaborator", Object.class);
        final ServiceFactory serviceFactory = new MethodBackedServiceFactory(new TestModule(), method, new StubArgumentResolverFactory((serviceGenerator, serviceDescriptor) -> new Object()));
        final Map<String, Optional<List<String>>> expectedParameters = Collections.emptyMap();
        Assert.assertEquals(expectedParameters, serviceFactory.parameters());
    }

    @Test
    public void factoryParametersHaveDefaultValuesIfDefaultAnnotationIsPresent() throws NoSuchMethodException {
        final Method method = TestModule.class.getDeclaredMethod("methodWithParameterAndDefault", Object.class);
        final ServiceFactory serviceFactory = new MethodBackedServiceFactory(new TestModule(), method, new StubArgumentResolverFactory((serviceGenerator, serviceDescriptor) -> new Object()));
        final Map<String, Optional<List<String>>> expectedParameters = Collections.singletonMap(TestModule.PARAMETER_NAME, Optional.of(Arrays.asList(TestModule.DEFAULT_VALUE)));
        Assert.assertEquals(expectedParameters, serviceFactory.parameters());
    }

    @Test
    public void factoryRolesAreTheMethodParametersAnnotatedWithRole() throws NoSuchMethodException {
        final Method method = TestModule.class.getDeclaredMethod("methodWithCollaborator", Object.class);
        final ServiceFactory serviceFactory = new MethodBackedServiceFactory(new TestModule(), method, new StubArgumentResolverFactory((serviceGenerator, serviceDescriptor) -> new Object()));
        final Map<String, Map.Entry<Class<?>, Optional<List<String>>>> expectedRoles = Collections.singletonMap(TestModule.ROLE_NAME, new AbstractMap.SimpleImmutableEntry(Object.class, Optional.empty()));
        Assert.assertEquals(expectedRoles, serviceFactory.roles());
    }

    @Test
    public void factoryRolesAreNotTheMethodParametersNotAnnotatedWithRole() throws NoSuchMethodException {
        final Method method = TestModule.class.getDeclaredMethod("methodWithParameter", Object.class);
        final ServiceFactory serviceFactory = new MethodBackedServiceFactory(new TestModule(), method, new StubArgumentResolverFactory((serviceGenerator, serviceDescriptor) -> new Object()));
        final Map<String, Optional<List<String>>> expectedParameters = Collections.emptyMap();
        Assert.assertEquals(expectedParameters, serviceFactory.roles());
    }

    @Test
    public void factoryRolesHaveDefaultValuesIfDefaultAnnotationIsPresent() throws NoSuchMethodException {
        final Method method = TestModule.class.getDeclaredMethod("methodWithCollaboratorAndDefault", Object.class);
        final ServiceFactory serviceFactory = new MethodBackedServiceFactory(new TestModule(), method, new StubArgumentResolverFactory((serviceGenerator, serviceDescriptor) -> new Object()));
        final Map<String, Map.Entry<Class<?>, Optional<List<String>>>> expectedRoles = Collections.singletonMap(TestModule.ROLE_NAME, new AbstractMap.SimpleImmutableEntry(Object.class, Optional.of(Arrays.asList(TestModule.DEFAULT_VALUE))));
        Assert.assertEquals(expectedRoles, serviceFactory.roles());
    }

    @Test
    public void factoryPassesMethodParametersToArgumentResolverFactoryInConstruction() throws NoSuchMethodException {
        final Method method = TestModule.class.getDeclaredMethod("methodWithParameter", Object.class);
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

        public static final String PARAMETER_NAME = "parameter";
        public static final String ROLE_NAME = "role";
        public static final String DEFAULT_VALUE = "default";

        public void service() {
        }

        public void methodWithParameter(@Parameter(PARAMETER_NAME) Object methodParameter) {
        }

        public void methodWithParameterAndDefault(@Parameter(PARAMETER_NAME) @Default(DEFAULT_VALUE) Object methodParameter) {
        }

        public void methodWithCollaborator(@Role(ROLE_NAME) Object methodParameter) {
        }

        public void methodWithCollaboratorAndDefault(@Role(ROLE_NAME) @Default(DEFAULT_VALUE) Object methodParameter) {
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
