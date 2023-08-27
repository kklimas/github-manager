package com.github.manager.service;

import com.github.manager.util.GithubBranchMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;

import static com.github.manager.service.GithubUserService.NOT_ACCEPTABLE_REQUEST_MSG;
import static com.github.manager.service.GithubUserServiceTestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GithubUserServiceTest {
    private final GithubRequestService githubRequestService = mock(GithubRequestService.class);
    private final GithubUserService githubManagerService = new GithubUserService(githubRequestService);

    @Test
    void shouldFindUserDetails() {
        // given mocked responses
        mockServiceResponse(mockedResponse());

        // when service is invoked
        var responseBody = githubManagerService.findUserDetails(mockedServerRequest()).block();
        var repositories = fromSuccessfulResponse(responseBody);

        // correct data should be returned
        assertEquals(2, repositories.size()); // forked repos should be rejected
        var r1 = findRepository(repositories, REPO_NAME_1);
        var r2 = findRepository(repositories, REPO_NAME_2);
        assertFalse(r1.isEmpty());
        assertFalse(r2.isEmpty());
        assertEquals(TEST_USERNAME, r1.get().getOwnerLogin());
        assertEquals(TEST_USERNAME, r2.get().getOwnerLogin());
        var branches = givenBranchesResponse().stream().map(GithubBranchMapper::fromResponse).toList();
        assertThat(branches).usingRecursiveComparison().isEqualTo(r1.get().getBranches().stream().toList());
        assertThat(branches).usingRecursiveComparison().isEqualTo(r2.get().getBranches().stream().toList());
    }

    @Test
    void shouldHandleUnacceptableHeader() {
        // when request is made with invalid MediaType header
        var serverResponse = githubManagerService.handleNotAllowedMediaType(mock(ServerRequest.class)).block();
        var body = fromErrorResponse(serverResponse);

        // correct response should be returned
        assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), body.getCode());
        assertEquals(NOT_ACCEPTABLE_REQUEST_MSG, body.getMessage());
    }

    private void mockServiceResponse(WebClient.ResponseSpec response) {
        var mockedBranches = mockedBranchesResponse();
        when(githubRequestService.userDetails(any())).thenReturn(response);
        when(githubRequestService.repositoryBranches(any(), any())).thenReturn(mockedBranches);
    }
}
