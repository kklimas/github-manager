package com.github.manager.router;

import com.github.manager.model.RepositoryBranchDTO;
import com.github.manager.model.UserRepositoryDTO;
import com.github.manager.web.exception.RestErrorResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReactiveRouterTestUtil {
    public static final String TEST_COMMIT_SHA = "16b8a98c4021f77e6dfc832d07e6e6d0d43d5702";
    private static final String USERS_PATH = "users";
    private static final String TEST_USERNAME = "kklimas";
    private static final String TEST_REPO_NAME = "checkers";
    private static final String TEST_BRANCH_NAME = "checkers";

    static WebTestClient.RequestHeadersSpec<?> findUserDetailsRequest(WebTestClient webTestClient) {
        return webTestClient.get().uri(builder -> builder.pathSegment(USERS_PATH, TEST_USERNAME).build());
    }

    static List<UserRepositoryDTO> givenSampleUserRepos() {
        return List.of(UserRepositoryDTO.builder()
                .ownerLogin(TEST_USERNAME)
                .repositoryName(TEST_REPO_NAME)
                .branches(List.of(RepositoryBranchDTO.of(TEST_BRANCH_NAME, TEST_COMMIT_SHA)))
                .build());
    }

    static Mono<ServerResponse> givenOkResponse(List<UserRepositoryDTO> userRepositories) {
        return ServerResponse.ok().body(BodyInserters.fromValue(userRepositories));
    }

    static Mono<ServerResponse> givenNotFoundResponse() {
        return ServerResponse.notFound().build();
    }

    static Mono<ServerResponse> givenNotAcceptableResponse(RestErrorResponse responseBody) {
        return ServerResponse.status(HttpStatus.NOT_ACCEPTABLE.value())
                .body(BodyInserters.fromValue(responseBody));
    }
}
