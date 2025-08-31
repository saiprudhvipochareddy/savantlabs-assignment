# GitHub Repository Activity Tracker

Connectors framework to fetch **GitHub repositories** and their **recent commits** for a given user or organization.  
It demonstrates handling GitHub REST API with **rate-limit handling**, **error retries**, **handle pagination**
and clean code practices.

---

## Tech Stack

- **Java 17+**
- **Spring Boot**
- **Maven**
- **GitHub REST API**

---

## GitHub REST API References

### List of repositories for a user
Lists repositories for the specified user.  
Reference: https://docs.github.com/en/rest/repos/repos?apiVersion=2022-11-28#list-repositories-for-a-user

### List of repositories for an organization
Lists repositories for the specified organization.  
Reference: https://docs.github.com/en/rest/repos/repos?apiVersion=2022-11-28#list-organization-repositories

### List of commits by username and repository name
Lists commits for the given user and repository.  
Reference: https://docs.github.com/en/rest/commits/commits?apiVersion=2022-11-28#list-commits

---

## Features Implemented

- Fetches repositories for a GitHub user or organization with pagination handled.
- Retrieves the 20 most recent commits per repository.
- Maps responses into structured Java POJOs
- Handles rate limits.
- Provides error handling for 401 Unauthorized, 403 Forbidden, and 404 Not Found.
- Follows clean separation of concerns and extensible design.
- Exposes a REST endpoint to trigger the fetch.

---

## Local Setup to Run the REST API

- Clone the project locally and import it as a Maven project
- Start the application either from your IDE run option or by using the command:
```bash
  mvn spring-boot:run
 ```
- Once the application is running, use the following endpoint:
### Request:
```curl
  curl --location 'http://localhost:8081/api/repository/v1/activity' \
--header 'Authorization: ${token}' \
--header 'Content-Type: application/json' \
--data '{
"owner": "saiprudhvipochareddy",
"ownerType": "USER",
"repositoryType": "GIT_HUB_REPOSITORY"
}'
```
### Response:
```json
[
  {
    "repositorySummary": {
      "name": "fullstack",
      "fullName": "saiprudhvipochareddy/fullstack",
      "htmlUrl": "https://github.com/saiprudhvipochareddy/fullstack",
      "fork": false,
      "defaultBranch": "main"
    },
    "commits": [
      {
        "authorEmail": "80394708+saiprudhvipochareddy@users.noreply.github.com",
        "authorName": "saiprudhvipochareddy",
        "htmlUrl": "https://github.com/saiprudhvipochareddy/fullstack/commit/5daac73442dd85af4d5bb40aa97d223b6b8ecde1",
        "message": "Initial commit",
        "sha": "5daac73442dd85af4d5bb40aa97d223b6b8ecde1",
        "authoredAt": "2021-03-10T12:22:45Z"
      }
    ]
  },
  {
    "repositorySummary": {
      "name": "LLD",
      "fullName": "saiprudhvipochareddy/LLD",
      "htmlUrl": "https://github.com/saiprudhvipochareddy/LLD",
      "fork": false,
      "defaultBranch": "main"
    },
    "commits": [
      {
        "authorEmail": "spochareddy@liftlab.com",
        "authorName": "saipr",
        "htmlUrl": "https://github.com/saiprudhvipochareddy/LLD/commit/ae9c63d2d01bcd095d95812483fcf136b888f923",
        "message": "initial commit of what's app",
        "sha": "ae9c63d2d01bcd095d95812483fcf136b888f923",
        "authoredAt": "2025-05-04T03:19:40Z"
      },
      {
        "authorEmail": "spochareddy@liftlab.com",
        "authorName": "saipr",
        "htmlUrl": "https://github.com/saiprudhvipochareddy/LLD/commit/6e3d8beb359c390bbd53678d08a9c101fa646c46",
        "message": "initial commit of what's app",
        "sha": "6e3d8beb359c390bbd53678d08a9c101fa646c46",
        "authoredAt": "2025-05-03T05:49:08Z"
      }
    ]
  }
]
```

### Note
- Please replace ${token} with the token shared in the email.
- To run at the organization level, change the ownerType value to ORGANIZATION.