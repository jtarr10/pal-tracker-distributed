package io.pivotal.pal.tracker.backlog;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.web.client.RestOperations;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ProjectClient {

    private final RestOperations restOperations;
    private final String endpoint;
    private ConcurrentMap<Long, ProjectInfo> projectCache;

    public ProjectClient(RestOperations restOperations, String registrationServerEndpoint) {
        this.restOperations = restOperations;
        this.endpoint = registrationServerEndpoint;
        this.projectCache = new ConcurrentHashMap<>();
    }

    @CircuitBreaker(name = "project", fallbackMethod = "getProjectFromCache")
    public ProjectInfo getProject(long projectId) {
        projectCache.putIfAbsent(projectId, restOperations.getForObject(endpoint + "/projects/" + projectId, ProjectInfo.class));
        return projectCache.get(projectId);
    }

    public ProjectInfo getProjectFromCache(long projectId, Throwable cause) {
        return projectCache.get(projectId);
    }
}
