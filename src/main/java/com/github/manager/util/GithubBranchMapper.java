package com.github.manager.util;

import com.github.manager.model.RepositoryBranchDTO;
import com.github.manager.model.github.response.GithubBranchResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GithubBranchMapper {
    public static RepositoryBranchDTO fromResponse(GithubBranchResponse response) {
        return RepositoryBranchDTO.of(response.getName(), response.getCommit().getSha());
    }
}
