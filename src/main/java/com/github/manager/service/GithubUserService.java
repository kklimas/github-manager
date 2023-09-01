package com.github.manager.service;

import com.github.manager.mapper.RepositoryBranchDTOMapper;
import com.github.manager.mapper.UserRepositoryDTOMapper;
import com.github.manager.model.RepositoryBranchDTO;
import com.github.manager.model.UserRepositoryDTO;
import com.github.manager.model.github.response.GithubBranchResponse;
import com.github.manager.model.github.response.GithubRepositoryResponse;
import com.github.manager.web.exception.github.RequestLimitExceededException;
import com.github.manager.web.exception.github.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GithubUserService {
    private static final String USERS_PATH_SEGMENT = "users";
    private static final String REPOS_PATH_SEGMENT = "repos";
    private static final String BRANCHES_PATH_SEGMENT = "branches";

    private final WebClient githubClient;

    public Flux<UserRepositoryDTO> findUserDetails(String username) {
        return githubClient.get()
                .uri(builder -> builder
                        .pathSegment(USERS_PATH_SEGMENT, username, REPOS_PATH_SEGMENT)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                .bodyToFlux(GithubRepositoryResponse.class)
                .filter(repo -> !repo.fork())
                .flatMap(repo -> Flux.zip(
                        Flux.just(repo),
                        findBranchDetails(repo.name(), username).collectList(),
                        UserRepositoryDTOMapper::map)
                );
    }

    private Flux<RepositoryBranchDTO> findBranchDetails(String repositoryName, String username) {
        return githubClient.get()
                .uri(builder -> builder
                        .pathSegment(REPOS_PATH_SEGMENT, username, repositoryName, BRANCHES_PATH_SEGMENT)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                .bodyToFlux(GithubBranchResponse.class)
                .map(RepositoryBranchDTOMapper::map);
    }

    private Mono<? extends Throwable> handleClientError(ClientResponse clientResponse) {
        return switch (clientResponse.statusCode().value()) {
            case 403 -> Mono.error(new RequestLimitExceededException());
            case 404 -> Mono.error(new UserNotFoundException());
            default -> Mono.error(new RuntimeException(clientResponse.toString()));
        };
    }
}
