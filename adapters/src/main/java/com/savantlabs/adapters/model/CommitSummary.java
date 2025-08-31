package com.savantlabs.adapters.model;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public class CommitSummary {
    private String sha;
    private String message;
    private String authorName;
    private String authorEmail;
    private String htmlUrl;
    private OffsetDateTime authoredAt;

    public CommitSummary(String authorEmail, String authorName, String htmlUrl, String message, String sha, OffsetDateTime authoredAt) {
        this.authorEmail = authorEmail;
        this.authorName = authorName;
        this.htmlUrl = htmlUrl;
        this.message = message;
        this.sha = sha;
        this.authoredAt = authoredAt;
    }

    public OffsetDateTime getAuthoredAt() {
        return authoredAt;
    }

    public void setAuthoredAt(OffsetDateTime authoredAt) {
        this.authoredAt = authoredAt;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }
}
