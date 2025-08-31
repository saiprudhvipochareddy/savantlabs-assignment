package com.savantlabs.adapters.controller;

import com.savantlabs.adapters.business.RepositoryAdapterFactory;
import com.savantlabs.adapters.enums.RepositoryAdapterType;
import com.savantlabs.adapters.model.RepositoryActivity;
import com.savantlabs.adapters.model.RepositoryRequest;
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
    public ResponseEntity<List<RepositoryActivity>> fetchActivitiesByUser(
            @RequestBody RepositoryRequest repositoryRequest) throws Exception {
        RepositoryAdapterType repositoryAdapterType =
                RepositoryAdapterType.valueOf(repositoryRequest.getRepositoryType());
        return ResponseEntity.ok(repositoryAdapterFactory
                .getRepositoryAdapterService(repositoryAdapterType).fetchActivity(repositoryRequest));
    }
}
