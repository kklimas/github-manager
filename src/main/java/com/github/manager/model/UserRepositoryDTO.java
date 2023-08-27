package com.github.manager.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRepositoryDTO {
    private String repositoryName;
    private String ownerLogin;
    private List<RepositoryBranchDTO> branches;
}
