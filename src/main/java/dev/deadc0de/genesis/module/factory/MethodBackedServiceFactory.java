package dev.deadc0de.genesis.module.factory;

import dev.deadc0de.genesis.AbstractServiceFactory;
import dev.deadc0de.genesis.ServiceDescriptor;
import dev.deadc0de.genesis.ServiceGenerator;
import dev.deadc0de.genesis.module.Parameter;
import dev.deadc0de.genesis.module.Role;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MethodBackedServiceFactory extends AbstractServiceFactory {

    private final Object module;
    private final Method method;
    private final List<BiFunction<ServiceGenerator, ServiceDescriptor, Object>> argumentResolvers;

    public MethodBackedServiceFactory(Object module, Method method) {
        super(method.getName(), method.getReturnType());
        this.module = module;
        this.method = method;
        argumentResolvers = Stream.of(method.getParameters()).map(MethodBackedServiceFactory::createArgumentResolver).collect(Collectors.toList());
    }

    @Override
    public Object create(ServiceGenerator serviceGenerator, ServiceDescriptor serviceDescriptor) {
        final Object[] arguments = argumentResolvers.stream().map(argumentResolver -> argumentResolver.apply(serviceGenerator, serviceDescriptor)).toArray();
        try {
            return method.invoke(module, arguments);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException(exception.getMessage(), exception);
        }
    }

    private static BiFunction<ServiceGenerator, ServiceDescriptor, Object> createArgumentResolver(java.lang.reflect.Parameter methodParameter) {
        final Class<?> parameterType = methodParameter.getType();
        if (parameterType.equals(ServiceGenerator.class)) {
            return (serviceGenerator, serviceDescriptor) -> serviceGenerator;
        }
        if (methodParameter.isAnnotationPresent(Parameter.class)) {
            return new ParameterResolver(methodParameter);
        }
        if (methodParameter.isAnnotationPresent(Role.class)) {
            return new RoleResolver(methodParameter);
        }
        throw new IllegalStateException("cannot fill argument of type " + parameterType);
    }
}
