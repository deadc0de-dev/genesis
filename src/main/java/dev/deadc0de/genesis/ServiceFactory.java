package dev.deadc0de.genesis;

public interface ServiceFactory<S> {

    String serviceName();

    Class<S> serviceType();

    S create(ServiceGenerator serviceGenerator, ServiceDescriptor serviceDescriptor);
}
