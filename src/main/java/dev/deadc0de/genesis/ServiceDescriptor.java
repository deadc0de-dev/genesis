package dev.deadc0de.genesis;

import java.util.Collections;
import java.util.Map;

public class ServiceDescriptor {

    public final String name;
    public final Map<String, String> configuration;
    public final Map<String, ServiceDescriptor> collaborators;

    public ServiceDescriptor(String name, Map<String, String> configuration, Map<String, ServiceDescriptor> collaborators) {
        this.name = name;
        this.configuration = configuration;
        this.collaborators = collaborators;
    }

    public static ServiceDescriptor notParameterized(String name) {
        return new ServiceDescriptor(name, Collections.emptyMap(), Collections.emptyMap());
    }
}
