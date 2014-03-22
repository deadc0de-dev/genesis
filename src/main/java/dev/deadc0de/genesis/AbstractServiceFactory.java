package dev.deadc0de.genesis;

public abstract class AbstractServiceFactory<S> implements ServiceFactory<S> {

    private final String serviceName;
    private final Class<S> serviceType;

    public AbstractServiceFactory(String serviceName, Class<S> serviceType) {
        this.serviceName = serviceName;
        this.serviceType = serviceType;
    }

    @Override
    public String serviceName() {
        return serviceName;
    }

    @Override
    public Class<S> serviceType() {
        return serviceType;
    }
}
