package dev.deadc0de.genesis.module;

import dev.deadc0de.genesis.module.factory.MethodBackedServiceFactory;
import dev.deadc0de.genesis.ServiceFactory;
import dev.deadc0de.genesis.module.factory.ArgumentResolverFactory;
import dev.deadc0de.genesis.module.factory.DispatchingArgumentResolverFactory;
import dev.deadc0de.genesis.module.factory.ParameterResolver;
import dev.deadc0de.genesis.module.factory.RoleResolver;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;

public interface ServiceModule {

    default Stream<ServiceFactory> assembleServiceFactories() {
        return assembleServiceFactories(this);
    }

    static Stream<ServiceFactory> assembleServiceFactories(Object module) {
        final ArgumentResolverFactory argumentResolverFactory = new DispatchingArgumentResolverFactory(ParameterResolver::new, RoleResolver::new);
        return Stream.of(module.getClass().getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .map(method -> new MethodBackedServiceFactory(module, method, argumentResolverFactory));
    }
}
