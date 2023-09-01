package com.github.manager.model.github.response;

public record GithubRepositoryResponse(long id, String name, GithubOwnerResponse owner, boolean fork) {
}
