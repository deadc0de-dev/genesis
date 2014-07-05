package dev.deadc0de.genesis.module.factory;

import dev.deadc0de.genesis.ServiceDescriptor;
import dev.deadc0de.genesis.ServiceGenerator;
import dev.deadc0de.genesis.module.Default;
import dev.deadc0de.genesis.module.Role;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class RoleResolverTest {

    private static final String ROLE_NAME = "role";
    private static final ServiceDescriptor COLLABORATOR_DESCRIPTOR = ServiceDescriptor.notParameterized("collaborator");
    private static final ServiceDescriptor SERVICE_DESCRIPTOR = new ServiceDescriptor(
            "service",
            Collections.emptyMap(),
            Collections.singletonMap(ROLE_NAME, Collections.singletonList(COLLABORATOR_DESCRIPTOR)));
    private static final List<ServiceDescriptor> COLLABORATOR_DESCRIPTORS = Arrays.asList(
            ServiceDescriptor.notParameterized("collaborator1"),
            ServiceDescriptor.notParameterized("collaborator2"),
            ServiceDescriptor.notParameterized("collaborator3"));
    private static final ServiceDescriptor SERVICE_DESCRIPTOR_WITH_MULTIPLE_COLLABORATORS = new ServiceDescriptor(
            "service",
            Collections.emptyMap(),
            Collections.singletonMap(ROLE_NAME, COLLABORATOR_DESCRIPTORS));

    @Test(expected = IllegalArgumentException.class)
    public void cannotCreateRoleResolverWhenTheMethodParameterIsNotAnnotatedWithTheRoleAnnotation() throws NoSuchMethodException {
        final Method method = TestModule.class.getDeclaredMethod("methodParameterNotAnnotated", Object.class);
        final RoleResolver notCreated = new RoleResolver(method.getParameters()[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotCreateRoleResolverWhenTheMethodParameterTypeIsNotArrayAndMultipleDefaultValuesAreSpecified() throws NoSuchMethodException {
        final Method method = TestModule.class.getDeclaredMethod("methodParameterOfNonArrayTypeWithMultipleDefaultValues", Object.class);
        final RoleResolver notCreated = new RoleResolver(method.getParameters()[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotCreateRoleResolverWhenTheMethodParameterTypeIsNotArrayAndZeroDefaultValuesAreSpecified() throws NoSuchMethodException {
        final Method method = TestModule.class.getDeclaredMethod("methodParameterOfNonArrayTypeWithZeroDefaultValues", Object.class);
        final RoleResolver notCreated = new RoleResolver(method.getParameters()[0]);
    }

    @Test
    public void whenServiceCollaboratorWithTheGivenRoleIsPresentThenTheParameterTypeIsPassedToTheServiceGenerator() throws NoSuchMethodException {
        final SpyServiceGenerator spyServiceGenerator = new SpyServiceGenerator();
        final Method method = TestModule.class.getDeclaredMethod("collaboratorWithGivenRoleIsPresent", Object.class);
        final RoleResolver roleResolver = new RoleResolver(method.getParameters()[0]);
        roleResolver.apply(spyServiceGenerator, SERVICE_DESCRIPTOR);
        Assert.assertEquals(Object.class, spyServiceGenerator.capturedServiceType);
    }

    @Test
    public void whenServiceCollaboratorWithTheGivenRoleIsPresentThenTheAssociatedDescriptorIsPassedToTheServiceGenerator() throws NoSuchMethodException {
        final SpyServiceGenerator spyServiceGenerator = new SpyServiceGenerator();
        final Method method = TestModule.class.getDeclaredMethod("collaboratorWithGivenRoleIsPresent", Object.class);
        final RoleResolver roleResolver = new RoleResolver(method.getParameters()[0]);
        roleResolver.apply(spyServiceGenerator, SERVICE_DESCRIPTOR);
        Assert.assertEquals(COLLABORATOR_DESCRIPTOR, spyServiceGenerator.capturedServiceDescriptor);
    }

    @Test
    public void whenServiceCollaboratorWithTheGivenRoleIsPresentThenTheGeneratedServiceIsReturned() throws NoSuchMethodException {
        final Object collaborator = new Object();
        final Method method = TestModule.class.getDeclaredMethod("collaboratorWithGivenRoleIsPresent", Object.class);
        final RoleResolver roleResolver = new RoleResolver(method.getParameters()[0]);
        final Object argument = roleResolver.apply(new StubServiceGenerator(collaborator), SERVICE_DESCRIPTOR);
        Assert.assertEquals(collaborator, argument);
    }

    @Test(expected = IllegalStateException.class)
    public void whenServiceCollaboratorWithTheGivenRoleIsAbsentThenThrows() throws NoSuchMethodException {
        final Method method = TestModule.class.getDeclaredMethod("collaboratorWithGivenRoleIsMissing", Object.class);
        final RoleResolver roleResolver = new RoleResolver(method.getParameters()[0]);
        roleResolver.apply(new DummyServiceGenerator(), SERVICE_DESCRIPTOR);
    }

    @Test
    public void whenDefaultValueIsProvidedAndServiceCollaboratorWithTheGivenRoleIsPresentThenTheParameterTypeIsPassedToTheServiceGenerator() throws NoSuchMethodException {
        final SpyServiceGenerator spyServiceGenerator = new SpyServiceGenerator();
        final Method method = TestModule.class.getDeclaredMethod("defaultValueProvidedAndCollaboratorWithGivenRoleIsPresent", Object.class);
        final RoleResolver roleResolver = new RoleResolver(method.getParameters()[0]);
        roleResolver.apply(spyServiceGenerator, SERVICE_DESCRIPTOR);
        Assert.assertEquals(Object.class, spyServiceGenerator.capturedServiceType);
    }

    @Test
    public void whenDefaultValueIsProvidedAndServiceCollaboratorWithTheGivenRoleIsPresentThenTheAssociatedDescriptorIsPassedToTheServiceGenerator() throws NoSuchMethodException {
        final SpyServiceGenerator spyServiceGenerator = new SpyServiceGenerator();
        final Method method = TestModule.class.getDeclaredMethod("defaultValueProvidedAndCollaboratorWithGivenRoleIsPresent", Object.class);
        final RoleResolver roleResolver = new RoleResolver(method.getParameters()[0]);
        roleResolver.apply(spyServiceGenerator, SERVICE_DESCRIPTOR);
        Assert.assertEquals(COLLABORATOR_DESCRIPTOR, spyServiceGenerator.capturedServiceDescriptor);
    }

    @Test
    public void whenDefaultValueIsProvidedAndServiceCollaboratorWithTheGivenRoleIsPresentThenTheGeneratedServiceIsReturned() throws NoSuchMethodException {
        final Object collaborator = new Object();
        final Method method = TestModule.class.getDeclaredMethod("defaultValueProvidedAndCollaboratorWithGivenRoleIsPresent", Object.class);
        final RoleResolver roleResolver = new RoleResolver(method.getParameters()[0]);
        final Object argument = roleResolver.apply(new StubServiceGenerator(collaborator), SERVICE_DESCRIPTOR);
        Assert.assertEquals(collaborator, argument);
    }

    @Test
    public void whenDefaultValueIsProvidedAndServiceCollaboratorWithTheGivenRoleIsAbsentThenTheParameterTypeIsPassedToTheServiceGenerator() throws NoSuchMethodException {
        final SpyServiceGenerator spyServiceGenerator = new SpyServiceGenerator();
        final Method method = TestModule.class.getDeclaredMethod("defaultValueProvidedAndCollaboratorWithGivenRoleIsMissing", Object.class);
        final RoleResolver roleResolver = new RoleResolver(method.getParameters()[0]);
        roleResolver.apply(spyServiceGenerator, SERVICE_DESCRIPTOR);
        Assert.assertEquals(Object.class, spyServiceGenerator.capturedServiceType);
    }

    @Test
    public void whenDefaultValueIsProvidedAndServiceCollaboratorWithTheGivenRoleIsAbsentThenANotParameterizedDescriptorWithTheDefaultValueAsServiceNameIsPassedToTheServiceGenerator() throws NoSuchMethodException {
        final SpyServiceGenerator spyServiceGenerator = new SpyServiceGenerator();
        final Method method = TestModule.class.getDeclaredMethod("defaultValueProvidedAndCollaboratorWithGivenRoleIsMissing", Object.class);
        final RoleResolver roleResolver = new RoleResolver(method.getParameters()[0]);
        roleResolver.apply(spyServiceGenerator, SERVICE_DESCRIPTOR);
        Assert.assertEquals(TestModule.DEFAULT_COLLABORATOR_NAME, spyServiceGenerator.capturedServiceDescriptor.name);
    }

    @Test
    public void whenDefaultValueIsProvidedAndServiceCollaboratorWithTheGivenRoleIsAbsentThenTheGeneratedServiceIsReturned() throws NoSuchMethodException {
        final Object collaborator = new Object();
        final Method method = TestModule.class.getDeclaredMethod("defaultValueProvidedAndCollaboratorWithGivenRoleIsMissing", Object.class);
        final RoleResolver roleResolver = new RoleResolver(method.getParameters()[0]);
        final Object argument = roleResolver.apply(new StubServiceGenerator(collaborator), SERVICE_DESCRIPTOR);
        Assert.assertEquals(collaborator, argument);
    }

    @Test(expected = IllegalStateException.class)
    public void whenCollaboratorIsNotOfTypeArrayAndServiceConfigurationContainsAnArrayOfCollaboratorsThenThrows() throws NoSuchMethodException {
        final Method method = TestModule.class.getDeclaredMethod("collaboratorWithGivenRoleIsPresent", Object.class);
        final RoleResolver roleResolver = new RoleResolver(method.getParameters()[0]);
        roleResolver.apply(new DummyServiceGenerator(), SERVICE_DESCRIPTOR_WITH_MULTIPLE_COLLABORATORS);
    }

    @Test
    public void whenCollaboratorIsPresentInServiceConfigurationThenTheComponentTypeOfTheCollaboratorsArrayIsPassedToTheServiceGenerator() throws NoSuchMethodException {
        final Method method = TestModule.class.getDeclaredMethod("collaboratorWithGivenRoleIsPresent", Object[].class);
        final RoleResolver roleResolver = new RoleResolver(method.getParameters()[0]);
        final SpyServiceGenerator serviceGenerator = new SpyServiceGenerator();
        roleResolver.apply(serviceGenerator, SERVICE_DESCRIPTOR);
        Assert.assertEquals(Object.class, serviceGenerator.capturedServiceType);
    }

    @Test
    public void whenCollaboratorIsPresentInServiceConfigurationThenAnArrayOfGeneratedCollaboratorsIsReturned() throws NoSuchMethodException {
        final Object collaborator = new Object();
        final Method method = TestModule.class.getDeclaredMethod("collaboratorWithGivenRoleIsPresent", Object[].class);
        final RoleResolver roleResolver = new RoleResolver(method.getParameters()[0]);
        final Object argument = roleResolver.apply(new StubServiceGenerator(collaborator), SERVICE_DESCRIPTOR_WITH_MULTIPLE_COLLABORATORS);
        final Object[] expected = new Object[COLLABORATOR_DESCRIPTORS.size()];
        Arrays.fill(expected, collaborator);
        Assert.assertArrayEquals(expected, (Object[]) argument);
    }

    @Test
    public void whenMethodParameterTypeIsArrayThenTheReturnedObjectIsAnArrayOfTheRequiredType() throws NoSuchMethodException {
        final Method method = TestModule.class.getDeclaredMethod("collaboratorsArrayOfStringType", String[].class);
        final RoleResolver roleResolver = new RoleResolver(method.getParameters()[0]);
        final Object argument = roleResolver.apply(new StubServiceGenerator("collaborator"), SERVICE_DESCRIPTOR_WITH_MULTIPLE_COLLABORATORS);
        Assert.assertEquals(String[].class, argument.getClass());
    }

    @Test
    public void whenDefaultCollaboratorNamesAreProvidedAndCollaboratorsAreMissingFromServiceConfigurationThenTheDefaultCollaboratorsAreGenerated() throws NoSuchMethodException {
        final Object collaborator = new Object();
        final Method method = TestModule.class.getDeclaredMethod("defaultValueProvidedAndCollaboratorWithGivenRoleIsMissing", Object[].class);
        final RoleResolver roleResolver = new RoleResolver(method.getParameters()[0]);
        final Object argument = roleResolver.apply(new StubServiceGenerator(collaborator), SERVICE_DESCRIPTOR);
        Assert.assertArrayEquals(new Object[]{collaborator, collaborator}, (Object[]) argument);
    }

    private static class TestModule {

        public static final String DEFAULT_COLLABORATOR_NAME = "default collaborator";

        public void methodParameterNotAnnotated(Object collaborator) {
        }

        public void methodParameterOfNonArrayTypeWithMultipleDefaultValues(@Role(ROLE_NAME) @Default({"multiple", "collaborators"}) Object collaborator) {
        }

        public void methodParameterOfNonArrayTypeWithZeroDefaultValues(@Role(ROLE_NAME) @Default({}) Object collaborator) {
        }

        public void collaboratorWithGivenRoleIsPresent(@Role(ROLE_NAME) Object collaborator) {
        }

        public void collaboratorWithGivenRoleIsPresent(@Role(ROLE_NAME) Object[] collaborators) {
        }

        public void collaboratorsArrayOfStringType(@Role(ROLE_NAME) String[] collaborators) {
        }

        public void collaboratorWithGivenRoleIsMissing(@Role("not present") Object collaborator) {
        }

        public void defaultValueProvidedAndCollaboratorWithGivenRoleIsPresent(@Role(ROLE_NAME) @Default(DEFAULT_COLLABORATOR_NAME) Object collaborator) {
        }

        public void defaultValueProvidedAndCollaboratorWithGivenRoleIsMissing(@Role("not present") @Default(DEFAULT_COLLABORATOR_NAME) Object collaborator) {
        }

        public void defaultValueProvidedAndCollaboratorWithGivenRoleIsMissing(@Role("not present") @Default({"default", "collaborators"}) Object[] collaborator) {
        }
    }

    private static class DummyServiceGenerator implements ServiceGenerator {

        @Override
        public Object generate(Class serviceType, ServiceDescriptor serviceDescriptor) {
            throw new UnsupportedOperationException("dummy implementation");
        }
    }

    private static class SpyServiceGenerator implements ServiceGenerator {

        public Class capturedServiceType;
        public ServiceDescriptor capturedServiceDescriptor;

        @Override
        public Object generate(Class serviceType, ServiceDescriptor serviceDescriptor) {
            capturedServiceType = serviceType;
            capturedServiceDescriptor = serviceDescriptor;
            return null;
        }
    }

    private static class StubServiceGenerator implements ServiceGenerator {

        private final Object generatedCollaborator;

        public StubServiceGenerator(Object generatedCollaborator) {
            this.generatedCollaborator = generatedCollaborator;
        }

        @Override
        public Object generate(Class serviceType, ServiceDescriptor serviceDescriptor) {
            return generatedCollaborator;
        }
    }
}
