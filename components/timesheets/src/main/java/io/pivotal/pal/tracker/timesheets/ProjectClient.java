package io.pivotal.pal.tracker.timesheets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.web.client.RestOperations;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ProjectClient {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final RestOperations restOperations;
    private final String endpoint;
    private final ConcurrentMap<Long, ProjectInfo> projectCache = new ConcurrentHashMap<>();

    public ProjectClient(RestOperations restOperations, String registrationServerEndpoint) {
        this.restOperations = restOperations;
        this.endpoint = registrationServerEndpoint;
    }

    @CircuitBreaker(name = "project", fallbackMethod = "getProjectFromCache")
    public ProjectInfo getProject(long projectId) {
        projectCache.putIfAbsent(projectId, restOperations.getForObject(endpoint + "/projects/" + projectId, ProjectInfo.class));
        return projectCache.get(projectId);
    }

    public ProjectInfo getProjectFromCache(long projectId, Throwable cause) {
        if(projectCache.containsKey(projectId)) {
            logger.info("Getting project with id {} from cache", projectId);
            return projectCache.get(projectId);
        } else {
            logger.error("Project not found in cache, returning null", projectId);
            return null;
        }
    }
}
