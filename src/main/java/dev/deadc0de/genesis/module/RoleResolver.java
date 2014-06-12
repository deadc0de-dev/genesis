package dev.deadc0de.genesis.module;

import dev.deadc0de.genesis.ServiceDescriptor;
import dev.deadc0de.genesis.ServiceGenerator;
import java.util.Optional;
import java.util.function.BiFunction;

public class RoleResolver implements BiFunction<ServiceGenerator, ServiceDescriptor, Object> {

    private final String roleName;
    private final Class<?> collaboratorType;
    private final Optional<ServiceDescriptor> defaultCollaboratorDescriptor;

    public RoleResolver(java.lang.reflect.Parameter methodParameter) {
        if (!methodParameter.isAnnotationPresent(Role.class)) {
            throw new IllegalArgumentException("method parameter must be annotated with @Role");
        }
        roleName = methodParameter.getAnnotation(Role.class).value();
        collaboratorType = methodParameter.getType();
        defaultCollaboratorDescriptor = Optional.ofNullable(methodParameter.getAnnotation(Default.class)).map(Default::value).map(ServiceDescriptor::notParameterized);
    }

    @Override
    public Object apply(ServiceGenerator serviceGenerator, ServiceDescriptor serviceDescriptor) {
        if (serviceDescriptor.collaborators.containsKey(roleName)) {
            final ServiceDescriptor collaboratorDescriptor = serviceDescriptor.collaborators.get(roleName);
            return serviceGenerator.generate(collaboratorType, collaboratorDescriptor);
        } else if (defaultCollaboratorDescriptor.isPresent()) {
            final ServiceDescriptor collaboratorDescriptor = defaultCollaboratorDescriptor.get();
            return serviceGenerator.generate(collaboratorType, collaboratorDescriptor);
        }
        throw new IllegalArgumentException(String.format("missing collaborator with role %s for service %s", roleName, serviceDescriptor.name));
    }
}
