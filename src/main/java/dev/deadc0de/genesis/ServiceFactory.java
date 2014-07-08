package dev.deadc0de.genesis;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ServiceFactory<S> {

    String serviceName();

    Class<S> serviceType();

    Map<String, Optional<List<String>>> parameters();

    Map<String, Map.Entry<Class<?>, Optional<List<String>>>> roles();

    S create(ServiceGenerator serviceGenerator, ServiceDescriptor serviceDescriptor);
}
