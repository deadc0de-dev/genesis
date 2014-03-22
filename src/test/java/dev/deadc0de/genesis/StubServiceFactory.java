package dev.deadc0de.genesis;

public class StubServiceFactory<S> extends AbstractServiceFactory<S> {

    private final S service;

    public StubServiceFactory(S service, String serviceName, Class<S> serviceType) {
        super(serviceName, serviceType);
        this.service = service;
    }

    @Override
    public S create(ServiceGenerator serviceGenerator, ServiceDescriptor serviceDescriptor) {
        return service;
    }
}
