package dev.deadc0de.genesis;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GenerationContext implements ServiceGenerator {

    private final Map<Class, Map<String, ServiceFactory>> context;

    public GenerationContext(Stream<ServiceFactory> serviceFactories) {
        this.context = serviceFactories.collect(
                Collectors.groupingBy(ServiceFactory::serviceType,
                        Collectors.groupingBy(ServiceFactory::serviceName,
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
        final Map<String, ServiceFactory<S>> serviceFactories = Map.class.cast(context.getOrDefault(serviceType, Collections.emptyMap()));
        if (!serviceFactories.containsKey(serviceDescriptor.name)) {
            throw new IllegalStateException(String.format("unknown service %s of type %s", serviceDescriptor.name, serviceType));
        }
        final ServiceFactory<S> serviceFactory = serviceFactories.get(serviceDescriptor.name);
        return serviceFactory.create(this, serviceDescriptor);
    }
}
