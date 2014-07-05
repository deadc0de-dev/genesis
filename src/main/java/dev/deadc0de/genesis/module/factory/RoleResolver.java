package dev.deadc0de.genesis.module.factory;

import dev.deadc0de.genesis.ServiceDescriptor;
import dev.deadc0de.genesis.ServiceGenerator;
import dev.deadc0de.genesis.module.Default;
import dev.deadc0de.genesis.module.Role;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RoleResolver implements BiFunction<ServiceGenerator, ServiceDescriptor, Object> {

    private final String roleName;
    private final Class<?> methodParameterType;
    private final Optional<List<ServiceDescriptor>> defaultCollaboratorDescriptors;
    private final BiFunction<List<ServiceDescriptor>, ServiceGenerator, Object> collaboratorsExtractor;

    public RoleResolver(java.lang.reflect.Parameter methodParameter) {
        if (!methodParameter.isAnnotationPresent(Role.class)) {
            throw new IllegalArgumentException("method parameter must be annotated with @Role");
        }
        roleName = methodParameter.getAnnotation(Role.class).value();
        methodParameterType = methodParameter.getType();
        defaultCollaboratorDescriptors = Optional.ofNullable(methodParameter.getAnnotation(Default.class)).map(Default::value).map(collaboratorNames -> {
            if (!methodParameterType.isArray() && collaboratorNames.length != 1) {
                throw new IllegalArgumentException();
            }
            return Stream.of(collaboratorNames).map(ServiceDescriptor::notParameterized).collect(Collectors.toList());
        });
        collaboratorsExtractor = methodParameterType.isArray() ? this::extractArray : this::extract;
    }

    private Object extract(List<ServiceDescriptor> collaborators, ServiceGenerator serviceGenerator) {
        if (collaborators.size() != 1) {
            throw new IllegalStateException("expected a single collaborator, but " + collaborators.size() + " collaborators found");
        }
        return serviceGenerator.generate(methodParameterType, collaborators.get(0));
    }

    private Object extractArray(List<ServiceDescriptor> collaborators, ServiceGenerator serviceGenerator) {
        final Class<?> collaboratorType = methodParameterType.getComponentType();
        return collaborators.stream()
                .<Object>map(collaborator -> serviceGenerator.generate(methodParameterType.getComponentType(), collaborator))
                .toArray(size -> (Object[]) Array.newInstance(collaboratorType, size));
    }

    @Override
    public Object apply(ServiceGenerator serviceGenerator, ServiceDescriptor serviceDescriptor) {
        if (serviceDescriptor.collaborators.containsKey(roleName)) {
            final List<ServiceDescriptor> collaborators = serviceDescriptor.collaborators.get(roleName);
            return collaboratorsExtractor.apply(collaborators, serviceGenerator);
        } else if (defaultCollaboratorDescriptors.isPresent()) {
            return collaboratorsExtractor.apply(defaultCollaboratorDescriptors.get(), serviceGenerator);
        }
        throw new IllegalStateException(String.format("missing collaborator: %s (%s)", roleName, methodParameterType.getCanonicalName()));
    }
}
