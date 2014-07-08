package dev.deadc0de.genesis.module.factory;

import dev.deadc0de.genesis.AbstractServiceFactory;
import dev.deadc0de.genesis.ServiceDescriptor;
import dev.deadc0de.genesis.ServiceGenerationException;
import dev.deadc0de.genesis.ServiceGenerator;
import dev.deadc0de.genesis.module.Default;
import dev.deadc0de.genesis.module.Parameter;
import dev.deadc0de.genesis.module.Role;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
    private final Map<String, Optional<List<String>>> parameters;
    private final Map<String, Map.Entry<Class<?>, Optional<List<String>>>> roles;

    public MethodBackedServiceFactory(Object module, Method method, ArgumentResolverFactory argumentResolverFactory) {
        super(method.getName(), method.getReturnType());
        this.module = module;
        this.method = method;
        parameters = new HashMap<>();
        roles = new HashMap<>();
        argumentResolvers = Stream.of(method.getParameters())
                .peek(this::collectParameterOrRole)
                .map(argumentResolverFactory::createArgumentResolver)
                .collect(Collectors.toList());
    }

    private void collectParameterOrRole(java.lang.reflect.Parameter methodParameter) {
        final Optional<List<String>> defaultValues = Optional.ofNullable(methodParameter.getAnnotation(Default.class))
                .map(Default::value)
                .map(Arrays::asList)
                .map(Collections::unmodifiableList);
        if (methodParameter.isAnnotationPresent(Parameter.class)) {
            parameters.put(methodParameter.getAnnotation(Parameter.class).value(), defaultValues);
        }
        if (methodParameter.isAnnotationPresent(Role.class)) {
            roles.put(methodParameter.getAnnotation(Role.class).value(), new AbstractMap.SimpleImmutableEntry<>(methodParameter.getType(), defaultValues));
        }
    }

    @Override
    public Map parameters() {
        return parameters;
    }

    @Override
    public Map roles() {
        return roles;
    }

    @Override
    public Object create(ServiceGenerator serviceGenerator, ServiceDescriptor serviceDescriptor) {
        try {
            final Object[] arguments = argumentResolvers.stream().map(argumentResolver -> argumentResolver.apply(serviceGenerator, serviceDescriptor)).toArray();
            return method.invoke(module, arguments);
        } catch (IllegalAccessException | InvocationTargetException | IllegalStateException exception) {
            throw new ServiceGenerationException(this, exception);
        }
    }

    @Override
    public String toString() {
        return super.toString() + '@' + module.getClass().getCanonicalName();
    }
}
