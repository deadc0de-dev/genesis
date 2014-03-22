package dev.deadc0de.genesis;

import java.util.Optional;

public class CapturingServiceFactory<S> extends AbstractServiceFactory<S> {

    private Optional<ServiceGenerator> capturedGenerator;
    private Optional<ServiceDescriptor> capturedDescriptor;

    public CapturingServiceFactory(String serviceName, Class<S> serviceType) {
        super(serviceName, serviceType);
        capturedGenerator = Optional.empty();
        capturedDescriptor = Optional.empty();
    }

    @Override
    public S create(ServiceGenerator serviceGenerator, ServiceDescriptor serviceDescriptor) {
        capturedGenerator = Optional.of(serviceGenerator);
        capturedDescriptor = Optional.of(serviceDescriptor);
        return null;
    }

    public Optional<ServiceGenerator> capturedGenerator() {
        return capturedGenerator;
    }

    public Optional<ServiceDescriptor> capturedDescriptor() {
        return capturedDescriptor;
    }
}
