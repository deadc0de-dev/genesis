package dev.deadc0de.genesis;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GenerationContext implements ServiceGenerator {

    private final Map<String, Map<Class, ServiceFactory>> context;

    public GenerationContext(Stream<ServiceFactory> serviceFactories) {
        this.context = serviceFactories.collect(
                Collectors.groupingBy(ServiceFactory::serviceName,
                        Collectors.groupingBy(ServiceFactory::serviceType,
                                Collectors.reducing(null, GenerationContext::ensureNoDuplicateFactories))));
    }

    private static ServiceFactory ensureNoDuplicateFactories(ServiceFactory former, ServiceFactory latter) {
        if (former != null) {
            throw new IllegalArgumentException(String.format("found two service factories with same name and type (%s, %s)", former.serviceName(), former.serviceType()));
        }
        return latter;
    }

    @Override
    public <S> S generate(Class<S> serviceType, ServiceDescriptor serviceDescriptor) {
        final Map<Class, ServiceFactory> serviceFactories = context.getOrDefault(serviceDescriptor.name, Collections.emptyMap());
        final ServiceFactory<S> serviceFactory = selectServiceFactoryByWidestSubtype(serviceFactories, serviceType);
        return serviceFactory.create(this, serviceDescriptor);
    }

    private static <S> ServiceFactory<S> selectServiceFactoryByWidestSubtype(Map<Class, ServiceFactory> serviceFactories, Class<S> serviceType) {
        final Set<Class> serviceTypes = new HashSet<>();
        serviceFactories.keySet().stream().filter(serviceType::isAssignableFrom).forEach(type -> {
            serviceTypes.removeIf(supertype -> supertype.isAssignableFrom(type));
            if (serviceTypes.stream().noneMatch(type::isAssignableFrom)) {
                serviceTypes.add(type);
            }
        });
        if (serviceTypes.isEmpty()) {
            throw new IllegalStateException("cannot find a service factory for the requested service");
        }
        if (serviceTypes.size() != 1) {
            final String collisions = serviceTypes.stream().map(Class::getCanonicalName).collect(Collectors.joining(",", "[", "]"));
            throw new IllegalStateException("found multiple service factory types for the requested service: " + collisions);
        }
        final Class type = serviceTypes.iterator().next();
        return (ServiceFactory<S>) serviceFactories.get(type);
    }
}
