package com.github.manager.service;

import com.github.manager.GithubManagerApplication;
import com.github.manager.web.controller.GithubUserController;
import com.github.manager.web.exception.github.RequestLimitExceededException;
import com.github.manager.web.exception.github.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = {GithubManagerApplication.class, GithubUserController.class})
class GithubManagerApplicationTest extends GithubManagerApplicationTestUtil {

    @Autowired
    private GithubUserService githubUserService;

    @Test
    void shouldFindGithubUserRepositories() {
        // given GitHub api response with one user repository and three repository branches
        mockRepositoriesResponse();

        // when service is invoked
        var serviceResponse = githubUserService.findUserDetails(SAMPLE_USERNAME).toStream().toList();

        // one repository should be returned and two requests should be made
        assertResponse(serviceResponse);
    }

    @Test
    void shouldNotFindForkedGithubUserRepositories() {
        // given GitHub api response with one forked user repository
        mockForkedRepositoriesResponse();

        // when service is invoked
        var serviceResponse = githubUserService.findUserDetails(SAMPLE_USERNAME).toStream().toList();

        // empty list of repositories should be returned and one request should be made (because of filtering)
        assertForkResponse(serviceResponse);
    }

    @Test
    void forbiddenRepositoryRequest() {
        // given forbidden API response when asking about user repositories
        mockForbiddenResponse();

        // when service is invoked
        var exception = assertThrows(
                RequestLimitExceededException.class,
                () -> githubUserService.findUserDetails(SAMPLE_USERNAME).toStream().toList());

        // RequestLimitExceededException should be thrown and one request should be made (exception handling)
        assertException(exception, "API rate limit exceeded.", RequestLimitExceededException.class);
    }

    @Test
    void forbiddenBranchRequest() {
        // given forbidden API response when asking about repository branches
        mockBranchForbiddenResponse();

        // when service is invoked
        var exception = assertThrows(
                RequestLimitExceededException.class,
                () -> githubUserService.findUserDetails(SAMPLE_USERNAME).toStream().toList());

        // RequestLimitExceededException should be thrown and two requests should be made (exception handling after second request)
        assertComplexForbiddenException(exception, "API rate limit exceeded.");
    }

    @Test
    void shouldNotFindGithubUser() {
        // given Not Found API response when asking about user repositories
        mockUserNotFoundResponse();

        // when service is invoked
        var exception = assertThrows(
                UserNotFoundException.class,
                () -> githubUserService.findUserDetails(SAMPLE_USERNAME).toStream().toList());

        // UserNotFoundException should be thrown and one request should be made
        assertException(exception, "Github user was not found.", UserNotFoundException.class);
    }

}
