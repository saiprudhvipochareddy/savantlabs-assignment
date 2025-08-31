package com.savantlabs.adapters.model;

import java.util.List;

public class RepositoryActivity {
    private RepositorySummary repositorySummary;
    private List<CommitSummary> commits;

    public RepositoryActivity(RepositorySummary repositorySummary, List<CommitSummary> commits) {
        this.commits = commits;
        this.repositorySummary = repositorySummary;
    }

    public List<CommitSummary> getCommits() {
        return commits;
    }

    public void setCommits(List<CommitSummary> commits) {
        this.commits = commits;
    }

    public RepositorySummary getRepositorySummary() {
        return repositorySummary;
    }

    public void setRepositorySummary(RepositorySummary repositorySummary) {
        this.repositorySummary = repositorySummary;
    }
}
