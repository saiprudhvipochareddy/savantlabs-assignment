# GitHub Repository Activity Tracker

Connectors framework to fetch **GitHub repositories** and their **recent commits** for a given user or organization.  
It demonstrates handling GitHub REST API with **rate-limit handling**, **error retries**, and clean code practices.

---

## Features Implemented

- Fetches repositories for a given GitHub user/org.
- Retrieves latest commits per repository.
- Handles **rate limits** with exponential backoff.
- Graceful error handling for `401 Unauthorized`, `403 Forbidden`, and `404 Not Found`.
- Clean separation of concerns and extensible design.

---

## Tech Stack

- **Java 17**
- **Spring Boot**
- **Maven**
- GitHub REST API

List of repos (user)
https://docs.github.com/en/rest/repos/repos?apiVersion=2022-11-28#list-repositories-for-a-user

List of repos (organization)
https://docs.github.com/en/rest/repos/repos?apiVersion=2022-11-28#list-organization-repositories

List of commits