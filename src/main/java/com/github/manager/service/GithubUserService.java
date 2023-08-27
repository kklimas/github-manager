package com.github.manager.service;

import com.github.manager.model.RepositoryBranchDTO;
import com.github.manager.model.UserRepositoryDTO;
import com.github.manager.model.github.response.GithubBranchResponse;
import com.github.manager.model.github.response.GithubRepositoryResponse;
import com.github.manager.util.GithubBranchMapper;
import com.github.manager.util.GithubResponseUtil;
import com.github.manager.web.exception.GithubApiException;
import com.github.manager.web.exception.RestErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Slf4j
@Service
@RequiredArgsConstructor
public class GithubUserService {

    public static final String NOT_ACCEPTABLE_REQUEST_MSG = "Only request with media type application/json can be accepted.";

    private static final String UNKNOWN_ERROR_MSG = "Unknown error occurred: %s.";
    private static final String USERNAME_PARAM = "username";

    private final GithubRequestService githubRequestService;

    public Mono<ServerResponse> findUserDetails(ServerRequest request) {
        var username = request.pathVariable(USERNAME_PARAM);
        return findUserDetails(username)
                .collectList()
                .flatMap(this::handleResponse)
                .onErrorResume(this::handleErrorResponse);
    }

    private Flux<UserRepositoryDTO> findUserDetails(String username) {
        return githubRequestService.userDetails(username)
                .onStatus(HttpStatusCode::is4xxClientError, GithubResponseUtil::handleClientError)
                .bodyToFlux(GithubRepositoryResponse.class)
                .filter(repo -> !repo.isFork())
                .flatMap(repo -> Flux.zip(
                        Flux.just(repo),
                        findBranchDetails(repo.getName(), username).collectList(),
                        GithubResponseUtil::handleResponse
                ));
    }

    private Flux<RepositoryBranchDTO> findBranchDetails(String repositoryName, String username) {
        return githubRequestService.repositoryBranches(repositoryName, username)
                .onStatus(HttpStatusCode::is4xxClientError, GithubResponseUtil::handleClientError)
                .bodyToFlux(GithubBranchResponse.class)
                .map(GithubBranchMapper::fromResponse);
    }

    private Mono<ServerResponse> handleResponse(List<UserRepositoryDTO> userRepos) {
        return ServerResponse.ok().body(BodyInserters.fromValue(userRepos));
    }

    private Mono<ServerResponse> handleErrorResponse(Throwable error) {
        if (error instanceof GithubApiException ex) {
            var restError = RestErrorResponse.of(ex.getStatusCode().value(), ex.getMessage());
            return ServerResponse.status(ex.getStatusCode()).body(BodyInserters.fromValue(restError));
        }
        var code = HttpStatus.INTERNAL_SERVER_ERROR.value();
        var errorBody = RestErrorResponse.of(code, UNKNOWN_ERROR_MSG.formatted(error.getMessage()));
        return ServerResponse.status(code).body(BodyInserters.fromValue(errorBody));
    }

    public Mono<ServerResponse> handleNotAllowedMediaType(ServerRequest request) {
        var body = RestErrorResponse.of(NOT_ACCEPTABLE.value(), NOT_ACCEPTABLE_REQUEST_MSG);
        return ServerResponse.status(NOT_ACCEPTABLE).body(BodyInserters.fromValue(body));
    }

}
