package com.savantlabs.adapters.controller;

import com.savantlabs.adapters.business.RepositoryAdapterFactory;
import com.savantlabs.adapters.enums.RepositoryAdapterType;
import com.savantlabs.adapters.exception.CustomException;
import com.savantlabs.adapters.model.RepositoryActivity;
import com.savantlabs.adapters.model.RepositoryRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller to handle repository activity-related API requests.
 */
@RestController
@RequestMapping("/api/repository")
public class RepositoryController {
    private final RepositoryAdapterFactory repositoryAdapterFactory;

    public RepositoryController(RepositoryAdapterFactory repositoryAdapterFactory) {
        this.repositoryAdapterFactory = repositoryAdapterFactory;
    }

    /**
     * Handles POST request to fetch repository activities based on the provided request details.
     */
    @PostMapping("/v1/activity")
    public ResponseEntity<List<RepositoryActivity>> fetchRepositoryActivities(
            @RequestBody RepositoryRequest repositoryRequest,
            @RequestHeader("Authorization") String authHeader) throws Exception {
        String token = authHeader.replace("Bearer ", "");
        repositoryRequest.setToken(token);
        RepositoryAdapterType repositoryAdapterType =
                RepositoryAdapterType.valueOf(repositoryRequest.getRepositoryType());
        if (repositoryAdapterFactory.getRepositoryAdapterService(repositoryAdapterType).isPresent()) {
            return ResponseEntity.ok(repositoryAdapterFactory
                    .getRepositoryAdapterService(repositoryAdapterType).get().fetchRepositoryActivities(repositoryRequest));
        }

        throw new CustomException(HttpStatusCode.valueOf(400), "repositoryAdapterType: is not implemented");
    }
}
