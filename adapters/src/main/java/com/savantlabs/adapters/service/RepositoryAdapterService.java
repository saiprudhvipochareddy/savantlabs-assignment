package com.savantlabs.adapters.service;

import com.savantlabs.adapters.enums.RepositoryAdapterType;
import com.savantlabs.adapters.model.RepositoryActivity;
import com.savantlabs.adapters.model.RepositoryRequest;

import java.util.List;

public interface RepositoryAdapterService {

    List<RepositoryActivity> fetchActivity(RepositoryRequest repositoryRequest) throws Exception;

    RepositoryAdapterType getRepository();
}
