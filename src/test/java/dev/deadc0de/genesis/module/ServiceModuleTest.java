package dev.deadc0de.genesis.module;

import dev.deadc0de.genesis.ServiceFactory;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Test;

public class ServiceModuleTest {

    @Test
    public void factoriesAreAssembledFromPublicStaticMethods() {
        final Stream<ServiceFactory> assembledFactories = ServiceModule.assembleServiceFactories(new ConfigurationWithPublicStaticMethod());
        Assert.assertEquals(1, assembledFactories.count());
    }

    @Test
    public void factoriesAreAssembledFromPublicInstanceMethods() {
        final Stream<ServiceFactory> assembledFactories = ServiceModule.assembleServiceFactories(new ConfigurationWithPublicInstanceMethod());
        Assert.assertEquals(1, assembledFactories.count());
    }

    @Test
    public void nonPublicMethodsAreNotUsedToAssembleFactories() {
        final Stream<ServiceFactory> assembledFactories = ServiceModule.assembleServiceFactories(new ConfigurationWithoutPublicMethods());
        Assert.assertEquals(0, assembledFactories.count());
    }

    private static class ConfigurationWithPublicStaticMethod {

        public static void nothing() {
        }
    }

    private static class ConfigurationWithPublicInstanceMethod {

        public void nothing() {
        }
    }

    private static class ConfigurationWithoutPublicMethods {

        private void privateInstanceMethod() {
        }

        private static void privateStaticMethod() {
        }

        protected void protectedInstanceMethod() {
        }

        protected static void protectedStaticMethod() {
        }

        void instanceMethod() {
        }

        static void staticMethod() {
        }
    }
}
