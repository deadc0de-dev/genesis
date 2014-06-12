package dev.deadc0de.genesis.module.factory;

import dev.deadc0de.genesis.ServiceDescriptor;
import dev.deadc0de.genesis.ServiceGenerator;
import dev.deadc0de.genesis.module.Default;
import dev.deadc0de.genesis.module.Parameter;
import java.lang.reflect.Method;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

public class ParameterResolverTest {

    private static final String PARAMETER_NAME = "parameter";
    private static final String PARAMETER_VALUE = "argument";
    private static final ServiceDescriptor SERVICE_DESCRIPTOR = new ServiceDescriptor(
            "service",
            Collections.singletonMap(PARAMETER_NAME, PARAMETER_VALUE),
            Collections.emptyMap());

    @Test(expected = IllegalArgumentException.class)
    public void cannotCreateParameterResolverWhenTheMethodParameterIsNotAnnotatedWithTheParameterAnnotation() throws NoSuchMethodException {
        final Method method = TestModule.class.getDeclaredMethod("methodParameterNotAnnotated", String.class);
        final ParameterResolver notCreated = new ParameterResolver(method.getParameters()[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotCreateParameterResolverWhenTheMethodParameterTypeIsNotString() throws NoSuchMethodException {
        final Method method = TestModule.class.getDeclaredMethod("methodParameterNotOfTypeString", Object.class);
        final ParameterResolver notCreated = new ParameterResolver(method.getParameters()[0]);
    }

    @Test
    public void whenParameterNameIsPresentInServiceConfigurationThenTheAssociatedArgumentIsReturned() throws NoSuchMethodException {
        final Method method = TestModule.class.getDeclaredMethod("parameterNamePresentInConfiguration", String.class);
        final ParameterResolver parameterResolver = new ParameterResolver(method.getParameters()[0]);
        final Object argument = parameterResolver.apply(new DummyServiceGenerator(), SERVICE_DESCRIPTOR);
        Assert.assertEquals(PARAMETER_VALUE, argument);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenParameterNameIsMissingFromServiceConfigurationThenThrows() throws NoSuchMethodException {
        final Method method = TestModule.class.getDeclaredMethod("parameterNameMissingFromConfiguration", String.class);
        final ParameterResolver parameterResolver = new ParameterResolver(method.getParameters()[0]);
        parameterResolver.apply(new DummyServiceGenerator(), SERVICE_DESCRIPTOR);
    }

    @Test
    public void whenDefaultValueIsProvidedAndParameterNameIsPresentInServiceConfigurationThenTheDefaultValueIsIgnored() throws NoSuchMethodException {
        final Method method = TestModule.class.getDeclaredMethod("defaultValueProvidedAndParameterNamePresentInConfiguration", String.class);
        final ParameterResolver parameterResolver = new ParameterResolver(method.getParameters()[0]);
        final Object argument = parameterResolver.apply(new DummyServiceGenerator(), SERVICE_DESCRIPTOR);
        Assert.assertEquals(PARAMETER_VALUE, argument);
    }

    @Test
    public void whenDefaultValueIsProvidedAndParameterNameIsMissingFromServiceConfigurationThenTheDefaultValueIsUsed() throws NoSuchMethodException {
        final Method method = TestModule.class.getDeclaredMethod("defaultValueProvidedAndParameterNameMissingFromConfiguration", String.class);
        final ParameterResolver parameterResolver = new ParameterResolver(method.getParameters()[0]);
        final Object argument = parameterResolver.apply(new DummyServiceGenerator(), SERVICE_DESCRIPTOR);
        Assert.assertEquals(TestModule.DEFAULT_VALUE, argument);
    }

    private static class TestModule {

        public static final String DEFAULT_VALUE = "default argument";

        public void methodParameterNotAnnotated(String argument) {
        }

        public void methodParameterNotOfTypeString(@Parameter(PARAMETER_NAME) Object argument) {
        }

        public void parameterNamePresentInConfiguration(@Parameter(PARAMETER_NAME) String argument) {
        }

        public void parameterNameMissingFromConfiguration(@Parameter("not present") String argument) {
        }

        public void defaultValueProvidedAndParameterNamePresentInConfiguration(@Parameter(PARAMETER_NAME) @Default(DEFAULT_VALUE) String argument) {
        }

        public void defaultValueProvidedAndParameterNameMissingFromConfiguration(@Parameter("not present") @Default(DEFAULT_VALUE) String argument) {
        }
    }

    private static class DummyServiceGenerator implements ServiceGenerator {

        @Override
        public Object generate(Class serviceType, ServiceDescriptor serviceDescriptor) {
            throw new UnsupportedOperationException("dummy implementation");
        }
    }
}
