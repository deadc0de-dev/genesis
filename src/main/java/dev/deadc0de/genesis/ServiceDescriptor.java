package dev.deadc0de.genesis;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ServiceDescriptor {

    public final String name;
    public final Map<String, List<String>> configuration;
    public final Map<String, List<ServiceDescriptor>> collaborators;

    public ServiceDescriptor(String name, Map<String, List<String>> configuration, Map<String, List<ServiceDescriptor>> collaborators) {
        this.name = name;
        this.configuration = configuration;
        this.collaborators = collaborators;
    }

    public static ServiceDescriptor notParameterized(String name) {
        return new ServiceDescriptor(name, Collections.emptyMap(), Collections.emptyMap());
    }
}
