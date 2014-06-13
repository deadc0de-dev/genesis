package dev.deadc0de.genesis.module.factory;

import dev.deadc0de.genesis.ServiceDescriptor;
import dev.deadc0de.genesis.ServiceGenerator;
import dev.deadc0de.genesis.module.Parameter;
import dev.deadc0de.genesis.module.Role;
import java.util.function.BiFunction;

public class DispatchingArgumentResolverFactory implements ArgumentResolverFactory {

    private final ArgumentResolverFactory parameterResolverFactory;
    private final ArgumentResolverFactory roleResolverFactory;

    public DispatchingArgumentResolverFactory(ArgumentResolverFactory parameterResolverFactory, ArgumentResolverFactory roleResolverFactory) {
        this.parameterResolverFactory = parameterResolverFactory;
        this.roleResolverFactory = roleResolverFactory;
    }

    @Override
    public BiFunction<ServiceGenerator, ServiceDescriptor, Object> createArgumentResolver(java.lang.reflect.Parameter methodParameter) {
        final boolean isParameter = methodParameter.isAnnotationPresent(Parameter.class);
        final boolean isRole = methodParameter.isAnnotationPresent(Role.class);
        if (isParameter && isRole) {
            throw new IllegalArgumentException("method parameter cannot be annotated with both @Parameter and @Role");
        }
        final Class<?> parameterType = methodParameter.getType();
        if (parameterType.equals(ServiceGenerator.class)) {
            return (serviceGenerator, serviceDescriptor) -> serviceGenerator;
        }
        if (isParameter) {
            return parameterResolverFactory.createArgumentResolver(methodParameter);
        }
        if (isRole) {
            return roleResolverFactory.createArgumentResolver(methodParameter);
        }
        throw new IllegalArgumentException("method parameter must be annotated with either @Parameter or @Role, or be of ServiceGenerator type");
    }
}
