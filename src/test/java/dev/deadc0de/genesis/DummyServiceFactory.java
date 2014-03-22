package dev.deadc0de.genesis;

public class DummyServiceFactory<S> extends AbstractServiceFactory<S> {

    public DummyServiceFactory(String serviceName, Class<S> serviceType) {
        super(serviceName, serviceType);
    }

    @Override
    public S create(ServiceGenerator serviceGenerator, ServiceDescriptor service) {
        throw new UnsupportedOperationException("dummy implementation");
    }
}
