package com.savantlabs.adapters.model;

public class RepositoryRequest {
    private String owner;
    private String ownerType;
    private String repositoryType;

    public String getOwner() {
        return owner;
    }

    public String getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(String ownerType) {
        this.ownerType = ownerType;
    }

    public String getRepositoryType() {
        return repositoryType;
    }

    public void setRepositoryType(String repositoryType) {
        this.repositoryType = repositoryType;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
