package com.github.manager.router;

import com.github.manager.configuration.ReactiveRouterConfiguration;
import com.github.manager.model.UserRepositoryDTO;
import com.github.manager.properties.GithubApiProperties;
import com.github.manager.service.GithubUserService;
import com.github.manager.web.exception.RestErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.manager.router.ReactiveRouterTestUtil.*;
import static com.github.manager.service.GithubUserService.NOT_ACCEPTABLE_REQUEST_MSG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.ACCEPT;

@WebFluxTest
@ContextConfiguration(classes = {GithubUserService.class, ReactiveRouterConfiguration.class, GithubApiProperties.class})
public class ReactiveRouterTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private GithubUserService githubManagerService;

    @Test
    void shouldFindGithubUser() {
        // given mocked responses
        var sampleUserRepos = givenSampleUserRepos();
        var reactiveServiceResponse = givenOkResponse(sampleUserRepos);
        when(githubManagerService.findUserDetails(any()))
                .thenReturn(reactiveServiceResponse);

        // when request is made some data should be returned
        findUserDetailsRequest(webTestClient)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserRepositoryDTO.class)
                .value(v -> assertThat(v).usingRecursiveComparison().isEqualTo(sampleUserRepos));
    }

    @Test
    void shouldNotFoundGithubUser() {
        // given mocked responses
        when(githubManagerService.findUserDetails(any()))
                .thenReturn(givenNotFoundResponse());

        // when request is made not found status code should be returned
        findUserDetailsRequest(webTestClient)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void shouldRejectRequestWithInvalidHeader() {
        // given mocked responses
        var notAllowedResponseBody = RestErrorResponse.of(HttpStatus.NOT_ACCEPTABLE.value(), NOT_ACCEPTABLE_REQUEST_MSG);
        when(githubManagerService.handleNotAllowedMediaType(any()))
                .thenReturn(givenNotAcceptableResponse(notAllowedResponseBody));

        // when request is made not acceptable status code should be returned
        findUserDetailsRequest(webTestClient)
                .header(ACCEPT, MediaType.APPLICATION_XML_VALUE)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_ACCEPTABLE)
                .expectBody(RestErrorResponse.class)
                .value(v -> assertThat(v).usingRecursiveComparison().isEqualTo(notAllowedResponseBody));
    }

}

