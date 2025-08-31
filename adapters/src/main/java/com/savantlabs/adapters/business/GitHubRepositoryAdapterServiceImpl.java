package com.savantlabs.adapters.business;

import com.savantlabs.adapters.enums.OwnerType;
import com.savantlabs.adapters.enums.RepositoryAdapterType;
import com.savantlabs.adapters.exception.CustomException;
import com.savantlabs.adapters.helpers.GitHubClient;
import com.savantlabs.adapters.model.RepositoryActivity;
import com.savantlabs.adapters.model.RepositoryRequest;
import com.savantlabs.adapters.service.RepositoryAdapterService;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service implementation for integrating with GitHub repositories.
 **/
@Service
public class GitHubRepositoryAdapterServiceImpl implements RepositoryAdapterService {

    private final GitHubClient gitHubClient;

    public GitHubRepositoryAdapterServiceImpl(GitHubClient gitHubClient) {
        this.gitHubClient = gitHubClient;
    }

    /**
     * Fetches repository activity data for the given request.
     *
     * @param repositoryRequest the repository request information
     * @return a list of repository activity entries
     */
    @Override
    public List<RepositoryActivity> fetchRepositoryActivities(RepositoryRequest repositoryRequest) throws Exception {
        String owner = repositoryRequest.getOwner();
        String ownerType = repositoryRequest.getOwnerType();
        if (OwnerType.USER.toString().equals(ownerType)) {
            System.out.println("Fetching at the user level");
            return gitHubClient.fetchUserReposWithCommits(owner, repositoryRequest.getToken());
        } else if (OwnerType.ORGANIZATION.toString().equals(ownerType)) {
            System.out.println("Fetching at the organization level");
            return gitHubClient.fetchOrgReposWithCommits(owner, repositoryRequest.getToken());
        }

        throw new CustomException(HttpStatusCode.valueOf(400), "Owner Type: " + ownerType + " is not valid.");
    }

    /**
     * Returns the type of repository supported by this adapter.
     *
     * @return the repository adapter type
     */
    @Override
    public RepositoryAdapterType getRepository() {
        return RepositoryAdapterType.GIT_HUB_REPOSITORY;
    }
}
