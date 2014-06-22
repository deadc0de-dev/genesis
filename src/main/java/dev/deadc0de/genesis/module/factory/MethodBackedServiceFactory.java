package dev.deadc0de.genesis.module.factory;

import dev.deadc0de.genesis.AbstractServiceFactory;
import dev.deadc0de.genesis.ServiceDescriptor;
import dev.deadc0de.genesis.ServiceGenerationException;
import dev.deadc0de.genesis.ServiceGenerator;
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

    public MethodBackedServiceFactory(Object module, Method method, ArgumentResolverFactory argumentResolverFactory) {
        super(method.getName(), method.getReturnType());
        this.module = module;
        this.method = method;
        argumentResolvers = Stream.of(method.getParameters()).map(argumentResolverFactory::createArgumentResolver).collect(Collectors.toList());
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
