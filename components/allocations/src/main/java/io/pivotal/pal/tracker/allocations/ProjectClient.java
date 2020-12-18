package io.pivotal.pal.tracker.allocations;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.web.client.RestOperations;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ProjectClient {

    private final RestOperations restOperations;
    private final String registrationServerEndpoint;
    private ConcurrentMap<Long, ProjectInfo> projectCache;

    public ProjectClient(RestOperations restOperations, String registrationServerEndpoint) {
        this.restOperations= restOperations;
        this.registrationServerEndpoint = registrationServerEndpoint;
        projectCache = new ConcurrentHashMap<>();
    }

    @CircuitBreaker(name = "project", fallbackMethod = "getProjectFromCache")
    public ProjectInfo getProject(long projectId) {
        projectCache.putIfAbsent(projectId, restOperations.getForObject(registrationServerEndpoint + "/projects/" + projectId, ProjectInfo.class));
        return projectCache.get(projectId);
    }

    public ProjectInfo getProjectFromCache(long projectId, Throwable cause) {
        return projectCache.get(projectId);
    }
}
