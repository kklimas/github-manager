package com.github.manager.controller;

import com.github.manager.model.RepositoryBranchDTO;
import com.github.manager.model.UserRepositoryDTO;
import com.github.manager.service.GithubUserService;
import com.github.manager.web.GlobalControllerAdvice;
import com.github.manager.web.controller.GithubUserController;
import com.github.manager.web.exception.RestErrorResponse;
import com.github.manager.web.exception.github.RequestLimitExceededException;
import com.github.manager.web.exception.github.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Import(GlobalControllerAdvice.class)
@WebFluxTest(controllers = GithubUserController.class)
public class GithubUserControllerTest {

    public static final String TEST_COMMIT_SHA = "16b8a98c4021f77e6dfc832d07e6e6d0d43d5702";
    private static final String USERS_PATH = "users";
    private static final String TEST_USERNAME = "kklimas";
    private static final String TEST_REPO_NAME = "checkers";
    private static final String TEST_BRANCH_NAME = "checkers";

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private GithubUserService githubManagerService;

    @Test
    void shouldFindGithubUser() {
        // given mocked service responses
        var sampleUserRepos = givenSampleUserRepos();
        when(githubManagerService.findUserDetails(any())).thenReturn(Flux.fromIterable(sampleUserRepos));

        // when request is made with correct header mocked data should be returned
        webTestClient.get()
                .uri(builder -> builder.pathSegment(USERS_PATH, TEST_USERNAME).build())
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserRepositoryDTO.class)
                .value(v -> assertThat(v).usingRecursiveComparison().isEqualTo(sampleUserRepos));
    }

    @Test
    void shouldExceededApiLimit() {
        // given mocked responses
        var forbiddenMessage = "API rate limit exceeded.";
        when(githubManagerService.findUserDetails(any())).thenReturn(Flux.error(new RequestLimitExceededException()));

        // when request is made forbidden status should be returned
        webTestClient.get()
                .uri(builder -> builder.pathSegment(USERS_PATH, TEST_USERNAME).build())
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(RestErrorResponse.class)
                .value(v -> {
                    assertEquals(HttpStatus.FORBIDDEN.value(), v.status());
                    assertEquals(forbiddenMessage, v.error());
                });
    }

    @Test
    void shouldNotFoundGithubUser() {
        // given mocked responses
        var notFoundMessage = "Github user was not found.";
        when(githubManagerService.findUserDetails(any())).thenReturn(Flux.error(new UserNotFoundException()));

        // when request is made not found status should be returned
        webTestClient.get()
                .uri(builder -> builder.pathSegment(USERS_PATH, TEST_USERNAME).build())
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(RestErrorResponse.class)
                .value(v -> {
                    assertEquals(HttpStatus.NOT_FOUND.value(), v.status());
                    assertEquals(notFoundMessage, v.error());
                });
    }

    @Test
    void shouldRejectRequestWithInvalidHeader() {
        // when request is made not acceptable status should be returned
        var notAcceptableMessage = "Not Acceptable";
        webTestClient.get()
                .uri(builder -> builder.pathSegment(USERS_PATH, TEST_USERNAME).build())
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.NOT_ACCEPTABLE)
                .expectBody(RestErrorResponse.class)
                .value(v -> {
                    assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), v.status());
                    assertEquals(notAcceptableMessage, v.error());
                });
    }

    private List<UserRepositoryDTO> givenSampleUserRepos() {
        return List.of(new UserRepositoryDTO(
                TEST_USERNAME,
                TEST_REPO_NAME,
                List.of(new RepositoryBranchDTO(TEST_BRANCH_NAME, TEST_COMMIT_SHA)))
        );
    }
}
