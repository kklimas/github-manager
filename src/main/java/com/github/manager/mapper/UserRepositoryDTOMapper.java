package com.github.manager.mapper;

import com.github.manager.model.RepositoryBranchDTO;
import com.github.manager.model.UserRepositoryDTO;
import com.github.manager.model.github.response.GithubRepositoryResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserRepositoryDTOMapper {
    public static UserRepositoryDTO map(GithubRepositoryResponse repositoryResponse, List<RepositoryBranchDTO> branches) {
        return new UserRepositoryDTO(repositoryResponse.name(), repositoryResponse.owner().login(), branches);
    }
}
