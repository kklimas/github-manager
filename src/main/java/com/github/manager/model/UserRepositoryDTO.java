package com.github.manager.model;

import java.util.List;

public record UserRepositoryDTO(String repositoryName, String ownerLogin, List<RepositoryBranchDTO> branches) {
}
