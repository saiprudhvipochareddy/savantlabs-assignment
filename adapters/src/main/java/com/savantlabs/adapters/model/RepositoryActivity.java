package com.savantlabs.adapters.model;

import java.util.List;

public class RepositoryActivity {
    private RepositorySummary repo;
    private List<CommitSummary> commits;

    public RepositoryActivity(RepositorySummary repo, List<CommitSummary> commits) {
        this.commits = commits;
        this.repo = repo;
    }

    public List<CommitSummary> getCommits() {
        return commits;
    }

    public void setCommits(List<CommitSummary> commits) {
        this.commits = commits;
    }

    public RepositorySummary getRepo() {
        return repo;
    }

    public void setRepo(RepositorySummary repo) {
        this.repo = repo;
    }
}
