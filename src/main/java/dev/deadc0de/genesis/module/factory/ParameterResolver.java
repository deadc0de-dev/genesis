package dev.deadc0de.genesis.module.factory;

import dev.deadc0de.genesis.ServiceDescriptor;
import dev.deadc0de.genesis.ServiceGenerator;
import dev.deadc0de.genesis.module.Default;
import dev.deadc0de.genesis.module.Parameter;
import java.util.Optional;
import java.util.function.BiFunction;

public class ParameterResolver implements BiFunction<ServiceGenerator, ServiceDescriptor, Object> {

    private final String parameterName;
    private final Optional<String> defaultValue;

    public ParameterResolver(java.lang.reflect.Parameter methodParameter) {
        if (!methodParameter.isAnnotationPresent(Parameter.class)) {
            throw new IllegalArgumentException("method parameter must be annotated with @Parameter");
        }
        if (!methodParameter.getType().equals(String.class)) {
            throw new IllegalArgumentException("method parameter must be of type String, but found " + methodParameter.getType().getCanonicalName());
        }
        parameterName = methodParameter.getAnnotation(Parameter.class).value();
        defaultValue = Optional.ofNullable(methodParameter.getAnnotation(Default.class)).map(Default::value);
    }

    @Override
    public Object apply(ServiceGenerator serviceGenerator, ServiceDescriptor serviceDescriptor) {
        if (serviceDescriptor.configuration.containsKey(parameterName)) {
            return serviceDescriptor.configuration.get(parameterName);
        } else if (defaultValue.isPresent()) {
            return defaultValue.get();
        }
        throw new IllegalStateException(String.format("missing parameter: %s", parameterName));
    }
}
