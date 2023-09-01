package com.github.manager.model.github.response;

public record GithubBranchResponse(String name, GithubCommitResponse commit) {
}
