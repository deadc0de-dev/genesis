package dev.deadc0de.genesis;

public interface ServiceGenerator {

    <S> S generate(Class<S> serviceType, ServiceDescriptor serviceDescriptor);
}
