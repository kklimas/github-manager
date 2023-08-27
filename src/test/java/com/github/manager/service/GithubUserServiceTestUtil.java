package com.github.manager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.manager.model.UserRepositoryDTO;
import com.github.manager.model.github.response.GithubBranchResponse;
import com.github.manager.model.github.response.GithubCommitResponse;
import com.github.manager.model.github.response.GithubOwnerResponse;
import com.github.manager.model.github.response.GithubRepositoryResponse;
import com.github.manager.web.exception.RestErrorResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;

import static com.github.manager.router.ReactiveRouterTestUtil.TEST_COMMIT_SHA;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GithubUserServiceTestUtil {

    static final String TEST_USERNAME = "username";
    static final String REPO_NAME_1 = "repo_name_1";
    static final String REPO_NAME_2 = "repo_name_2";
    static final String REPO_NAME_3 = "repo_name_3";

    static final String COMMIT_NAME_1 = "commit_name_1";
    static final String COMMIT_NAME_2 = "commit_name_2";

    private static final String EMPTY_STRING = "";
    private static final String TEST_COMMIT_URL = "sample-url";

    private static final ServerResponse.Context EMPTY_CONTEXT = new ServerResponse.Context() {
        @Override
        public List<HttpMessageWriter<?>> messageWriters() {
            return HandlerStrategies.withDefaults().messageWriters();
        }

        @Override
        public List<ViewResolver> viewResolvers() {
            return HandlerStrategies.withDefaults().viewResolvers();
        }
    };

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static ServerRequest mockedServerRequest() {
        var mockedRequest = mock(ServerRequest.class);
        when(mockedRequest.pathVariable(any())).thenReturn(TEST_USERNAME);
        return mockedRequest;
    }

    static WebClient.ResponseSpec mockedResponse() {
        var response = mock(WebClient.ResponseSpec.class);
        when(response.onStatus(any(), any())).thenReturn(response);
        when(response.bodyToFlux(GithubRepositoryResponse.class)).thenReturn(Flux.fromIterable(givenResponseBody()));
        return response;
    }

    private static List<GithubRepositoryResponse> givenResponseBody() {
        var owner = new GithubOwnerResponse(TEST_USERNAME);
        var r1 = new GithubRepositoryResponse(0, REPO_NAME_1, owner, false);
        var r2 = new GithubRepositoryResponse(1, REPO_NAME_2, owner, false);
        var r3 = new GithubRepositoryResponse(2, REPO_NAME_3, owner, true);
        return List.of(r1, r2, r3);
    }

    static WebClient.ResponseSpec mockedBranchesResponse() {
        var response = mock(WebClient.ResponseSpec.class);
        when(response.onStatus(any(), any())).thenReturn(response);
        when(response.bodyToFlux(GithubBranchResponse.class)).thenReturn(Flux.fromIterable(givenBranchesResponse()));
        return response;
    }


    static List<GithubBranchResponse> givenBranchesResponse() {
        var commitDetails = new GithubCommitResponse(TEST_COMMIT_SHA, TEST_COMMIT_URL);
        var c1 = new GithubBranchResponse(COMMIT_NAME_1, commitDetails);
        var c2 = new GithubBranchResponse(COMMIT_NAME_2, commitDetails);
        return List.of(c1, c2);
    }

    static List<UserRepositoryDTO> fromSuccessfulResponse(ServerResponse response) {
        return fromResponse(response, new TypeReference<>() {
        });
    }

    static RestErrorResponse fromErrorResponse(ServerResponse response) {
        return fromResponse(response, new TypeReference<>() {
        });
    }

    private static <T> T fromResponse(ServerResponse response, TypeReference<T> reference) {
        assertNotNull(response);
        var webExchange = MockServerWebExchange.builder(MockServerHttpRequest.get(EMPTY_STRING).build()).build();
        response.writeTo(webExchange, EMPTY_CONTEXT).block();
        var exchangeStringBody = webExchange.getResponse().getBodyAsString().block();
        return assertDoesNotThrow(() -> objectMapper.readValue(exchangeStringBody, reference));
    }

    static Optional<UserRepositoryDTO> findRepository(List<UserRepositoryDTO> repositories, String repoName) {
        return repositories.stream()
                .filter(repository -> repository.getRepositoryName().equals(repoName))
                .findFirst();
    }

}
