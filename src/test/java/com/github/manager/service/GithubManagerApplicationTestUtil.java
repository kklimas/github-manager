package com.github.manager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.manager.mapper.RepositoryBranchDTOMapper;
import com.github.manager.model.UserRepositoryDTO;
import com.github.manager.model.github.response.GithubBranchResponse;
import com.github.manager.model.github.response.GithubCommitResponse;
import com.github.manager.model.github.response.GithubOwnerResponse;
import com.github.manager.model.github.response.GithubRepositoryResponse;
import com.github.manager.properties.GithubApiProperties;
import com.github.manager.web.exception.RestErrorResponse;
import com.github.manager.web.exception.github.RequestLimitExceededException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.List;

import static com.github.manager.mapper.UserRepositoryDTOMapper.map;
import static org.junit.jupiter.api.Assertions.*;

abstract class GithubManagerApplicationTestUtil {

    @Autowired
    private GithubApiProperties githubApiProperties;

    private static final String REPOSITORY_NAME = "flight-app";
    private static final List<String> BRANCH_NAMES = List.of("master", "develop", "feature/BS-0000");
    private static final String SAMPLE_COMMIT_HASH = "915db6d1cd55a4f5e5cfd82d71d8627849e2386b";
    private static final String USER_REPOSITORY_PATH = "/users/kklimas/repos";
    private static final String BRANCHES_REPOSITORY_PATH = "/repos/kklimas/%s/branches";
    protected static final String SAMPLE_USERNAME = "kklimas";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockWebServer mockWebServer;

    @BeforeEach
    void beforeEach() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start(githubApiProperties.getPort());
    }

    @AfterEach
    void afterEach() throws IOException {
        mockWebServer.close();
    }

    protected void mockRepositoriesResponse() {
        mockResponse(List.of(givenUserRepository()));
        mockResponse(givenRepositoryBranches());
    }

    protected void mockForkedRepositoriesResponse() {
        mockResponse(List.of(givenForkedUserRepository()));
    }

    protected void mockForbiddenResponse() {
        var githubApiException = new RestErrorResponse(403, "Forbidden");
        mockExceptionResponse(HttpStatus.FORBIDDEN, githubApiException);
    }

    protected void mockBranchForbiddenResponse() {
        mockResponse(List.of(givenUserRepository()));
        mockForbiddenResponse();
    }

    protected void mockUserNotFoundResponse() {
        var githubApiException = new RestErrorResponse(404, "Not found");
        mockExceptionResponse(HttpStatus.NOT_FOUND, githubApiException);
    }

    private void mockExceptionResponse(HttpStatus httpStatus, RestErrorResponse errorResponse) {
        assertDoesNotThrow(() -> {
            var stringBody = objectMapper.writeValueAsString(errorResponse);
            var mockedGithubResponse = new MockResponse()
                    .setResponseCode(httpStatus.value())
                    .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(stringBody);
            mockWebServer.enqueue(mockedGithubResponse);
        });
    }

    private List<GithubBranchResponse> givenRepositoryBranches() {
        return BRANCH_NAMES.stream()
                .map(b -> new GithubBranchResponse(b, new GithubCommitResponse(SAMPLE_COMMIT_HASH, SAMPLE_COMMIT_HASH)))
                .toList();
    }

    private GithubRepositoryResponse givenUserRepository() {
        return new GithubRepositoryResponse(0, REPOSITORY_NAME, new GithubOwnerResponse(SAMPLE_USERNAME), false);
    }

    private GithubRepositoryResponse givenForkedUserRepository() {
        return new GithubRepositoryResponse(0, REPOSITORY_NAME, new GithubOwnerResponse(SAMPLE_USERNAME), true);
    }

    private void mockResponse(Object o) {
        assertDoesNotThrow(() -> {
            var stringBody = objectMapper.writeValueAsString(o);
            var mockedGithubResponse = new MockResponse()
                    .setResponseCode(HttpStatus.OK.value())
                    .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(stringBody);
            mockWebServer.enqueue(mockedGithubResponse);
        });
    }

    protected void assertResponse(List<UserRepositoryDTO> userRepositories) {
        var branches = givenRepositoryBranches().stream().map(RepositoryBranchDTOMapper::map).toList();
        var expectedRepos = List.of(map(givenUserRepository(), branches)).toArray();

        assertNotNull(userRepositories);
        assertArrayEquals(expectedRepos, userRepositories.toArray());

        assertServerRequests();
    }

    protected void assertForkResponse(List<UserRepositoryDTO> userRepositories) {
        assertNotNull(userRepositories);
        assertTrue(userRepositories.isEmpty());
        assertSingleUserRepositoryRequest();
    }

    protected void assertException(RuntimeException ex, String message, Class<? extends RuntimeException> clazz) {
        assertEquals(clazz, ex.getClass());
        assertEquals(ex.getMessage(), message);
        assertSingleUserRepositoryRequest();
    }

    protected void assertComplexForbiddenException(RuntimeException ex, String message) {
        assertEquals(RequestLimitExceededException.class, ex.getClass());
        assertEquals(ex.getMessage(), message);
        assertServerRequests();
    }

    private void assertServerRequests() {
        assertDoesNotThrow(() -> {
            var requestCount = mockWebServer.getRequestCount();
            var r1 = mockWebServer.takeRequest();
            var r2 = mockWebServer.takeRequest();
            assertEquals(2, requestCount);
            assertRequest(USER_REPOSITORY_PATH, r1);
            assertRequest(BRANCHES_REPOSITORY_PATH.formatted(REPOSITORY_NAME), r2);
        });
    }

    private void assertSingleUserRepositoryRequest() {
        assertDoesNotThrow(() -> {
            var requestCount = mockWebServer.getRequestCount();
            var r1 = mockWebServer.takeRequest();
            assertEquals(1, requestCount);
            assertRequest(USER_REPOSITORY_PATH, r1);
        });
    }

    private void assertRequest(String pathName, RecordedRequest request) {
        assertEquals(HttpMethod.GET.name(), request.getMethod());
        assertEquals(pathName, request.getPath());
    }
}
