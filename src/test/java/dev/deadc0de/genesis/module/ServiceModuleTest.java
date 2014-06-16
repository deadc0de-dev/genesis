package dev.deadc0de.genesis.module;

import dev.deadc0de.genesis.ServiceFactory;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Test;

public class ServiceModuleTest {

    @Test
    public void factoriesAreAssembledFromPublicStaticMethods() {
        final Stream<ServiceFactory> assembledFactories = ServiceModule.assembleModule(new ModuleWithPublicStaticMethod());
        Assert.assertEquals(1, assembledFactories.count());
    }

    @Test
    public void factoriesAreAssembledFromPublicInstanceMethods() {
        final Stream<ServiceFactory> assembledFactories = ServiceModule.assembleModule(new ModuleWithPublicInstanceMethod());
        Assert.assertEquals(1, assembledFactories.count());
    }

    @Test
    public void nonPublicMethodsAreNotUsedToAssembleFactories() {
        final Stream<ServiceFactory> assembledFactories = ServiceModule.assembleModule(new ModuleWithoutPublicMethods());
        Assert.assertEquals(0, assembledFactories.count());
    }

    private static class ModuleWithPublicStaticMethod {

        public static void nothing() {
        }
    }

    private static class ModuleWithPublicInstanceMethod {

        public void nothing() {
        }
    }

    private static class ModuleWithoutPublicMethods {

        private void privateInstanceMethod() {
        }

        private static void privateStaticMethod() {
        }

        protected void protectedInstanceMethod() {
        }

        protected static void protectedStaticMethod() {
        }

        void packageProtectedInstanceMethod() {
        }

        static void packageProtectedStaticMethod() {
        }
    }
}
