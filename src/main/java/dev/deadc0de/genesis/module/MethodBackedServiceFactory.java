package dev.deadc0de.genesis.module;

import dev.deadc0de.genesis.AbstractServiceFactory;
import dev.deadc0de.genesis.ServiceDescriptor;
import dev.deadc0de.genesis.ServiceGenerator;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
            if (parameterType.equals(String.class)) {
                return resolveAsParameter(methodParameter);
            }
            throw new IllegalArgumentException("service parameter must be of type String, but found " + parameterType);
        }
        if (methodParameter.isAnnotationPresent(Role.class)) {
            return resolveAsRole(methodParameter);
        }
        throw new IllegalStateException("cannot fill argument of type " + parameterType);
    }

    private static BiFunction<ServiceGenerator, ServiceDescriptor, Object> resolveAsParameter(java.lang.reflect.Parameter parameter) {
        final String parameterName = parameter.getAnnotation(Parameter.class).value();
        return (serviceGenerator, serviceDescriptor) -> search(parameterName, serviceDescriptor.configuration)
                .orElseThrow(() -> new IllegalStateException(String.format("missing parameter %s in configuration for service %s", parameterName, serviceDescriptor.name)));
    }

    private static BiFunction<ServiceGenerator, ServiceDescriptor, Object> resolveAsRole(java.lang.reflect.Parameter parameter) {
        final String roleName = parameter.getAnnotation(Role.class).value();
        return (serviceGenerator, serviceDescriptor) -> search(roleName, serviceDescriptor.collaborators)
                .<Object>map(collaborator -> serviceGenerator.generate(parameter.getType(), collaborator))
                .orElseThrow(() -> new IllegalStateException(String.format("missing collaborator with role %s for service %s", roleName, serviceDescriptor.name)));
    }

    private static <K, V> Optional<V> search(K key, Map<K, V> map) {
        return map.containsKey(key) ? Optional.of(map.get(key)) : Optional.empty();
    }
}
