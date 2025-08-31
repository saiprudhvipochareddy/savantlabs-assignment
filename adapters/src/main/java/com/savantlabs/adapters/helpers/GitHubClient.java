package com.savantlabs.adapters.helpers;

import com.savantlabs.adapters.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.*;

@Service
public class GitHubClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl;
    private final String token;
    private final Integer perRepoCommits;
    private final String userReposUrl;
    private final String orgReposUrl;

    public GitHubClient(@Value("${github.baseUrl}") String baseUrl,
                        @Value("${github.token:}") String token,
                        @Value("${github.max.commits}") Integer perRepoCommits,
                        @Value("${github.user.repos.url}") String userReposUrl,
                        @Value("${github.org.repos.url}") String orgReposUrl) {
        this.baseUrl = baseUrl;
        this.token = token;
        this.perRepoCommits = perRepoCommits;
        this.userReposUrl = userReposUrl;
        this.orgReposUrl = orgReposUrl;
    }

    public List<RepositoryActivity> fetchUserReposWithCommits(String username) {
        String url = baseUrl + userReposUrl.replace("{userName}", username);
        return fetchReposAndCommits(url, username, token);
    }

    public List<RepositoryActivity> fetchOrgReposWithCommits(String orgName) {
        String url = baseUrl + orgReposUrl.replace("{orgName}", orgName);
        return fetchReposAndCommits(url, orgName, token);
    }

    private List<RepositoryActivity> fetchReposAndCommits(String url, String owner, String token) {
        List<GitHubRepository> gitHubRepositories =
                fetchPaged(url, token, new ParameterizedTypeReference<List<GitHubRepository>>() {});

        return gitHubRepositories.stream()
                .map(gitHubRepository -> {
                    List<CommitSummary> commits = fetchCommits(owner, gitHubRepository.getName());
                    RepositorySummary repositorySummary = new RepositorySummary();
                    repositorySummary.setName(gitHubRepository.getName());
                    repositorySummary.setFullName(gitHubRepository.getFullName());
                    repositorySummary.setHtmlUrl(gitHubRepository.getHtmlUrl());
                    repositorySummary.setFork(gitHubRepository.isFork());
                    repositorySummary.setDefaultBranch(gitHubRepository.getDefaultBranch());
                    return new RepositoryActivity(repositorySummary, commits);
                }).toList();
    }

    public List<CommitSummary> fetchCommits(String owner, String repo) {
        String url = baseUrl + "/repos/" + owner + "/" + repo + "/commits?per_page=" + perRepoCommits;
        
        try {

            ResponseEntity<List<GitHubCommit>> gitHubCommits
                    = restTemplateExchangeGet(url, new ParameterizedTypeReference<List<GitHubCommit>>() {
            });

            gitHubCommits = handleRateLimitAndErrors(gitHubCommits, url, new ParameterizedTypeReference<List<GitHubCommit>>() {
            });

            return getCommitSummaries(gitHubCommits);

        } catch (Exception e) {
//            return handleRateLimitAndErrors(gitHubCommits, url, new ParameterizedTypeReference<List<GitHubCommit>>() {
//            });
        }
        return null;
    }

    private static List<CommitSummary> getCommitSummaries(ResponseEntity<List<GitHubCommit>> gitHubCommits) {
        List<CommitSummary> commits = new ArrayList<>();
        for (GitHubCommit dto : Objects.requireNonNull(gitHubCommits.getBody())) {
            Commit commit = dto.getCommit();
            Author author = commit.getAuthor();

            CommitSummary summary = new CommitSummary(
                    author.getEmail(),
                    author.getName(),
                    dto.getHtml_url(),
                    commit.getMessage(),
                    dto.getSha(),
                    author.getDate()
            );

            commits.add(summary);
        }
        return commits;
    }

    public <T> List<T> fetchPaged(String firstUrl, String token, ParameterizedTypeReference<List<T>> typeRef) {
        List<T> out = new ArrayList<>();
        String next = firstUrl;

        while (next != null) {
            ResponseEntity<List<T>> resp = restTemplateExchangeGet(next, typeRef);

            resp = handleRateLimitAndErrors(resp, next, typeRef);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                out.addAll(resp.getBody());
                next = nextLink(resp.getHeaders()).orElse(null);
                System.out.println(next);
            } else {
                break;
            }
        }

        return out;
    }

    private Optional<String> nextLink(HttpHeaders headers) {
        List<String> links = headers.get(HttpHeaders.LINK);
        if (links == null || links.isEmpty()) {
            return Optional.empty();
        }

        for (String header : links) {
            String[] parts = header.split(",\\s*");
            for (String part : parts) {
                int start = part.indexOf("<") + 1;
                int end = part.indexOf(">", start);
                if (start > 0 && end > start) {
                    String url = part.substring(start, end);
                    if (part.contains("rel=\"next\"")) {
                        return Optional.of(url);
                    }
                }
            }
        }

        return Optional.empty();
    }

    private <T> ResponseEntity<List<T>> restTemplateExchangeGet(String url, ParameterizedTypeReference<List<T>> typeRef) {
        return restTemplate.exchange(
                URI.create(url),
                HttpMethod.GET,
                new HttpEntity<>(headers()),
                typeRef
        );
    }

    private HttpHeaders headers() {
        HttpHeaders h = new HttpHeaders();
        h.set(HttpHeaders.ACCEPT, "application/vnd.github+json");
        h.set(HttpHeaders.USER_AGENT, "gh-commits-api/1.0");
        if (token != null && !token.isBlank()) h.setBearerAuth(token);
        return h;
    }

    // Check this out once
    private <T> ResponseEntity<List<T>> handleRateLimitAndErrors(ResponseEntity<List<T>> response,
                                                                 String url,
                                                                 ParameterizedTypeReference<List<T>> typeRef) {
        int status = response.getStatusCode().value();

        if (status == 401) {
            throw new RuntimeException("Unauthorized: missing or invalid GitHub token");
        }

        if (status == 403) {
            HttpHeaders headers = response.getHeaders();
            String remaining = headers.getFirst("X-RateLimit-Remaining");
            String reset = headers.getFirst("X-RateLimit-Reset");

            // Only wait if it's really a rate limit
            if ("0".equals(remaining) && reset != null) {
                long resetEpoch = Long.parseLong(reset);
                long waitMs = (resetEpoch - Instant.now().getEpochSecond()) * 1000L;
                if (waitMs > 0) {
                    System.out.println("Rate limited. Waiting " + (waitMs / 1000) + "s...");
                    try {
                        Thread.sleep(waitMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                return restTemplateExchangeGet(url, typeRef);
            }

            throw new RuntimeException("Forbidden: request blocked (not rate-limit?)");
        }

        if (status == 404) {
            throw new RuntimeException("Not found: " + url);
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("GitHub error: " + response.getStatusCode());
        }

        return response;
    }

}
