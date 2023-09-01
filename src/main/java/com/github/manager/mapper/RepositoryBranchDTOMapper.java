package com.github.manager.mapper;

import com.github.manager.model.RepositoryBranchDTO;
import com.github.manager.model.github.response.GithubBranchResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RepositoryBranchDTOMapper {
    public static RepositoryBranchDTO map(GithubBranchResponse branchResponse) {
        return new RepositoryBranchDTO(branchResponse.name(), branchResponse.commit().sha());
    }
}
