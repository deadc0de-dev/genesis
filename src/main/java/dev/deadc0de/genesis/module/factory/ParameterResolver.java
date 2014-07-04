package dev.deadc0de.genesis.module.factory;

import dev.deadc0de.genesis.ServiceDescriptor;
import dev.deadc0de.genesis.ServiceGenerator;
import dev.deadc0de.genesis.module.Default;
import dev.deadc0de.genesis.module.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ParameterResolver implements BiFunction<ServiceGenerator, ServiceDescriptor, Object> {

    private final String parameterName;
    private final Optional<List<String>> defaultParameters;
    private final Function<List<String>, Object> parametersExtractor;

    public ParameterResolver(java.lang.reflect.Parameter methodParameter) {
        if (!methodParameter.isAnnotationPresent(Parameter.class)) {
            throw new IllegalArgumentException("method parameter must be annotated with @Parameter");
        }
        final Class<?> parameterType = methodParameter.getType();
        if (!isSupported(parameterType)) {
            throw new IllegalArgumentException("method parameter must be of type String or String[], but found " + parameterType.getCanonicalName());
        }
        parameterName = methodParameter.getAnnotation(Parameter.class).value();
        defaultParameters = Optional.ofNullable(methodParameter.getAnnotation(Default.class)).map(Default::value).map(values -> {
            if (parameterType.equals(String.class) && values.length != 1) {
                throw new IllegalArgumentException("only one default parameter must be specified when parameter type is String");
            }
            return Arrays.asList(values);
        });
        parametersExtractor = parameterType.equals(String.class) ? ParameterResolver::extractString : ParameterResolver::extractStringArray;
    }

    private static boolean isSupported(Class<?> parameterType) {
        return parameterType.equals(String.class) || (parameterType.isArray() && parameterType.getComponentType().equals(String.class));
    }

    private static Object extractString(List<String> parameters) {
        if (parameters.size() != 1) {
            throw new IllegalStateException("expected a single parameter, but " + parameters.size() + " parameters found");
        }
        return parameters.get(0);
    }

    private static Object extractStringArray(List<String> parameters) {
        return parameters.toArray(new String[parameters.size()]);
    }

    @Override
    public Object apply(ServiceGenerator serviceGenerator, ServiceDescriptor serviceDescriptor) {
        if (serviceDescriptor.configuration.containsKey(parameterName)) {
            final List<String> parameters = serviceDescriptor.configuration.get(parameterName);
            return parametersExtractor.apply(parameters);
        } else if (defaultParameters.isPresent()) {
            return parametersExtractor.apply(defaultParameters.get());
        }
        throw new IllegalStateException(String.format("missing parameter: %s", parameterName));
    }
}
