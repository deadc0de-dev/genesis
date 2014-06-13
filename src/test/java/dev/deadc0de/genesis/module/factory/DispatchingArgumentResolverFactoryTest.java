package dev.deadc0de.genesis.module.factory;

import dev.deadc0de.genesis.ServiceDescriptor;
import dev.deadc0de.genesis.ServiceGenerator;
import dev.deadc0de.genesis.module.Parameter;
import dev.deadc0de.genesis.module.Role;
import java.lang.reflect.Method;
import java.util.function.BiFunction;
import org.junit.Assert;
import org.junit.Test;

public class DispatchingArgumentResolverFactoryTest {

    @Test(expected = IllegalArgumentException.class)
    public void cannotCreateArgumentResolverWhenMethodParameterIsNotAnnotatedWithParameterNorRoleAndIsNotOfTypeServiceGenerator() throws NoSuchMethodException {
        final Method method = TestModule.class.getDeclaredMethod("unknownType", Object.class);
        final ArgumentResolverFactory argumentResolverFactory = new DispatchingArgumentResolverFactory(new DummyArgumentResolverFactory(), new DummyArgumentResolverFactory());
        argumentResolverFactory.createArgumentResolver(method.getParameters()[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotCreateArgumentResolverWhenMethodParameterIsAnnotatedWithBothParameterAndRole() throws NoSuchMethodException {
        final Method method = TestModule.class.getDeclaredMethod("annotatedWithBothParameterAndRole", Object.class);
        final ArgumentResolverFactory argumentResolverFactory = new DispatchingArgumentResolverFactory(new DummyArgumentResolverFactory(), new DummyArgumentResolverFactory());
        argumentResolverFactory.createArgumentResolver(method.getParameters()[0]);
    }

    @Test
    public void whenMethodParameterHasServiceGeneratorTypeThenTheArgumentResolverReturnsTheOnePassed() throws NoSuchMethodException {
        final Method method = TestModule.class.getDeclaredMethod("serviceGeneratorType", ServiceGenerator.class);
        final ArgumentResolverFactory argumentResolverFactory = new DispatchingArgumentResolverFactory(new DummyArgumentResolverFactory(), new DummyArgumentResolverFactory());
        final BiFunction<ServiceGenerator, ServiceDescriptor, Object> argumentResolver = argumentResolverFactory.createArgumentResolver(method.getParameters()[0]);
        final ServiceGenerator serviceGenerator = new DummyServiceGenerator();
        final Object argument = argumentResolver.apply(serviceGenerator, null);
        Assert.assertEquals(serviceGenerator, argument);
    }

    @Test
    public void whenMethodParameterIsAnnotatedWithParameterThenItIsPassedToTheParameterResolverFactory() throws NoSuchMethodException {
        final Method method = TestModule.class.getDeclaredMethod("parameterType", Object.class);
        final SpyArgumentResolverFactory parameterResolverFactory = new SpyArgumentResolverFactory();
        final ArgumentResolverFactory argumentResolverFactory = new DispatchingArgumentResolverFactory(parameterResolverFactory, new DummyArgumentResolverFactory());
        argumentResolverFactory.createArgumentResolver(method.getParameters()[0]);
        Assert.assertEquals(method.getParameters()[0], parameterResolverFactory.capturedMethodParameter);
    }

    @Test
    public void whenMethodParameterIsAnnotatedWithParameterThenTheArgumentResolverCreatedByTheParameterResolverFactoryIsReturned() throws NoSuchMethodException {
        final Method method = TestModule.class.getDeclaredMethod("parameterType", Object.class);
        final BiFunction<ServiceGenerator, ServiceDescriptor, Object> parameterResolver = (serviceGenerator, serviceDescriptor) -> new Object();
        final ArgumentResolverFactory argumentResolverFactory = new DispatchingArgumentResolverFactory(new StubArgumentResolverFactory(parameterResolver), new DummyArgumentResolverFactory());
        Assert.assertEquals(parameterResolver, argumentResolverFactory.createArgumentResolver(method.getParameters()[0]));
    }

    @Test
    public void whenMethodParameterIsAnnotatedWithRoleThenItIsPassedToTheRoleResolverFactory() throws NoSuchMethodException {
        final Method method = TestModule.class.getDeclaredMethod("roleType", Object.class);
        final SpyArgumentResolverFactory roleResolverFactory = new SpyArgumentResolverFactory();
        final ArgumentResolverFactory argumentResolverFactory = new DispatchingArgumentResolverFactory(new DummyArgumentResolverFactory(), roleResolverFactory);
        argumentResolverFactory.createArgumentResolver(method.getParameters()[0]);
        Assert.assertEquals(method.getParameters()[0], roleResolverFactory.capturedMethodParameter);
    }

    @Test
    public void whenMethodParameterIsAnnotatedWithRoleThenTheArgumentResolverCreatedByTheRoleResolverFactoryIsReturned() throws NoSuchMethodException {
        final Method method = TestModule.class.getDeclaredMethod("roleType", Object.class);
        final BiFunction<ServiceGenerator, ServiceDescriptor, Object> roleResolver = (serviceGenerator, serviceDescriptor) -> new Object();
        final ArgumentResolverFactory argumentResolverFactory = new DispatchingArgumentResolverFactory(new DummyArgumentResolverFactory(), new StubArgumentResolverFactory(roleResolver));
        Assert.assertEquals(roleResolver, argumentResolverFactory.createArgumentResolver(method.getParameters()[0]));
    }

    private static class TestModule {

        public void unknownType(Object unknownType) {
        }

        public void annotatedWithBothParameterAndRole(@Parameter("parameter") @Role("role") Object unknownType) {
        }

        public void serviceGeneratorType(ServiceGenerator serviceGenerator) {
        }

        public void parameterType(@Parameter("parameter") Object argument) {
        }

        public void roleType(@Role("role") Object collaborator) {
        }
    }

    private static class DummyServiceGenerator implements ServiceGenerator {

        @Override
        public Object generate(Class serviceType, ServiceDescriptor serviceDescriptor) {
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
