package com.savantlabs.adapters.helpers;

import com.savantlabs.adapters.exception.CustomException;
import com.savantlabs.adapters.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Instant;
import java.util.*;

@Service
public class GitHubClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl;
    private final Integer perRepoCommits;
    private final String userReposUrl;
    private final String orgReposUrl;
    private final String repoCommitsUrl;

    public GitHubClient(@Value("${github.baseUrl}") String baseUrl,
                        @Value("${github.max.commits}") Integer perRepoCommits,
                        @Value("${github.user.repos.url}") String userReposUrl,
                        @Value("${github.org.repos.url}") String orgReposUrl,
                        @Value("${github.repo.commits}") String repoCommitsUrl) {
        this.baseUrl = baseUrl;
        this.perRepoCommits = perRepoCommits;
        this.userReposUrl = userReposUrl;
        this.orgReposUrl = orgReposUrl;
        this.repoCommitsUrl = repoCommitsUrl;
    }

    public List<RepositoryActivity> fetchUserReposWithCommits(String username, String token) throws Exception {
        String url = baseUrl + userReposUrl.replace("{userName}", username);
        return fetchReposAndCommits(url, username, token);
    }

    public List<RepositoryActivity> fetchOrgReposWithCommits(String orgName, String token) throws Exception {
        String url = baseUrl + orgReposUrl.replace("{orgName}", orgName);
        return fetchReposAndCommits(url, orgName, token);
    }

    private List<RepositoryActivity> fetchReposAndCommits(String url, String owner, String token) throws Exception {
        List<GitHubRepository> gitHubRepositories =
                fetchPaged(url, token, new ParameterizedTypeReference<List<GitHubRepository>>() {});

        return gitHubRepositories.stream()
                .map(gitHubRepository -> {
                    List<CommitSummary> commits = fetchCommits(owner, gitHubRepository.getName(), token);
                    RepositorySummary repositorySummary = new RepositorySummary();
                    repositorySummary.setName(gitHubRepository.getName());
                    repositorySummary.setFullName(gitHubRepository.getFullName());
                    repositorySummary.setHtmlUrl(gitHubRepository.getHtmlUrl());
                    repositorySummary.setFork(gitHubRepository.isFork());
                    repositorySummary.setDefaultBranch(gitHubRepository.getDefaultBranch());
                    return new RepositoryActivity(repositorySummary, commits);
                }).toList();
    }

    private <T> List<T> fetchPaged(String firstUrl, String token, ParameterizedTypeReference<List<T>> typeRef) throws Exception {
        List<T> out = new ArrayList<>();
        String next = firstUrl;

        while (next != null) {
            ResponseEntity<List<T>> resp = restTemplateExchangeGet(next, typeRef, token);

            resp = handleRateLimitAndErrors(resp, next, typeRef, token);

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

    public List<CommitSummary> fetchCommits(String owner, String repo, String token) {
        String url = baseUrl +
                repoCommitsUrl.replace("{userName}", owner).replace("{repoName}", repo) + perRepoCommits;

        ResponseEntity<List<GitHubCommit>> gitHubCommits
                = restTemplateExchangeGet(url, new ParameterizedTypeReference<List<GitHubCommit>>() {
        }, token);

        return getCommitSummaries(gitHubCommits);
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

    private <T> ResponseEntity<List<T>> restTemplateExchangeGet(String url, ParameterizedTypeReference<List<T>> typeRef, String token) {
        try {
            return restTemplate.exchange(
                    URI.create(url),
                    HttpMethod.GET,
                    new HttpEntity<>(headers(token)),
                    typeRef
            );
        } catch (HttpStatusCodeException ex) {
            String errorBody = ex.getResponseBodyAsString();
            HttpStatusCode status = ex.getStatusCode();

            System.err.println("GitHub API error: " + status + " → " + errorBody);

            return ResponseEntity.status(status).body(Collections.emptyList());
        } catch (Exception ex) {
            throw new RuntimeException("Unexpected error calling GitHub API: " + url, ex);
        }
    }

    private HttpHeaders headers(String token) {
        HttpHeaders h = new HttpHeaders();
        h.set(HttpHeaders.ACCEPT, "application/vnd.github+json");
        h.set(HttpHeaders.USER_AGENT, "gh-commits-api/1.0");
        if (token != null && !token.isBlank()) h.setBearerAuth(token);
        return h;
    }

    private <T> ResponseEntity<List<T>> handleRateLimitAndErrors(ResponseEntity<List<T>> response,
                                                                 String url,
                                                                 ParameterizedTypeReference<List<T>> typeRef,
                                                                 String token) throws Exception {
        HttpStatusCode status = response.getStatusCode();

        if (status == HttpStatus.UNAUTHORIZED) {
            System.err.println("Unauthorized: missing or invalid GitHub token");
            throw new CustomException(status, "Unauthorized: missing or invalid GitHub token");
        }

        if (status == HttpStatus.FORBIDDEN) {
            HttpHeaders headers = response.getHeaders();
            String remaining = headers.getFirst("X-RateLimit-Remaining");
            String reset = headers.getFirst("X-RateLimit-Reset");

            if ("0".equals(remaining) && reset != null) {
                long resetEpoch = Long.parseLong(reset);
                long waitMs = (resetEpoch - Instant.now().getEpochSecond()) * 1000L;

                if (waitMs > 0) {
                    System.out.println("Rate limited. Waiting " + (waitMs / 1000));
                    try {
                        Thread.sleep(waitMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                return restTemplateExchangeGet(url, typeRef, token);
            }

            System.err.println("Forbidden: rate-limit exceeded");
            throw new CustomException(status, "Forbidden: rate-limit exceeded");
        }

        if (status == HttpStatus.NOT_FOUND) {
            System.err.println("Not found: " + url);
            throw new CustomException(status, "Not found: " + url);
        }

        if (!status.is2xxSuccessful()) {
            System.err.println("GitHub error: " + status);
            throw new CustomException(status, "GitHub error: " + status);
        }

        return response;
    }

}
