package com.github.manager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class GithubRequestService {
    private static final String USERS_PATH_SEGMENT = "users";
    private static final String REPOS_PATH_SEGMENT = "repos";
    private static final String BRANCHES_PATH_SEGMENT = "branches";

    private final WebClient githubClient;

    public WebClient.ResponseSpec userDetails(String username) {
        return githubClient.get()
                .uri(builder -> builder
                        .pathSegment(USERS_PATH_SEGMENT, username, REPOS_PATH_SEGMENT)
                        .build())
                .retrieve();
    }

    public WebClient.ResponseSpec repositoryBranches(String repositoryName, String username) {
        return githubClient.get()
                .uri(builder -> builder
                        .pathSegment(REPOS_PATH_SEGMENT, username, repositoryName, BRANCHES_PATH_SEGMENT)
                        .build())
                .retrieve();
    }
}
