package dev.deadc0de.genesis;

import java.util.ArrayList;
import java.util.List;

public class ServiceGenerationException extends IllegalStateException {

    private final ServiceFactory<?> serviceFactory;

    public ServiceGenerationException(ServiceFactory<?> serviceFactory, Throwable cause) {
        super(cause.getMessage(), cause);
        this.serviceFactory = serviceFactory;
    }

    public List<ServiceFactory<?>> serviceGenerationStack() {
        final List<ServiceFactory<?>> stack;
        if (getCause() instanceof ServiceGenerationException) {
            stack = ((ServiceGenerationException) getCause()).serviceGenerationStack();
        } else {
            stack = new ArrayList<>();
        }
        stack.add(serviceFactory);
        return stack;
    }
}
