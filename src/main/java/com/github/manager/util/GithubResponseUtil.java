package com.github.manager.util;

import com.github.manager.model.RepositoryBranchDTO;
import com.github.manager.model.UserRepositoryDTO;
import com.github.manager.model.github.response.GithubRepositoryResponse;
import com.github.manager.web.exception.GithubApiException;
import com.github.manager.web.exception.RestErrorResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GithubResponseUtil {
    private static final String UNKNOWN_EXCEPTION_MSG = "Unknown exception occurred.";

    public static Mono<GithubApiException> handleClientError(ClientResponse clientResponse) {
        var errorCode = clientResponse.statusCode();
        return clientResponse.bodyToMono(RestErrorResponse.class)
                .flatMap(errorBody -> {
                    var errorMessage = hasMessage(errorBody) ? errorBody.getMessage() : UNKNOWN_EXCEPTION_MSG;
                    return Mono.error(new GithubApiException(errorMessage, errorCode));
                });
    }

    private static boolean hasMessage(RestErrorResponse response) {
        return response != null && !response.getMessage().isEmpty();
    }

    public static UserRepositoryDTO handleResponse(GithubRepositoryResponse response, List<RepositoryBranchDTO> branches) {
        return UserRepositoryDTO.builder()
                .repositoryName(response.getName())
                .ownerLogin(response.getOwner().getLogin())
                .branches(branches)
                .build();
    }
}
