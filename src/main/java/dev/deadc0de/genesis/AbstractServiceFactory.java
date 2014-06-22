package dev.deadc0de.genesis;

import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ServiceFactory)) {
            return false;
        }
        final ServiceFactory<?> other = (ServiceFactory<?>) obj;
        return Objects.equals(serviceName, other.serviceName()) && Objects.equals(serviceType, other.serviceType());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(serviceName);
        hash = 59 * hash + Objects.hashCode(serviceType);
        return hash;
    }

    @Override
    public String toString() {
        return "(" + serviceName() + ':' + serviceType().getCanonicalName() + ')';
    }
}
