package com.savantlabs.adapters.business;

import com.savantlabs.adapters.enums.OwnerType;
import com.savantlabs.adapters.enums.RepositoryAdapterType;
import com.savantlabs.adapters.helpers.GitHubClient;
import com.savantlabs.adapters.model.RepositoryActivity;
import com.savantlabs.adapters.model.RepositoryRequest;
import com.savantlabs.adapters.service.RepositoryAdapterService;
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
    public List<RepositoryActivity> fetchActivity(RepositoryRequest repositoryRequest) throws Exception {
//        var repos = gitHubClient.(repositoryRequest);
//        List<RepositoryActivity> result = new ArrayList<>();
//        for (var repo : repos) {
//            var commits = gitHubClient.listCommits(owner, repo.getName(), perRepoCommits);
//            result.add(new RepoActivity(repo, commits));
//        }
//        return result;
        String owner = repositoryRequest.getOwner();
        String ownerType = repositoryRequest.getOwnerType();
        if (OwnerType.USER.toString().equals(ownerType)) {
            System.out.println("Fetching at the user level");
            return gitHubClient.fetchUserReposWithCommits(owner);
        } else if (OwnerType.ORGANIZATION.toString().equals(ownerType)) {
            System.out.println("Fetching at the organization level");
            return gitHubClient.fetchOrgReposWithCommits(owner);
        }

        throw new Exception("Owner Type: " + ownerType + " is not valid.");
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
