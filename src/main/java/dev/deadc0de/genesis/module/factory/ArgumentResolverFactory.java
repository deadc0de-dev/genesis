package dev.deadc0de.genesis.module.factory;

import dev.deadc0de.genesis.ServiceDescriptor;
import dev.deadc0de.genesis.ServiceGenerator;
import java.lang.reflect.Parameter;
import java.util.function.BiFunction;

public interface ArgumentResolverFactory {

    BiFunction<ServiceGenerator, ServiceDescriptor, Object> createArgumentResolver(Parameter methodParameter);
}
