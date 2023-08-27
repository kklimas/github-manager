package com.github.manager.model.github.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GithubRepositoryResponse {
    private long id;
    private String name;
    private GithubOwnerResponse owner;
    private boolean fork;
}
